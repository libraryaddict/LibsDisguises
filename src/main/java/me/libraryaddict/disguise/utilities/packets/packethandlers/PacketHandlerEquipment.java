package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.packets.PacketsHandler;
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
        if (DisguiseConfig.isPlayerHideArmor() && packetsHandler.isCancelMeta(disguise, observer)) {
            packets.clear();

            PacketContainer equipPacket = sentPacket.shallowClone();

            packets.addPacket(equipPacket);

            equipPacket.getModifier().write(2, ReflectionManager.getNmsItem(new ItemStack(Material.AIR)));
            return;
        }

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

        if (disguise.getWatcher().isRightClicking() && slot == EquipmentSlot.HAND) {
            ItemStack heldItem = packets.getPackets().get(0).getItemModifier().read(0);

            if (heldItem != null && heldItem.getType() != Material.AIR) {
                // Convert the datawatcher
                List<WrappedWatchableObject> list = new ArrayList<>();

                if (DisguiseConfig.isMetaPacketsEnabled()) {
                    WrappedWatchableObject watch = ReflectionManager.createWatchable(MetaIndex.ENTITY_META,
                            WrappedDataWatcher.getEntityWatcher(entity).getByte(0));

                    if (watch != null)
                        list.add(watch);

                    list = disguise.getWatcher().convert(list);
                } else {
                    for (WrappedWatchableObject obj : disguise.getWatcher().getWatchableObjects()) {
                        if (obj.getIndex() == 0) {
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
                    watcher.setValue((byte) ((byte) watcher.getValue() & ~(1 << 4)));
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
