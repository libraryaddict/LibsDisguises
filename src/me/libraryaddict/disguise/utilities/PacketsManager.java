package me.libraryaddict.disguise.utilities;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseSound.SoundType;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class PacketsManager {
    private static boolean cancelSound;
    private static PacketListener inventoryListenerClient;
    private static PacketListener inventoryListenerServer;
    private static boolean inventoryModifierEnabled;
    private static LibsDisguises libsDisguises;
    private static PacketListener soundsListener;
    private static boolean soundsListenerEnabled;
    private static PacketListener viewDisguisesListener;
    private static boolean viewDisguisesListenerEnabled;

    public static void addPacketListeners(JavaPlugin libsDisguises) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ENTITY_METADATA, Packets.Server.ARM_ANIMATION,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ADD_EXP_ORB, Packets.Server.VEHICLE_SPAWN, Packets.Server.MOB_SPAWN,
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT, Packets.Server.UPDATE_ATTRIBUTES,
                Packets.Server.ENTITY_EQUIPMENT, Packets.Server.BED, Packets.Server.ENTITY_STATUS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player observer = event.getPlayer();
                // First get the entity, the one sending this packet
                StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == event.getPacketID() ? 1 : 0));
                // If the entity is the same as the sender. Don't disguise!
                // Prevents problems and there is no advantage to be gained.
                if (entity == observer)
                    return;
                PacketContainer[] packets = transformPacket(event.getPacket(), event.getPlayer());
                if (packets.length == 0)
                    event.setCancelled(true);
                else {
                    event.setPacket(packets[0]);
                    final PacketContainer[] delayedPackets = new PacketContainer[packets.length - 1];
                    for (int i = 1; i < packets.length; i++) {
                        delayedPackets[i - 1] = packets[i];
                    }
                    if (delayedPackets.length > 0) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            public void run() {
                                try {
                                    for (PacketContainer packet : delayedPackets) {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                                    }
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
        // Now add a client listener to cancel them interacting with uninteractable disguised entitys.
        // You ain't supposed to be allowed to 'interact' with a item that cannot be clicked.
        // Because it kicks you for hacking.
        manager.addPacketListener(new PacketAdapter(libsDisguises, ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL,
                Packets.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read(ReflectionManager.isAfter17() ? 0 : 1);
                    if (entity instanceof ExperienceOrb || entity instanceof Item || entity instanceof Arrow) {
                        event.setCancelled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Construct the packets I need to spawn in the disguise
     */
    public static PacketContainer[] constructSpawnPackets(Disguise disguise, Entity disguisedEntity) {
        if (disguise.getEntity() == null)
            disguise.setEntity(disguisedEntity);
        Object nmsEntity = ReflectionManager.getNmsEntity(disguisedEntity);
        ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();
        // This sends the armor packets so that the player isn't naked.
        for (int nmsSlot = 0; nmsSlot < 5; nmsSlot++) {
            int armorSlot = nmsSlot - 1;
            if (armorSlot < 0)
                armorSlot = 4;
            org.bukkit.inventory.ItemStack itemstack = disguise.getWatcher().getItemStack(armorSlot);
            if (itemstack != null && itemstack.getTypeId() != 0) {
                ItemStack item = null;
                if (disguisedEntity instanceof LivingEntity) {
                    if (nmsSlot == 0) {
                        item = ((LivingEntity) disguisedEntity).getEquipment().getItemInHand();
                    } else {
                        item = ((LivingEntity) disguisedEntity).getEquipment().getArmorContents()[armorSlot];
                    }
                }
                if (item == null || item.getType() == Material.AIR) {
                    PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_EQUIPMENT);
                    StructureModifier<Object> mods = packet.getModifier();
                    mods.write(0, disguisedEntity.getEntityId());
                    mods.write(1, nmsSlot);
                    mods.write(2, ReflectionManager.getNmsItem(itemstack));
                    packets.add(packet);
                }
            }
        }
        PacketContainer[] spawnPackets = new PacketContainer[2 + packets.size()];
        for (int i = 0; i < packets.size(); i++) {
            spawnPackets[i + 2] = packets.get(i);
        }
        Location loc = disguisedEntity.getLocation().clone().add(0, getYModifier(disguisedEntity, disguise.getType()), 0);
        byte yaw = getYaw(disguise.getType(), disguise.getEntity().getType(), (byte) (int) (loc.getYaw() * 256.0F / 360.0F));

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {

            spawnPackets[0] = new PacketContainer(Packets.Server.ADD_EXP_ORB);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32));
            mods.write(2, (int) Math.floor(loc.getY() * 32) + 2);
            mods.write(3, (int) Math.floor(loc.getZ() * 32));
            mods.write(4, 1);

        } else if (disguise.getType() == DisguiseType.PAINTING) {
            spawnPackets[0] = new PacketContainer(Packets.Server.ENTITY_PAINTING);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getBlockX());
            mods.write(2, loc.getBlockY());
            mods.write(3, loc.getBlockZ());
            mods.write(4, ((int) loc.getYaw()) % 4);
            int id = ((MiscDisguise) disguise).getData();
            mods.write(5, ReflectionManager.getEnumArt(Art.values()[id]));

            // Make the teleport packet to make it visible..
            spawnPackets[1] = new PacketContainer(Packets.Server.ENTITY_TELEPORT);
            mods = spawnPackets[1].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32D));
            mods.write(2, (int) Math.floor(loc.getY() * 32D));
            mods.write(3, (int) Math.floor(loc.getZ() * 32D));
            mods.write(4, yaw);
            mods.write(5, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));

        } else if (disguise.getType().isPlayer()) {

            spawnPackets[0] = new PacketContainer(Packets.Server.NAMED_ENTITY_SPAWN);
            StructureModifier<String> stringMods = spawnPackets[0].getStrings();
            if (stringMods.size() > 0) {
                for (int i = 0; i < stringMods.size(); i++) {
                    stringMods.write(i, ((PlayerDisguise) disguise).getName());
                }
            } else {
                spawnPackets[0].getModifier().write(1, ReflectionManager.getGameProfile(((PlayerDisguise) disguise).getName()));
            }
            StructureModifier<Integer> intMods = spawnPackets[0].getIntegers();
            intMods.write(0, disguisedEntity.getEntityId());
            intMods.write(1, (int) Math.floor(loc.getX() * 32));
            intMods.write(2, (int) Math.floor(loc.getY() * 32));
            intMods.write(3, (int) Math.floor(loc.getZ() * 32));
            ItemStack item = null;
            if (disguisedEntity instanceof Player && ((Player) disguisedEntity).getItemInHand() != null) {
                item = ((Player) disguisedEntity).getItemInHand();
            } else if (disguisedEntity instanceof LivingEntity) {
                item = ((LivingEntity) disguisedEntity).getEquipment().getItemInHand();
            }
            intMods.write(4, (item == null || item.getType() == Material.AIR ? 0 : item.getTypeId()));
            StructureModifier<Byte> byteMods = spawnPackets[0].getBytes();
            byteMods.write(0, yaw);
            byteMods.write(1, (byte) (int) (loc.getPitch() * 256F / 360F));
            spawnPackets[0].getDataWatcherModifier().write(0,
                    createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher()));

        } else if (disguise.getType().isMob()) {

            DisguiseValues values = DisguiseValues.getDisguiseValues(disguise.getType());
            Vector vec = disguisedEntity.getVelocity();
            spawnPackets[0] = new PacketContainer(Packets.Server.MOB_SPAWN);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) disguise.getType().getEntityType().getTypeId());
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
            mods.write(2, values.getEntitySize(loc.getX()));
            mods.write(3, (int) Math.floor(loc.getY() * 32D));
            mods.write(4, values.getEntitySize(loc.getZ()));
            mods.write(5, (int) (d2 * 8000.0D));
            mods.write(6, (int) (d3 * 8000.0D));
            mods.write(7, (int) (d4 * 8000.0D));
            mods.write(8, yaw);
            mods.write(9, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
            spawnPackets[0].getDataWatcherModifier().write(0,
                    createDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher()));

        } else if (disguise.getType().isMisc()) {

            int id = disguise.getType().getEntityId();
            int data = 0;
            if (((MiscDisguise) disguise).getId() >= 0) {
                if (((MiscDisguise) disguise).getData() >= 0) {
                    data = (((MiscDisguise) disguise).getId() | ((MiscDisguise) disguise).getData() << 16);
                } else {
                    data = ((MiscDisguise) disguise).getId();
                }
            }
            // This won't actually work.
            // But if someone constructing the disguise uses it properly. It will work.
            if (disguise.getType() == DisguiseType.FISHING_HOOK)
                data = disguise.getEntity().getEntityId();
            /*     else if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                     data = (int) loc.getYaw();
                     if (data < 0)
                         data = -data;
                 }*/
            spawnPackets[0] = ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(Packets.Server.VEHICLE_SPAWN, nmsEntity, id, data).createPacket(nmsEntity, id, data);
            spawnPackets[0].getModifier().write(2, (int) Math.floor(loc.getY() * 32D));
            spawnPackets[0].getModifier().write(8, yaw);

            // Make the teleport packet to make it visible..
            spawnPackets[1] = new PacketContainer(Packets.Server.ENTITY_TELEPORT);
            StructureModifier<Object> mods = spawnPackets[1].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32D));
            mods.write(2, (int) Math.floor(loc.getY() * 32D));
            mods.write(3, (int) Math.floor(loc.getZ() * 32D));
            mods.write(4, yaw);
            mods.write(5, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));

        }
        if (spawnPackets[1] == null) {
            // Make a packet to turn his head!
            spawnPackets[1] = new PacketContainer(Packets.Server.ENTITY_HEAD_ROTATION);
            StructureModifier<Object> mods = spawnPackets[1].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, yaw);
        }
        return spawnPackets;
    }

    /**
     * Create a new datawatcher but with the 'correct' values
     */
    private static WrappedDataWatcher createDataWatcher(WrappedDataWatcher watcher, FlagWatcher flagWatcher) {
        WrappedDataWatcher newWatcher = new WrappedDataWatcher();
        try {
            // Calling c() gets the watchable objects exactly as they are.
            List<WrappedWatchableObject> list = watcher.getWatchableObjects();
            for (WrappedWatchableObject watchableObject : flagWatcher.convert(list)) {
                newWatcher.setObject(watchableObject.getIndex(), watchableObject.getValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newWatcher;

    }

    public static byte getPitch(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        switch (disguiseType) {
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
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
            value += 64;
            break;
        case ENDER_DRAGON:
        case WITHER_SKULL:
            value -= 128;
            break;
        // case ITEM_FRAME:
        case ARROW:
            value = (byte) -value;
            break;
        case PAINTING:
            value = (byte) -(value + 128);
            break;
        default:
            if (disguiseType.isMisc()) {
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
        // case ITEM_FRAME:
        case ARROW:
            value = (byte) -value;
            break;
        case PAINTING:
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
    private static double getYModifier(Entity entity, DisguiseType disguiseType) {
        switch (disguiseType) {
        case BAT:
            if (entity instanceof LivingEntity)
                return ((LivingEntity) entity).getEyeHeight();
        case MINECART:
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
                return 0;
            default:
                return 0.4;
            }
        case ARROW:
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
            return 0.7;
        default:
            break;
        }
        return 0;
    }

    /**
     * Creates the packet listeners
     */
    public static void init(LibsDisguises plugin) {
        libsDisguises = plugin;
        soundsListener = new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL,
                Packets.Server.NAMED_SOUND_EFFECT, Packets.Server.ENTITY_STATUS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled())
                    return;
                StructureModifier<Object> mods = event.getPacket().getModifier();
                Player observer = event.getPlayer();
                if (event.getPacketID() == Packets.Server.NAMED_SOUND_EFFECT) {
                    String soundName = (String) mods.read(0);
                    SoundType soundType = null;
                    Location soundLoc = new Location(observer.getWorld(), ((Integer) mods.read(1)) / 8D,
                            ((Integer) mods.read(2)) / 8D, ((Integer) mods.read(3)) / 8D);
                    Entity disguisedEntity = null;
                    DisguiseSound entitySound = null;
                    Disguise disguise = null;
                    for (Entity entity : soundLoc.getChunk().getEntities()) {
                        Disguise entityDisguise = DisguiseAPI.getDisguise(observer, entity);
                        if (entityDisguise != null) {
                            Location loc = entity.getLocation();
                            loc = new Location(observer.getWorld(), ((int) (loc.getX() * 8)) / 8D, ((int) (loc.getY() * 8)) / 8D,
                                    ((int) (loc.getZ() * 8)) / 8D);
                            if (loc.equals(soundLoc)) {
                                entitySound = DisguiseSound.getType(entity.getType().name());
                                if (entitySound != null) {
                                    Object obj = null;
                                    if (entity instanceof LivingEntity) {
                                        try {
                                            obj = LivingEntity.class.getMethod("getHealth").invoke(entity);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (obj instanceof Double ? (Double) obj == 0 : (Integer) obj == 0) {
                                            soundType = SoundType.DEATH;
                                        } else {
                                            obj = null;
                                        }
                                    }
                                    if (obj == null) {
                                        boolean hasInvun = false;
                                        Object nmsEntity = ReflectionManager.getNmsEntity(entity);
                                        try {
                                            Class entityClass = ReflectionManager.getNmsClass("Entity");
                                            if (entity instanceof LivingEntity) {
                                                hasInvun = entityClass.getField("noDamageTicks").getInt(nmsEntity) == ReflectionManager
                                                        .getNmsClass("EntityLiving").getField("maxNoDamageTicks")
                                                        .getInt(nmsEntity);
                                            } else {
                                                hasInvun = (Boolean) entityClass.getMethod("isInvulnerable").invoke(nmsEntity);
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        soundType = entitySound.getType(soundName, !hasInvun);
                                    }
                                    if (soundType != null) {
                                        disguise = entityDisguise;
                                        disguisedEntity = entity;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (disguise != null) {
                        if (disguise.isSelfDisguiseSoundsReplaced() || disguisedEntity != event.getPlayer()) {
                            if (disguise.isSoundsReplaced()) {
                                String sound = null;
                                DisguiseSound dSound = DisguiseSound.getType(disguise.getType().name());
                                if (dSound != null && soundType != null)
                                    sound = dSound.getSound(soundType);
                                if (sound == null) {
                                    event.setCancelled(true);
                                } else {
                                    if (sound.equals("step.grass")) {
                                        try {
                                            int typeId = soundLoc.getWorld().getBlockTypeIdAt(soundLoc.getBlockX(),
                                                    soundLoc.getBlockY() - 1, soundLoc.getBlockZ());
                                            Class blockClass = ReflectionManager.getNmsClass("Block");
                                            Object block;
                                            if (ReflectionManager.isAfter17()) {
                                                block = blockClass.getMethod("REGISTRY", int.class).invoke(null, typeId);
                                            } else {
                                                block = ((Object[]) blockClass.getField("byId").get(null))[typeId];
                                            }
                                            if (block != null) {
                                                Object step = blockClass.getField("stepSound").get(block);
                                                mods.write(0, step.getClass().getMethod("getStepSound").invoke(step));
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        // There is no else statement. Because seriously. This should never be null. Unless
                                        // someone is
                                        // sending fake sounds. In which case. Why cancel it.
                                    } else {
                                        mods.write(0, sound);
                                        // Time to change the pitch and volume
                                        if (soundType == SoundType.HURT || soundType == SoundType.DEATH
                                                || soundType == SoundType.IDLE) {
                                            // If the volume is the default
                                            if (soundType != SoundType.IDLE
                                                    && ((Float) mods.read(4)).equals(entitySound.getDamageSoundVolume())) {
                                                mods.write(4, dSound.getDamageSoundVolume());
                                            }
                                            // Here I assume its the default pitch as I can't calculate if its real.
                                            if (disguise instanceof MobDisguise && disguisedEntity instanceof LivingEntity
                                                    && ((MobDisguise) disguise).doesDisguiseAge()) {
                                                boolean baby = false;
                                                if (disguisedEntity instanceof Zombie) {
                                                    baby = ((Zombie) disguisedEntity).isBaby();
                                                } else if (disguisedEntity instanceof Ageable) {
                                                    baby = !((Ageable) disguisedEntity).isAdult();
                                                }
                                                if (((MobDisguise) disguise).isAdult() == baby) {

                                                    float pitch = (Integer) mods.read(5);
                                                    if (baby) {
                                                        // If the pitch is not the expected
                                                        if (pitch > 97 || pitch < 111)
                                                            return;
                                                        pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.5F;
                                                        // Min = 1.5
                                                        // Cap = 97.5
                                                        // Max = 1.7
                                                        // Cap = 110.5
                                                    } else {
                                                        // If the pitch is not the expected
                                                        if (pitch >= 63 || pitch <= 76)
                                                            return;
                                                        pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.0F;
                                                        // Min = 1
                                                        // Cap = 63
                                                        // Max = 1.2
                                                        // Cap = 75.6
                                                    }
                                                    pitch *= 63;
                                                    if (pitch < 0)
                                                        pitch = 0;
                                                    if (pitch > 255)
                                                        pitch = 255;
                                                    mods.write(5, (int) pitch);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (event.getPacketID() == Packets.Server.ENTITY_STATUS) {
                    if ((Byte) mods.read(1) == 1) {
                        // It made a damage animation
                        Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);
                        Disguise disguise = DisguiseAPI.getDisguise(observer, entity);
                        if (disguise != null && (disguise.isSelfDisguiseSoundsReplaced() || entity != event.getPlayer())) {
                            DisguiseSound disSound = DisguiseSound.getType(entity.getType().name());
                            if (disSound == null)
                                return;
                            SoundType soundType = null;
                            Object obj = null;
                            if (entity instanceof LivingEntity) {
                                try {
                                    obj = LivingEntity.class.getMethod("getHealth").invoke(entity);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (obj instanceof Double ? (Double) obj == 0 : (Integer) obj == 0) {
                                    soundType = SoundType.DEATH;
                                } else {
                                    obj = null;
                                }
                            }
                            if (obj == null) {
                                soundType = SoundType.HURT;
                            }
                            if (disSound.getSound(soundType) == null
                                    || (disguise.isSelfDisguiseSoundsReplaced() && entity == event.getPlayer())) {
                                if (disguise.isSelfDisguiseSoundsReplaced() && entity == event.getPlayer()) {
                                    cancelSound = !cancelSound;
                                    if (cancelSound)
                                        return;
                                }
                                disSound = DisguiseSound.getType(disguise.getType().name());
                                if (disSound != null) {
                                    String sound = disSound.getSound(soundType);
                                    if (sound != null) {
                                        Location loc = entity.getLocation();
                                        PacketContainer packet = new PacketContainer(Packets.Server.NAMED_SOUND_EFFECT);
                                        mods = packet.getModifier();
                                        mods.write(0, sound);
                                        mods.write(1, (int) (loc.getX() * 8D));
                                        mods.write(2, (int) (loc.getY() * 8D));
                                        mods.write(3, (int) (loc.getZ() * 8D));
                                        mods.write(4, disSound.getDamageSoundVolume());
                                        float pitch;
                                        if (disguise instanceof MobDisguise && !((MobDisguise) disguise).isAdult()) {
                                            pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.5F;
                                        } else
                                            pitch = (new Random().nextFloat() - new Random().nextFloat()) * 0.2F + 1.0F;
                                        if (disguise.getType() == DisguiseType.BAT)
                                            pitch *= 95F;
                                        pitch *= 63;
                                        if (pitch < 0)
                                            pitch = 0;
                                        if (pitch > 255)
                                            pitch = 255;
                                        mods.write(5, (int) pitch);
                                        try {
                                            ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet);
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        viewDisguisesListener = new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ATTACH_ENTITY, Packets.Server.REL_ENTITY_MOVE,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ENTITY_HEAD_ROTATION, Packets.Server.ENTITY_METADATA, Packets.Server.ENTITY_EQUIPMENT,
                Packets.Server.ARM_ANIMATION, Packets.Server.ENTITY_LOCATION_ACTION, Packets.Server.MOB_EFFECT,
                Packets.Server.ENTITY_VELOCITY, Packets.Server.UPDATE_ATTRIBUTES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player observer = event.getPlayer();
                if (event.getPacket().getIntegers().read(0) == observer.getEntityId()) {
                    int fakeId = DisguiseAPI.getFakeDisguise(observer.getEntityId());
                    if (fakeId > 0) {
                        // Here I grab the packets to convert them to, So I can display them as if the disguise sent them.
                        PacketContainer[] packets = transformPacket(event.getPacket(), observer);
                        final PacketContainer[] delayedPackets = new PacketContainer[packets.length > 0 ? packets.length - 1 : 0];
                        for (int i = 0; i < packets.length; i++) {
                            PacketContainer packet = packets[i];
                            if (packet.equals(event.getPacket()))
                                packet = packet.deepClone();
                            packet.getModifier().write(0, fakeId);
                            if (i == 0) {
                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                delayedPackets[i - 1] = packet;
                            }
                        }
                        if (delayedPackets.length > 0) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                public void run() {
                                    try {
                                        for (PacketContainer packet : delayedPackets) {
                                            ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                                        }
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        switch (event.getPacketID()) {
                        case Packets.Server.ENTITY_METADATA:
                            event.setPacket(event.getPacket().deepClone());
                            Iterator<WrappedWatchableObject> itel = event.getPacket().getWatchableCollectionModifier().read(0)
                                    .iterator();
                            while (itel.hasNext()) {
                                WrappedWatchableObject watch = itel.next();
                                if (watch.getIndex() == 0) {
                                    byte b = (Byte) watch.getValue();
                                    byte a = (byte) (b | 1 << 5);
                                    if ((b & 1 << 3) != 0)
                                        a = (byte) (a | 1 << 3);
                                    watch.setValue(a);
                                }
                            }
                            break;
                        case Packets.Server.NAMED_ENTITY_SPAWN:
                            PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_METADATA);
                            StructureModifier<Object> mods = packet.getModifier();
                            mods.write(0, observer.getEntityId());
                            List<WrappedWatchableObject> watchableList = new ArrayList<WrappedWatchableObject>();
                            byte b = (byte) (0 | 1 << 5);
                            if (observer.isSprinting())
                                b = (byte) (b | 1 << 3);
                            watchableList.add(new WrappedWatchableObject(0, b));
                            packet.getWatchableCollectionModifier().write(0, watchableList);
                            event.setPacket(packet);
                            break;
                        case Packets.Server.ATTACH_ENTITY:
                        case Packets.Server.REL_ENTITY_MOVE:
                        case Packets.Server.REL_ENTITY_MOVE_LOOK:
                        case Packets.Server.ENTITY_LOOK:
                        case Packets.Server.ENTITY_TELEPORT:
                        case Packets.Server.ENTITY_HEAD_ROTATION:
                        case Packets.Server.MOB_EFFECT:
                        case Packets.Server.ENTITY_EQUIPMENT:
                            event.setCancelled(true);
                            break;

                        /*     case Packets.Server.ENTITY_STATUS:
                                 if (DisguiseAPI.getDisguise(entity).canHearSelfDisguise()
                                         && (Byte) event.getPacket().getModifier().read(1) == 1) {
                                     event.setCancelled(true);
                                 }
                                 break;*/
                        default:
                            break;
                        }
                    }
                }
            }
        };
        // TODO Potentionally combine both listeners.
        inventoryListenerServer = new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST,
                Packets.Server.SET_SLOT, Packets.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // If the inventory is the players inventory
                if (event.getPlayer().isOnline() && event.getPlayer().getVehicle() == null
                        && event.getPacket().getIntegers().read(0) == 0) {
                    Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer(), event.getPlayer());
                    // If the player is disguised, views self disguises and is hiding a item.
                    if (disguise != null && disguise.isSelfDisguiseVisible()
                            && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
                        switch (event.getPacketID()) {
                        // If the server is setting the slot
                        // Need to set it to air if its in a place it shouldn't be.
                        // Things such as picking up a item, spawned in item. Plugin sets the item. etc. Will fire this
                        /**
                         * Done
                         */
                        case Packets.Server.SET_SLOT: {
                            // The raw slot
                            // nms code has the start of the hotbar being 36.
                            int slot = event.getPacket().getIntegers().read(1);
                            // If the slot is a armor slot
                            if (slot >= 5 && slot <= 8) {
                                if (disguise.isHidingArmorFromSelf()) {
                                    // Get the bukkit armor slot!
                                    int armorSlot = Math.abs((slot - 5) - 3);
                                    org.bukkit.inventory.ItemStack item = event.getPlayer().getInventory().getArmorContents()[armorSlot];
                                    if (item != null && item.getType() != Material.AIR) {
                                        event.setPacket(event.getPacket().shallowClone());
                                        event.getPacket().getModifier()
                                                .write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                    }
                                }
                                // Else if its a hotbar slot
                            } else if (slot >= 36 && slot <= 44) {
                                if (disguise.isHidingHeldItemFromSelf()) {
                                    int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();
                                    // Check if the player is on the same slot as the slot that its setting
                                    if (slot == currentSlot + 36) {
                                        org.bukkit.inventory.ItemStack item = event.getPlayer().getItemInHand();
                                        if (item != null && item.getType() != Material.AIR) {
                                            event.setPacket(event.getPacket().shallowClone());
                                            event.getPacket()
                                                    .getModifier()
                                                    .write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        /**
                         * Done
                         */
                        case Packets.Server.WINDOW_ITEMS: {
                            event.setPacket(event.getPacket().deepClone());
                            StructureModifier<ItemStack[]> mods = event.getPacket().getItemArrayModifier();
                            ItemStack[] items = mods.read(0);
                            for (int slot = 0; slot < items.length; slot++) {
                                if (slot >= 5 && slot <= 8) {
                                    if (disguise.isHidingArmorFromSelf()) {
                                        // Get the bukkit armor slot!
                                        int armorSlot = Math.abs((slot - 5) - 3);
                                        org.bukkit.inventory.ItemStack item = event.getPlayer().getInventory().getArmorContents()[armorSlot];
                                        if (item != null && item.getType() != Material.AIR) {
                                            items[slot] = new org.bukkit.inventory.ItemStack(0);
                                        }
                                    }
                                    // Else if its a hotbar slot
                                } else if (slot >= 36 && slot <= 44) {
                                    if (disguise.isHidingHeldItemFromSelf()) {
                                        int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();
                                        // Check if the player is on the same slot as the slot that its setting
                                        if (slot == currentSlot + 36) {
                                            org.bukkit.inventory.ItemStack item = event.getPlayer().getItemInHand();
                                            if (item != null && item.getType() != Material.AIR) {
                                                items[slot] = new org.bukkit.inventory.ItemStack(0);
                                            }
                                        }
                                    }
                                }
                            }
                            mods.write(0, items);
                            break;
                        }
                        default:
                            break;
                        }
                    }
                }
            }
        };
        inventoryListenerClient = new PacketAdapter(libsDisguises, ConnectionSide.CLIENT_SIDE, ListenerPriority.HIGHEST,
                Packets.Client.BLOCK_ITEM_SWITCH, Packets.Client.SET_CREATIVE_SLOT, Packets.Client.WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(final PacketEvent event) {
                if (event.getPlayer().getVehicle() == null) {
                    Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer(), event.getPlayer());
                    // If player is disguised, views self disguises and has a inventory modifier
                    if (disguise != null && disguise.isSelfDisguiseVisible()
                            && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
                        switch (event.getPacketID()) {
                        // If they are in creative and clicked on a slot
                        case Packets.Client.SET_CREATIVE_SLOT: {
                            int slot = event.getPacket().getIntegers().read(0);
                            if (slot >= 5 && slot <= 8) {
                                if (disguise.isHidingArmorFromSelf()) {
                                    int armorSlot = Math.abs((slot - 5) - 3);
                                    org.bukkit.inventory.ItemStack item = event.getPlayer().getInventory().getArmorContents()[armorSlot];
                                    if (item != null && item.getType() != Material.AIR) {
                                        PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                        StructureModifier<Object> mods = packet.getModifier();
                                        mods.write(0, 0);
                                        mods.write(1, slot);
                                        mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                        try {
                                            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet,
                                                    false);
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else if (slot >= 36 && slot <= 44) {
                                if (disguise.isHidingHeldItemFromSelf()) {
                                    int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();
                                    if (slot + 36 == currentSlot) {
                                        org.bukkit.inventory.ItemStack item = event.getPlayer().getItemInHand();
                                        if (item != null && item.getType() != Material.AIR) {
                                            PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                            StructureModifier<Object> mods = packet.getModifier();
                                            mods.write(0, 0);
                                            mods.write(1, slot);
                                            mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                            try {
                                                ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet,
                                                        false);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        // If the player switched item, aka he moved from slot 1 to slot 2
                        case Packets.Client.BLOCK_ITEM_SWITCH: {
                            if (disguise.isHidingHeldItemFromSelf()) {
                                // From logging, it seems that both bukkit and nms uses the same thing for the slot switching.
                                // 0 1 2 3 - 8
                                // If the packet is coming, then I need to replace the item they are switching to
                                // As for the old item, I need to restore it.
                                org.bukkit.inventory.ItemStack currentlyHeld = event.getPlayer().getItemInHand();
                                // If his old weapon isn't air
                                if (currentlyHeld != null && currentlyHeld.getType() != Material.AIR) {
                                    PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                    StructureModifier<Object> mods = packet.getModifier();
                                    mods.write(0, 0);
                                    mods.write(1, event.getPlayer().getInventory().getHeldItemSlot() + 36);
                                    mods.write(2, ReflectionManager.getNmsItem(currentlyHeld));
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                                org.bukkit.inventory.ItemStack newHeld = event.getPlayer().getInventory()
                                        .getItem(event.getPacket().getIntegers().read(0));
                                // If his new weapon isn't air either!
                                if (newHeld != null && newHeld.getType() != Material.AIR) {
                                    PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                    StructureModifier<Object> mods = packet.getModifier();
                                    mods.write(0, 0);
                                    mods.write(1, event.getPacket().getIntegers().read(0) + 36);
                                    mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        }

                        case Packets.Client.WINDOW_CLICK: {
                            int slot = event.getPacket().getIntegers().read(1);
                            org.bukkit.inventory.ItemStack clickedItem;
                            if (event.getPacket().getIntegers().read(3) == 1) {
                                // Its a shift click
                                clickedItem = event.getPacket().getItemModifier().read(0);
                                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                                    // Rather than predict the clients actions
                                    // Lets just update the entire inventory..
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                                        public void run() {
                                            event.getPlayer().updateInventory();
                                        }
                                    });
                                }
                                return;
                            } else {
                                // If its not a player inventory click
                                // Shift clicking is exempted for the item in hand..
                                if (event.getPacket().getIntegers().read(0) != 0) {
                                    return;
                                }
                                clickedItem = event.getPlayer().getItemOnCursor();
                            }
                            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                                // If the slot is a armor slot
                                if (slot >= 5 && slot <= 8) {
                                    if (disguise.isHidingArmorFromSelf()) {
                                        PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                        StructureModifier<Object> mods = packet.getModifier();
                                        mods.write(0, 0);
                                        mods.write(1, slot);
                                        mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                        try {
                                            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet,
                                                    false);
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    // Else if its a hotbar slot
                                } else if (slot >= 36 && slot <= 44) {
                                    if (disguise.isHidingHeldItemFromSelf()) {
                                        int currentSlot = event.getPlayer().getInventory().getHeldItemSlot();
                                        // Check if the player is on the same slot as the slot that its setting
                                        if (slot == currentSlot + 36) {
                                            PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                            StructureModifier<Object> mods = packet.getModifier();
                                            mods.write(0, 0);
                                            mods.write(1, slot);
                                            mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(0)));
                                            try {
                                                ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet,
                                                        false);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }

                        default:
                            break;
                        }
                    }
                }
            }
        };
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
                ProtocolLibrary.getProtocolManager().addPacketListener(inventoryListenerClient);
                ProtocolLibrary.getProtocolManager().addPacketListener(inventoryListenerServer);
            } else {
                ProtocolLibrary.getProtocolManager().removePacketListener(inventoryListenerClient);
                ProtocolLibrary.getProtocolManager().removePacketListener(inventoryListenerServer);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                Disguise disguise = DisguiseAPI.getDisguise(player, player);
                if (disguise != null) {
                    if (viewDisguisesListenerEnabled && disguise.isSelfDisguiseVisible()
                            && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
                        player.updateInventory();
                    }
                }
            }
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
     * Transform the packet magically into the one I have always dreamed off. My true luv!!!
     */
    private static PacketContainer[] transformPacket(PacketContainer sentPacket, Player observer) {
        PacketContainer[] packets = new PacketContainer[] { sentPacket };
        try {
            // First get the entity, the one sending this packet
            StructureModifier<Entity> entityModifer = sentPacket.getEntityModifier(observer.getWorld());
            org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == sentPacket.getID() ? 1 : 0));
            Disguise disguise = DisguiseAPI.getDisguise(observer, entity);
            // If disguised.
            if (disguise != null) {
                // If packet is Packets.Server.UPDATE_ATTRIBUTES
                // This packet sends attributes

                switch (sentPacket.getID()) {

                case Packets.Server.UPDATE_ATTRIBUTES:

                {

                    packets = new PacketContainer[0];
                    break;
                }

                // Else if the packet is sending entity metadata
                case Packets.Server.ENTITY_METADATA:

                {
                    List<WrappedWatchableObject> watchableObjects = disguise.getWatcher().convert(
                            packets[0].getWatchableCollectionModifier().read(0));
                    packets[0] = new PacketContainer(sentPacket.getID());
                    StructureModifier<Object> newMods = packets[0].getModifier();
                    newMods.write(0, entity.getEntityId());
                    packets[0].getWatchableCollectionModifier().write(0, watchableObjects);
                    break;
                }

                // Else if the packet is spawning..
                case Packets.Server.NAMED_ENTITY_SPAWN:
                case Packets.Server.MOB_SPAWN:
                case Packets.Server.ADD_EXP_ORB:
                case Packets.Server.VEHICLE_SPAWN:
                case Packets.Server.ENTITY_PAINTING:

                {
                    packets = constructSpawnPackets(disguise, entity);
                    break;
                }

                // Else if the disguise is attempting to send players a forbidden packet
                case Packets.Server.ARM_ANIMATION:

                {
                    if (disguise.getType().isMisc() || (packets[0].getIntegers().read(1) == 3 && !disguise.getType().isPlayer())) {
                        packets = new PacketContainer[0];
                    }
                    break;

                }

                case Packets.Server.COLLECT:

                {
                    if (disguise.getType().isMisc()) {
                        packets = new PacketContainer[0];
                    }
                    break;

                }
                // Else if the disguise is moving.
                case Packets.Server.REL_ENTITY_MOVE_LOOK:
                case Packets.Server.ENTITY_LOOK:
                case Packets.Server.ENTITY_TELEPORT:

                {
                    if (sentPacket.getID() == Packets.Server.ENTITY_LOOK && disguise.getType() == DisguiseType.WITHER_SKULL) {
                        packets = new PacketContainer[0];
                    } else {
                        packets[0] = sentPacket.shallowClone();
                        StructureModifier<Object> mods = packets[0].getModifier();
                        byte yawValue = (Byte) mods.read(4);
                        mods.write(4, getYaw(disguise.getType(), entity.getType(), yawValue));
                        byte pitchValue = (Byte) mods.read(5);
                        mods.write(5, getPitch(disguise.getType(), DisguiseType.getType(entity.getType()), pitchValue));
                        if (sentPacket.getID() == Packets.Server.ENTITY_TELEPORT) {
                            double y = getYModifier(entity, disguise.getType());
                            if (y != 0) {
                                y *= 32;
                                mods.write(2, (Integer) mods.read(2) + (int) Math.floor(y));
                            }
                        }
                    }
                    break;
                }

                case Packets.Server.ENTITY_EQUIPMENT:

                {
                    int slot = (Integer) packets[0].getModifier().read(1) - 1;
                    if (slot < 0)
                        slot = 4;
                    org.bukkit.inventory.ItemStack itemstack = disguise.getWatcher().getItemStack(slot);
                    if (itemstack != null) {
                        packets[0] = packets[0].shallowClone();
                        packets[0].getModifier().write(2,
                                (itemstack.getTypeId() == 0 ? null : ReflectionManager.getNmsItem(itemstack)));
                    }
                    break;
                }

                case Packets.Server.ENTITY_LOCATION_ACTION:

                {
                    if (!disguise.getType().isPlayer()) {
                        packets = new PacketContainer[0];
                    }
                    break;
                }

                case Packets.Server.ENTITY_STATUS:

                {
                    if (packets[0].getBytes().read(0) == (byte) 3) {
                        packets = new PacketContainer[0];
                    }
                }

                default:
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packets;
    }
}
