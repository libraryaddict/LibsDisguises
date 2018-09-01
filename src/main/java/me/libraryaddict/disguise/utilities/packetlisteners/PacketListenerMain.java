package me.libraryaddict.disguise.utilities.packetlisteners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.PacketsManager.LibsPackets;

public class PacketListenerMain extends PacketAdapter {
    public PacketListenerMain(LibsDisguises plugin, ArrayList<PacketType> packetsToListen) {
        super(plugin, ListenerPriority.HIGH, packetsToListen);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (event.isCancelled())
            return;

        final Player observer = event.getPlayer();

        if (observer.getName().contains("UNKNOWN[")) // If the player is temporary
            return;

        // First get the entity, the one sending this packet
        StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());

        org.bukkit.entity.Entity entity = entityModifer.read((Server.COLLECT == event.getPacketType() ? 1 : 0));

        // If the entity is the same as the sender. Don't disguise!
        // Prevents problems and there is no advantage to be gained.
        if (entity == observer)
            return;

        final Disguise disguise = DisguiseAPI.getDisguise(observer, entity);

        if (disguise == null)
            return;

        LibsPackets packets;

        try {
            packets = PacketsManager.transformPacket(event.getPacket(), disguise, observer, entity);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            event.setCancelled(true);
            return;
        }

        if (packets.isUnhandled()) {
            return;
        }

        packets.setPacketType(event.getPacketType());

        event.setCancelled(true);

        try {
            for (PacketContainer packet : packets.getPackets()) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
            }

            packets.sendDelayed(observer);
        }
        catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }

    }

}
