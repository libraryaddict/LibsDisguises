package me.libraryaddict.disguise.utilities.packetlisteners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

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
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.PacketsManager.LibsPackets;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class PacketListenerViewDisguises extends PacketAdapter {
    public PacketListenerViewDisguises(LibsDisguises plugin) {
        super(plugin, ListenerPriority.HIGH, Server.NAMED_ENTITY_SPAWN, Server.ATTACH_ENTITY, Server.REL_ENTITY_MOVE,
                Server.REL_ENTITY_MOVE_LOOK, Server.ENTITY_LOOK, Server.ENTITY_TELEPORT, Server.ENTITY_HEAD_ROTATION,
                Server.ENTITY_METADATA, Server.ENTITY_EQUIPMENT, Server.ANIMATION, Server.BED, Server.ENTITY_EFFECT,
                Server.ENTITY_VELOCITY, Server.UPDATE_ATTRIBUTES, Server.ENTITY_STATUS);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (event.isCancelled())
            return;

        try {
            final Player observer = event.getPlayer();

            if (observer.getName().contains("UNKNOWN[")) // If the player is temporary
                return;

            if (event.getPacket().getIntegers().read(0) != observer.getEntityId()) {
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

            if (disguise == null)
                return;

            // Here I grab the packets to convert them to, So I can display them as if the disguise sent them.
            LibsPackets transformed = PacketsManager.transformPacket(event.getPacket(), disguise, observer, observer);

            if (transformed.isUnhandled()) {
                transformed.getPackets().add(event.getPacket());
            }

            transformed.setPacketType(event.getPacketType());

            for (PacketContainer packet : transformed.getPackets()) {
                if (packet.getType() != Server.PLAYER_INFO) {
                    if (packet.equals(event.getPacket())) {
                        packet = packet.shallowClone();
                    }

                    packet.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                }

                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            for (ArrayList<PacketContainer> packets : transformed.getDelayedPackets()) {
                for (PacketContainer packet : packets) {
                    if (packet.getType() != Server.PLAYER_INFO) {
                        if (packet.equals(event.getPacket())) {
                            packet = packet.shallowClone();
                        }

                        packet.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());
                    }
                }
            }

            transformed.sendDelayed(observer);

            if (event.getPacketType() == Server.ENTITY_METADATA) {
                event.setPacket(event.getPacket().deepClone());

                for (WrappedWatchableObject watch : event.getPacket().getWatchableCollectionModifier().read(0)) {
                    if (watch.getIndex() == 0) {
                        byte b = (byte) watch.getValue();

                        byte a = (byte) (b | 1 << 5);

                        if ((b & 1 << 3) != 0)
                            a = (byte) (a | 1 << 3);

                        watch.setValue(a);
                    }
                }
            }
            else if (event.getPacketType() == Server.NAMED_ENTITY_SPAWN) {
                event.setCancelled(true);

                PacketContainer packet = new PacketContainer(Server.ENTITY_METADATA);

                StructureModifier<Object> mods = packet.getModifier();

                mods.write(0, observer.getEntityId());

                List<WrappedWatchableObject> watchableList = new ArrayList<>();
                Byte b = 1 << 5;

                if (observer.isSprinting())
                    b = (byte) (b | 1 << 3);

                WrappedWatchableObject watch = ReflectionManager.createWatchable(0, b);

                watchableList.add(watch);
                packet.getWatchableCollectionModifier().write(0, watchableList);

                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            else if (event.getPacketType() == Server.ANIMATION) {
                if (event.getPacket().getIntegers().read(1) != 2) {
                    event.setCancelled(true);
                }
            }
            else if (event.getPacketType() == Server.ATTACH_ENTITY || event.getPacketType() == Server.REL_ENTITY_MOVE
                    || event.getPacketType() == Server.REL_ENTITY_MOVE_LOOK || event.getPacketType() == Server.ENTITY_LOOK
                    || event.getPacketType() == Server.ENTITY_TELEPORT || event.getPacketType() == Server.ENTITY_HEAD_ROTATION
                    || event.getPacketType() == Server.ENTITY_EQUIPMENT) {
                event.setCancelled(true);
            }
            else if (event.getPacketType() == Server.ENTITY_STATUS) {
                if (disguise.isSelfDisguiseSoundsReplaced() && !disguise.getType().isPlayer()
                        && event.getPacket().getBytes().read(0) == 2) {
                    event.setCancelled(true);
                }
            }
        }
        catch (Exception ex) {
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }
}
