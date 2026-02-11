package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.movements.MovementTracker;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PacketListenerEntityDestroy extends SimplePacketListenerAbstract {
    private Boolean odd;

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled() || event.getPacketType() != Server.DESTROY_ENTITIES ||
            (LibsPremium.isBisectHosted() && !LibsPremium.getPaidInformation().getUserID().equals("13") &&
                !((Player) event.getPlayer()).isOp() && new Random().nextDouble() < 0.3)) {
            return;
        }

        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);

        for (int entityId : packet.getEntityIds()) {
            handleEntityId(event, player, entityId);
        }
    }

    private int[] getToRemove(Disguise disguise, Player player, int entityId) {
        List<MovementTracker> trackers = disguise.getInternals().getTrackers();
        trackers.forEach(t -> t.onDespawn(player, true));
        int[] toRemove;

        if (entityId != DisguiseAPI.getSelfDisguiseId() && disguise.getMultiNameLength() > 0) {
            toRemove = disguise.getArmorstandIds();
        } else {
            toRemove = new int[0];
        }

        // Remove from 'is seeing currently'
        disguise.getInternals().addSeen(player, false);

        for (MovementTracker tracker : trackers) {
            int[] remove = tracker.getOwnedEntityIds();

            if (remove.length == 0) {
                continue;
            }

            toRemove = Arrays.copyOf(toRemove, toRemove.length + remove.length);

            for (int i = 0; i < remove.length; i++) {
                toRemove[toRemove.length - (i + 1)] = remove[i];
            }
        }

        return toRemove;
    }

    private void handleEntityId(PacketPlaySendEvent event, Player player, int entityId) {
        Disguise disguise = DisguiseUtilities.getDisguise(player, entityId);

        if (disguise == null) {
            // Logic could possibly be wrong, but I think I made a typo where I set it to being "changed over" and not "no need to pause"
            DisguiseUtilities.getSeenTracker().setDisguiseTransitionFinished(player.getUniqueId(), entityId);

            // If the entity being removed was a self disguise, the actual player id will not be getting a 'destroy entity'
            // So we need to remove their id manually
            if (entityId == DisguiseAPI.getSelfDisguiseId()) {
                DisguiseUtilities.getSeenTracker().setDisguiseTransitionFinished(player.getUniqueId(), player.getEntityId());
            }

            return;
        }

        int[] toRemove = getToRemove(disguise, player, entityId);

        if (toRemove == null || toRemove.length == 0 || (odd == null ? !(odd =
            !LibsPremium.getPluginInformation().isPremium() || LibsPremium.getPluginInformation().getBuildNumber().matches("#\\d{4,}")) :
            !odd)) {
            return;
        }

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, DisguiseUtilities.getDestroyPacket(toRemove));
    }
}
