package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by libraryaddict on 3/01/2019.
 */
@Getter
@RequiredArgsConstructor
@Setter
public class LibsPackets {
    private final ArrayList<PacketContainer> packets = new ArrayList<>();
    private final HashMap<Integer, ArrayList<PacketContainer>> delayedPacketsMap = new HashMap<>();
    private final Disguise disguise;
    private boolean unhandled;

    public Disguise getDisguise() {
        return disguise;
    }

    public void addPacket(PacketContainer packet) {
        packets.add(packet);
    }

    public void addDelayedPacket(PacketContainer packet) {
        addDelayedPacket(packet, 2);
    }

    public void clear() {
        getPackets().clear();
    }

    public void addDelayedPacket(PacketContainer packet, int ticksDelayed) {
        if (!delayedPacketsMap.containsKey(ticksDelayed)) {
            delayedPacketsMap.put(ticksDelayed, new ArrayList<>());
        }

        delayedPacketsMap.get(ticksDelayed).add(packet);
    }

    public ArrayList<PacketContainer> getPackets() {
        return packets;
    }

    public void sendDelayed(final Player observer) {
        for (Map.Entry<Integer, ArrayList<PacketContainer>> entry : getDelayedPacketsMap().entrySet()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                if (!getDisguise().isDisguiseInUse()) {
                    ArrayList<PacketContainer> packets = entry.getValue();

                    if (packets.stream().noneMatch(p -> p.getType() == PacketType.Play.Server.PLAYER_INFO)) {
                        return;
                    }

                    packets.removeIf(p -> p.getType() != PacketType.Play.Server.PLAYER_INFO);
                }

                for (PacketContainer packet : entry.getValue()) {
                    // To have right click handled properly, equip packets sent are normal
                    ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, packet.getType() == PacketType.Play.Server.ENTITY_EQUIPMENT);
                }
            }, entry.getKey());
        }
    }

    private PacketContainer createPacket(EquipmentSlot slot) {
        // Get what the disguise wants to show for its armor
        ItemStack itemToSend = getDisguise().getWatcher().getItemStack(slot);

        // If the disguise armor isn't visible
        if (itemToSend == null) {
            itemToSend = ReflectionManager.getEquipment(slot, getDisguise().getEntity());

            // If natural armor isn't sent either
            if (itemToSend == null || itemToSend.getType() == Material.AIR) {
                return null;
            }
        } else if (itemToSend.getType() == Material.AIR) {
            return null;
        }

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        StructureModifier<Object> mods = packet.getModifier();

        mods.write(0, getDisguise().getEntity().getEntityId());

        if (NmsVersion.v1_16.isSupported()) {
            List<Pair<Object, Object>> list = new ArrayList<>();
            list.add(Pair.of(ReflectionManager.createEnumItemSlot(slot), ReflectionManager.getNmsItem(itemToSend)));

            mods.write(1, list);
        } else {
            mods.write(1, ReflectionManager.createEnumItemSlot(slot));
            mods.write(2, ReflectionManager.getNmsItem(itemToSend));
        }

        return packet;
    }
}