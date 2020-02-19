package me.libraryaddict.disguise.utilities.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class LibsPackets {
    private ArrayList<PacketContainer> packets = new ArrayList<>();
    private HashMap<Integer, ArrayList<PacketContainer>> delayedPackets = new HashMap<>();
    private boolean isSpawnPacket;
    private Disguise disguise;
    private boolean doNothing;
    private int removeMetaAt = -1;
    @Getter
    @Setter
    private boolean sendArmor;

    public LibsPackets(Disguise disguise) {
        this.disguise = disguise;
    }

    public void setUnhandled() {
        doNothing = true;
    }

    public void setRemoveMetaAt(int tick) {
        removeMetaAt = tick;
    }

    public boolean isUnhandled() {
        return doNothing;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    public void setSpawnPacketCheck(PacketType type) {
        isSpawnPacket = type.name().contains("SPAWN") && type.name().contains("ENTITY");
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
        if (!delayedPackets.containsKey(ticksDelayed))
            delayedPackets.put(ticksDelayed, new ArrayList<>());

        delayedPackets.get(ticksDelayed).add(packet);
    }

    public ArrayList<PacketContainer> getPackets() {
        return packets;
    }

    public Collection<ArrayList<PacketContainer>> getDelayedPackets() {
        return delayedPackets.values();
    }

    public void sendDelayed(final Player observer) {
        Iterator<Map.Entry<Integer, ArrayList<PacketContainer>>> itel = delayedPackets.entrySet().iterator();

        while (itel.hasNext()) {
            Map.Entry<Integer, ArrayList<PacketContainer>> entry = itel.next();
            // If this is the last delayed packet
            final boolean isRemoveCancel = isSpawnPacket && entry.getKey() >= removeMetaAt && removeMetaAt >= 0;

            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {

                if (isRemoveCancel && "%%__USER__%%".equals("%%__USER__%%") && !"%%__USER__%%".equals("12345")) {
                    PacketsManager.getPacketsHandler().removeCancel(disguise, observer);
                }

                if (!disguise.isDisguiseInUse()) {
                    ArrayList<PacketContainer> packets = entry.getValue();

                    if (packets.stream().noneMatch(p -> p.getType() == PacketType.Play.Server.PLAYER_INFO)) {
                        return;
                    }

                    packets.removeIf(p -> p.getType() != PacketType.Play.Server.PLAYER_INFO);
                }

                if (isRemoveCancel) {
                    if (isSendArmor()) {
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            PacketContainer packet = createPacket(slot);

                            if (packet == null) {
                                continue;
                            }

                            try {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                            }
                            catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                try {
                    for (PacketContainer packet : entry.getValue()) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }, entry.getKey());
        }
    }

    private PacketContainer createPacket(EquipmentSlot slot) {
        // Get what the disguise wants to show for its armor
        ItemStack itemToSend = disguise.getWatcher().getItemStack(slot);

        // If the disguise armor isn't visible
        if (itemToSend == null) {
            itemToSend = ReflectionManager.getEquipment(slot, disguise.getEntity());

            // If natural armor isn't sent either
            if (itemToSend == null || itemToSend.getType() == Material.AIR) {
                return null;
            }
        } else if (itemToSend.getType() == Material.AIR) {
            return null;
        }

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        StructureModifier<Object> mods = packet.getModifier();

        mods.write(0, disguise.getEntity().getEntityId());
        mods.write(1, ReflectionManager.createEnumItemSlot(slot));
        mods.write(2, ReflectionManager.getNmsItem(itemToSend));

        return packet;
    }
}