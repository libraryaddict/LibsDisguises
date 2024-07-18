package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class PacketListenerClientCustomPayload extends SimplePacketListenerAbstract {
    private final String mcChannel = NmsVersion.v1_13.isSupported() ? "minecraft:brand" : "MC|Brand";

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Client.PLUGIN_MESSAGE) {
            return;
        }

        if (!mcChannel.equals(new WrapperPlayClientPluginMessage(event).getChannelName())) {
            return;
        }

        Player player = (Player) event.getPlayer();

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
                    ArrayList<PacketWrapper> packets = (ArrayList<PacketWrapper>) player.getMetadata("ld_tabsend").get(0).value();

                    player.removeMetadata("ld_tabsend", LibsDisguises.getInstance());

                    for (PacketWrapper packet : packets) {
                        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
                    }
                }

                player.setMetadata("ld_loggedin", new FixedMetadataValue(LibsDisguises.getInstance(), true));
            }
        }.runTaskLater(LibsDisguises.getInstance(), 20);
    }
}
