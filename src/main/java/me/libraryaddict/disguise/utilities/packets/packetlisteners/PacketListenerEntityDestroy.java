package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
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
        if (event.isCancelled()) {
            return;
        }

        if (!NmsVersion.v1_17.isSupported()) {
            onPre17Packet(event);
            return;
        }

        int[] toAdd = getToRemove(event.getPlayer(), event.getPacket().getIntegers().read(0));

        if (toAdd == null) {
            return;
        }

        try {
            for (PacketContainer container : DisguiseUtilities.getDestroyPackets(toAdd)) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), container);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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

    public void onPre17Packet(PacketEvent event) {
        int[] entityIds = event.getPacket().getIntegerArrays().read(0);
        int[] newEntityIds = entityIds;

        for (int entityId : entityIds) {
            int[] toAdd = getToRemove(event.getPlayer(), entityId);

            if (toAdd == null) {
                continue;
            }

            newEntityIds = Arrays.copyOf(entityIds, entityIds.length + toAdd.length);

            for (int a = 0; a < toAdd.length; a++) {
                newEntityIds[newEntityIds.length - (a + 1)] = toAdd[a];
            }
        }

        if (entityIds.length == newEntityIds.length) {
            return;
        }

        event.getPacket().getIntegerArrays().write(0, newEntityIds);
    }
}
