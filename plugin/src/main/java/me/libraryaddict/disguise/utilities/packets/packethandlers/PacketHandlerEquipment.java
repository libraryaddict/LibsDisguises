package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PacketHandlerEquipment implements IPacketHandler<WrapperPlayServerEntityEquipment> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_EQUIPMENT};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityEquipment> packets, Player observer, Entity entity) {
        WrapperPlayServerEntityEquipment originalPacket = packets.getOriginalPacket();
        // This list is only actually used if we construct a new packet, because otherwise we're wasting time
        List<Equipment> equipmentBeingSent = new ArrayList<>();

        // Prior to 1.16, only one equipment is sent. But this means the loop will only run once, which means it's a non-issue as only
        // one equipment will be written at most
        for (Equipment equipment : originalPacket.getEquipment()) {
            EquipmentSlot slot = equipment.getSlot();
            ItemStack itemInDisguise = disguise.getWatcher().getItemStack(DisguiseUtilities.getSlot(slot));
            com.github.retrooper.packetevents.protocol.item.ItemStack itemInPacket = equipment.getItem();

            // Workaround for this pending fix https://github.com/retrooper/packetevents/issues/869
            equipment.setItem(itemInPacket);

            if (itemInDisguise != null) {
                // If we haven't decided to send a new packet yet, then construct it
                if (packets.getPackets().contains(originalPacket)) {
                    packets.getPackets().remove(originalPacket);

                    packets.addPacket(new WrapperPlayServerEntityEquipment(originalPacket.getEntityId(), equipmentBeingSent));
                }

                itemInPacket = itemInDisguise.getType() == Material.AIR ? com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY :
                    DisguiseUtilities.fromBukkitItemStack(itemInDisguise);
                equipmentBeingSent.add(new Equipment(slot, itemInPacket));
            } else {
                equipmentBeingSent.add(equipment);
            }

            // If item not exists, either naturally or as part of the disguise
            if (itemInPacket.isEmpty()) {
                continue;
            }

            // If not raising the hand of the equipment slot this is
            if (!((slot == EquipmentSlot.MAIN_HAND && disguise.getWatcher().isMainHandRaised()) ||
                (slot == EquipmentSlot.OFF_HAND && disguise.getWatcher() instanceof LivingWatcher &&
                    ((LivingWatcher) disguise.getWatcher()).isOffhandRaised()))) {
                continue;
            }

            // Convert the datawatcher
            List<WatcherValue> list = new ArrayList<>();
            MetaIndex toUse = NmsVersion.v1_13.isSupported() ? MetaIndex.LIVING_META : MetaIndex.ENTITY_META;

            if (DisguiseConfig.isMetaPacketsEnabled()) {
                List<EntityData> data = ReflectionManager.getEntityWatcher(entity);
                byte b = (byte) toUse.getDefault();

                for (EntityData d : data) {
                    if (d.getIndex() != toUse.getIndex()) {
                        continue;
                    }

                    b = (byte) d.getValue();
                    break;
                }

                list.add(new WatcherValue(toUse, b, true));

                list = disguise.getWatcher().convert(observer, list);
            } else {
                for (WatcherValue obj : disguise.getWatcher().getWatchableObjects()) {
                    if (obj.getIndex() == toUse.getIndex()) {
                        list.add(obj);
                        break;
                    }
                }
            }

            // Construct the packets to return
            WrapperPlayServerEntityMetadata packetBlock = ReflectionManager.getMetadataPacket(entity.getEntityId(), list);

            list.forEach(v -> v.setValue(NmsVersion.v1_13.isSupported() ? (byte) 0 : (byte) ((byte) v.getValue() & ~(1 << 4))));

            // Make a packet to send the 'unblock'
            WrapperPlayServerEntityMetadata packetUnblock = ReflectionManager.getMetadataPacket(entity.getEntityId(), list);

            // Send the unblock before the itemstack change so that the 2nd metadata packet works. Why?
            // Scheduler delay.
            packets.getPackets().add(0, packetUnblock);
            packets.addPacket(packetBlock);
            // Silly mojang made the right clicking datawatcher value only valid for one use. So I have to reset it.
        }
    }
}
