package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class PacketListenerViewSelfDisguise extends PacketAdapter {
    public PacketListenerViewSelfDisguise(LibsDisguises plugin) {
        super(plugin, ListenerPriority.HIGH, Server.NAMED_ENTITY_SPAWN, Server.ATTACH_ENTITY, Server.REL_ENTITY_MOVE,
                Server.REL_ENTITY_MOVE_LOOK, Server.ENTITY_LOOK, Server.ENTITY_TELEPORT, Server.ENTITY_HEAD_ROTATION,
                Server.ENTITY_METADATA, Server.ENTITY_EQUIPMENT, Server.ANIMATION, Server.ENTITY_EFFECT,
                Server.ENTITY_VELOCITY, Server.UPDATE_ATTRIBUTES, Server.ENTITY_STATUS);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (event.isCancelled())
            return;

        try {
            final Player observer = event.getPlayer();

            if (observer.getName().contains("UNKNOWN[")) {// If the player is temporary
                return;
            }

            PacketContainer packet = event.getPacket();

            // If packet isn't meant for the disguised player's self disguise
            if (packet.getIntegers().read(0) != observer.getEntityId()) {
                return;
            }

            if (!DisguiseAPI.isSelfDisguised(observer)) {
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                    Disguise disguise = DisguiseAPI.getDisguise(observer, observer);

                    if (disguise != null && disguise.isSelfDisguiseVisible()) {
                        event.setCancelled(true);
                    }
                }

                return;
            }

            final Disguise disguise = DisguiseAPI.getDisguise(observer, observer);

            if (disguise == null) {
                return;
            }

            // Here I grab the packets to convert them to, So I can display them as if the disguise sent them.
            LibsPackets transformed = PacketsManager.getPacketsHandler()
                    .transformPacket(packet, disguise, observer, observer);

            if (transformed.isUnhandled()) {
                transformed.getPackets().add(packet);
            }

            transformed.setSpawnPacketCheck(event.getPacketType());

            for (PacketContainer newPacket : transformed.getPackets()) {
                if (newPacket.getType() != Server.PLAYER_INFO) {
                    if (newPacket == packet) {
                        newPacket = newPacket.shallowClone();
                    }

                    newPacket.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                }

                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, newPacket, false);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            for (ArrayList<PacketContainer> packets : transformed.getDelayedPackets()) {
                for (PacketContainer newPacket : packets) {
                    if (newPacket.getType() == Server.PLAYER_INFO) {
                        continue;
                    }

                    if (newPacket.equals(packet)) {
                        newPacket = newPacket.shallowClone();
                    }

                    newPacket.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                }
            }

            transformed.sendDelayed(observer);

            if (event.getPacketType() == Server.ENTITY_METADATA) {
                if (!LibsPremium.getPluginInformation().isPremium() || LibsPremium.getPaidInformation() != null ||
                        LibsPremium.getPluginInformation().getBuildNumber().matches("#[0-9]+")) {
                    event.setPacket(packet = packet.deepClone());
                }

                for (WrappedWatchableObject watch : packet.getWatchableCollectionModifier().read(0)) {
                    if (watch.getIndex() == 0) {
                        byte b = (byte) watch.getValue();

                        byte a = (byte) (b | 1 << 5);

                        if ((b & 1 << 3) != 0)
                            a = (byte) (a | 1 << 3);

                        watch.setValue(a);
                    }
                }
            } else if (event.getPacketType() == Server.NAMED_ENTITY_SPAWN) {
                event.setCancelled(true);

                PacketContainer metaPacket = new PacketContainer(Server.ENTITY_METADATA);

                StructureModifier<Object> mods = metaPacket.getModifier();

                mods.write(0, observer.getEntityId());

                List<WrappedWatchableObject> watchableList = new ArrayList<>();
                byte b = 1 << 5;

                if (observer.isSprinting())
                    b = (byte) (b | 1 << 3);

                WrappedWatchableObject watch = ReflectionManager.createWatchable(MetaIndex.ENTITY_META, b);

                if (watch != null)
                    watchableList.add(watch);

                metaPacket.getWatchableCollectionModifier().write(0, watchableList);

                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, metaPacket);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (event.getPacketType() == Server.ANIMATION) {
                if (packet.getIntegers().read(1) != 2) {
                    event.setCancelled(true);
                }
            } else if (event.getPacketType() == Server.ATTACH_ENTITY ||
                    event.getPacketType() == Server.REL_ENTITY_MOVE ||
                    event.getPacketType() == Server.REL_ENTITY_MOVE_LOOK ||
                    event.getPacketType() == Server.ENTITY_LOOK || event.getPacketType() == Server.ENTITY_TELEPORT ||
                    event.getPacketType() == Server.ENTITY_HEAD_ROTATION ||
                    event.getPacketType() == Server.ENTITY_EQUIPMENT) {
                event.setCancelled(true);
            } else if (event.getPacketType() == Server.ENTITY_STATUS) {
                if (disguise.isSelfDisguiseSoundsReplaced() && !disguise.getType().isPlayer() &&
                        packet.getBytes().read(0) == 2) {
                    event.setCancelled(true);
                }
            } else if (event.getPacketType() == Server.ENTITY_VELOCITY &&
                    !DisguiseUtilities.isPlayerVelocity(observer)) {
                // The player only sees velocity changes when there is a velocity event. As the method claims there
                // was no velocity event...
                event.setCancelled(true);
                // Clear old velocity, this should only occur once.
                DisguiseUtilities.setPlayerVelocity(null);
            }
        }
        catch (Exception ex) {
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }
}
