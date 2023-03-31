package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Created by libraryaddict on 3/05/2020.
 */
public class PacketListenerEntityDestroy extends PacketAdapter {
    public PacketListenerEntityDestroy(Plugin plugin) {
        super(plugin, PacketType.Play.Server.ENTITY_DESTROY);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!NmsVersion.v1_17.isSupported()) {
            int[] entityIds = event.getPacket().getIntegerArrays().read(0);

            for (int entityId : entityIds) {
                handleEntityId(event.getPlayer(), entityId);
            }

            return;
        }

        List<Integer> entityIds = event.getPacket().getIntLists().read(0);

        for (int entityId : entityIds) {
            handleEntityId(event.getPlayer(), entityId);
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

        if (toRemove == null) {
            return;
        }

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, DisguiseUtilities.getDestroyPacket(toRemove));
    }
}
