package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PacketListenerMain extends PacketAdapter {
    public PacketListenerMain(LibsDisguises plugin, ArrayList<PacketType> packetsToListen) {
        super(plugin, ListenerPriority.HIGH, packetsToListen);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (event.isCancelled() || event.isPlayerTemporary()) {
            return;
        }

        final Player observer = event.getPlayer();

        if (observer.getName().contains("UNKNOWN[")) // If the player is temporary
        {
            return;
        }

        // First get the entity, the one sending this packet

        int entityId = event.getPacket().getIntegers().read(Server.COLLECT == event.getPacketType() ? 1 : 0);

        final Disguise disguise = DisguiseUtilities.getDisguise(observer, entityId);

        // If the entity is the same as the sender. Don't disguise!
        // Prevents problems and there is no advantage to be gained.
        // Or if they are null and there's no disguise
        if (disguise == null || disguise.getEntity() == observer) {
            return;
        }

        LibsPackets packets;

        try {
            packets = PacketsManager.getPacketsHandler().transformPacket(event.getPacket(), disguise, observer, disguise.getEntity());

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

        event.setCancelled(true);

        for (PacketContainer packet : packets.getPackets()) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
        }

        packets.sendDelayed(observer);
    }
}
