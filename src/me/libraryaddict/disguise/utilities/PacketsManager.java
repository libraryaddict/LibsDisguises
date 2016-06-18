package me.libraryaddict.disguise.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedAttribute.Builder;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerClientInteract;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerInventory;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerMain;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerSounds;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerViewDisguises;

public class PacketsManager
{
    private static PacketListener clientInteractEntityListener;
    private static PacketListener inventoryListener;
    private static boolean inventoryModifierEnabled;
    private static LibsDisguises libsDisguises;
    private static PacketListener mainListener;
    private static PacketListener soundsListener;
    private static boolean soundsListenerEnabled;
    private static PacketListener viewDisguisesListener;
    private static boolean viewDisguisesListenerEnabled;

    public static void addPacketListeners()
    {
        // Add a client listener to cancel them interacting with uninteractable disguised entitys.
        // You ain't supposed to be allowed to 'interact' with a item that cannot be clicked.
        // Because it kicks you for hacking.

        clientInteractEntityListener = new PacketListenerClientInteract(libsDisguises);

        ProtocolLibrary.getProtocolManager().addPacketListener(clientInteractEntityListener);

        // Now I call this and the main listener is registered!
        setupMainPacketsListener();
    }

    /**
     * Construct the packets I need to spawn in the disguise
     */
    public static PacketContainer[][] constructSpawnPackets(final Player observer, Disguise disguise, Entity disguisedEntity)
    {
        if (disguise.getEntity() == null)
        {
            disguise.setEntity(disguisedEntity);
        }

        ArrayList<PacketContainer> packets = new ArrayList<>();

        // This sends the armor packets so that the player isn't naked.
        // Please note it only sends the packets that wouldn't be sent normally
        if (DisguiseConfig.isEquipmentPacketsEnabled())
        {
            for (EquipmentSlot slot : EquipmentSlot.values())
            {
                ItemStack itemstack = disguise.getWatcher().getItemStack(slot);

                if (itemstack == null || itemstack.getType() == Material.AIR)
                {
                    continue;
                }

                ItemStack item = null;

                if (disguisedEntity instanceof LivingEntity)
                {
                    item = ReflectionManager.getEquipment(slot, disguisedEntity);
                }

                if (item != null && item.getType() != Material.AIR)
                {
                    continue;
                }

                PacketContainer packet = new PacketContainer(Server.ENTITY_EQUIPMENT);

                StructureModifier<Object> mods = packet.getModifier();

                mods.write(0, disguisedEntity.getEntityId());
                mods.write(1, ReflectionManager.createEnumItemSlot(slot));
                mods.write(2, ReflectionManager.getNmsItem(itemstack));

                packets.add(packet);
            }
        }

        if (DisguiseConfig.isMiscDisguisesForLivingEnabled())
        {
            if (disguise.getWatcher() instanceof LivingWatcher)
            {

                ArrayList<WrappedAttribute> attributes = new ArrayList<WrappedAttribute>();

                Builder builder = WrappedAttribute.newBuilder().attributeKey("generic.maxHealth");

                if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet())
                {
                    builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                }
                else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity() && disguisedEntity instanceof Damageable)
                {
                    builder.baseValue(((Damageable) disguisedEntity).getMaxHealth());
                }
                else
                {
                    builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                }

                PacketContainer packet = new PacketContainer(Server.UPDATE_ATTRIBUTES);

                builder.packet(packet);

                attributes.add(builder.build());

                packet.getIntegers().write(0, disguisedEntity.getEntityId());
                packet.getAttributeCollectionModifier().write(0, attributes);

                packets.add(packet);
            }
        }

        PacketContainer[] spawnPackets = new PacketContainer[2 + packets.size()];
        PacketContainer[] delayedPackets = new PacketContainer[0];

        for (int i = 0; i < packets.size(); i++)
        {
            spawnPackets[i + 2] = packets.get(i);
        }

        Location loc = disguisedEntity.getLocation().clone().add(0, getYModifier(disguisedEntity, disguise), 0);

        byte yaw = (byte) (int) (loc.getYaw() * 256.0F / 360.0F);
        byte pitch = (byte) (int) (loc.getPitch() * 256.0F / 360.0F);

        if (DisguiseConfig.isMovementPacketsEnabled())
        {
            yaw = getYaw(disguise.getType(), disguisedEntity.getType(), yaw);
            pitch = getPitch(disguise.getType(), DisguiseType.getType(disguisedEntity.getType()), pitch);
        }

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB)
        {
            spawnPackets[0] = new PacketContainer(Server.SPAWN_ENTITY_EXPERIENCE_ORB);

            StructureModifier<Object> mods = spawnPackets[0].getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY() + 0.06);
            mods.write(3, loc.getZ());
            mods.write(4, 1);
        }
        else if (disguise.getType() == DisguiseType.PAINTING)
        {
            spawnPackets[0] = new PacketContainer(Server.SPAWN_ENTITY_PAINTING);

            StructureModifier<Object> mods = spawnPackets[0].getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguisedEntity.getUniqueId());
            mods.write(2, ReflectionManager.getBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            mods.write(3, ReflectionManager.getEnumDirection(((int) loc.getYaw()) % 4));

            int id = ((MiscDisguise) disguise).getData();

            mods.write(4, ReflectionManager.getEnumArt(Art.values()[id]));

            // Make the teleport packet to make it visible..
            spawnPackets[1] = new PacketContainer(Server.ENTITY_TELEPORT);

            mods = spawnPackets[1].getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY());
            mods.write(3, loc.getZ());
            mods.write(4, yaw);
            mods.write(5, pitch);
        }
        else if (disguise.getType().isPlayer())
        {
            PlayerDisguise playerDisguise = (PlayerDisguise) disguise;

            String name = playerDisguise.getName();
            int entityId = disguisedEntity.getEntityId();
            boolean removeName = false;

            if (!DisguiseUtilities.hasGameProfile(name))
            {
                removeName = !DisguiseUtilities.getAddedByPlugins().contains(name);
            }

            WrappedGameProfile gameProfile = playerDisguise.getGameProfile();

            if (removeName)
            {
                DisguiseUtilities.getAddedByPlugins().remove(name);
            }

            Object entityPlayer = ReflectionManager.createEntityPlayer(observer.getWorld(), gameProfile);
            spawnPackets[0] = ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(Server.NAMED_ENTITY_SPAWN, entityPlayer).createPacket(entityPlayer);

            // Write spawn packet in order
            spawnPackets[0].getIntegers().write(0, entityId); // Id
            spawnPackets[0].getDoubles().write(0, loc.getX());
            spawnPackets[0].getDoubles().write(1, loc.getY());
            spawnPackets[0].getDoubles().write(2, loc.getZ());

            spawnPackets[0].getDataWatcherModifier().write(0,
                    createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher())); // watcher,
                                                                                                                     // duh

            if (DisguiseConfig.isBedPacketsEnabled() && ((PlayerWatcher) disguise.getWatcher()).isSleeping())
            {
                PacketContainer[] newPackets = new PacketContainer[spawnPackets.length + 1];

                System.arraycopy(spawnPackets, 1, newPackets, 2, spawnPackets.length - 1);

                newPackets[0] = spawnPackets[0];
                spawnPackets = newPackets;

                PacketContainer[] bedPackets = DisguiseUtilities.getBedPackets(
                        loc.clone().subtract(0, PacketsManager.getYModifier(disguisedEntity, disguise), 0),
                        observer.getLocation(), ((PlayerDisguise) disguise));

                System.arraycopy(bedPackets, 0, spawnPackets, 1, 2);
            }

            ArrayList<PacketContainer> newPackets = new ArrayList<PacketContainer>();
            newPackets.add(null);

            for (PacketContainer spawnPacket : spawnPackets)
            {
                if (spawnPacket != null)
                { // Get rid of empty packet '1' if it exists.
                    newPackets.add(spawnPacket);
                }
            }

            // Send player info along with the disguise
            spawnPackets = newPackets.toArray(new PacketContainer[newPackets.size()]);
            spawnPackets[0] = new PacketContainer(Server.PLAYER_INFO);

            // Add player to the list, necessary to spawn them
            spawnPackets[0].getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(0));

            List playerList = new ArrayList();

            playerList.add(ReflectionManager.getPlayerInfoData(spawnPackets[0].getHandle(), playerDisguise.getGameProfile()));
            spawnPackets[0].getModifier().write(1, playerList);

            // Remove player from the list
            PacketContainer delayedPacket = spawnPackets[0].shallowClone();

            delayedPacket.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(4));

            delayedPackets = new PacketContainer[]
                {
                        delayedPacket
                };
        }
        else if (disguise.getType().isMob() || disguise.getType() == DisguiseType.ARMOR_STAND)
        {
            Vector vec = disguisedEntity.getVelocity();

            spawnPackets[0] = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

            StructureModifier<Object> mods = spawnPackets[0].getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, UUID.randomUUID());
            mods.write(2, disguise.getType().getTypeId());

            // region Vector calculations
            double d1 = 3.9D;
            double d2 = vec.getX();
            double d3 = vec.getY();
            double d4 = vec.getZ();
            if (d2 < -d1)
                d2 = -d1;
            if (d3 < -d1)
                d3 = -d1;
            if (d4 < -d1)
                d4 = -d1;
            if (d2 > d1)
                d2 = d1;
            if (d3 > d1)
                d3 = d1;
            if (d4 > d1)
                d4 = d1;
            // endregion

            mods.write(3, loc.getX());
            mods.write(4, loc.getY());
            mods.write(5, loc.getZ());
            mods.write(6, (int) (d2 * 8000.0D));
            mods.write(7, (int) (d3 * 8000.0D));
            mods.write(8, (int) (d4 * 8000.0D));
            mods.write(9, yaw);
            mods.write(10, pitch);

            spawnPackets[0].getDataWatcherModifier().write(0,
                    createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher()));
        }
        else if (disguise.getType().isMisc())
        {
            int objectId = disguise.getType().getObjectId();
            int data = ((MiscDisguise) disguise).getData();

            if (disguise.getType() == DisguiseType.FALLING_BLOCK)
            {
                data = ReflectionManager.getCombinedId(((MiscDisguise) disguise).getId(), data);
            }
            else if (disguise.getType() == DisguiseType.FISHING_HOOK && data == 0)
            {
                // If the MiscDisguise data isn't set. Then no entity id was provided, so default to the owners entity id
                data = disguisedEntity.getEntityId();
            }
            else if (disguise.getType() == DisguiseType.ITEM_FRAME)
            {
                data = ((((int) loc.getYaw() % 360) + 720 + 45) / 90) % 4;
            }

            Object nmsEntity = ReflectionManager.getNmsEntity(disguisedEntity);

            spawnPackets[0] = ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, nmsEntity, objectId, data)
                    .createPacket(nmsEntity, objectId, data);
            spawnPackets[0].getModifier().write(8, pitch);
            spawnPackets[0].getModifier().write(9, yaw);

            if (disguise.getType() == DisguiseType.ITEM_FRAME)
            {
                if (data % 2 == 0)
                {
                    spawnPackets[0].getModifier().write(4, loc.getZ() + (data == 0 ? -1 : 1));
                }
                else
                {
                    spawnPackets[0].getModifier().write(2, loc.getX() + (data == 3 ? -1 : 1));
                }
            }
        }
        if (spawnPackets[1] == null || disguise.isPlayerDisguise())
        {
            int entry = spawnPackets[1] == null ? 1 : 0;

            if (entry == 0)
            {
                entry = spawnPackets.length;
                spawnPackets = Arrays.copyOf(spawnPackets, spawnPackets.length + 1);
            }
            // Make a packet to turn his head!

            spawnPackets[entry] = new PacketContainer(Server.ENTITY_HEAD_ROTATION);

            StructureModifier<Object> mods = spawnPackets[entry].getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, yaw);
        }
        return new PacketContainer[][]
            {
                    spawnPackets, delayedPackets
            };
    }

    /**
     * Create a new datawatcher but with the 'correct' values
     */
    private static WrappedDataWatcher createDataWatcher(WrappedDataWatcher watcher, FlagWatcher flagWatcher)
    {
        WrappedDataWatcher newWatcher = new WrappedDataWatcher();

        try
        {
            List<WrappedWatchableObject> list = DisguiseConfig.isMetadataPacketsEnabled()
                    ? flagWatcher.convert(watcher.getWatchableObjects()) : flagWatcher.getWatchableObjects();

            for (WrappedWatchableObject watchableObject : list)
            {
                if (watchableObject == null)
                    continue;

                if (watchableObject.getValue() == null)
                    continue;

                if (Registry.get(watchableObject.getValue().getClass()) == null)
                    continue;

                WrappedDataWatcherObject obj = new WrappedDataWatcherObject(watchableObject.getIndex(),
                        Registry.get(watchableObject.getValue().getClass()));

                newWatcher.setObject(obj, watchableObject.getValue());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return newWatcher;
    }

    public static byte getPitch(DisguiseType disguiseType, DisguiseType entityType, byte value)
    {
        switch (disguiseType)
        {
        case MINECART:
        case MINECART_CHEST:
        case MINECART_COMMAND:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
            value = (byte) -value;
            break;
        default:
            break;
        }
        switch (entityType)
        {
        case MINECART:
        case MINECART_CHEST:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
            value = (byte) -value;
            break;
        default:
            break;
        }

        return value;
    }

    /**
     * Add the yaw for the disguises
     */
    public static byte getYaw(DisguiseType disguiseType, EntityType entityType, byte value)
    {
        switch (disguiseType)
        {
        case MINECART:
        case MINECART_CHEST:
        case MINECART_COMMAND:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
            value += 64;
            break;
        case ENDER_DRAGON:
            value -= 128;
            break;
        case WITHER_SKULL:
            value -= 128;
            break;
        case ARROW:
        case TIPPED_ARROW:
        case SPECTRAL_ARROW:
            value = (byte) -value;
            break;
        case PAINTING:
        case ITEM_FRAME:
            value = (byte) -(value + 128);
            break;
        default:
            if (disguiseType.isMisc() && disguiseType != DisguiseType.ARMOR_STAND)
            {
                value -= 64;
            }

            break;
        }
        switch (entityType)
        {
        case MINECART:
        case MINECART_CHEST:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
            value -= 64;
            break;
        case ENDER_DRAGON:
        case WITHER_SKULL:
            value += 128;
            break;
        case ARROW:
            value = (byte) -value;
            break;
        case PAINTING:
        case ITEM_FRAME:
            value = (byte) -(value - 128);
            break;
        default:
            if (!entityType.isAlive())
            {
                value += 64;
            }

            break;
        }

        return value;
    }

    /**
     * Get the Y level to add to the disguise for realism.
     */
    public static double getYModifier(Entity entity, Disguise disguise)
    {
        double yMod = 0;

        if ((disguise.getType() != DisguiseType.PLAYER || !((PlayerWatcher) disguise.getWatcher()).isSleeping())
                && entity.getType() == EntityType.DROPPED_ITEM)
        {
            yMod -= 0.13;
        }

        switch (disguise.getType())
        {
        case BAT:
            if (entity instanceof LivingEntity)
                return yMod + ((LivingEntity) entity).getEyeHeight();
        case MINECART:
        case MINECART_COMMAND:
        case MINECART_CHEST:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
            switch (entity.getType())
            {
            case MINECART:
            case MINECART_CHEST:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                return yMod;
            default:
                return yMod + 0.4;
            }
        case TIPPED_ARROW:
        case SPECTRAL_ARROW:
        case BOAT:
        case EGG:
        case ENDER_PEARL:
        case ENDER_SIGNAL:
        case FIREWORK:
        case PAINTING:
        case SMALL_FIREBALL:
        case SNOWBALL:
        case SPLASH_POTION:
        case THROWN_EXP_BOTTLE:
        case WITHER_SKULL:
            return yMod + 0.7;
        case PLAYER:
            if (DisguiseConfig.isBedPacketsEnabled() && ((PlayerWatcher) disguise.getWatcher()).isSleeping())
            {
                return yMod + 0.35;
            }

            break;
        case DROPPED_ITEM:
            return yMod + 0.13;
        default:
            break;
        }
        return yMod;
    }

    /**
     * Creates the packet listeners
     */
    public static void init(LibsDisguises plugin)
    {
        libsDisguises = plugin;
        soundsListener = new PacketListenerSounds(libsDisguises);

        // Self disguise (/vsd) listener
        viewDisguisesListener = new PacketListenerViewDisguises(libsDisguises);

        inventoryListener = new PacketListenerInventory(libsDisguises);
    }

    public static boolean isHearDisguisesEnabled()
    {
        return soundsListenerEnabled;
    }

    public static boolean isInventoryListenerEnabled()
    {
        return inventoryModifierEnabled;
    }

    public static boolean isViewDisguisesListenerEnabled()
    {
        return viewDisguisesListenerEnabled;
    }

    public static void setHearDisguisesListener(boolean enabled)
    {
        if (soundsListenerEnabled != enabled)
        {
            soundsListenerEnabled = enabled;

            if (soundsListenerEnabled)
            {
                ProtocolLibrary.getProtocolManager().addPacketListener(soundsListener);
            }
            else
            {
                ProtocolLibrary.getProtocolManager().removePacketListener(soundsListener);
            }
        }
    }

    public static void setInventoryListenerEnabled(boolean enabled)
    {
        if (inventoryModifierEnabled != enabled)
        {
            inventoryModifierEnabled = enabled;

            if (inventoryModifierEnabled)
            {
                ProtocolLibrary.getProtocolManager().addPacketListener(inventoryListener);
            }
            else
            {
                ProtocolLibrary.getProtocolManager().removePacketListener(inventoryListener);
            }

            for (Player player : Bukkit.getOnlinePlayers())
            {
                Disguise disguise = DisguiseAPI.getDisguise(player, player);

                if (disguise != null)
                {
                    if (viewDisguisesListenerEnabled && disguise.isSelfDisguiseVisible()
                            && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()))
                    {
                        player.updateInventory();
                    }
                }
            }
        }
    }

    public static void setupMainPacketsListener()
    {
        if (clientInteractEntityListener != null)
        {
            if (mainListener != null)
            {
                ProtocolLibrary.getProtocolManager().removePacketListener(mainListener);
            }

            ArrayList<PacketType> packetsToListen = new ArrayList<PacketType>();
            // Add spawn packets
            {
                packetsToListen.add(Server.NAMED_ENTITY_SPAWN);
                packetsToListen.add(Server.SPAWN_ENTITY_EXPERIENCE_ORB);
                packetsToListen.add(Server.SPAWN_ENTITY);
                packetsToListen.add(Server.SPAWN_ENTITY_LIVING);
                packetsToListen.add(Server.SPAWN_ENTITY_PAINTING);
            }

            // Add packets that always need to be enabled to ensure safety
            {
                packetsToListen.add(Server.ENTITY_METADATA);
            }

            if (DisguiseConfig.isCollectPacketsEnabled())
            {
                packetsToListen.add(Server.COLLECT);
            }

            if (DisguiseConfig.isMiscDisguisesForLivingEnabled())
            {
                packetsToListen.add(Server.UPDATE_ATTRIBUTES);
            }

            // The bed packet.
            if (DisguiseConfig.isBedPacketsEnabled())
            {
                packetsToListen.add(Server.BED);
            }

            // Add movement packets
            if (DisguiseConfig.isMovementPacketsEnabled())
            {
                packetsToListen.add(Server.ENTITY_LOOK);
                packetsToListen.add(Server.REL_ENTITY_MOVE_LOOK);
                packetsToListen.add(Server.REL_ENTITY_MOVE_LOOK);
                packetsToListen.add(Server.ENTITY_HEAD_ROTATION);
                packetsToListen.add(Server.ENTITY_TELEPORT);
                packetsToListen.add(Server.REL_ENTITY_MOVE);
            }

            // Add equipment packet
            if (DisguiseConfig.isEquipmentPacketsEnabled())
            {
                packetsToListen.add(Server.ENTITY_EQUIPMENT);
            }

            // Add the packet that ensures if they are sleeping or not
            if (DisguiseConfig.isAnimationPacketsEnabled())
            {
                packetsToListen.add(Server.ANIMATION);
            }

            // Add the packet that makes sure that entities with armor do not send unpickupable armor on death
            if (DisguiseConfig.isEntityStatusPacketsEnabled())
            {
                packetsToListen.add(Server.ENTITY_STATUS);
            }

            mainListener = new PacketListenerMain(libsDisguises, packetsToListen);

            ProtocolLibrary.getProtocolManager().addPacketListener(mainListener);
        }
    }

    public static void setViewDisguisesListener(boolean enabled)
    {
        if (viewDisguisesListenerEnabled != enabled)
        {
            viewDisguisesListenerEnabled = enabled;

            if (viewDisguisesListenerEnabled)
            {
                ProtocolLibrary.getProtocolManager().addPacketListener(viewDisguisesListener);
            }
            else
            {
                ProtocolLibrary.getProtocolManager().removePacketListener(viewDisguisesListener);
            }

            for (Player player : Bukkit.getOnlinePlayers())
            {
                Disguise disguise = DisguiseAPI.getDisguise(player, player);

                if (disguise != null)
                {
                    if (disguise.isSelfDisguiseVisible())
                    {
                        if (enabled)
                        {
                            DisguiseUtilities.setupFakeDisguise(disguise);
                        }
                        else
                        {
                            DisguiseUtilities.removeSelfDisguise(player);
                        }

                        if (inventoryModifierEnabled && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf()))
                        {
                            player.updateInventory();
                        }
                    }
                }
            }
        }
    }

    /**
     * Transform the packet magically into the one I have always dreamed off. My true luv!!! This will return null if its not
     * transformed
     */
    public static PacketContainer[][] transformPacket(PacketContainer sentPacket, Player observer, Entity entity)
    {
        PacketContainer[] packets = null;

        PacketContainer[] delayedPackets = new PacketContainer[0];

        try
        {
            Disguise disguise = DisguiseAPI.getDisguise(observer, entity);

            // If disguised.
            if (disguise != null)
            {
                packets = new PacketContainer[]
                    {
                            sentPacket
                    };

                // This packet sends attributes
                if (sentPacket.getType() == Server.UPDATE_ATTRIBUTES)
                {
                    if (disguise.isMiscDisguise())
                    {
                        packets = new PacketContainer[0];
                    }
                    else
                    {
                        List<WrappedAttribute> attributes = new ArrayList<>();

                        for (WrappedAttribute attribute : sentPacket.getAttributeCollectionModifier().read(0))
                        {
                            if (attribute.getAttributeKey().equals("generic.maxHealth"))
                            {
                                packets[0] = new PacketContainer(Server.UPDATE_ATTRIBUTES);

                                Builder builder;

                                if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet())
                                {
                                    builder = WrappedAttribute.newBuilder();
                                    builder.attributeKey("generic.maxHealth");
                                    builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                                }
                                else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity())
                                {
                                    builder = WrappedAttribute.newBuilder(attribute);
                                }
                                else
                                {
                                    builder = WrappedAttribute.newBuilder();
                                    builder.attributeKey("generic.maxHealth");
                                    builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                                }

                                builder.packet(packets[0]);

                                attributes.add(builder.build());
                                break;
                            }
                        }

                        if (!attributes.isEmpty())
                        {
                            packets[0].getIntegers().write(0, entity.getEntityId());
                            packets[0].getAttributeCollectionModifier().write(0, attributes);
                        }
                        else
                        {
                            packets = new PacketContainer[0];
                        }
                    }
                }

                // Else if the packet is sending entity metadata
                else if (sentPacket.getType() == Server.ENTITY_METADATA)
                {
                    if (DisguiseConfig.isMetadataPacketsEnabled() && !isStaticMetadataDisguiseType(disguise))
                    {
                        List<WrappedWatchableObject> watchableObjects = disguise.getWatcher()
                                .convert(packets[0].getWatchableCollectionModifier().read(0));

                        packets[0] = new PacketContainer(sentPacket.getType());

                        StructureModifier<Object> newMods = packets[0].getModifier();

                        newMods.write(0, entity.getEntityId());

                        packets[0].getWatchableCollectionModifier().write(0, watchableObjects);
                    }
                    else
                    {
                        packets = new PacketContainer[0];
                    }
                }

                // Else if the packet is spawning..
                else if (sentPacket.getType() == Server.NAMED_ENTITY_SPAWN || sentPacket.getType() == Server.SPAWN_ENTITY_LIVING
                        || sentPacket.getType() == Server.SPAWN_ENTITY_EXPERIENCE_ORB
                        || sentPacket.getType() == Server.SPAWN_ENTITY || sentPacket.getType() == Server.SPAWN_ENTITY_PAINTING)
                {
                    PacketContainer[][] spawnPackets = constructSpawnPackets(observer, disguise, entity);

                    packets = spawnPackets[0];
                    delayedPackets = spawnPackets[1];
                }

                // Else if the disguise is attempting to send players a forbidden packet
                else if (sentPacket.getType() == Server.ANIMATION)
                {
                    if (disguise.getType().isMisc() || (packets[0].getIntegers().read(1) == 2 && (!disguise.getType().isPlayer()
                            || (DisguiseConfig.isBedPacketsEnabled() && ((PlayerWatcher) disguise.getWatcher()).isSleeping()))))
                    {
                        packets = new PacketContainer[0];
                    }
                }

                // Else if the disguise is collecting stuff
                else if (sentPacket.getType() == Server.COLLECT)
                {
                    if (disguise.getType().isMisc())
                    {
                        packets = new PacketContainer[0];
                    }

                    else if (DisguiseConfig.isBedPacketsEnabled() && disguise.getType().isPlayer()
                            && ((PlayerWatcher) disguise.getWatcher()).isSleeping())
                    {
                        PacketContainer newPacket = new PacketContainer(Server.ANIMATION);

                        StructureModifier<Integer> mods = newPacket.getIntegers();
                        mods.write(0, disguise.getEntity().getEntityId());
                        mods.write(1, 3);

                        packets = new PacketContainer[]
                            {
                                    newPacket, sentPacket
                            };
                    }
                }

                // Else if the disguise is moving.
                else if (sentPacket.getType() == Server.REL_ENTITY_MOVE_LOOK || sentPacket.getType() == Server.ENTITY_LOOK
                        || sentPacket.getType() == Server.ENTITY_TELEPORT || sentPacket.getType() == Server.REL_ENTITY_MOVE)
                {
                    if (disguise.getType() == DisguiseType.RABBIT && (sentPacket.getType() == Server.REL_ENTITY_MOVE
                            || sentPacket.getType() == Server.REL_ENTITY_MOVE_LOOK))
                    {
                        // Rabbit robbing...
                        if (entity.getMetadata("LibsRabbitHop").isEmpty()
                                || System.currentTimeMillis() - entity.getMetadata("LibsRabbitHop").get(0).asLong() < 100
                                || System.currentTimeMillis() - entity.getMetadata("LibsRabbitHop").get(0).asLong() > 500)
                        {
                            if (entity.getMetadata("LibsRabbitHop").isEmpty()
                                    || System.currentTimeMillis() - entity.getMetadata("LibsRabbitHop").get(0).asLong() > 500)
                            {
                                entity.removeMetadata("LibsRabbitHop", libsDisguises);
                                entity.setMetadata("LibsRabbitHop",
                                        new FixedMetadataValue(libsDisguises, System.currentTimeMillis()));
                            }

                            packets = Arrays.copyOf(packets, packets.length + 1);

                            packets[1] = new PacketContainer(Server.ENTITY_STATUS);

                            packets[1].getIntegers().write(0, entity.getEntityId());
                            packets[1].getBytes().write(0, (byte) 1);
                        }
                    }

                    // Stop wither skulls from looking
                    if (sentPacket.getType() == Server.ENTITY_LOOK && disguise.getType() == DisguiseType.WITHER_SKULL)
                    {
                        packets = new PacketContainer[0];
                    }
                    else if (sentPacket.getType() != Server.REL_ENTITY_MOVE)
                    {
                        packets[0] = sentPacket.shallowClone();

                        StructureModifier<Byte> bytes = packets[0].getBytes();

                        byte yawValue = bytes.read(0);
                        byte pitchValue = bytes.read(1);

                        bytes.write(0, getYaw(disguise.getType(), entity.getType(), yawValue));
                        bytes.write(1, getPitch(disguise.getType(), DisguiseType.getType(entity.getType()), pitchValue));

                        if (sentPacket.getType() == Server.ENTITY_TELEPORT && disguise.getType() == DisguiseType.ITEM_FRAME)
                        {
                            StructureModifier<Double> doubles = packets[0].getDoubles();

                            Location loc = entity.getLocation();

                            double data = (((loc.getYaw() % 360) + 720 + 45) / 90) % 4;

                            if (data % 2 == 0)
                            {
                                if (data % 2 == 0)
                                {
                                    doubles.write(3, loc.getZ());
                                }
                                else
                                {
                                    doubles.write(1, loc.getZ());
                                }
                            }

                            double y = getYModifier(entity, disguise);

                            if (y != 0)
                            {
                                doubles.write(2, doubles.read(2) + y);
                            }
                        }
                    }
                }

                // Else if the disguise is updating equipment
                else if (sentPacket.getType() == Server.ENTITY_EQUIPMENT)
                {
                    EquipmentSlot slot = ReflectionManager.createEquipmentSlot(packets[0].getModifier().read(1));

                    org.bukkit.inventory.ItemStack itemStack = disguise.getWatcher().getItemStack(slot);

                    if (itemStack != null)
                    {
                        packets[0] = packets[0].shallowClone();

                        packets[0].getModifier().write(2,
                                (itemStack.getTypeId() == 0 ? null : ReflectionManager.getNmsItem(itemStack)));
                    }
                    if (disguise.getWatcher().isRightClicking() && slot == EquipmentSlot.HAND)
                    {
                        ItemStack heldItem = packets[0].getItemModifier().read(0);

                        if (heldItem != null && heldItem.getType() != Material.AIR)
                        {
                            // Convert the datawatcher
                            List<WrappedWatchableObject> list = new ArrayList<>();

                            if (DisguiseConfig.isMetadataPacketsEnabled() && !isStaticMetadataDisguiseType(disguise))
                            {
                                WrappedWatchableObject watch = ReflectionManager.createWatchable(0,
                                        WrappedDataWatcher.getEntityWatcher(entity).getByte(0));

                                list.add(watch);

                                list = disguise.getWatcher().convert(list);
                            }
                            else
                            {
                                for (WrappedWatchableObject obj : disguise.getWatcher().getWatchableObjects())
                                {
                                    if (obj.getIndex() == 0)
                                    {
                                        list.add(obj);
                                        break;
                                    }
                                }
                            }

                            // Construct the packets to return
                            PacketContainer packetBlock = new PacketContainer(Server.ENTITY_METADATA);

                            packetBlock.getModifier().write(0, entity.getEntityId());
                            packetBlock.getWatchableCollectionModifier().write(0, list);

                            PacketContainer packetUnblock = packetBlock.deepClone();
                            // Make a packet to send the 'unblock'
                            for (WrappedWatchableObject watcher : packetUnblock.getWatchableCollectionModifier().read(0))
                            {
                                watcher.setValue((byte) ((byte) watcher.getValue() & ~(1 << 4)));
                            }

                            // Send the unblock before the itemstack change so that the 2nd metadata packet works. Why? Scheduler
                            // delay.
                            packets = new PacketContainer[]
                                {
                                        packetUnblock, packets[0], packetBlock
                                };
                            // Silly mojang made the right clicking datawatcher value only valid for one use. So I have to reset
                            // it.
                        }
                    }
                }

                // If the entity is going into a bed, stop everything but players from doing this
                else if (sentPacket.getType() == Server.BED)
                {
                    if (!disguise.getType().isPlayer())
                    {
                        packets = new PacketContainer[0];
                    }
                }

                // If the entity is updating their Facebook status, stop them from showing death
                else if (sentPacket.getType() == Server.ENTITY_STATUS)
                {
                    if (packets[0].getBytes().read(0) == (byte) 3)
                    {
                        packets = new PacketContainer[0];
                    }
                }

                // If the entity is rotating his head
                else if (sentPacket.getType() == Server.ENTITY_HEAD_ROTATION)
                {
                    if (disguise.getType().isPlayer() && entity.getType() != EntityType.PLAYER)
                    {
                        Location loc = entity.getLocation();

                        byte pitch = getPitch(disguise.getType(), DisguiseType.getType(entity.getType()),
                                (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
                        byte yaw = getYaw(disguise.getType(), entity.getType(), sentPacket.getBytes().read(0));

                        PacketContainer rotation = new PacketContainer(Server.ENTITY_HEAD_ROTATION);

                        StructureModifier<Object> mods = rotation.getModifier();

                        mods.write(0, entity.getEntityId());
                        mods.write(1, yaw);

                        PacketContainer look = new PacketContainer(Server.ENTITY_LOOK);

                        look.getIntegers().write(0, entity.getEntityId());
                        look.getBytes().write(0, yaw);
                        look.getBytes().write(1, pitch);

                        packets = new PacketContainer[]
                            {
                                    look, rotation
                            };
                    }
                }

                // Whatever
                else
                {
                    packets = null;
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return packets == null ? null : new PacketContainer[][]
            {
                    packets, delayedPackets
            };
    }

    /**
     * Returns true if this disguise type doesn't have changing metadata.
     * 
     * @param disguise
     * @return
     */
    public static boolean isStaticMetadataDisguiseType(Disguise disguise)
    {
        return false;
        /*        return (disguise.getType() == DisguiseType.WOLF || disguise.getType() == DisguiseType.OCELOT
                || disguise.getType() == DisguiseType.ENDERMAN || disguise.getType() == DisguiseType.SHULKER
                || disguise.getType() == DisguiseType.SPLASH_POTION || disguise.getType() == DisguiseType.FIREWORK
                || disguise.getType() == DisguiseType.DROPPED_ITEM || disguise.getType() == DisguiseType.ENDER_CRYSTAL);*/
    }
}
