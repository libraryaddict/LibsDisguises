package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PacketListenerViewSelfDisguise extends SimplePacketListenerAbstract {
    private final boolean[] listenedPackets = new boolean[Server.values().length];
    private final boolean[] rewriteEntityId = new boolean[Server.values().length];
    private final boolean[] neverSend = new boolean[Server.values().length];
    private final boolean[] conflictingPackets;

    public PacketListenerViewSelfDisguise(boolean[] conflictingPackets) {
        this.conflictingPackets = conflictingPackets;

        for (Server packet : new Server[]{Server.ATTACH_ENTITY, Server.SET_PASSENGERS, Server.ENTITY_RELATIVE_MOVE,
            Server.ENTITY_RELATIVE_MOVE_AND_ROTATION, Server.ENTITY_HEAD_LOOK, Server.ENTITY_POSITION_SYNC, Server.ENTITY_TELEPORT,
            Server.ENTITY_ROTATION, Server.ENTITY_EQUIPMENT}) {
            neverSend[packet.ordinal()] = true;
        }

        // Fill every value with true
        Arrays.fill(rewriteEntityId, true);
        // Only these packets are not rewritten, the others are either not handled or are rewritten
        for (Server packet : new Server[]{Server.PLAYER_INFO, Server.PLAYER_INFO_UPDATE, Server.DESTROY_ENTITIES}) {
            rewriteEntityId[packet.ordinal()] = false;
        }

        for (Server packet : new Server[]{NmsVersion.v1_20_R2.isSupported() ? Server.SPAWN_ENTITY : Server.SPAWN_PLAYER,
            Server.SET_PASSENGERS, Server.ENTITY_RELATIVE_MOVE_AND_ROTATION, Server.ENTITY_RELATIVE_MOVE, Server.ENTITY_HEAD_LOOK,
            Server.ENTITY_ROTATION, Server.ENTITY_TELEPORT, Server.ENTITY_MOVEMENT, Server.ENTITY_METADATA, Server.ENTITY_EQUIPMENT,
            Server.ENTITY_ANIMATION, Server.ENTITY_EFFECT, Server.ENTITY_VELOCITY, Server.UPDATE_ATTRIBUTES, Server.ENTITY_STATUS,
            Server.ENTITY_POSITION_SYNC, Server.DAMAGE_EVENT}) {
            // Packet DAMAGE_EVENT does not have all mappings added for every version of Minecraft in PacketEvents
            // https://github.com/retrooper/packetevents/blob/2.0/mappings/damage/damagetype_mappings.json
            if (packet == Server.DAMAGE_EVENT) {
                if (!DisguiseUtilities.isRegistered(DamageTypes.CRAMMING)) {
                    continue;
                }

                // This was added because apparently in 1.21.1, a brief glance indicates that the mappings is probably incomplete
                // A player was experiencing kicks when a wither skull attacked something
                if (NmsVersion.v1_21_R1.isVersion()) {
                    continue;
                }
            }

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

            if (conflictingPackets[event.getPacketType().ordinal()] && disguise.getInternals().shouldAvoidSendingPackets(observer)) {
                if (neverSend[event.getPacketType().ordinal()]) {
                    event.setCancelled(true);
                } else if (observer.getEntityId() != entityId) {
                    event.setCancelled(true);
                }

                return;
            }

            // Here I grab the packets to convert them to, So I can display them as if the disguise sent them.
            LibsPackets<?> transformed =
                PacketsManager.getPacketsManager().getPacketsHandler().transformPacket(wrapper, disguise, observer, observer);

            if (transformed.isUnhandled()) {
                transformed.addPacket(DisguiseUtilities.unsafeClone(event, wrapper));
            }

            LibsPackets<?> selfTransformed = new LibsPackets(wrapper, disguise);
            selfTransformed.setSkinHandling(transformed.isSkinHandling());

            for (PacketWrapper newPacket : transformed.getPackets()) {
                if (newPacket == selfTransformed.getOriginalPacket()) {
                    newPacket = DisguiseUtilities.unsafeClone(event, newPacket);
                }

                if (rewriteEntityId[((PacketType.Play.Server) newPacket.getPacketTypeData().getPacketType()).ordinal()] &&
                    DisguiseUtilities.getEntityId(newPacket) == observer.getEntityId()) {
                    // No need to check if this is self disguise ID since we're only looking for unmapped
                    DisguiseUtilities.writeSelfDisguiseId(observer.getEntityId(), newPacket);
                }

                // Ensure that the self disguise is always underscaled
                if (newPacket.getPacketTypeData().getPacketType() == Server.UPDATE_ATTRIBUTES && disguise.isTallSelfDisguisesScaling()) {
                    runAttributes(disguise, newPacket);
                }

                selfTransformed.addPacket(newPacket);
            }

            for (Map.Entry<Integer, List<PacketWrapper>> entry : transformed.getDelayedPacketsMap().entrySet()) {
                for (PacketWrapper newPacket : entry.getValue()) {
                    if (newPacket == selfTransformed.getOriginalPacket()) {
                        newPacket = DisguiseUtilities.unsafeClone(event, newPacket);
                    }

                    if (rewriteEntityId[((PacketType.Play.Server) newPacket.getPacketTypeData().getPacketType()).ordinal()]) {
                        DisguiseUtilities.writeSelfDisguiseId(observer.getEntityId(), newPacket);
                    }

                    selfTransformed.addDelayedPacket(newPacket, entry.getKey());
                }
            }

            if (disguise.isPlayerDisguise()) {
                LibsDisguises.getInstance().getSkinHandler().handlePackets(observer, (PlayerDisguise) disguise, selfTransformed);
            }

            if (event.getPacketType() == Server.SPAWN_PLAYER || event.getPacketType() == Server.SPAWN_ENTITY) {
                // Add to 'is currently seeing'
                disguise.getInternals().addSeen(observer, true);
            }

            for (PacketWrapper newPacket : selfTransformed.getPackets()) {
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(observer, newPacket);
            }

            selfTransformed.sendDelayed(observer);

            if (event.getPacketType() == Server.ENTITY_METADATA) {
                sendMetadata(observer, wrapper);
            } else if (event.getPacketType() == Server.SPAWN_PLAYER || event.getPacketType() == Server.SPAWN_ENTITY) {
                handleSpawn(event, observer);
            } else if (event.getPacketType() == Server.ENTITY_ANIMATION) {
                handleAllNonWakeAnimation(event, wrapper);
            } else if (event.getPacketType() == Server.ENTITY_STATUS) {
                handleStatus(observer, disguise, event, wrapper);
            } else if (event.getPacketType() == Server.DAMAGE_EVENT) {
                handleDamage(disguise, event, observer);
            } else if (event.getPacketType() == Server.ENTITY_VELOCITY) {
                handleVelocity(event, observer);
            } else if (neverSend[event.getPacketType().ordinal()]) {
                event.setCancelled(true);
            }
        } catch (Throwable ex) {
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }

    private void runAttributes(Disguise disguise, PacketWrapper newPacket) {
        WrapperPlayServerUpdateAttributes attributes = (WrapperPlayServerUpdateAttributes) newPacket;

        // The 'clone' is a shallow clone, packet data is sent to player as well for their own attributes
        List<WrapperPlayServerUpdateAttributes.Property> toSend = new ArrayList<>(attributes.getProperties());

        for (WrapperPlayServerUpdateAttributes.Property prop : attributes.getProperties()) {
            // Only modify scale attribute
            if (prop.getAttribute() != Attributes.GENERIC_SCALE) {
                continue;
            }

            double playerValue = prop.calcValue();
            double max = disguise.getInternals().getPrevSelfDisguiseTallScaleMax();

            // If the disguise height is under the max height
            if (playerValue <= max) {
                break;
            }

            toSend.remove(prop);
            toSend.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, max, new ArrayList<>()));
        }

        attributes.setProperties(toSend);
    }

    private void sendMetadata(Player player, PacketWrapper wrapper) {
        WrapperPlayServerEntityMetadata metadata = (WrapperPlayServerEntityMetadata) wrapper;

        if (metadata.getEntityId() != player.getEntityId()) {
            return;
        }

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

    private void handleSpawn(PacketPlaySendEvent event, Player observer) {
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
    }

    private void handleAllNonWakeAnimation(PacketPlaySendEvent event, PacketWrapper wrapper) {
        if (((WrapperPlayServerEntityAnimation) wrapper).getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
            return;
        }

        event.setCancelled(true);
    }

    private void handleStatus(Player observer, Disguise disguise, PacketPlaySendEvent event, PacketWrapper wrapper) {
        if (!disguise.isSelfDisguiseSoundsReplaced() || disguise.getType().isPlayer() ||
            ((WrapperPlayServerEntityStatus) wrapper).getStatus() != 2) {
            return;
        }

        event.setCancelled(true);

        // As of 1.19.3, no sound is sent but instead the client is expected to play a hurt sound on entity status effect
        if (!NmsVersion.v1_19_R2.isSupported()) {
            return;
        }

        SoundGroup group = SoundGroup.getGroup(disguise);
        ResourceLocation sound = group.getSound(SoundGroup.SoundType.HURT);

        if (sound == null) {
            return;
        }

        observer.playSound(observer.getLocation(), sound.toString(), disguise.getEffectiveSoundCategory().getBukkitSoundCategory(disguise),
            1f, 1f);
    }

    private void handleDamage(Disguise disguise, PacketPlaySendEvent event, Player observer) {
        if (!disguise.isSelfDisguiseSoundsReplaced()) {
            return;
        }

        event.setCancelled(true);
        // No sound is sent but instead the client is expected to play a hurt sound on damage
        SoundGroup group = SoundGroup.getGroup(disguise);

        if (group == null) {
            return;
        }

        ResourceLocation sound = group.getSound(SoundGroup.SoundType.HURT);

        if (sound == null) {
            return;
        }

        observer.playSound(observer.getLocation(), sound.toString(), disguise.getEffectiveSoundCategory().getBukkitSoundCategory(disguise),
            1f, 1f);
    }

    private void handleVelocity(PacketPlaySendEvent event, Player observer) {
        if (DisguiseUtilities.isPlayerVelocity(observer)) {
            return;
        }

        // The player only sees velocity changes when there is a velocity event. As the method claims there
        // was no velocity event...
        event.setCancelled(true);
        // Clear old velocity, this should only occur once.
        DisguiseUtilities.clearPlayerVelocity(observer);
    }
}
