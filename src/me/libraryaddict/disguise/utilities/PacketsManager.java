package me.libraryaddict.disguise.utilities;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
import com.comphenix.protocol.PacketType.Play;
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
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerClientInteract;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerInventory;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerMain;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerSounds;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerTabList;
import me.libraryaddict.disguise.utilities.packetlisteners.PacketListenerViewDisguises;

public class PacketsManager {
    public static class LibsPackets {
        private ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();
        private HashMap<Integer, ArrayList<PacketContainer>> delayedPackets = new HashMap<Integer, ArrayList<PacketContainer>>();
        private boolean isSpawnPacket;
        private Disguise disguise;
        private boolean doNothing;

        public void setUnhandled() {
            doNothing = true;
        }

        public boolean isUnhandled() {
            return doNothing;
        }

        private LibsPackets(Disguise disguise) {
            this.disguise = disguise;
        }

        public Disguise getDisguise() {
            return disguise;
        }

        public void setPacketType(PacketType type) {
            isSpawnPacket = type.name().contains("SPAWN") && type.name().contains("ENTITY");
        }

        public void addPacket(PacketContainer packet) {
            packets.add(packet);
        }

        public void addDelayedPacket(PacketContainer packet) {
            addDelayedPacket(packet, 2);
        }

        public void clear() {
            getPackets().clear();
        }

        public void addDelayedPacket(PacketContainer packet, int ticksDelayed) {
            if (!delayedPackets.containsKey(ticksDelayed))
                delayedPackets.put(ticksDelayed, new ArrayList<PacketContainer>());

            delayedPackets.get(ticksDelayed).add(packet);
        }

        public ArrayList<PacketContainer> getPackets() {
            return packets;
        }

        public Collection<ArrayList<PacketContainer>> getDelayedPackets() {
            return delayedPackets.values();
        }

        public void sendDelayed(final Player observer) {
            for (final Entry<Integer, ArrayList<PacketContainer>> entry : delayedPackets.entrySet()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                    public void run() {
                        try {
                            for (PacketContainer packet : entry.getValue()) {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                            }
                        }
                        catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        if (isSpawnPacket) {
                            PacketsManager.removeCancel(disguise, observer);
                        }
                    }
                }, entry.getKey());
            }
        }
    }

    private static PacketListener clientInteractEntityListener;
    private static PacketListener inventoryListener;
    private static boolean inventoryModifierEnabled;
    private static LibsDisguises libsDisguises;
    private static PacketListener mainListener;
    private static PacketListener soundsListener;
    private static boolean soundsListenerEnabled;
    private static PacketListener viewDisguisesListener;
    private static PacketListener tabListListener;
    private static boolean viewDisguisesListenerEnabled;
    private static HashMap<Disguise, ArrayList<UUID>> cancelMeta = new HashMap<Disguise, ArrayList<UUID>>();

    public static void addPacketListeners() {
        // Add a client listener to cancel them interacting with uninteractable disguised entitys.
        // You ain't supposed to be allowed to 'interact' with a item that cannot be clicked.
        // Because it kicks you for hacking.

        clientInteractEntityListener = new PacketListenerClientInteract(libsDisguises);
        tabListListener = new PacketListenerTabList(libsDisguises);

        ProtocolLibrary.getProtocolManager().addPacketListener(clientInteractEntityListener);
        ProtocolLibrary.getProtocolManager().addPacketListener(tabListListener);

        // Now I call this and the main listener is registered!
        setupMainPacketsListener();
    }

    public static void removeCancel(Disguise disguise, Player observer) {
        ArrayList<UUID> cancel;

        if ((cancel = cancelMeta.get(disguise)) == null)
            return;

        cancel.remove(observer.getUniqueId());

        if (!cancel.isEmpty())
            return;

        cancelMeta.remove(disguise);
    }

    /**
     * Construct the packets I need to spawn in the disguise
     */
    private static LibsPackets constructSpawnPackets(final Player observer, LibsPackets packets,
            Entity disguisedEntity) {
        Disguise disguise = packets.getDisguise();

        if (disguise.getEntity() == null) {
            disguise.setEntity(disguisedEntity);
        }

        // This sends the armor packets so that the player isn't naked.
        // Please note it only sends the packets that wouldn't be sent normally
        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack itemstack = disguise.getWatcher().getItemStack(slot);

                if (itemstack == null || itemstack.getType() == Material.AIR) {
                    continue;
                }

                ItemStack item = null;

                if (disguisedEntity instanceof LivingEntity) {
                    item = ReflectionManager.getEquipment(slot, disguisedEntity);
                }

                if (item != null && item.getType() != Material.AIR) {
                    continue;
                }

                PacketContainer packet = new PacketContainer(Server.ENTITY_EQUIPMENT);

                StructureModifier<Object> mods = packet.getModifier();

                mods.write(0, disguisedEntity.getEntityId());
                mods.write(1, ReflectionManager.createEnumItemSlot(slot));
                mods.write(2, ReflectionManager.getNmsItem(itemstack));

                packets.addPacket(packet);
            }
        }

        if (DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            if (disguise.getWatcher() instanceof LivingWatcher) {

                ArrayList<WrappedAttribute> attributes = new ArrayList<WrappedAttribute>();

                Builder builder = WrappedAttribute.newBuilder().attributeKey("generic.maxHealth");

                if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                    builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity() && disguisedEntity instanceof Damageable) {
                    builder.baseValue(((Damageable) disguisedEntity).getMaxHealth());
                } else {
                    builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                }

                PacketContainer packet = new PacketContainer(Server.UPDATE_ATTRIBUTES);

                builder.packet(packet);

                attributes.add(builder.build());

                packet.getIntegers().write(0, disguisedEntity.getEntityId());
                packet.getAttributeCollectionModifier().write(0, attributes);

                packets.addPacket(packet);
            }
        }

        Location loc = disguisedEntity.getLocation().clone().add(0, getYModifier(disguisedEntity, disguise), 0);

        byte yaw = (byte) (int) (loc.getYaw() * 256.0F / 360.0F);
        byte pitch = (byte) (int) (loc.getPitch() * 256.0F / 360.0F);

        if (DisguiseConfig.isMovementPacketsEnabled()) {
            yaw = getYaw(disguise.getType(), disguisedEntity.getType(), yaw);
            pitch = getPitch(disguise.getType(), DisguiseType.getType(disguisedEntity.getType()), pitch);
        }

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {
            PacketContainer spawnOrb = new PacketContainer(Server.SPAWN_ENTITY_EXPERIENCE_ORB);
            packets.addPacket(spawnOrb);

            StructureModifier<Object> mods = spawnOrb.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY() + 0.06);
            mods.write(3, loc.getZ());
            mods.write(4, 1);
        } else if (disguise.getType() == DisguiseType.PAINTING) {
            PacketContainer spawnPainting = new PacketContainer(Server.SPAWN_ENTITY_PAINTING);
            packets.addPacket(spawnPainting);

            StructureModifier<Object> mods = spawnPainting.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguisedEntity.getUniqueId());
            mods.write(2, ReflectionManager.getBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            mods.write(3, ReflectionManager.getEnumDirection(((int) loc.getYaw()) % 4));

            int id = ((MiscDisguise) disguise).getData();

            mods.write(4, ReflectionManager.getEnumArt(Art.values()[id]));

            // Make the teleport packet to make it visible..
            PacketContainer teleportPainting = new PacketContainer(Server.ENTITY_TELEPORT);
            packets.addPacket(teleportPainting);

            mods = teleportPainting.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY());
            mods.write(3, loc.getZ());
            mods.write(4, yaw);
            mods.write(5, pitch);
        } else if (disguise.getType().isPlayer()) {
            PlayerDisguise playerDisguise = (PlayerDisguise) disguise;

            String name = playerDisguise.getName();
            int entityId = disguisedEntity.getEntityId();

            // Send player info along with the disguise
            PacketContainer sendTab = new PacketContainer(Server.PLAYER_INFO);

            if (!((PlayerDisguise) disguise).isDisplayedInTab())
                packets.addPacket(sendTab);

            // Add player to the list, necessary to spawn them
            sendTab.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(0));

            WrappedGameProfile gameProfile = playerDisguise.getGameProfile();
            List playerList = new ArrayList();

            playerList.add(ReflectionManager.getPlayerInfoData(sendTab.getHandle(), gameProfile));
            sendTab.getModifier().write(1, playerList);

            // Spawn the player
            PacketContainer spawnPlayer = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

            spawnPlayer.getIntegers().write(0, entityId); // Id
            spawnPlayer.getModifier().write(1, gameProfile.getUUID());

            Location spawnAt = disguisedEntity.getLocation();

            boolean selfDisguise = observer == disguisedEntity;

            WrappedDataWatcher newWatcher;

            if (selfDisguise) {
                newWatcher = createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity),
                        disguise.getWatcher());
            } else {
                newWatcher = new WrappedDataWatcher();

                spawnAt = observer.getLocation();
                spawnAt.add(spawnAt.getDirection().normalize().multiply(20));
            }

            // Spawn him in front of the observer
            StructureModifier<Double> doubles = spawnPlayer.getDoubles();
            doubles.write(0, spawnAt.getX());
            doubles.write(1, spawnAt.getY());
            doubles.write(2, spawnAt.getZ());

            StructureModifier<Byte> bytes = spawnPlayer.getBytes();
            bytes.write(0, ((byte) (int) (loc.getYaw() * 256.0F / 360.0F)));
            bytes.write(1, ((byte) (int) (loc.getPitch() * 256.0F / 360.0F)));

            spawnPlayer.getDataWatcherModifier().write(0, newWatcher);

            // Make him invisible
            newWatcher.setObject(
                    new WrappedDataWatcherObject(MetaIndex.ENTITY_META.getIndex(), Registry.get(Byte.class)),
                    (byte) 32);

            packets.addPacket(spawnPlayer);

            if (DisguiseConfig.isBedPacketsEnabled() && ((PlayerWatcher) disguise.getWatcher()).isSleeping()) {
                PacketContainer[] bedPackets = DisguiseUtilities.getBedPackets(
                        loc.clone().subtract(0, PacketsManager.getYModifier(disguisedEntity, disguise), 0),
                        observer.getLocation(), ((PlayerDisguise) disguise));

                for (PacketContainer packet : bedPackets) {
                    packets.addPacket(packet);
                }
            } else if (!selfDisguise) {
                // Teleport the player back to where he's supposed to be
                PacketContainer teleportPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

                doubles = teleportPacket.getDoubles();

                teleportPacket.getIntegers().write(0, entityId); // Id
                doubles.write(0, loc.getX());
                doubles.write(1, loc.getY());
                doubles.write(2, loc.getZ());

                bytes = teleportPacket.getBytes();
                bytes.write(0, ((byte) (int) (loc.getYaw() * 256.0F / 360.0F)));
                bytes.write(1, ((byte) (int) (loc.getPitch() * 256.0F / 360.0F)));

                packets.addPacket(teleportPacket);
            }

            if (!selfDisguise) {
                // Send a metadata packet
                PacketContainer metaPacket = new PacketContainer(Play.Server.ENTITY_METADATA);

                newWatcher = createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity),
                        disguise.getWatcher());

                metaPacket.getIntegers().write(0, entityId); // Id
                metaPacket.getWatchableCollectionModifier().write(0, newWatcher.getWatchableObjects());

                if (!cancelMeta.containsKey(disguise))
                    cancelMeta.put(disguise, new ArrayList<UUID>());

                cancelMeta.get(disguise).add(observer.getUniqueId());

                packets.addDelayedPacket(metaPacket, 4);
            }

            // Remove player from the list
            PacketContainer deleteTab = sendTab.shallowClone();
            deleteTab.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(4));

            if (!((PlayerDisguise) disguise).isDisplayedInTab()) {
                packets.addDelayedPacket(deleteTab, 40);
            }
        } else if (disguise.getType().isMob() || disguise.getType() == DisguiseType.ARMOR_STAND) {
            Vector vec = disguisedEntity.getVelocity();

            PacketContainer spawnEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packets.addPacket(spawnEntity);

            StructureModifier<Object> mods = spawnEntity.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguisedEntity.getUniqueId());
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

            spawnEntity.getDataWatcherModifier().write(0,
                    createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher()));
        } else if (disguise.getType().isMisc()) {
            int objectId = disguise.getType().getObjectId();
            int data = ((MiscDisguise) disguise).getData();

            if (disguise.getType() == DisguiseType.FALLING_BLOCK) {
                data = ReflectionManager.getCombinedId(((MiscDisguise) disguise).getId(), data);
            } else if (disguise.getType() == DisguiseType.FISHING_HOOK && data == -1) {
                // If the MiscDisguise data isn't set. Then no entity id was provided, so default to the owners entity id
                data = observer.getEntityId();
            } else if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                data = ((((int) loc.getYaw() % 360) + 720 + 45) / 90) % 4;
            }

            Object nmsEntity = ReflectionManager.getNmsEntity(disguisedEntity);

            PacketContainer spawnEntity = ProtocolLibrary.getProtocolManager().createPacketConstructor(
                    PacketType.Play.Server.SPAWN_ENTITY, nmsEntity, objectId, data).createPacket(nmsEntity, objectId,
                    data);
            packets.addPacket(spawnEntity);

            spawnEntity.getModifier().write(8, pitch);
            spawnEntity.getModifier().write(9, yaw);

            if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                if (data % 2 == 0) {
                    spawnEntity.getModifier().write(4, loc.getZ() + (data == 0 ? -1 : 1));
                } else {
                    spawnEntity.getModifier().write(2, loc.getX() + (data == 3 ? -1 : 1));
                }
            }
        }

        if (packets.getPackets().size() <= 1 || disguise.isPlayerDisguise()) {
            PacketContainer rotateHead = new PacketContainer(Server.ENTITY_HEAD_ROTATION);
            packets.addPacket(rotateHead);

            StructureModifier<Object> mods = rotateHead.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, yaw);
        }

        if (disguise.getType() == DisguiseType.EVOKER_FANGS) {
            PacketContainer newPacket = new PacketContainer(Server.ENTITY_STATUS);

            StructureModifier<Object> mods = newPacket.getModifier();
            mods.write(0, disguise.getEntity().getEntityId());
            mods.write(1, (byte) 4);

            packets.addPacket(newPacket);
        }

        return packets;
    }

    /**
     * Create a new datawatcher but with the 'correct' values
     */
    private static WrappedDataWatcher createDataWatcher(WrappedDataWatcher watcher, FlagWatcher flagWatcher) {
        WrappedDataWatcher newWatcher = new WrappedDataWatcher();

        try {
            List<WrappedWatchableObject> list = DisguiseConfig.isMetadataPacketsEnabled() ? flagWatcher.convert(
                    watcher.getWatchableObjects()) : flagWatcher.getWatchableObjects();

            for (WrappedWatchableObject watchableObject : list) {
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
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return newWatcher;
    }

    public static byte getPitch(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        switch (disguiseType) {
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
        switch (entityType) {
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
    public static byte getYaw(DisguiseType disguiseType, EntityType entityType, byte value) {
        switch (disguiseType) {
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                value += 64;
                break;
            case BOAT:
            case ENDER_DRAGON:
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
                if (disguiseType.isMisc() && disguiseType != DisguiseType.ARMOR_STAND) {
                    value -= 64;
                }

                break;
        }
        switch (entityType) {
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
                if (!entityType.isAlive()) {
                    value += 64;
                }

                break;
        }

        return value;
    }

    /**
     * Get the Y level to add to the disguise for realism.
     */
    public static double getYModifier(Entity entity, Disguise disguise) {
        double yMod = 0;

        if ((disguise.getType() != DisguiseType.PLAYER || !((PlayerWatcher) disguise.getWatcher()).isSleeping()) && entity.getType() == EntityType.DROPPED_ITEM) {
            yMod -= 0.13;
        }

        switch (disguise.getType()) {
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
                switch (entity.getType()) {
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
                if (DisguiseConfig.isBedPacketsEnabled() && ((PlayerWatcher) disguise.getWatcher()).isSleeping()) {
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
    public static void init(LibsDisguises plugin) {
        libsDisguises = plugin;
        soundsListener = new PacketListenerSounds(libsDisguises);

        // Self disguise (/vsd) listener
        viewDisguisesListener = new PacketListenerViewDisguises(libsDisguises);

        inventoryListener = new PacketListenerInventory(libsDisguises);
    }

    public static boolean isHearDisguisesEnabled() {
        return soundsListenerEnabled;
    }

    public static boolean isInventoryListenerEnabled() {
        return inventoryModifierEnabled;
    }

    public static boolean isViewDisguisesListenerEnabled() {
        return viewDisguisesListenerEnabled;
    }

    public static void setHearDisguisesListener(boolean enabled) {
        if (soundsListenerEnabled != enabled) {
            soundsListenerEnabled = enabled;

            if (soundsListenerEnabled) {
                ProtocolLibrary.getProtocolManager().addPacketListener(soundsListener);
            } else {
                ProtocolLibrary.getProtocolManager().removePacketListener(soundsListener);
            }
        }
    }

    public static void setInventoryListenerEnabled(boolean enabled) {
        if (inventoryModifierEnabled != enabled) {
            inventoryModifierEnabled = enabled;

            if (inventoryModifierEnabled) {
                ProtocolLibrary.getProtocolManager().addPacketListener(inventoryListener);
            } else {
                ProtocolLibrary.getProtocolManager().removePacketListener(inventoryListener);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                Disguise disguise = DisguiseAPI.getDisguise(player, player);

                if (disguise != null) {
                    if (viewDisguisesListenerEnabled && disguise.isSelfDisguiseVisible() && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
                        player.updateInventory();
                    }
                }
            }
        }
    }

    public static void setupMainPacketsListener() {
        if (clientInteractEntityListener != null) {
            if (mainListener != null) {
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

            if (DisguiseConfig.isCollectPacketsEnabled()) {
                packetsToListen.add(Server.COLLECT);
            }

            if (DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
                packetsToListen.add(Server.UPDATE_ATTRIBUTES);
            }

            // The bed packet.
            if (DisguiseConfig.isBedPacketsEnabled()) {
                packetsToListen.add(Server.BED);
            }

            // Add movement packets
            if (DisguiseConfig.isMovementPacketsEnabled()) {
                packetsToListen.add(Server.ENTITY_LOOK);
                packetsToListen.add(Server.REL_ENTITY_MOVE_LOOK);
                packetsToListen.add(Server.ENTITY_HEAD_ROTATION);
                packetsToListen.add(Server.ENTITY_TELEPORT);
                packetsToListen.add(Server.REL_ENTITY_MOVE);
            }

            // Add equipment packet
            if (DisguiseConfig.isEquipmentPacketsEnabled()) {
                packetsToListen.add(Server.ENTITY_EQUIPMENT);
            }

            // Add the packet that ensures if they are sleeping or not
            if (DisguiseConfig.isAnimationPacketsEnabled()) {
                packetsToListen.add(Server.ANIMATION);
            }

            // Add the packet that makes sure that entities with armor do not send unpickupable armor on death
            if (DisguiseConfig.isEntityStatusPacketsEnabled()) {
                packetsToListen.add(Server.ENTITY_STATUS);
            }

            mainListener = new PacketListenerMain(libsDisguises, packetsToListen);

            ProtocolLibrary.getProtocolManager().addPacketListener(mainListener);
        }
    }

    public static void setViewDisguisesListener(boolean enabled) {
        if (viewDisguisesListenerEnabled != enabled) {
            viewDisguisesListenerEnabled = enabled;

            if (viewDisguisesListenerEnabled) {
                ProtocolLibrary.getProtocolManager().addPacketListener(viewDisguisesListener);
            } else {
                ProtocolLibrary.getProtocolManager().removePacketListener(viewDisguisesListener);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                Disguise disguise = DisguiseAPI.getDisguise(player, player);

                if (disguise != null) {
                    if (disguise.isSelfDisguiseVisible()) {
                        if (enabled) {
                            DisguiseUtilities.setupFakeDisguise(disguise);
                        } else {
                            DisguiseUtilities.removeSelfDisguise(player);
                        }

                        if (inventoryModifierEnabled && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
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
    public static LibsPackets transformPacket(PacketContainer sentPacket, Disguise disguise, Player observer,
            Entity entity) {
        LibsPackets packets = new LibsPackets(disguise);

        try {
            packets.addPacket(sentPacket);

            // This packet sends attributes
            if (sentPacket.getType() == Server.UPDATE_ATTRIBUTES) {
                if (disguise.isMiscDisguise()) {
                    packets.clear();
                } else {
                    List<WrappedAttribute> attributes = new ArrayList<>();

                    for (WrappedAttribute attribute : sentPacket.getAttributeCollectionModifier().read(0)) {
                        if (attribute.getAttributeKey().equals("generic.maxHealth")) {
                            packets.clear();

                            PacketContainer updateAttributes = new PacketContainer(Server.UPDATE_ATTRIBUTES);
                            packets.addPacket(updateAttributes);

                            Builder builder;

                            if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                                builder = WrappedAttribute.newBuilder();
                                builder.attributeKey("generic.maxHealth");
                                builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                            } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity()) {
                                builder = WrappedAttribute.newBuilder(attribute);
                            } else {
                                builder = WrappedAttribute.newBuilder();
                                builder.attributeKey("generic.maxHealth");
                                builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                            }

                            builder.packet(updateAttributes);

                            attributes.add(builder.build());
                            break;
                        }
                    }

                    if (!attributes.isEmpty()) {
                        packets.getPackets().get(0).getIntegers().write(0, entity.getEntityId());
                        packets.getPackets().get(0).getAttributeCollectionModifier().write(0, attributes);
                    } else {
                        packets.clear();
                    }
                }
            }

            // Else if the packet is sending entity metadata
            else if (sentPacket.getType() == Server.ENTITY_METADATA) {
                packets.clear();

                if (DisguiseConfig.isMetadataPacketsEnabled() && (!cancelMeta.containsKey(disguise) || !cancelMeta.get(
                        disguise).contains(observer.getUniqueId()))) {
                    List<WrappedWatchableObject> watchableObjects = disguise.getWatcher().convert(
                            sentPacket.getWatchableCollectionModifier().read(0));

                    PacketContainer metaPacket = new PacketContainer(Server.ENTITY_METADATA);

                    packets.addPacket(metaPacket);

                    StructureModifier<Object> newMods = metaPacket.getModifier();

                    newMods.write(0, entity.getEntityId());

                    metaPacket.getWatchableCollectionModifier().write(0, watchableObjects);
                }
            }

            // Else if the packet is spawning..
            else if (sentPacket.getType() == Server.NAMED_ENTITY_SPAWN || sentPacket.getType() == Server.SPAWN_ENTITY_LIVING || sentPacket.getType() == Server.SPAWN_ENTITY_EXPERIENCE_ORB || sentPacket.getType() == Server.SPAWN_ENTITY || sentPacket.getType() == Server.SPAWN_ENTITY_PAINTING) {
                packets.clear();

                constructSpawnPackets(observer, packets, entity);
            }

            // Else if the disguise is attempting to send players a forbidden packet
            else if (sentPacket.getType() == Server.ANIMATION) {
                if (disguise.getType().isMisc() || (sentPacket.getIntegers().read(
                        1) == 2 && (!disguise.getType().isPlayer() || (DisguiseConfig.isBedPacketsEnabled() && ((PlayerWatcher) disguise.getWatcher()).isSleeping())))) {
                    packets.clear();
                }
            }

            // Else if the disguise is collecting stuff
            else if (sentPacket.getType() == Server.COLLECT) {
                if (disguise.getType().isMisc()) {
                    packets.clear();
                } else if (DisguiseConfig.isBedPacketsEnabled() && disguise.getType().isPlayer() && ((PlayerWatcher) disguise.getWatcher()).isSleeping()) {
                    PacketContainer newPacket = new PacketContainer(Server.ANIMATION);

                    StructureModifier<Integer> mods = newPacket.getIntegers();
                    mods.write(0, disguise.getEntity().getEntityId());
                    mods.write(1, 3);

                    packets.clear();

                    packets.addPacket(newPacket);
                    packets.addPacket(sentPacket);
                }
            }

            // Else if the disguise is moving.
            else if (sentPacket.getType() == Server.REL_ENTITY_MOVE_LOOK || sentPacket.getType() == Server.ENTITY_LOOK || sentPacket.getType() == Server.ENTITY_TELEPORT || sentPacket.getType() == Server.REL_ENTITY_MOVE) {
                if (disguise.getType() == DisguiseType.RABBIT && (sentPacket.getType() == Server.REL_ENTITY_MOVE || sentPacket.getType() == Server.REL_ENTITY_MOVE_LOOK)) {
                    // Rabbit robbing...
                    if (entity.getMetadata(
                            "LibsRabbitHop").isEmpty() || System.currentTimeMillis() - entity.getMetadata(
                            "LibsRabbitHop").get(0).asLong() < 100 || System.currentTimeMillis() - entity.getMetadata(
                            "LibsRabbitHop").get(0).asLong() > 500) {
                        if (entity.getMetadata(
                                "LibsRabbitHop").isEmpty() || System.currentTimeMillis() - entity.getMetadata(
                                "LibsRabbitHop").get(0).asLong() > 500) {
                            entity.removeMetadata("LibsRabbitHop", libsDisguises);
                            entity.setMetadata("LibsRabbitHop",
                                    new FixedMetadataValue(libsDisguises, System.currentTimeMillis()));
                        }

                        PacketContainer statusPacket = new PacketContainer(Server.ENTITY_STATUS);
                        packets.addPacket(statusPacket);

                        statusPacket.getIntegers().write(0, entity.getEntityId());
                        statusPacket.getBytes().write(0, (byte) 1);
                    }
                }

                // Stop wither skulls from looking
                if (sentPacket.getType() == Server.ENTITY_LOOK && disguise.getType() == DisguiseType.WITHER_SKULL) {
                    packets.clear();
                } else if (sentPacket.getType() != Server.REL_ENTITY_MOVE) {
                    packets.clear();

                    PacketContainer movePacket = sentPacket.shallowClone();

                    packets.addPacket(movePacket);

                    StructureModifier<Byte> bytes = movePacket.getBytes();

                    byte yawValue = bytes.read(0);
                    byte pitchValue = bytes.read(1);

                    bytes.write(0, getYaw(disguise.getType(), entity.getType(), yawValue));
                    bytes.write(1, getPitch(disguise.getType(), DisguiseType.getType(entity.getType()), pitchValue));

                    if (sentPacket.getType() == Server.ENTITY_TELEPORT && disguise.getType() == DisguiseType.ITEM_FRAME) {
                        StructureModifier<Double> doubles = movePacket.getDoubles();

                        Location loc = entity.getLocation();

                        double data = (((loc.getYaw() % 360) + 720 + 45) / 90) % 4;

                        if (data % 2 == 0) {
                            if (data % 2 == 0) {
                                doubles.write(3, loc.getZ());
                            } else {
                                doubles.write(1, loc.getZ());
                            }
                        }

                        double y = getYModifier(entity, disguise);

                        if (y != 0) {
                            doubles.write(2, doubles.read(2) + y);
                        }
                    }
                }
            }

            // Else if the disguise is updating equipment
            else if (sentPacket.getType() == Server.ENTITY_EQUIPMENT) {
                EquipmentSlot slot = ReflectionManager.createEquipmentSlot(
                        packets.getPackets().get(0).getModifier().read(1));

                org.bukkit.inventory.ItemStack itemStack = disguise.getWatcher().getItemStack(slot);

                if (itemStack != null) {
                    packets.clear();

                    PacketContainer equipPacket = sentPacket.shallowClone();

                    packets.addPacket(equipPacket);

                    equipPacket.getModifier().write(2,
                            ReflectionManager.getNmsItem(itemStack.getType() == Material.AIR ? null : itemStack));
                }

                if (disguise.getWatcher().isRightClicking() && slot == EquipmentSlot.HAND) {
                    ItemStack heldItem = packets.getPackets().get(0).getItemModifier().read(0);

                    if (heldItem != null && heldItem.getType() != Material.AIR) {
                        // Convert the datawatcher
                        List<WrappedWatchableObject> list = new ArrayList<>();

                        if (DisguiseConfig.isMetadataPacketsEnabled()) {
                            WrappedWatchableObject watch = ReflectionManager.createWatchable(0,
                                    WrappedDataWatcher.getEntityWatcher(entity).getByte(0));

                            list.add(watch);

                            list = disguise.getWatcher().convert(list);
                        } else {
                            for (WrappedWatchableObject obj : disguise.getWatcher().getWatchableObjects()) {
                                if (obj.getIndex() == 0) {
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
                        for (WrappedWatchableObject watcher : packetUnblock.getWatchableCollectionModifier().read(0)) {
                            watcher.setValue((byte) ((byte) watcher.getValue() & ~(1 << 4)));
                        }

                        // Send the unblock before the itemstack change so that the 2nd metadata packet works. Why? Scheduler
                        // delay.

                        PacketContainer packet1 = packets.getPackets().get(0);

                        packets.clear();

                        packets.addPacket(packetUnblock);
                        packets.addPacket(packet1);
                        packets.addPacket(packetBlock);
                        // Silly mojang made the right clicking datawatcher value only valid for one use. So I have to reset
                        // it.
                    }
                }
            }

            // If the entity is going into a bed, stop everything but players from doing this
            else if (sentPacket.getType() == Server.BED) {
                if (!disguise.getType().isPlayer()) {
                    packets.clear();
                }
            }

            // If the entity is updating their Facebook status, stop them from showing death
            else if (sentPacket.getType() == Server.ENTITY_STATUS) {
                if (packets.getPackets().get(0).getBytes().read(0) == (byte) 3) {
                    packets.clear();
                }
            }

            // If the entity is rotating his head
            else if (sentPacket.getType() == Server.ENTITY_HEAD_ROTATION) {
                if (disguise.getType().isPlayer() && entity.getType() != EntityType.PLAYER) {
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

                    packets.clear();

                    packets.addPacket(look);
                    packets.addPacket(rotation);
                }
            } else {
                packets.setUnhandled();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return packets;
    }
}
