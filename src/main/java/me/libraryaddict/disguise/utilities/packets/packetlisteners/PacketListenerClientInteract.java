package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.events.DisguiseInteractEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PacketListenerClientInteract extends PacketAdapter {
    public PacketListenerClientInteract(LibsDisguises plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.isCancelled())
            return;

        Player observer = event.getPlayer();

        if (observer.getName().contains("UNKNOWN[")) // If the player is temporary
            return;

        PacketContainer packet = event.getPacket();

        final Disguise disguise = DisguiseUtilities.getDisguise(event.getPlayer(), packet.getIntegers().read(0));

        if (disguise == null) {
            return;
        }

        if (disguise.getEntity() == observer) {
            // If it's a self-interact
            event.setCancelled(true);

            // The type of interact, we don't care the difference with "Interact_At" however as it's not
            // useful
            // for self disguises
            EnumWrappers.EntityUseAction interactType = packet.getEntityUseActions().read(0);
            final EquipmentSlot handUsed;

            // Attack has a null hand, which throws an error if you attempt to fetch
            // If the hand used wasn't their main hand
            if (interactType != EnumWrappers.EntityUseAction.ATTACK &&
                    packet.getHands().read(0) == EnumWrappers.Hand.OFF_HAND) {
                handUsed = EquipmentSlot.OFF_HAND;
            } else {
                handUsed = EquipmentSlot.HAND;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Fire self interact event
                    DisguiseInteractEvent selfEvent = new DisguiseInteractEvent((TargetedDisguise) disguise, handUsed,
                            interactType == EnumWrappers.EntityUseAction.ATTACK);

                    Bukkit.getPluginManager().callEvent(selfEvent);
                }
            }.runTask(LibsDisguises.getInstance());
        } else {
            Entity entity = disguise.getEntity();

            if (entity instanceof ExperienceOrb || entity instanceof Item || entity instanceof Arrow) {
                event.setCancelled(true);
            }
        }

        if (disguise.getType() != DisguiseType.SHEEP && disguise.getType() != DisguiseType.WOLF) {
            return;
        }

        // If this is something the player can dye the disguise with
        for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(),
                observer.getInventory().getItemInOffHand()}) {
            if (item == null) {
                continue;
            }

            AnimalColor color = AnimalColor.getColorByMaterial(item.getType());

            if (color == null) {
                continue;
            }

            if (disguise.getType() == DisguiseType.SHEEP) {
                SheepWatcher watcher = (SheepWatcher) disguise.getWatcher();

                watcher.setColor(DisguiseConfig.isSheepDyeable() ? color : watcher.getColor());
            } else {
                WolfWatcher watcher = (WolfWatcher) disguise.getWatcher();

                watcher.setCollarColor(DisguiseConfig.isWolfDyeable() ? color : watcher.getCollarColor());
            }
        }
    }
}
