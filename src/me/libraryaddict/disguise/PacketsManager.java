package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseSound;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.Values;
import me.libraryaddict.disguise.disguisetypes.DisguiseSound.SoundType;
import net.minecraft.server.v1_6_R3.AttributeMapServer;
import net.minecraft.server.v1_6_R3.AttributeSnapshot;
import net.minecraft.server.v1_6_R3.Block;
import net.minecraft.server.v1_6_R3.DataWatcher;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityInsentient;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.EntityTrackerEntry;
import net.minecraft.server.v1_6_R3.EnumArt;
import net.minecraft.server.v1_6_R3.EnumEntitySize;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.MobEffect;
import net.minecraft.server.v1_6_R3.Packet17EntityLocationAction;
import net.minecraft.server.v1_6_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R3.Packet28EntityVelocity;
import net.minecraft.server.v1_6_R3.Packet35EntityHeadRotation;
import net.minecraft.server.v1_6_R3.Packet39AttachEntity;
import net.minecraft.server.v1_6_R3.Packet40EntityMetadata;
import net.minecraft.server.v1_6_R3.Packet41MobEffect;
import net.minecraft.server.v1_6_R3.Packet44UpdateAttributes;
import net.minecraft.server.v1_6_R3.Packet5EntityEquipment;
import net.minecraft.server.v1_6_R3.WatchableObject;
import net.minecraft.server.v1_6_R3.World;
import net.minecraft.server.v1_6_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

public class PacketsManager {
    private static boolean cancelSound;
    private static DisguiseAPI disguiseAPI = new DisguiseAPI();
    private static PacketListener inventoryListenerClient;
    private static PacketListener inventoryListenerServer;
    private static boolean inventoryModifierEnabled;
    private static LibsDisguises libsDisguises;
    private static PacketListener soundsListener;
    private static boolean soundsListenerEnabled;
    private static PacketListener viewDisguisesListener;
    private static boolean viewDisguisesListenerEnabled;

    protected static void addPacketListeners(final JavaPlugin libsDisguises) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(libsDisguises, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ENTITY_METADATA, Packets.Server.ARM_ANIMATION,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ADD_EXP_ORB, Packets.Server.VEHICLE_SPAWN, Packets.Server.MOB_SPAWN,
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT, Packets.Server.UPDATE_ATTRIBUTES,
                Packets.Server.ENTITY_EQUIPMENT, Packets.Server.BED) {
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
                    for (int i = 1; i < packets.length; i++)
                        delayedPackets[i - 1] = packets[i];
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            try {
                                for (PacketContainer packet : delayedPackets)
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet);
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    });
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
                    org.bukkit.entity.Entity entity = entityModifer.read(1);
                    if (DisguiseAPI.isDisguised(entity)
                            && (entity instanceof ExperienceOrb || entity instanceof Item || entity instanceof Arrow)) {
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
        net.minecraft.server.v1_6_R3.Entity nmsEntity = ((CraftEntity) disguisedEntity).getHandle();
        ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();
        for (int i = 0; i < 5; i++) {
            int slot = i - 1;
            if (slot < 0)
                slot = 4;
            org.bukkit.inventory.ItemStack itemstack = disguise.getWatcher().getItemStack(slot);
            if (itemstack != null && itemstack.getTypeId() != 0) {
                ItemStack item = null;
                if (nmsEntity instanceof EntityLiving)
                    item = ((EntityLiving) nmsEntity).getEquipment(i);
                if (item == null) {
                    PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_EQUIPMENT);
                    StructureModifier<Object> mods = packet.getModifier();
                    mods.write(0, disguisedEntity.getEntityId());
                    mods.write(1, i);
                    mods.write(2, CraftItemStack.asNMSCopy(itemstack));
                    packets.add(packet);
                }
            }
        }
        PacketContainer[] spawnPackets = new PacketContainer[2 + packets.size()];
        for (int i = 0; i < packets.size(); i++) {
            spawnPackets[i + 2] = packets.get(i);
        }
        Location loc = disguisedEntity.getLocation().clone().add(0, getYModifier(disguisedEntity, disguise.getType()), 0);
        byte yaw = getYaw(disguise.getType(), DisguiseType.getType(disguise.getEntity().getType()),
                (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
        EnumEntitySize entitySize = Values.getValues(disguise.getType()).getEntitySize();

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
            int id = ((MiscDisguise) disguise).getId();
            if (id == -1)
                id = new Random().nextInt(EnumArt.values().length);
            mods.write(5, EnumArt.values()[id].B);

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
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, ((PlayerDisguise) disguise).getName());
            mods.write(2, (int) Math.floor(loc.getX() * 32));
            mods.write(3, (int) Math.floor(loc.getY() * 32));
            mods.write(4, (int) Math.floor(loc.getZ() * 32));
            mods.write(5, yaw);
            mods.write(6, (byte) (int) (loc.getPitch() * 256F / 360F));
            ItemStack item = null;
            if (disguisedEntity instanceof Player && ((Player) disguisedEntity).getItemInHand() != null) {
                item = CraftItemStack.asNMSCopy(((Player) disguisedEntity).getItemInHand());
            } else if (disguisedEntity instanceof LivingEntity) {
                item = CraftItemStack.asNMSCopy(((CraftLivingEntity) disguisedEntity).getEquipment().getItemInHand());
            }
            mods.write(7, (item == null ? 0 : item.id));
            mods.write(8, createDataWatcher(nmsEntity.getDataWatcher(), disguise.getWatcher()));

        } else if (disguise.getType().isMob()) {

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
            mods.write(2, entitySize.a(loc.getX()));
            mods.write(3, (int) Math.floor(loc.getY() * 32D));
            mods.write(4, entitySize.a(loc.getZ()));
            mods.write(5, (int) (d2 * 8000.0D));
            mods.write(6, (int) (d3 * 8000.0D));
            mods.write(7, (int) (d4 * 8000.0D));
            mods.write(8, yaw);
            mods.write(9, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
            if (nmsEntity instanceof EntityLiving)
                mods.write(10, (byte) (int) (((EntityLiving) nmsEntity).aA * 256.0F / 360.0F));
            mods.write(11, createDataWatcher(nmsEntity.getDataWatcher(), disguise.getWatcher()));
            // Theres a list sometimes written with this. But no problems have appeared!
            // Probably just the metadata to be sent. But the next meta packet after fixes that anyways.

        } else if (disguise.getType().isMisc()) {

            int id = disguise.getType().getEntityId();
            int data = 0;
            if (((MiscDisguise) disguise).getId() >= 0)
                if (((MiscDisguise) disguise).getData() >= 0)
                    data = (((MiscDisguise) disguise).getId() | ((MiscDisguise) disguise).getData() << 16);
                else
                    data = ((MiscDisguise) disguise).getId();
            // This won't actually work.
            // But if someone constructing the disguise uses it properly. It will work.
            if (disguise.getType() == DisguiseType.FISHING_HOOK)
                data = disguise.getEntity().getEntityId();
            else if (disguise.getType() == DisguiseType.ITEM_FRAME)
                data = (int) Math.abs(loc.getYaw() % 4);
            spawnPackets[0] = new PacketContainer(Packets.Server.VEHICLE_SPAWN);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32D));
            mods.write(2, (int) Math.floor(loc.getY() * 32D));
            mods.write(3, (int) Math.floor(loc.getZ() * 32D));
            if (data > 0) {
                Vector vec = disguisedEntity.getVelocity();
                double d1 = vec.getX();
                double d2 = vec.getY();
                double d3 = vec.getZ();
                double d4 = 3.9D;
                if (d1 < -d4)
                    d1 = -d4;
                if (d2 < -d4)
                    d2 = -d4;
                if (d3 < -d4)
                    d3 = -d4;
                if (d1 > d4)
                    d1 = d4;
                if (d2 > d4)
                    d2 = d4;
                if (d3 > d4)
                    d3 = d4;
                mods.write(4, (int) (d1 * 8000.0D));
                mods.write(5, (int) (d2 * 8000.0D));
                mods.write(6, (int) (d3 * 8000.0D));
            }
            mods.write(7, (int) MathHelper.floor(loc.getPitch() * 256.0F / 360.0F));
            mods.write(8, yaw);
            mods.write(9, id);
            mods.write(10, data);

        }
        if (spawnPackets[1] == null) {
            // Make a packet to turn his head!
            spawnPackets[1] = new PacketContainer(Packets.Server.ENTITY_HEAD_ROTATION);
            StructureModifier<Object> mods = spawnPackets[1].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (byte) (int) Math.floor(loc.getYaw() * 256.0F / 360.0F));
        }
        return spawnPackets;
    }

    /**
     * Create a new datawatcher but with the 'correct' values
     */
    private static DataWatcher createDataWatcher(DataWatcher watcher, FlagWatcher flagWatcher) {
        DataWatcher newWatcher = new DataWatcher();
        try {
            Field map = newWatcher.getClass().getDeclaredField("c");
            map.setAccessible(true);
            HashMap c = (HashMap) map.get(newWatcher);
            // Calling c() gets the watchable objects exactly as they are.
            List<WatchableObject> list = watcher.c();
            for (WatchableObject watchableObject : flagWatcher.convert(list)) {
                c.put(watchableObject.a(), watchableObject);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newWatcher;

    }

    /**
     * Add the yaw for the disguises
     */
    public static byte getYaw(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        switch (disguiseType) {
        case ENDER_DRAGON:
        case WITHER_SKULL:
            value -= 128;
            break;
        case ITEM_FRAME:
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
        case ENDER_DRAGON:
        case WITHER_SKULL:
            value += 128;
            break;
        case ITEM_FRAME:
        case ARROW:
            value = (byte) -value;
            break;
        case PAINTING:
            value = (byte) -(value - 128);
            break;
        default:
            if (entityType.isMisc()) {
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
        case ARROW:
        case BOAT:
        case EGG:
        case ENDER_PEARL:
        case ENDER_SIGNAL:
        case FIREWORK:
        case MINECART:
        case MINECART_CHEST:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
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
    protected static void init(LibsDisguises plugin) {
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
                    for (Entity entity : soundLoc.getChunk().getEntities()) {
                        if (DisguiseAPI.isDisguised(entity)) {
                            Location loc = entity.getLocation();
                            loc = new Location(observer.getWorld(), ((int) (loc.getX() * 8)) / 8D, ((int) (loc.getY() * 8)) / 8D,
                                    ((int) (loc.getZ() * 8)) / 8D);
                            if (loc.equals(soundLoc)) {
                                entitySound = DisguiseSound.getType(entity.getType().name());
                                if (entitySound != null) {
                                    if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() == 0) {
                                        soundType = SoundType.DEATH;
                                    } else {
                                        boolean hasInvun = false;
                                        if (entity instanceof LivingEntity) {
                                            net.minecraft.server.v1_6_R3.EntityLiving e = ((CraftLivingEntity) entity)
                                                    .getHandle();
                                            hasInvun = (e.noDamageTicks == e.maxNoDamageTicks);
                                        } else {
                                            net.minecraft.server.v1_6_R3.Entity e = ((CraftEntity) entity).getHandle();
                                            hasInvun = e.isInvulnerable();
                                        }
                                        soundType = entitySound.getType(soundName, !hasInvun);
                                    }
                                    if (soundType != null) {
                                        disguisedEntity = entity;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    Disguise disguise = DisguiseAPI.getDisguise(disguisedEntity);
                    if (disguise != null) {
                        if (disguise.canHearSelfDisguise() || disguisedEntity != event.getPlayer()) {
                            if (disguise.replaceSounds()) {
                                String sound = null;
                                DisguiseSound dSound = DisguiseSound.getType(disguise.getType().name());
                                if (dSound != null && soundType != null)
                                    sound = dSound.getSound(soundType);
                                if (sound == null) {
                                    event.setCancelled(true);
                                } else {
                                    if (sound.equals("step.grass")) {
                                        World world = ((CraftEntity) disguisedEntity).getHandle().world;
                                        Block b = Block.byId[world.getTypeId(soundLoc.getBlockX(), soundLoc.getBlockY() - 1,
                                                soundLoc.getBlockZ())];
                                        if (b != null)
                                            mods.write(0, b.stepSound.getStepSound());
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
                                                boolean baby = ((CraftLivingEntity) disguisedEntity).getHandle().isBaby();
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
                    if ((Byte) mods.read(1) == 2) {
                        // It made a damage animation
                        Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);
                        Disguise disguise = DisguiseAPI.getDisguise(entity);
                        if (disguise != null && (disguise.canHearSelfDisguise() || entity != event.getPlayer())) {
                            DisguiseSound disSound = DisguiseSound.getType(entity.getType().name());
                            if (disSound == null)
                                return;
                            SoundType soundType = null;
                            if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() == 0) {
                                soundType = SoundType.DEATH;
                            } else {
                                soundType = SoundType.HURT;
                            }
                            if (disSound.getSound(soundType) == null
                                    || (disguise.canHearSelfDisguise() && entity == event.getPlayer())) {
                                if (disguise.canHearSelfDisguise() && entity == event.getPlayer()) {
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
                Packets.Server.ENTITY_STATUS, Packets.Server.ENTITY_VELOCITY, Packets.Server.UPDATE_ATTRIBUTES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(event.getPlayer().getWorld());
                org.bukkit.entity.Entity entity = entityModifer.read(0);
                if (entity == event.getPlayer()) {
                    int fakeId = DisguiseAPI.getFakeDisguise(entity.getEntityId());
                    if (fakeId > 0) {
                        // Here I grab the packets to convert them to, So I can display them as if the disguise sent them.
                        PacketContainer[] packets = transformPacket(event.getPacket(), event.getPlayer());
                        try {
                            for (PacketContainer packet : packets) {
                                if (packet.equals(event.getPacket()))
                                    packet = packet.deepClone();
                                packet.getModifier().write(0, fakeId);
                                ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (event.getPacketID() == Packets.Server.ENTITY_METADATA) {
                            event.setPacket(event.getPacket().deepClone());
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            Iterator<WatchableObject> itel = ((List<WatchableObject>) mods.read(1)).iterator();
                            while (itel.hasNext()) {
                                WatchableObject watch = itel.next();
                                if (watch.a() == 0) {
                                    byte b = (Byte) watch.b();
                                    byte a = (byte) (b | 1 << 5);
                                    if ((b & 1 << 3) != 0)
                                        a = (byte) (a | 1 << 3);
                                    watch.a(a);
                                }
                            }
                        } else {
                            switch (event.getPacketID()) {
                            case Packets.Server.NAMED_ENTITY_SPAWN:
                            case Packets.Server.ATTACH_ENTITY:
                            case Packets.Server.REL_ENTITY_MOVE:
                            case Packets.Server.REL_ENTITY_MOVE_LOOK:
                            case Packets.Server.ENTITY_LOOK:
                            case Packets.Server.ENTITY_TELEPORT:
                            case Packets.Server.ENTITY_HEAD_ROTATION:
                            case Packets.Server.MOB_EFFECT:
                            case Packets.Server.ENTITY_EQUIPMENT:
                                if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                                    PacketContainer packet = new PacketContainer(Packets.Server.ENTITY_METADATA);
                                    StructureModifier<Object> mods = packet.getModifier();
                                    mods.write(0, entity.getEntityId());
                                    List watchableList = new ArrayList();
                                    byte b = (byte) (0 | 1 << 5);
                                    if (event.getPlayer().isSprinting())
                                        b = (byte) (b | 1 << 3);
                                    watchableList.add(new WatchableObject(0, 0, b));
                                    mods.write(1, watchableList);
                                    try {
                                        ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet, false);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                event.setCancelled(true);
                                break;

                            case Packets.Server.ENTITY_STATUS:
                                if (DisguiseAPI.getDisguise(entity).canHearSelfDisguise()
                                        && (Byte) event.getPacket().getModifier().read(1) == 2) {
                                    event.setCancelled(true);
                                }
                                break;
                            default:
                                break;
                            }
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
                if (event.getPlayer().getVehicle() == null && event.getPacket().getIntegers().read(0) == 0) {
                    Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer());
                    // If the player is disguised, views self disguises and is hiding a item.
                    if (disguise != null && disguise.viewSelfDisguise()
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
                                                .write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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
                                            event.getPacket().getModifier()
                                                    .write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            ItemStack[] items = (ItemStack[]) mods.read(1);
                            for (int slot = 0; slot < items.length; slot++) {
                                if (slot >= 5 && slot <= 8) {
                                    if (disguise.isHidingArmorFromSelf()) {
                                        // Get the bukkit armor slot!
                                        int armorSlot = Math.abs((slot - 5) - 3);
                                        org.bukkit.inventory.ItemStack item = event.getPlayer().getInventory().getArmorContents()[armorSlot];
                                        if (item != null && item.getType() != Material.AIR) {
                                            items[slot] = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0));
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
                                                items[slot] = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0));
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
        inventoryListenerClient = new PacketAdapter(libsDisguises, ConnectionSide.CLIENT_SIDE, ListenerPriority.HIGHEST,
                Packets.Client.BLOCK_ITEM_SWITCH, Packets.Client.SET_CREATIVE_SLOT, Packets.Client.WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(final PacketEvent event) {
                if (event.getPlayer().getVehicle() == null) {
                    Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer());
                    // If player is disguised, views self disguises and has a inventory modifier
                    if (disguise != null && disguise.viewSelfDisguise()
                            && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
                        switch (event.getPacketID()) {
                        // If they are in creative and clicked on a slot
                        case Packets.Client.SET_CREATIVE_SLOT: {
                            int slot = event.getPacket().getIntegers().read(0);
                            if (slot >= 5 && slot <= 8) {
                                if (disguise.isHidingArmorFromSelf()) {
                                    int armorSlot = Math.abs(slot - 9);
                                    org.bukkit.inventory.ItemStack item = event.getPlayer().getInventory().getArmorContents()[armorSlot];
                                    if (item != null && item.getType() != Material.AIR) {
                                        PacketContainer packet = new PacketContainer(Packets.Server.SET_SLOT);
                                        StructureModifier<Object> mods = packet.getModifier();
                                        mods.write(0, 0);
                                        mods.write(1, slot);
                                        mods.write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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
                                            mods.write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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
                                    mods.write(2, CraftItemStack.asNMSCopy(currentlyHeld));
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
                                    mods.write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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
                                        mods.write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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
                                            mods.write(2, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(0)));
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

    /**
     * Sends the self disguise to the player
     */
    public static void sendSelfDisguise(final Player player) {
        EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();
        EntityTrackerEntry tracker = (EntityTrackerEntry) ((WorldServer) entityplayer.world).tracker.trackedEntities.get(player
                .getEntityId());
        if (tracker == null) {
            // A check incase the tracker is null.
            // If it is, then this method will be run again in one tick. Which is when it should be constructed.
            // Else its going to run in a infinite loop hue hue hue..
            Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable() {
                public void run() {
                    sendSelfDisguise(player);
                }
            });
            return;
        }
        // Add himself to his own entity tracker
        tracker.trackedPlayers.add(entityplayer);
        // Send the player a packet with himself being spawned
        Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn((EntityHuman) entityplayer);
        entityplayer.playerConnection.sendPacket(packet);
        if (!tracker.tracker.getDataWatcher().d()) {
            entityplayer.playerConnection.sendPacket(new Packet40EntityMetadata(player.getEntityId(), tracker.tracker
                    .getDataWatcher(), true));
        }
        // Send himself some entity attributes
        if (tracker.tracker instanceof EntityLiving) {
            AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) tracker.tracker).aX();
            Collection collection = attributemapserver.c();

            if (!collection.isEmpty()) {
                entityplayer.playerConnection.sendPacket(new Packet44UpdateAttributes(player.getEntityId(), collection));
            }
        }

        // Why do we even have this?
        tracker.j = tracker.tracker.motX;
        tracker.k = tracker.tracker.motY;
        tracker.l = tracker.tracker.motZ;
        boolean isMoving = false;
        try {
            Field field = EntityTrackerEntry.class.getDeclaredField("isMoving");
            field.setAccessible(true);
            isMoving = field.getBoolean(tracker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Send the velocity packets
        if (isMoving) {
            entityplayer.playerConnection.sendPacket(new Packet28EntityVelocity(player.getEntityId(), tracker.tracker.motX,
                    tracker.tracker.motY, tracker.tracker.motZ));
        }

        // Why the hell would he even need this. Meh.
        if (tracker.tracker.vehicle != null && player.getEntityId() > tracker.tracker.vehicle.id) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(0, tracker.tracker, tracker.tracker.vehicle));
        } else if (tracker.tracker.passenger != null && player.getEntityId() > tracker.tracker.passenger.id) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(0, tracker.tracker.passenger, tracker.tracker));
        }

        if (tracker.tracker instanceof EntityInsentient && ((EntityInsentient) tracker.tracker).getLeashHolder() != null) {
            entityplayer.playerConnection.sendPacket(new Packet39AttachEntity(1, tracker.tracker,
                    ((EntityInsentient) tracker.tracker).getLeashHolder()));
        }

        // Resend the armor
        for (int i = 0; i < 5; ++i) {
            ItemStack itemstack = ((EntityLiving) tracker.tracker).getEquipment(i);

            if (itemstack != null) {
                entityplayer.playerConnection.sendPacket(new Packet5EntityEquipment(player.getEntityId(), i, itemstack));
            }
        }
        // If the disguised is sleeping for w/e reason
        if (entityplayer.isSleeping()) {
            entityplayer.playerConnection
                    .sendPacket(new Packet17EntityLocationAction(entityplayer, 0, (int) Math.floor(tracker.tracker.locX),
                            (int) Math.floor(tracker.tracker.locY), (int) Math.floor(tracker.tracker.locZ)));
        }

        // CraftBukkit start - Fix for nonsensical head yaw
        tracker.i = (int) Math.floor(tracker.tracker.getHeadRotation() * 256.0F / 360.0F); // tracker.ao() should be
        // getHeadRotation
        tracker.broadcast(new Packet35EntityHeadRotation(player.getEntityId(), (byte) tracker.i));
        // CraftBukkit end

        // Resend any active potion effects
        Iterator iterator = entityplayer.getEffects().iterator();
        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            entityplayer.playerConnection.sendPacket(new Packet41MobEffect(player.getEntityId(), mobeffect));
        }
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
                Disguise disguise = DisguiseAPI.getDisguise(player);
                if (disguise != null) {
                    if (viewDisguisesListenerEnabled && disguise.viewSelfDisguise()
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
                Disguise disguise = DisguiseAPI.getDisguise(player);
                if (disguise != null) {
                    if (disguise.viewSelfDisguise()) {
                        if (enabled) {
                            disguiseAPI.setupFakeDisguise(disguise);
                        } else {
                            disguiseAPI.removeVisibleDisguise(player);
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
            Disguise disguise = DisguiseAPI.getDisguise(entity);
            // If disguised.
            if (disguise != null) {
                // If packet is Packets.Server.UPDATE_ATTRIBUTES
                // This packet sends attributes

                switch (sentPacket.getID()) {

                case Packets.Server.UPDATE_ATTRIBUTES:

                {
                    // Grab the values which are 'approved' to be sent for this entity
                    HashMap<String, Double> values = Values.getAttributesValues(disguise.getType());
                    Collection collection = new ArrayList<AttributeSnapshot>();
                    for (AttributeSnapshot att : (List<AttributeSnapshot>) sentPacket.getModifier().read(1)) {
                        if (values.containsKey(att.a())) {
                            collection.add(new AttributeSnapshot(null, att.a(), values.get(att.a()), att.c()));
                        }
                    }
                    if (collection.size() > 0) {
                        packets[0] = new PacketContainer(sentPacket.getID());
                        StructureModifier<Object> mods = packets[0].getModifier();
                        mods.write(0, entity.getEntityId());
                        mods.write(1, collection);
                    } else {
                        packets = new PacketContainer[0];
                    }
                    break;
                }

                // Else if the packet is sending entity metadata
                case Packets.Server.ENTITY_METADATA:

                {
                    List<WatchableObject> watchableObjects = disguise.getWatcher().convert(
                            (List<WatchableObject>) packets[0].getModifier().read(1));
                    packets[0] = new PacketContainer(sentPacket.getID());
                    StructureModifier<Object> newMods = packets[0].getModifier();
                    newMods.write(0, entity.getEntityId());
                    newMods.write(1, watchableObjects);
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
                    if (disguise.getType().isMisc())
                        packets = new PacketContainer[0];
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
                        byte value = (Byte) mods.read(4);
                        mods.write(4, getYaw(disguise.getType(), DisguiseType.getType(entity.getType()), value));
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
                                (itemstack.getTypeId() == 0 ? null : CraftItemStack.asNMSCopy(itemstack)));
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
