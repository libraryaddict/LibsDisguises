package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * Created by libraryaddict on 3/05/2020.
 */
public class PacketListenerEntityDestroy extends PacketAdapter {
    public PacketListenerEntityDestroy(Plugin plugin) {
        super(plugin, PacketType.Play.Server.ENTITY_DESTROY);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        int[] entityIds = event.getPacket().getIntegerArrays().read(0);
        int[] newEntityIds = entityIds;

        for (int entityId : entityIds) {
            if (entityId == DisguiseAPI.getSelfDisguiseId()) {
                return;
            }

            Disguise disguise = DisguiseUtilities.getDisguise(event.getPlayer(), entityId);

            if (disguise == null) {
                continue;
            }

            int len = disguise.getMultiNameLength();

            if (len == 0) {
                continue;
            }

            // If this packet has already been processed
            if (entityIds.length == newEntityIds.length &&
                    Arrays.stream(entityIds).anyMatch(id -> id == disguise.getArmorstandIds()[0])) {
                return;
            }

            newEntityIds = Arrays.copyOf(entityIds, entityIds.length + len);

            for (int a = 0; a < len; a++) {
                newEntityIds[newEntityIds.length - (a + 1)] = disguise.getArmorstandIds()[a];
            }
        }

        if (entityIds.length == newEntityIds.length) {
            return;
        }

        event.getPacket().getIntegerArrays().write(0, newEntityIds);
    }
}
