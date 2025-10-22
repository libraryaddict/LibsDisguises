package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import org.bukkit.entity.Player;

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

        if (!listenedPackets[event.getPacketType().ordinal()]) {
            return;
        }

        final Player observer = event.getPlayer();

        if (observer == null) {
            return;
        }

        // First get the entity, the one sending this packet

        PacketWrapper wrapper = DisguiseUtilities.constructWrapper(event);
        Integer entityId = DisguiseUtilities.getEntityId(wrapper);

        if (entityId == null) {
            throw new IllegalStateException("Entity id should not be null on " + wrapper.getClass());
        }

        final Disguise disguise = DisguiseUtilities.getDisguise(observer, entityId);

        // If the entity is the same as the sender. Don't disguise!
        // Prevents problems and there is no advantage to be gained.
        // Or if they are null and there's no disguise
        if (disguise == null || disguise.getEntity() == observer) {
            return;
        }

        LibsPackets<?> packets;

        try {
            packets =
                PacketsManager.getPacketsManager().getPacketsHandler().transformPacket(wrapper, disguise, observer, disguise.getEntity());

            if (disguise.isPlayerDisguise()) {
                LibsDisguises.getInstance().getSkinHandler().handlePackets(observer, (PlayerDisguise) disguise, packets);
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

        // If packet is spawn
        if (spawnPackets[event.getPacketType().ordinal()]) {
            // Add to 'is currently seeing'
            disguise.getInternals().addSeen(observer, true);
        } else if (conflictingTypes[event.getPacketType().ordinal()] && disguise.getInternals().shouldAvoidSendingPackets(observer)) {
            // This array is always an entity rewrite packet type
            event.setCancelled(true);
            return;
        }

        for (PacketWrapper packet : packets.getPackets()) {
            if (packet == wrapper) {
                event.markForReEncode(true);
                continue;
            }

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(observer, packet);
        }

        packets.sendDelayed(observer);
    }
}
