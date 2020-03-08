package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import me.libraryaddict.disguise.events.DisguiseInteractEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

        if ("%%__USER__%%".equals(123 + "45")) {
            event.setCancelled(true);
        }

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

        switch (disguise.getType()) {
            case CAT:
            case WOLF:
            case SHEEP:
                doDyeable(observer, disguise);
                break;
            case MULE:
            case DONKEY:
            case HORSE:
            case ZOMBIE_HORSE:
            case SKELETON_HORSE:
                if (DisguiseConfig.isHorseSaddleable()) {
                    doSaddleable(observer, disguise);
                }

                break;
            case LLAMA:
            case TRADER_LLAMA:
                if (DisguiseConfig.isLlamaCarpetable()) {
                    doCarpetable(observer, disguise);
                }

                break;
            default:
                break;
        }
    }

    private void doSaddleable(Player observer, Disguise disguise) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // If this is something the player can dye the disguise with
                for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(),
                        observer.getInventory().getItemInOffHand()}) {

                    if (item == null || item.getType() != Material.SADDLE) {
                        continue;
                    }

                    AbstractHorseWatcher watcher = (AbstractHorseWatcher) disguise.getWatcher();

                    watcher.setSaddled(true);
                    break;
                }
            }
        }.runTask(LibsDisguises.getInstance());
    }

    private void doCarpetable(Player observer, Disguise disguise) {

        new BukkitRunnable() {
            @Override
            public void run() {
                // If this is something the player can dye the disguise with
                for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(),
                        observer.getInventory().getItemInOffHand()}) {
                    if (item == null || !item.getType().name().endsWith("_CARPET")) {
                        continue;
                    }

                    AnimalColor color = AnimalColor.getColorByItem(item);

                    if (color == null) {
                        continue;
                    }

                    LlamaWatcher llamaWatcher = (LlamaWatcher) disguise.getWatcher();

                    llamaWatcher.setSaddled(true);
                    llamaWatcher.setCarpet(color);
                    break;
                }
            }
        }.runTask(LibsDisguises.getInstance());
    }

    private void doDyeable(Player observer, Disguise disguise) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // If this is something the player can dye the disguise with
                for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(),
                        observer.getInventory().getItemInOffHand()}) {
                    if (item == null) {
                        continue;
                    }

                    AnimalColor color = AnimalColor.getColorByItem(item);

                    if (color == null) {
                        continue;
                    }

                    if (disguise.getType() == DisguiseType.SHEEP) {
                        SheepWatcher watcher = (SheepWatcher) disguise.getWatcher();

                        watcher.setColor(DisguiseConfig.isSheepDyeable() ? color.getDyeColor() : watcher.getColor());
                        break;
                    } else if (disguise.getType() == DisguiseType.WOLF) {
                        WolfWatcher watcher = (WolfWatcher) disguise.getWatcher();

                        watcher.setCollarColor(
                                DisguiseConfig.isWolfDyeable() ? color.getDyeColor() : watcher.getCollarColor());
                        break;
                    } else if (disguise.getType() == DisguiseType.CAT) {
                        CatWatcher watcher = (CatWatcher) disguise.getWatcher();

                        watcher.setCollarColor(
                                DisguiseConfig.isCatDyeable() ? color.getDyeColor() : watcher.getCollarColor());
                        break;
                    }
                }
            }
        }.runTask(LibsDisguises.getInstance());
    }
}
