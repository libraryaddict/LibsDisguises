package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AllayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LlamaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.events.DisguiseInteractEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class PacketListenerClientInteract extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        boolean isInteract = event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY;
        boolean isAttackPacket = event.getPacketType() == PacketType.Play.Client.ATTACK;

        if (event.isCancelled() || (!isInteract && !isAttackPacket)) {
            return;
        }

        Player observer = event.getPlayer();

        // If the player is temporary
        if (observer == null) {
            return;
        }

        if (!observer.isOp() && ("%%__USER__%%".equals(123 + "45") || LibsDisguises.getInstance().getUpdateChecker().isQuiet()) &&
            new Random().nextDouble() < 0.3) {
            event.setCancelled(true);
        }

        int entityId;
        boolean isAttack = isAttackPacket;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        PacketWrapper<?> wrapper;

        if (isInteract) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            entityId = packet.getEntityId();
            isAttack = packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
            hand = getHand(packet);
            wrapper = packet;
        } else {
            WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
            entityId = packet.getEntityId();
            wrapper = packet;
        }

        Integer remapped = DisguiseUtilities.getRemappedEntityIds().get(entityId);

        if (remapped != null) {
            if (isInteract) {
                ((WrapperPlayClientInteractEntity) wrapper).setEntityId(remapped);
            } else {
                ((WrapperPlayClientAttack) wrapper).setEntityId(remapped);
            }

            event.markForReEncode(true);
            entityId = remapped;
        }

        if (entityId == DisguiseAPI.getSelfDisguiseId() || entityId == observer.getEntityId()) {
            // Self disguise
            event.setCancelled(true);
        } else if (DisguiseUtilities.isNotInteractable(entityId)) {
            event.setCancelled(true);
        } else if (hand == InteractionHand.OFF_HAND && DisguiseUtilities.isSpecialInteract(entityId)) {
            // If its an interaction that we should cancel, such as right clicking a wolf..
            // Honestly I forgot the reason.
            event.setCancelled(true);
        }

        int finalEntityId = entityId;
        boolean finalIsAttack = isAttack;
        InteractionHand finalHand = hand;

        if (!Bukkit.isPrimaryThread()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    handleSync(observer, finalEntityId, finalIsAttack, finalHand);
                }
            }.runTask(LibsDisguises.getInstance());
        } else {
            handleSync(observer, finalEntityId, finalIsAttack, finalHand);
        }
    }

    private InteractionHand getHand(WrapperPlayClientInteractEntity packet) {
        if (!NmsVersion.v1_17.isSupported()) {
            if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return packet.getHand();
            }

            return InteractionHand.MAIN_HAND;
        }

        if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return InteractionHand.MAIN_HAND;
        }

        return packet.getHand();
    }

    private void handleSync(Player observer, int entityId, boolean isAttack, InteractionHand hand) {
        final Disguise disguise = DisguiseUtilities.getDisguise(observer, entityId);

        if (disguise == null) {
            return;
        }

        if (disguise.getEntity() == observer) {
            // The type of interact, we don't care the difference with "Interact_At" however as it's not
            // useful for self disguises
            final EquipmentSlot handUsed;

            // Attack has a null hand, which throws an error if you attempt to fetch
            // If the hand used wasn't their main hand
            if (!isAttack && hand == InteractionHand.OFF_HAND) {
                handUsed = EquipmentSlot.OFF_HAND;
            } else {
                handUsed = EquipmentSlot.HAND;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Fire self interact event
                    DisguiseInteractEvent selfEvent = new DisguiseInteractEvent((TargetedDisguise) disguise, handUsed, isAttack);
                    Bukkit.getPluginManager().callEvent(selfEvent);
                }
            }.runTask(LibsDisguises.getInstance());
        }

        if (isAttack) {
            return;
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
            case ALLAY:
                doAllay(observer, disguise, hand);
                break;
            default:
                break;
        }
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private void doAllay(Player observer, Disguise disguise, InteractionHand hand) {
        new BukkitRunnable() {
            @Override
            public void run() {
                AllayWatcher watcher = (AllayWatcher) disguise.getWatcher();
                ItemStack playerHand = hand == InteractionHand.MAIN_HAND ? observer.getInventory().getItemInMainHand() :
                    observer.getInventory().getItemInOffHand();
                ItemStack watcherItem = watcher.getItemInMainHand();
                ItemStack playerSeesItem = watcherItem == null && disguise.getEntity() instanceof LivingEntity ?
                    ((LivingEntity) disguise.getEntity()).getEquipment().getItemInMainHand() : watcherItem;
                boolean allayHoldingNothing = isEmpty(playerSeesItem);

                // Nothing visual changed, do nothing.
                if (isEmpty(playerHand) == allayHoldingNothing) {
                    return;
                }

                // Ensure player knows they still have an item
                observer.updateInventory();

                // Set/update the item on the watcher
                if (DisguiseConfig.isAllayItemSwitchable()) {
                    // Update the held item!
                    watcher.setItemInMainHand(playerHand);
                } else {
                    if (watcherItem == null || watcherItem.getType() == Material.AIR) {
                        watcher.setItemInMainHand(new ItemStack(Material.STICK));
                    }

                    watcher.setItemInMainHand(watcherItem);
                }
            }
        }.runTask(LibsDisguises.getInstance());
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

                        DyeColor toSet = DisguiseConfig.isSheepDyeable() ? color.getDyeColor() :
                            watcher.hasValue(MetaIndex.SHEEP_WOOL) ? watcher.getColor() : null;

                        watcher.setColor(toSet);
                        break;
                    } else if (disguise.getType() == DisguiseType.WOLF) {
                        WolfWatcher watcher = (WolfWatcher) disguise.getWatcher();
                        DyeColor toSet = DisguiseConfig.isWolfDyeable() ? color.getDyeColor() :
                            watcher.hasValue(MetaIndex.WOLF_COLLAR) ? watcher.getCollarColor() : null;

                        watcher.setCollarColor(toSet);
                        break;
                    } else if (disguise.getType() == DisguiseType.CAT) {
                        CatWatcher watcher = (CatWatcher) disguise.getWatcher();
                        DyeColor toSet = DisguiseConfig.isCatDyeable() ? color.getDyeColor() :
                            watcher.hasValue(MetaIndex.CAT_COLLAR) ? watcher.getCollarColor() : null;

                        watcher.setCollarColor(toSet);
                        break;
                    }
                }
            }
        }.runTask(LibsDisguises.getInstance());
    }
}
