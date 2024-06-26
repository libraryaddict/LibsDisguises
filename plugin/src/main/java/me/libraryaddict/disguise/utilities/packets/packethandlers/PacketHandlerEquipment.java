package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
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

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerEquipment implements IPacketHandler<WrapperPlayServerEntityEquipment> {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        return new PacketTypeCommon[]{PacketType.Play.Server.ENTITY_EQUIPMENT};
    }

    @Override
    public void handle(Disguise disguise, LibsPackets<WrapperPlayServerEntityEquipment> packets, Player observer, Entity entity) {
        // Else if the disguise is updating equipment
        WrapperPlayServerEntityEquipment packet = packets.getOriginalPacket();
        WrapperPlayServerEntityEquipment toModify = null;
        List<Equipment> newSlots = new ArrayList<>();

        for (Equipment equipment : packet.getEquipment()) {
            EquipmentSlot slot = equipment.getSlot();
            ItemStack sItem = disguise.getWatcher().getItemStack(ReflectionManager.getSlot(slot));

            // If it's not 1.16, then there should only be 1 equipment in the list which means only 1 equipment added to the list!
            if (sItem != null) {
                if (toModify == null) {
                    if (packets.getPackets().size() > 1) {
                        packets.getPackets().remove(1);
                    } else {
                        packets.clear();
                    }

                    toModify = new WrapperPlayServerEntityEquipment(packet.getEntityId(), newSlots);
                    packets.addPacket(toModify);
                }

                newSlots.add(new Equipment(slot,
                    sItem.getType() == Material.AIR ? com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY :
                        SpigotConversionUtil.fromBukkitItemStack(sItem)));

            } else {
                newSlots.add(equipment);
                sItem = SpigotConversionUtil.toBukkitItemStack(equipment.getItem());
            }

            // If item not exists, either naturally or as part of the disguise
            if (sItem == null || sItem.getType() == Material.AIR) {
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
