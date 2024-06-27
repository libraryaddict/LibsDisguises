package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by libraryaddict on 3/01/2019.
 */
@Getter
@RequiredArgsConstructor
@Setter
public class LibsPackets<T extends PacketWrapper<T>> {
    @Getter
    private final List<PacketWrapper> packets = new ArrayList<>();
    private final HashMap<Integer, ArrayList<PacketWrapper>> delayedPacketsMap = new HashMap<>();
    @Getter
    private final T originalPacket;
    @Getter
    private final Disguise disguise;
    private boolean unhandled;
    private boolean skinHandling;

    public boolean shouldCancelPacketEvent() {
        return !packets.contains(getOriginalPacket());
    }

    public void addPacket(PacketWrapper packet) {
        packets.add(packet);
    }

    public void addDelayedPacket(PacketWrapper packet) {
        addDelayedPacket(packet, 2);
    }

    public void clear() {
        getPackets().clear();
    }

    public void addDelayedPacket(PacketWrapper packet, int ticksDelayed) {
        if (!delayedPacketsMap.containsKey(ticksDelayed)) {
            delayedPacketsMap.put(ticksDelayed, new ArrayList<>());
        }

        delayedPacketsMap.get(ticksDelayed).add(packet);
    }

    public void sendDelayed(final Player observer) {
        for (Map.Entry<Integer, ArrayList<PacketWrapper>> entry : getDelayedPacketsMap().entrySet()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                if (!getDisguise().isDisguiseInUse()) {
                    ArrayList<PacketWrapper> packets = entry.getValue();

                    if (packets.stream().noneMatch(p -> p.getPacketTypeData().getPacketType() == PacketType.Play.Server.PLAYER_INFO)) {
                        return;
                    }

                    packets.removeIf(p -> p.getPacketTypeData().getPacketType() != PacketType.Play.Server.PLAYER_INFO);
                }

                for (PacketWrapper packet : entry.getValue()) {
                    // To have right click handled properly, equip packets sent are normal
                    if (packet.getPacketTypeData().getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(observer, packet);
                    } else {
                        PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
                    }
                }
            }, entry.getKey());
        }
    }

    private WrapperPlayServerEntityEquipment createPacket(EquipmentSlot slot) {
        // Get what the disguise wants to show for its armor
        ItemStack itemToSend = getDisguise().getWatcher().getItemStack(slot);

        // If the disguise armor isn't visible
        if (itemToSend == null) {
            itemToSend = DisguiseUtilities.getEquipment(slot, getDisguise().getEntity());

            // If natural armor isn't sent either
            if (itemToSend == null || itemToSend.getType() == Material.AIR) {
                return null;
            }
        } else if (itemToSend.getType() == Material.AIR) {
            return null;
        }

        return new WrapperPlayServerEntityEquipment(getDisguise().getEntity().getEntityId(), Collections.singletonList(
            new Equipment(DisguiseUtilities.getSlot(slot), SpigotConversionUtil.fromBukkitItemStack(itemToSend))));
    }
}