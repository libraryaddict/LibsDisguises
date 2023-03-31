package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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

        Function<UUID, Boolean> shouldRemove = uuid -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                return false;
            }

            Disguise disguise = DisguiseAPI.getDisguise(observer, player);

            return disguise != null && disguise.isHidePlayer();
        };

        if (NmsVersion.v1_19_R2.isSupported()) {
            ReflectionManager.getNmsReflection().handleTablistPacket(event, shouldRemove);
            return;
        }

        PacketContainer packet = event.getPacket();

        if (packet.getPlayerInfoAction().read(0) != PlayerInfoAction.ADD_PLAYER) {
            return;
        }

        List<PlayerInfoData> list = packet.getPlayerInfoDataLists().read(0);
        Iterator<PlayerInfoData> itel = list.iterator();
        boolean modified = false;

        while (itel.hasNext()) {
            PlayerInfoData data = itel.next();

            if (data == null) {
                continue;
            }

            if (!shouldRemove.apply(data.getProfile().getUUID())) {
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
            return;
        }

        packet.getPlayerInfoDataLists().write(0, list);
    }
}
