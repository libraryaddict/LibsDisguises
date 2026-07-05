package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.wrapped.BundleContext;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;
import me.libraryaddict.disguise.utilities.wrapped.WrappedManager;

import java.util.UUID;

@RequiredArgsConstructor
public class PacketListenerMain extends SimplePacketListenerAbstract {
    // The packets that are being listened to, this is all-comphrensive
    private final boolean[] listenedPackets;
    // The packets that are directly spawn
    private final boolean[] spawnPackets;
    // The packets that are to do with conflicting
    private final boolean[] conflictingTypes;

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final IWrappedPlayer observer = WrappedManager.getWrappedPlayer(event.getPlayer());

        if (observer == null) {
            return;
        }

        if (!listenedPackets[event.getPacketType().ordinal()]) {
            if (event.getPacketType() == PacketType.Play.Server.BUNDLE) {
                observer.getBundleContext().onBundle();
            }

            return;
        }

        // First get the entity, the one sending this packet

        PacketWrapper wrapper = DisguiseUtilities.constructWrapper(event);
        Integer entityId = DisguiseUtilities.getEntityId(wrapper);

        if (entityId == null) {
            throw new IllegalStateException("Entity id should not be null on " + wrapper.getClass());
        }

        boolean spawnMetadata = false;
        BundleContext ctx = observer.getBundleContext();

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA && entityId == ctx.getPendingSpawnEntityId()) {
            ctx.clearPendingSpawnMetadata();
            spawnMetadata = true;
        }

        UUID uuid = null;

        if (wrapper instanceof WrapperPlayServerSpawnEntity) {
            uuid = ((WrapperPlayServerSpawnEntity) wrapper).getUUID().orElse(null);
        } else if (wrapper instanceof WrapperPlayServerSpawnLivingEntity) {
            uuid = ((WrapperPlayServerSpawnLivingEntity) wrapper).getEntityUUID();
        } else if (wrapper instanceof WrapperPlayServerSpawnPlayer) {
            uuid = ((WrapperPlayServerSpawnPlayer) wrapper).getUUID();
        }

        final Disguise disguise = DisguiseUtilities.getDisguise(observer, uuid, entityId);

        // If not disguised
        if (disguise == null) {
            // If this is a spawn packet
            if (spawnPackets[event.getPacketType().ordinal()]) {
                // Mark them as not in limbo
                DisguiseUtilities.getSeenTracker().setDisguiseTransitionFinished(observer.getUniqueId(), entityId);
            } else if (conflictingTypes[event.getPacketType().ordinal()]) {
                // If this is a conflicting packet, and a new spawn/destroy has not yet been sent
                if (DisguiseUtilities.getSeenTracker().isDisguiseChangingOver(observer.getUniqueId(), entityId)) {
                    event.setCancelled(true);
                }
            }

            return;
        } else if (disguise.getEntity() == observer.getEntity()) {
            // If the entity is the same as the sender. Don't do anything here!
            // Prevents problems and there is no advantage to be gained.
            return;
        }

        if (conflictingTypes[event.getPacketType().ordinal()] &&
            disguise.getInternals().shouldAvoidSendingPackets(observer.getUniqueId())) {
            // This array is always an entity rewrite packet type
            if (event.getPacketType() != PacketType.Play.Server.UPDATE_ATTRIBUTES || observer.getEntityId() != entityId) {
                event.setCancelled(true);
            }
            return;
        }

        LibsPackets<?> packets = new LibsPackets(entityId, wrapper, disguise);
        packets.setSpawnMetadata(spawnMetadata);

        try {
            packets = PacketsManager.getPacketsManager().getPacketsHandler().transformPacket(packets, observer);

            if (disguise.isPlayerDisguise()) {
                LibsDisguises.getInstance().getSkinHandler().handlePackets(observer.getEntity(), (PlayerDisguise) disguise, packets);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            event.setCancelled(true);
            return;
        }

        if (packets.isUnhandled()) {
            return;
        }

        if (packets.shouldCancelPacketEvent()) {
            event.setCancelled(true);
        }

        for (PacketWrapper packet : packets.getPackets()) {
            if (packet == wrapper) {
                event.markForReEncode(true);
                continue;
            }

            observer.sendPacketSilently(packet);
        }

        packets.sendDelayed(observer);

        // If packet is spawn
        if (spawnPackets[event.getPacketType().ordinal()]) {
            // Add to 'is currently seeing'
            disguise.getInternals().addSeen(observer.getUniqueId(), true);
        }
    }
}
