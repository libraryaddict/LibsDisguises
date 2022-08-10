package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LlamaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.events.DisguiseInteractEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PacketListenerClientInteract extends PacketAdapter {
    public PacketListenerClientInteract(LibsDisguises plugin) {
        super(new AdapterParameteters().optionAsync().plugin(plugin).types(PacketType.Play.Client.USE_ENTITY));
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player observer = event.getPlayer();

        if (observer == null || event.isPlayerTemporary() || observer.getName().contains("UNKNOWN[")) // If the player is temporary
        {
            return;
        }

        if (!observer.isOp() && ("%%__USER__%%".equals(123 + "45") || LibsDisguises.getInstance().getUpdateChecker().isGoSilent())) {
            event.setCancelled(true);
        }

        PacketContainer packet = event.getPacket();

        if (packet.getIntegers().read(0) == DisguiseAPI.getSelfDisguiseId()) {
            // Self disguise
            event.setCancelled(true);
        } else if (DisguiseUtilities.isNotInteractable(packet.getIntegers().read(0))) {
            event.setCancelled(true);
        } else if (DisguiseUtilities.isSpecialInteract(packet.getIntegers().read(0)) && getHand(packet) == EnumWrappers.Hand.OFF_HAND) {
            // If its an interaction that we should cancel, such as right clicking a wolf..
            // Honestly I forgot the reason.
            event.setCancelled(true);
        }

        if (event.isAsync()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    handleSync(observer, packet);
                }
            }.runTask(LibsDisguises.getInstance());
        } else {
            handleSync(observer, packet);
        }
    }

    private EnumWrappers.Hand getHand(PacketContainer packet) {
        if (!NmsVersion.v1_17.isSupported()) {
            if (getInteractType(packet) != EnumWrappers.EntityUseAction.ATTACK) {
                return packet.getHands().read(0);
            }

            return EnumWrappers.Hand.MAIN_HAND;
        }

        WrappedEnumEntityUseAction action = packet.getEnumEntityUseActions().read(0);

        if (action.getAction() == EnumWrappers.EntityUseAction.ATTACK) {
            return EnumWrappers.Hand.MAIN_HAND;
        }

        return action.getHand();
    }

    private EnumWrappers.EntityUseAction getInteractType(PacketContainer packet) {
        if (!NmsVersion.v1_17.isSupported()) {
            return packet.getEntityUseActions().read(0);
        }

        return packet.getEnumEntityUseActions().read(0).getAction();
    }

    private void handleSync(Player observer, PacketContainer packet) {
        final Disguise disguise = DisguiseUtilities.getDisguise(observer, packet.getIntegers().read(0));

        if (disguise == null) {
            return;
        }

        if (disguise.getEntity() == observer) {
            // The type of interact, we don't care the difference with "Interact_At" however as it's not
            // useful
            // for self disguises
            final EquipmentSlot handUsed;
            final EnumWrappers.EntityUseAction interactType = getInteractType(packet);

            // Attack has a null hand, which throws an error if you attempt to fetch
            // If the hand used wasn't their main hand
            if (interactType != EnumWrappers.EntityUseAction.ATTACK && getHand(packet) == EnumWrappers.Hand.OFF_HAND) {
                handUsed = EquipmentSlot.OFF_HAND;
            } else {
                handUsed = EquipmentSlot.HAND;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Fire self interact event
                    DisguiseInteractEvent selfEvent =
                        new DisguiseInteractEvent((TargetedDisguise) disguise, handUsed, interactType == EnumWrappers.EntityUseAction.ATTACK);

                    Bukkit.getPluginManager().callEvent(selfEvent);
                }
            }.runTask(LibsDisguises.getInstance());
        }

        switch (disguise.getType()) {
            case AXOLOTL:
                // They can't be picked up by a bucket sir if they are fake
                if (!(disguise.getEntity() instanceof Axolotl)) {
                    DisguiseUtilities.refreshTrackers((TargetedDisguise) disguise);
                    observer.updateInventory(); // Remove their fake bucket
                }
                break;
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
                for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(), observer.getInventory().getItemInOffHand()}) {

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
                for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(), observer.getInventory().getItemInOffHand()}) {
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
                for (ItemStack item : new ItemStack[]{observer.getInventory().getItemInMainHand(), observer.getInventory().getItemInOffHand()}) {
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

                        watcher.setCollarColor(DisguiseConfig.isWolfDyeable() ? color.getDyeColor() : watcher.getCollarColor());
                        break;
                    } else if (disguise.getType() == DisguiseType.CAT) {
                        CatWatcher watcher = (CatWatcher) disguise.getWatcher();

                        watcher.setCollarColor(DisguiseConfig.isCatDyeable() ? color.getDyeColor() : watcher.getCollarColor());
                        break;
                    }
                }
            }
        }.runTask(LibsDisguises.getInstance());
    }
}
