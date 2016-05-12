package me.libraryaddict.disguise.utilities.packetlisteners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
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

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.PacketsManager;

public class PacketListenerMain extends PacketAdapter
{
    private LibsDisguises libsDisguises;

    public PacketListenerMain(LibsDisguises plugin, ArrayList<PacketType> packetsToListen)
    {
        super(plugin, ListenerPriority.HIGH, packetsToListen);

        libsDisguises = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        if (event.isCancelled())
            return;

        if (event.getPlayer().getName().contains("UNKNOWN[")) // If the player is temporary
            return;

        final Player observer = event.getPlayer();

        // First get the entity, the one sending this packet
        StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());

        org.bukkit.entity.Entity entity = entityModifer.read((Server.COLLECT == event.getPacketType() ? 1 : 0));

        // If the entity is the same as the sender. Don't disguise!
        // Prevents problems and there is no advantage to be gained.
        if (entity == observer)
            return;

        PacketContainer[][] packets = PacketsManager.transformPacket(event.getPacket(), event.getPlayer(), entity);

        if (packets != null)
        {
            event.setCancelled(true);

            try
            {
                for (PacketContainer packet : packets[0])
                {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                }

                final PacketContainer[] delayed = packets[1];

                if (delayed.length > 0)
                {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(libsDisguises, new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                for (PacketContainer packet : delayed)
                                {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                                }
                            }
                            catch (InvocationTargetException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, 2);
                }
            }
            catch (InvocationTargetException ex)
            {
                ex.printStackTrace();
            }
        }
    }

}
