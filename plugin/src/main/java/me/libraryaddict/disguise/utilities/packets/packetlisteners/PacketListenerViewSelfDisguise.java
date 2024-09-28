package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketListenerViewSelfDisguise extends SimplePacketListenerAbstract {
    private final boolean[] listenedPackets = new boolean[Server.values().length];

    public PacketListenerViewSelfDisguise() {
        for (Server packet : new Server[]{NmsVersion.v1_20_R2.isSupported() ? Server.SPAWN_ENTITY : Server.SPAWN_PLAYER,
            Server.ATTACH_ENTITY, Server.ENTITY_RELATIVE_MOVE_AND_ROTATION, Server.ENTITY_RELATIVE_MOVE, Server.ENTITY_HEAD_LOOK,
            Server.ENTITY_ROTATION, Server.ENTITY_TELEPORT, Server.ENTITY_MOVEMENT, Server.ENTITY_METADATA, Server.ENTITY_EQUIPMENT,
            Server.ENTITY_ANIMATION, Server.ENTITY_EFFECT, Server.ENTITY_VELOCITY, Server.UPDATE_ATTRIBUTES, Server.ENTITY_STATUS}) {
            listenedPackets[packet.ordinal()] = true;
        }
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!listenedPackets[event.getPacketType().ordinal()]) {
            return;
        }

        try {
            final Player observer = event.getPlayer();

            if (observer == null) {
                return;
            }

            PacketWrapper wrapper = DisguiseUtilities.constructWrapper(event);

            int entityId = DisguiseUtilities.getEntityId(wrapper);

            // If packet isn't meant for the disguised player's self disguise
            if (entityId != observer.getEntityId() && entityId != DisguiseAPI.getSelfDisguiseId()) {
                return;
            }

            if (!DisguiseAPI.isSelfDisguised(observer)) {
                return;
            }

            final Disguise disguise = DisguiseAPI.getDisguise(observer, observer);

            if (disguise == null) {
                return;
            }

            // Here I grab the packets to convert them to, So I can display them as if the disguise sent them.
            LibsPackets<?> transformed = PacketsManager.getPacketsHandler().transformPacket(wrapper, disguise, observer, observer);

            if (transformed.isUnhandled()) {
                transformed.addPacket(DisguiseUtilities.unsafeClone(event, wrapper));
            }

            LibsPackets<?> selfTransformed = new LibsPackets(wrapper, disguise);
            selfTransformed.setSkinHandling(transformed.isSkinHandling());

            for (PacketWrapper newPacket : transformed.getPackets()) {
                if (newPacket == selfTransformed.getOriginalPacket()) {
                    newPacket = DisguiseUtilities.unsafeClone(event, newPacket);
                }

                if (newPacket.getPacketTypeData().getPacketType() != Server.PLAYER_INFO &&
                    newPacket.getPacketTypeData().getPacketType() != Server.PLAYER_INFO_UPDATE &&
                    newPacket.getPacketTypeData().getPacketType() != Server.DESTROY_ENTITIES &&
                    DisguiseUtilities.getEntityId(newPacket) == observer.getEntityId()) {
                    // No need to check if this is self disguise ID since we're only looking for unmapped
                    DisguiseUtilities.writeSelfDisguiseId(observer.getEntityId(), newPacket);
                }

                selfTransformed.addPacket(newPacket);
            }

            for (Map.Entry<Integer, ArrayList<PacketWrapper>> entry : transformed.getDelayedPacketsMap().entrySet()) {
                for (PacketWrapper newPacket : entry.getValue()) {
                    if (newPacket == selfTransformed.getOriginalPacket()) {
                        newPacket = DisguiseUtilities.unsafeClone(event, newPacket);
                    }

                    if (newPacket.getPacketTypeData().getPacketType() != Server.PLAYER_INFO &&
                        newPacket.getPacketTypeData().getPacketType() != Server.PLAYER_INFO_UPDATE &&
                        newPacket.getPacketTypeData().getPacketType() != Server.DESTROY_ENTITIES) {
                        DisguiseUtilities.writeSelfDisguiseId(observer.getEntityId(), newPacket);
                    }

                    selfTransformed.addDelayedPacket(newPacket, entry.getKey());
                }
            }

            if (disguise.isPlayerDisguise()) {
                LibsDisguises.getInstance().getSkinHandler().handlePackets(observer, (PlayerDisguise) disguise, selfTransformed);
            }

            for (PacketWrapper newPacket : selfTransformed.getPackets()) {
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(observer, newPacket);
            }

            selfTransformed.sendDelayed(observer);

            if (event.getPacketType() == Server.ENTITY_METADATA) {
                WrapperPlayServerEntityMetadata metadata = (WrapperPlayServerEntityMetadata) wrapper;

                if (metadata.getEntityId() == observer.getEntityId()) {
               /* if (!LibsPremium.getPluginInformation().isPremium() || LibsPremium.getPaidInformation() != null ||
                    LibsPremium.getPluginInformation().getBuildNumber().matches("#\\d+")) {

                    event.setPacket(packet = packet.deepClone());
                }*/

                    for (EntityData data : metadata.getEntityMetadata()) {
                        if (data.getIndex() != 0) {
                            continue;
                        }
                        byte b = (byte) data.getValue();

                        // Add invisibility, remove glowing
                        byte a = (byte) ((b | 1 << 5) & ~(1 << 6));

                        data.setValue(a);
                    }
                }
            } else if (event.getPacketType() == Server.SPAWN_PLAYER || event.getPacketType() == Server.SPAWN_ENTITY) {
                event.setCancelled(true);

                List<WatcherValue> watchableList = new ArrayList<>();
                byte b = 1 << 5;

                if (observer.isSprinting()) {
                    b = (byte) (b | 1 << 3);
                }

                if (observer.isGliding()) {
                    b = (byte) (b | 1 << 7);
                }

                WatcherValue watch = new WatcherValue(MetaIndex.ENTITY_META, b, true);

                watchableList.add(watch);

                WrapperPlayServerEntityMetadata metaPacket = ReflectionManager.getMetadataPacket(observer.getEntityId(), watchableList);

                PacketEvents.getAPI().getPlayerManager().sendPacket(observer, metaPacket);
            } else if (event.getPacketType() == Server.ENTITY_ANIMATION) {
                if (((WrapperPlayServerEntityAnimation) wrapper).getType() !=
                    WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
                    event.setCancelled(true);
                }
            } else if (event.getPacketType() == Server.ATTACH_ENTITY || event.getPacketType() == Server.ENTITY_RELATIVE_MOVE ||
                event.getPacketType() == Server.ENTITY_RELATIVE_MOVE_AND_ROTATION || event.getPacketType() == Server.ENTITY_HEAD_LOOK ||
                event.getPacketType() == Server.ENTITY_TELEPORT || event.getPacketType() == Server.ENTITY_ROTATION ||
                event.getPacketType() == Server.ENTITY_EQUIPMENT) {
                event.setCancelled(true);
            } else if (event.getPacketType() == Server.ENTITY_STATUS) {
                if (disguise.isSelfDisguiseSoundsReplaced() && !disguise.getType().isPlayer() &&
                    ((WrapperPlayServerEntityStatus) wrapper).getStatus() == 2) {
                    event.setCancelled(true);

                    // As of 1.19.3, no sound is sent but instead the client is expected to play a hurt sound on entity status effect
                    if (NmsVersion.v1_19_R2.isSupported()) {
                        SoundGroup group = SoundGroup.getGroup(disguise);
                        String sound = group.getSound(SoundGroup.SoundType.HURT);

                        if (sound != null) {
                            observer.playSound(observer.getLocation(), sound, 1f, 1f);
                        }
                    }
                }
            } else if (event.getPacketType() == Server.ENTITY_VELOCITY && !DisguiseUtilities.isPlayerVelocity(observer)) {
                // The player only sees velocity changes when there is a velocity event. As the method claims there
                // was no velocity event...
                event.setCancelled(true);
                // Clear old velocity, this should only occur once.
                DisguiseUtilities.clearPlayerVelocity(observer);
            }
        } catch (Throwable ex) {
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }
}
