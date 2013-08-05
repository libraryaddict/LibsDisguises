package me.libraryaddict.disguise;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.libraryaddict.disguise.Commands.*;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;
import me.libraryaddict.disguise.DisguiseTypes.MiscDisguise;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import me.libraryaddict.disguise.DisguiseTypes.Values;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.LivingWatcher;
import net.minecraft.server.v1_6_R2.AttributeSnapshot;
import net.minecraft.server.v1_6_R2.ChatMessage;
import net.minecraft.server.v1_6_R2.ChunkCoordinates;
import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EnumArt;
import net.minecraft.server.v1_6_R2.EnumEntitySize;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.MathHelper;
import net.minecraft.server.v1_6_R2.WatchableObject;
import net.minecraft.server.v1_6_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.Ageable;
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
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin {
    private class DisguiseHuman extends EntityHuman {

        public DisguiseHuman(World world) {
            super(world, "LibsDisguises");
        }

        public boolean a(int arg0, String arg1) {
            return false;
        }

        public ChunkCoordinates b() {
            return null;
        }

        public void sendMessage(ChatMessage arg0) {
        }

    }

    private void addPacketListeners() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ENTITY_METADATA, Packets.Server.ARM_ANIMATION,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ADD_EXP_ORB, Packets.Server.VEHICLE_SPAWN, Packets.Server.MOB_SPAWN,
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT, Packets.Server.UPDATE_ATTRIBUTES,
                Packets.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player observer = event.getPlayer();
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
                    for (int i = 1; i < packets.length; i++) {
                        sendDelayedPacket(packets[i], event.getPlayer());
                    }
                }
            }
        });
        // Now add a client listener to cancel them interacting with uninteractable disguised entitys.
        // You ain't supposed to be allowed to 'interact' with a item that cannot be clicked.
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL,
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

    protected PacketContainer[] constructPacket(Disguise disguise, Entity disguisedEntity) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        net.minecraft.server.v1_6_R2.Entity nmsEntity = ((CraftEntity) disguisedEntity).getHandle();
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
        Location loc = disguisedEntity.getLocation();
        byte yaw = getYaw(disguise.getType(), DisguiseType.getType(disguise.getEntity().getType()),
                (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
        EnumEntitySize entitySize = Values.getValues(disguise.getType()).getEntitySize();

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {

            spawnPackets[0] = manager.createPacket(Packets.Server.ADD_EXP_ORB);
            StructureModifier<Object> mods = spawnPackets[0].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) Math.floor(loc.getX() * 32));
            mods.write(2, (int) Math.floor(loc.getY() * 32) + 2);
            mods.write(3, (int) Math.floor(loc.getZ() * 32));
            mods.write(4, 1);

        } else if (disguise.getType() == DisguiseType.PAINTING) {
            spawnPackets[0] = manager.createPacket(Packets.Server.ENTITY_PAINTING);
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
            spawnPackets[1] = manager.createPacket(Packets.Server.ENTITY_TELEPORT);
            mods = spawnPackets[1].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (int) Math.floor(entitySize.a(loc.getX()) * 32D));
            mods.write(2, (int) Math.floor(loc.getY() * 32D));
            mods.write(3, (int) Math.floor(entitySize.a(loc.getZ()) * 32D));
            mods.write(4, yaw);
            mods.write(5, (byte) (int) (loc.getPitch() * 256.0F / 360.0F));

        } else if (disguise.getType().isPlayer()) {

            spawnPackets[0] = manager.createPacket(Packets.Server.NAMED_ENTITY_SPAWN);
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
            mods.write(8, convertDataWatcher(nmsEntity.getDataWatcher(), disguise.getWatcher()));

        } else if (disguise.getType().isMob()) {

            Vector vec = disguisedEntity.getVelocity();
            spawnPackets[0] = manager.createPacket(Packets.Server.MOB_SPAWN);
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
            mods.write(11, convertDataWatcher(nmsEntity.getDataWatcher(), disguise.getWatcher()));
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
            spawnPackets[0] = manager.createPacket(Packets.Server.VEHICLE_SPAWN);
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
            spawnPackets[1] = manager.createPacket(Packets.Server.ENTITY_HEAD_ROTATION);
            StructureModifier<Object> mods = spawnPackets[1].getModifier();
            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, (byte) (int) Math.floor(loc.getYaw() * 256.0F / 360.0F));
        }
        return spawnPackets;
    }

    private DataWatcher convertDataWatcher(DataWatcher watcher, FlagWatcher flagWatcher) {
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

    private byte getYaw(DisguiseType disguiseType, DisguiseType entityType, byte value) {
        switch (disguiseType) {
        case ENDER_DRAGON:
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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        if (!config.contains("DisguiseRadiusMax"))
            config.set("DisguiseRadiusMax", getConfig().getInt("DisguiseRadiusMax"));
        if (!config.contains("UndisguiseRadiusMax"))
            config.set("UndisguiseRadiusMax", getConfig().getInt("UndisguiseRadiusMax"));
        if (!config.contains("DisguiseSounds"))
            config.set("DisguiseSounds", getConfig().getBoolean("DisguiseSounds"));
        if (!config.contains("HearSelfDisguise"))
            config.set("HearSelfDisguise", getConfig().getBoolean("HearSelfDisguise"));
        if (!config.contains("SendVelocity"))
            config.set("SendVelocity", getConfig().getBoolean("SendVelocity"));
        DisguiseAPI.init(this);
        DisguiseAPI.enableSounds(getConfig().getBoolean("DisguiseSounds"));
        DisguiseAPI.setVelocitySent(getConfig().getBoolean("SendVelocity"));
        DisguiseAPI.setViewDisguises(getConfig().getBoolean("ViewDisguises"));
        DisguiseAPI.setHearSelfDisguise(getConfig().getBoolean("HearSelfDisguise"));
        try {
            // Here I use reflection to set the plugin for Disguise..
            // Kinda stupid but I don't want open API calls.
            Field field = Disguise.class.getDeclaredField("plugin");
            field.setAccessible(true);
            field.set(null, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        addPacketListeners();
        DisguiseListener listener = new DisguiseListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("undisguise").setExecutor(new UndisguiseCommand());
        getCommand("disguiseplayer").setExecutor(new DisguisePlayerCommand());
        getCommand("undisguiseplayer").setExecutor(new UndisguisePlayerCommand());
        getCommand("undisguiseentity").setExecutor(new UndisguiseEntityCommand(listener));
        getCommand("disguiseentity").setExecutor(new DisguiseEntityCommand(listener));
        getCommand("disguiseradius").setExecutor(new DisguiseRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
        getCommand("undisguiseradius").setExecutor(new UndisguiseRadiusCommand(getConfig().getInt("UndisguiseRadiusMax")));
        registerValues();
    }

    private void registerValues() {
        World world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        for (DisguiseType disguiseType : DisguiseType.values()) {
            Class watcherClass = null;
            try {
                String name;
                switch (disguiseType) {
                case MINECART_FURNACE:
                case MINECART_HOPPER:
                case MINECART_MOB_SPAWNER:
                case MINECART_TNT:
                case MINECART_CHEST:
                    name = "Minecart";
                    break;
                case DONKEY:
                case MULE:
                case UNDEAD_HORSE:
                case SKELETON_HORSE:
                    name = "Horse";
                    break;
                case ZOMBIE_VILLAGER:
                case PIG_ZOMBIE:
                    name = "Zombie";
                    break;
                case MAGMA_CUBE:
                    name = "Slime";
                    break;
                default:
                    name = toReadable(disguiseType.name());
                    break;
                }
                watcherClass = Class.forName("me.libraryaddict.disguise.DisguiseTypes.Watchers." + name + "Watcher");
            } catch (Exception ex) {
                // There is no watcher for this entity, or a error was thrown.
                try {
                    Class c = disguiseType.getEntityType().getEntityClass();
                    if (Ageable.class.isAssignableFrom(c))
                        watcherClass = AgeableWatcher.class;
                    else if (LivingEntity.class.isAssignableFrom(c))
                        watcherClass = LivingWatcher.class;
                    else
                        watcherClass = FlagWatcher.class;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            disguiseType.setWatcherClass(watcherClass);
            String name = toReadable(disguiseType.name());
            boolean dontDo = false;
            switch (disguiseType) {
            case WITHER_SKELETON:
            case ZOMBIE_VILLAGER:
            case DONKEY:
            case MULE:
            case UNDEAD_HORSE:
            case SKELETON_HORSE:
                dontDo = true;
                break;
            case PRIMED_TNT:
                name = "TNTPrimed";
                break;
            case MINECART_TNT:
                name = "MinecartTNT";
                break;
            case MINECART:
                name = "MinecartRideable";
                break;
            case FIREWORK:
                name = "Fireworks";
                break;
            case SPLASH_POTION:
                name = "Potion";
                break;
            case GIANT:
                name = "GiantZombie";
                break;
            case DROPPED_ITEM:
                name = "Item";
                break;
            case FIREBALL:
                name = "LargeFireball";
                break;
            default:
                break;
            }
            if (dontDo)
                continue;
            try {
                net.minecraft.server.v1_6_R2.Entity entity = null;
                Class entityClass;
                if (disguiseType == DisguiseType.PLAYER) {
                    entityClass = EntityHuman.class;
                    entity = new DisguiseHuman(world);
                } else {
                    entityClass = Class.forName("net.minecraft.server.v1_6_R2.Entity" + name);
                    entity = (net.minecraft.server.v1_6_R2.Entity) entityClass.getConstructor(World.class).newInstance(world);
                }
                Values value = new Values(disguiseType, entityClass, entity.at);
                List<WatchableObject> watchers = entity.getDataWatcher().c();
                for (WatchableObject watch : watchers)
                    value.setMetaValue(watch.a(), watch.b());
                if (entity instanceof EntityLiving) {
                    EntityLiving livingEntity = (EntityLiving) entity;
                    value.setAttributesValue(GenericAttributes.d.a(), livingEntity.getAttributeInstance(GenericAttributes.d)
                            .getValue());
                }
                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());
                if (sound != null) {
                    Method soundStrength = EntityLiving.class.getDeclaredMethod("aZ");
                    soundStrength.setAccessible(true);
                    sound.setDamageSoundVolume((Float) soundStrength.invoke(entity));
                }
            } catch (Exception e1) {
                System.out.print("[LibsDisguises] Trouble while making values for " + name + ": " + e1.getMessage());
                System.out.print("[LibsDisguises] Please report this to LibsDisguises author");
                e1.printStackTrace();
            }
        }
    }

    private void sendDelayedPacket(final PacketContainer packet, final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String toReadable(String string) {
        StringBuilder builder = new StringBuilder();
        for (String s : string.split("_")) {
            builder.append(s.substring(0, 1) + s.substring(1).toLowerCase());
        }
        return builder.toString();
    }

    protected PacketContainer[] transformPacket(PacketContainer sentPacket, Player observer) {
        PacketContainer[] packets = new PacketContainer[] { sentPacket };
        try {
            // First get the entity, the one sending this packet
            StructureModifier<Entity> entityModifer = sentPacket.getEntityModifier(observer.getWorld());
            org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == sentPacket.getID() ? 1 : 0));
            Disguise disguise = DisguiseAPI.getDisguise(entity);
            // If disguised.
            if (disguise != null) {
                // If packet is Packets.Server.UPDATE_ATTRIBUTES - For some reason maven doesn't let me..
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
                    packets = constructPacket(disguise, entity);
                    break;
                }

                // Else if the disguise is attempting to send players a forbidden packet
                case Packets.Server.ARM_ANIMATION:
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
                    packets[0] = sentPacket.shallowClone();
                    StructureModifier<Object> mods = packets[0].getModifier();
                    byte value = (Byte) mods.read(4);
                    mods.write(4, getYaw(disguise.getType(), DisguiseType.getType(entity.getType()), value));
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