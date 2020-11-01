package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.mojang.datafixers.util.Pair;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsHandler;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerEquipment implements IPacketHandler {
    private PacketsHandler packetsHandler;

    public PacketHandlerEquipment(PacketsHandler packetsHandler) {
        this.packetsHandler = packetsHandler;
    }

    @Override
    public PacketType[] getHandledPackets() {
        return new PacketType[]{PacketType.Play.Server.ENTITY_EQUIPMENT};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {
        if (NmsVersion.v1_16.isSupported()) {
            handleNew(disguise, sentPacket, packets, observer, entity);
        } else {
            handleOld(disguise, sentPacket, packets, observer, entity);
        }
    }

    public void handleNew(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {
        // Else if the disguise is updating equipment

        List<Pair<Object, Object>> slots = (List<Pair<Object, Object>>) packets.getPackets().get(0).getModifier()
                .read(1);
        List<Pair<Object, Object>> newSlots = new ArrayList<>();
        boolean constructed = false;

        for (Pair<Object, Object> pair : slots) {
            EquipmentSlot slot = ReflectionManager.createEquipmentSlot(pair.getFirst());

            org.bukkit.inventory.ItemStack itemStack = disguise.getWatcher().getItemStack(slot);

            if (itemStack != null) {
                if (!constructed) {
                    constructed = true;

                    if (packets.getPackets().size() > 1) {
                        packets.getPackets().remove(1);
                    } else {
                        packets.clear();
                    }

                    PacketContainer equipPacket = sentPacket.shallowClone();

                    packets.getPackets().add(packets.getPackets().size(), equipPacket);

                    equipPacket.getModifier().write(1, newSlots);
                }

                newSlots.add(Pair.of(pair.getFirst(),
                        ReflectionManager.getNmsItem(itemStack.getType() == Material.AIR ? null : itemStack)));
            } else {
                newSlots.add(pair);
            }

            if (disguise.getWatcher().isRightClicking() &&
                    (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND)) {
                itemStack = ReflectionManager.getBukkitItem(pair.getSecond());

                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    // Convert the datawatcher
                    List<WrappedWatchableObject> list = new ArrayList<>();

                    if (DisguiseConfig.isMetaPacketsEnabled()) {
                        WrappedWatchableObject watch = ReflectionManager.createWatchable(MetaIndex.LIVING_HAND,
                                WrappedDataWatcher.getEntityWatcher(entity).getByte(MetaIndex.LIVING_HAND.getIndex()));

                        if (watch != null)
                            list.add(watch);

                        list = disguise.getWatcher().convert(list);
                    } else {
                        for (WrappedWatchableObject obj : disguise.getWatcher().getWatchableObjects()) {
                            if (obj.getIndex() == MetaIndex.LIVING_HAND.getIndex()) {
                                list.add(obj);
                                break;
                            }
                        }
                    }

                    // Construct the packets to return
                    PacketContainer packetBlock = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

                    packetBlock.getModifier().write(0, entity.getEntityId());
                    packetBlock.getWatchableCollectionModifier().write(0, list);

                    PacketContainer packetUnblock = packetBlock.deepClone();
                    // Make a packet to send the 'unblock'
                    for (WrappedWatchableObject watcher : packetUnblock.getWatchableCollectionModifier().read(0)) {
                        watcher.setValue((byte) 0);
                    }

                    // Send the unblock before the itemstack change so that the 2nd metadata packet works. Why?
                    // Scheduler
                    // delay.

                    PacketContainer packet1 = packets.getPackets().get(0);

                    packets.clear();

                    packets.addPacket(packetUnblock);
                    packets.addPacket(packet1);
                    packets.addPacket(packetBlock);
                    // Silly mojang made the right clicking datawatcher value only valid for one use. So I have
                    // to reset
                    // it.
                }
            }
        }
    }

    public void handleOld(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {
        // Else if the disguise is updating equipment

        EquipmentSlot slot = ReflectionManager.createEquipmentSlot(packets.getPackets().get(0).getModifier().read(1));

        org.bukkit.inventory.ItemStack itemStack = disguise.getWatcher().getItemStack(slot);

        if (itemStack != null) {
            packets.clear();

            PacketContainer equipPacket = sentPacket.shallowClone();

            packets.addPacket(equipPacket);

            equipPacket.getModifier()
                    .write(2, ReflectionManager.getNmsItem(itemStack.getType() == Material.AIR ? null : itemStack));
        }

        if (disguise.getWatcher().isRightClicking() && (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND)) {
            ItemStack heldItem = packets.getPackets().get(0).getItemModifier().read(0);

            if (heldItem != null && heldItem.getType() != Material.AIR) {
                // Convert the datawatcher
                List<WrappedWatchableObject> list = new ArrayList<>();
                MetaIndex toUse = NmsVersion.v1_13.isSupported() ? MetaIndex.LIVING_HAND : MetaIndex.ENTITY_META;

                if (DisguiseConfig.isMetaPacketsEnabled()) {
                    WrappedWatchableObject watch = ReflectionManager.createWatchable(toUse,
                            WrappedDataWatcher.getEntityWatcher(entity).getByte(toUse.getIndex()));

                    if (watch != null)
                        list.add(watch);

                    list = disguise.getWatcher().convert(list);
                } else {
                    for (WrappedWatchableObject obj : disguise.getWatcher().getWatchableObjects()) {
                        if (obj.getIndex() == toUse.getIndex()) {
                            list.add(obj);
                            break;
                        }
                    }
                }

                // Construct the packets to return
                PacketContainer packetBlock = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

                packetBlock.getModifier().write(0, entity.getEntityId());
                packetBlock.getWatchableCollectionModifier().write(0, list);

                PacketContainer packetUnblock = packetBlock.deepClone();
                // Make a packet to send the 'unblock'
                for (WrappedWatchableObject watcher : packetUnblock.getWatchableCollectionModifier().read(0)) {
                    if (NmsVersion.v1_13.isSupported()) {
                        watcher.setValue((byte) 0);
                    } else {
                        watcher.setValue((byte) ((byte) watcher.getRawValue() & ~(1 << 4)));
                    }
                }

                // Send the unblock before the itemstack change so that the 2nd metadata packet works. Why?
                // Scheduler
                // delay.

                PacketContainer packet1 = packets.getPackets().get(0);

                packets.clear();

                packets.addPacket(packetUnblock);
                packets.addPacket(packet1);
                packets.addPacket(packetBlock);
                // Silly mojang made the right clicking datawatcher value only valid for one use. So I have
                // to reset
                // it.
            }
        }
    }
}
