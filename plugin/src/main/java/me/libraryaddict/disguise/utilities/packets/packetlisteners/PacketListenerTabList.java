package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PacketListenerTabList extends PacketAdapter {
    public PacketListenerTabList(LibsDisguises plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player observer = event.getPlayer();
        Set<PlayerInfoAction> actions;

        if (NmsVersion.v1_19_R2.isSupported()) {
            actions = event.getPacket().getPlayerInfoActions().read(0);
        } else {
            actions = EnumSet.of(event.getPacket().getPlayerInfoAction().read(0));
        }

        if (actions.stream().noneMatch(a -> a == PlayerInfoAction.ADD_PLAYER)) {
            return;
        }

        List<PlayerInfoData> list = event.getPacket().getPlayerInfoDataLists().read(NmsVersion.v1_19_R2.isSupported() ? 1 : 0);
        Iterator<PlayerInfoData> itel = list.iterator();
        Iterator<PlayerInfoAction> actionItel = actions.iterator();

        while (itel.hasNext() && actionItel.hasNext()) {
            PlayerInfoData data = itel.next();

            if (NmsVersion.v1_19_R2.isSupported()) {
                if (actionItel.next() != PlayerInfoAction.ADD_PLAYER) {
                    continue;
                }
            }

            if (data == null) {
                continue;
            }

            Player player = Bukkit.getPlayer(data.getProfile().getUUID());

            if (player == null) {
                continue;
            }

            Disguise disguise = DisguiseAPI.getDisguise(observer, player);

            if (disguise == null) {
                continue;
            }

            if (!disguise.isHidePlayer()) {
                continue;
            }

            itel.remove();

            if (NmsVersion.v1_19_R2.isSupported()) {
                actionItel.remove();
            }
        }

        if (list.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        event.getPacket().getPlayerInfoDataLists().write(NmsVersion.v1_19_R2.isSupported() ? 1 : 0, list);

        if (!NmsVersion.v1_19_R2.isSupported()) {
            return;
        }

        event.getPacket().getPlayerInfoActions().write(0, actions);
    }
}
