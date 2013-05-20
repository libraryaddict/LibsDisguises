package me.libraryaddict.disguise;

import java.util.List;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import net.minecraft.server.v1_5_R3.WatchableObject;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("disguise").setExecutor(new DisguiseCommand());
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.NAMED_ENTITY_SPAWN,
                        Packets.Server.ENTITY_METADATA) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        StructureModifier<Object> mods = event.getPacket().getModifier();
                        try {
                            Player observer = event.getPlayer();
                            org.bukkit.entity.Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);
                            if (entity instanceof Player) {
                                Player watched = (Player) entity;
                                if (DisguiseAPI.isDisguised(watched.getName())) {
                                    Disguise disguise = DisguiseAPI.getDisguise(watched);
                                    if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                                        if (disguise.getType().isPlayer()) {
                                            String name = (String) mods.read(1);
                                            if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                                event.setCancelled(true);
                                                DisguiseAPI.disguiseToPlayer(watched, observer, disguise);
                                            }
                                        } else {
                                            event.setCancelled(true);
                                            DisguiseAPI.disguiseToPlayer(watched, observer, disguise);
                                        }
                                    } else if (!disguise.getType().isPlayer()) {
                                        if (disguise.hasWatcher()) {
                                            mods.write(1, disguise.getWatcher().convert((List<WatchableObject>) mods.read(1)));
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}