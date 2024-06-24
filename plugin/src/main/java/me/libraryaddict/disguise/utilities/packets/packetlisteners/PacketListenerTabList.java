package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo.PlayerData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PacketListenerTabList extends SimplePacketListenerAbstract {
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Server.PLAYER_INFO) {
            return;
        }

        Player observer = (Player) event.getPlayer();

        Function<UUID, Boolean> shouldRemove = uuid -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                return false;
            }

            Disguise disguise = DisguiseAPI.getDisguise(observer, player);

            return disguise != null && disguise.isHidePlayer();
        };

        if (NmsVersion.v1_19_R2.isSupported()) {
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);

            if (!packet.getActions().contains(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER)) {
                return;
            }

            packet.getEntries().removeIf(p -> shouldRemove.apply(p.getGameProfile().getUUID()));

            if (packet.getEntries().isEmpty()) {
                event.setCancelled(true);
            }

            event.markForReEncode(true);
            return;
        }

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(event);

        if (packet.getAction() != WrapperPlayServerPlayerInfo.Action.ADD_PLAYER) {
            return;
        }

        List<PlayerData> list = packet.getPlayerDataList();
        Iterator<PlayerData> itel = list.iterator();
        boolean modified = false;

        while (itel.hasNext()) {
            PlayerData data = itel.next();

            if (data == null || data.getUser() == null) {
                continue;
            }

            if (!shouldRemove.apply(data.getUser().getUUID())) {
                continue;
            }

            itel.remove();
            modified = true;
        }

        if (!modified) {
            return;
        }

        if (list.isEmpty()) {
            event.setCancelled(true);
        }

        event.markForReEncode(true);
    }
}
