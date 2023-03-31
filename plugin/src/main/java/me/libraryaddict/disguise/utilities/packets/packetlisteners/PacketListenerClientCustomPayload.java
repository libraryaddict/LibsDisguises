package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * Created by libraryaddict on 21/05/2020.
 */
public class PacketListenerClientCustomPayload extends PacketAdapter {
    public PacketListenerClientCustomPayload() {
        super(LibsDisguises.getInstance(), PacketType.Play.Client.CUSTOM_PAYLOAD);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.isPlayerTemporary()) {
            return;
        }

        if (NmsVersion.v1_13.isSupported()) {
            if (!event.getPacket().getMinecraftKeys().read(0).getFullKey().equals("minecraft:brand")) {
                return;
            }
        } else {
            if (!event.getPacket().getStrings().read(0).equals("MC|Brand")) {
                return;
            }
        }

        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.hasMetadata("ld_loggedin")) {
                    return;
                }

                if (player.hasMetadata("ld_tabsend") && !player.getMetadata("ld_tabsend").isEmpty()) {
                    ArrayList<PacketContainer> packets = (ArrayList<PacketContainer>) player.getMetadata("ld_tabsend").get(0).value();

                    player.removeMetadata("ld_tabsend", LibsDisguises.getInstance());

                    for (PacketContainer packet : packets) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                    }
                }

                player.setMetadata("ld_loggedin", new FixedMetadataValue(LibsDisguises.getInstance(), true));
            }
        }.runTaskLater(LibsDisguises.getInstance(), 20);
    }
}
