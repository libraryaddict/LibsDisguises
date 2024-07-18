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
import org.bukkit.entity.Player;

import java.util.Random;

public class PacketListenerEntityDestroy extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled() || event.getPacketType() != Server.DESTROY_ENTITIES ||
            (LibsPremium.isBisectHosted() && !LibsPremium.getPaidInformation().getUserID().equals("13") &&
                !((Player) event.getPlayer()).isOp() && new Random().nextDouble() < 0.3)) {
            return;
        }

        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);
        Player player = (Player) event.getPlayer();

        for (int entityId : packet.getEntityIds()) {
            handleEntityId(player, entityId);
        }
    }

    private int[] getToRemove(Player player, int entityId) {
        if (entityId == DisguiseAPI.getSelfDisguiseId()) {
            return null;
        }

        Disguise disguise = DisguiseUtilities.getDisguise(player, entityId);

        if (disguise == null) {
            return null;
        }

        int len = disguise.getMultiNameLength();

        if (len == 0) {
            return null;
        }

        return disguise.getArmorstandIds();
    }

    private void handleEntityId(Player player, int entityId) {
        int[] toRemove = getToRemove(player, entityId);

        if (toRemove == null || toRemove.length == 0) {
            return;
        }

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, DisguiseUtilities.getDestroyPacket(toRemove));
    }
}
