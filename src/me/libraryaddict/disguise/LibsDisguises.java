package me.libraryaddict.disguise;

import java.util.List;

import me.libraryaddict.disguise.Commands.DisguiseCommand;
import me.libraryaddict.disguise.Commands.DisguisePlayerCommand;
import me.libraryaddict.disguise.Commands.UndisguiseCommand;
import me.libraryaddict.disguise.Commands.UndisguisePlayerCommand;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import net.minecraft.server.v1_5_R3.WatchableObject;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin {

    @Override
    public void onEnable() {
        DisguiseAPI.init(this);
        DisguiseAPI.enableSounds(true);
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ENTITY_METADATA, Packets.Server.ARM_ANIMATION,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ADD_EXP_ORB, Packets.Server.VEHICLE_SPAWN, Packets.Server.MOB_SPAWN,
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                StructureModifier<Object> mods = event.getPacket().getModifier();
                try {
                    Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == event.getPacketID() ? 1 : 0));
                    if (DisguiseAPI.isDisguised(entity)) {
                        Disguise disguise = DisguiseAPI.getDisguise(entity);
                        if (event.getPacketID() == Packets.Server.ENTITY_METADATA) {
                            if (!(entity instanceof Player && disguise.getType().isPlayer()))
                                if (disguise.hasWatcher())
                                    mods.write(1, disguise.getWatcher().convert((List<WatchableObject>) mods.read(1)));
                        } else if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                            if (disguise.getType().isPlayer()) {
                                String name = (String) mods.read(1);
                                if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                    // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                                    event.setPacket(disguise.constructPacket(entity));
                                }
                            } else {
                                // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                                event.setPacket(disguise.constructPacket(entity));
                            }
                        } else if (event.getPacketID() == Packets.Server.MOB_SPAWN
                                || event.getPacketID() == Packets.Server.ADD_EXP_ORB
                                || event.getPacketID() == Packets.Server.VEHICLE_SPAWN
                                || event.getPacketID() == Packets.Server.ENTITY_PAINTING) {
                            // manager.sendServerPacket(observer, disguise.constructDestroyPacket(entity.getEntityId()));
                            event.setPacket(disguise.constructPacket(entity));
                        } else if (event.getPacketID() == Packets.Server.ARM_ANIMATION
                                || event.getPacketID() == Packets.Server.COLLECT) {
                            if (disguise.getType().isMisc()) {
                                event.setCancelled(true);
                            }
                        } else if (Packets.Server.REL_ENTITY_MOVE_LOOK == event.getPacketID()
                                || Packets.Server.ENTITY_LOOK == event.getPacketID()
                                || Packets.Server.ENTITY_TELEPORT == event.getPacketID()) {
                            if (disguise.getType() == DisguiseType.ENDER_DRAGON) {
                                byte value = (Byte) mods.read(4);
                                mods.write(4, (byte) (value - 128));
                            } else if (disguise.getType().isMisc()) {
                                byte value = (Byte) mods.read(4);
                                if (disguise.getType() != DisguiseType.PAINTING)
                                    mods.write(4, (byte) (value + 64));
                                else
                                    mods.write(4, (byte) -(value += 128));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("undisguise").setExecutor(new UndisguiseCommand());
        getCommand("disguiseplayer").setExecutor(new DisguisePlayerCommand());
        getCommand("undisguiseplayer").setExecutor(new UndisguisePlayerCommand());
    }
}