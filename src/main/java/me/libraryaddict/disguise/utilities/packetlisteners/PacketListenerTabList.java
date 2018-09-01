package me.libraryaddict.disguise.utilities.packetlisteners;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;

public class PacketListenerTabList extends PacketAdapter {
    public PacketListenerTabList(LibsDisguises plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (event.isCancelled())
            return;

        Player observer = event.getPlayer();

        if (event.getPacket().getPlayerInfoAction().read(0) != PlayerInfoAction.ADD_PLAYER)
            return;

        List<PlayerInfoData> list = event.getPacket().getPlayerInfoDataLists().read(0);
        Iterator<PlayerInfoData> itel = list.iterator();

        while (itel.hasNext()) {
            PlayerInfoData data = itel.next();

            Player player = Bukkit.getPlayer(data.getProfile().getUUID());

            if (player == null)
                continue;

            Disguise disguise = DisguiseAPI.getDisguise(observer, player);

            if (disguise == null)
                continue;

            if (!disguise.isHidePlayer())
                continue;

            itel.remove();
        }

        if (list.isEmpty()) {
            event.setCancelled(true);
        }
        else {
            event.getPacket().getPlayerInfoDataLists().write(0, list);
        }
    }

}
