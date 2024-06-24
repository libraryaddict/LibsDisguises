package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
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

import java.util.Random;

public class PacketListenerClientInteract extends SimplePacketListenerAbstract {
    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }

        Player observer = (Player) event.getPlayer();

        // If the player is temporary
        if (observer == null) {
            return;
        }

        if (!observer.isOp() && ("%%__USER__%%".equals(123 + "45") || LibsDisguises.getInstance().getUpdateChecker().isQuiet()) &&
            new Random().nextDouble() < 0.3) {
            event.setCancelled(true);
        }

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event.clone());

        if (packet.getEntityId() == DisguiseAPI.getSelfDisguiseId()) {
            // Self disguise
            event.setCancelled(true);
        } else if (DisguiseUtilities.isNotInteractable(packet.getEntityId())) {
            event.setCancelled(true);
        } else if (DisguiseUtilities.isSpecialInteract(packet.getEntityId()) && getHand(packet) == InteractionHand.OFF_HAND) {
            // If its an interaction that we should cancel, such as right clicking a wolf..
            // Honestly I forgot the reason.
            event.setCancelled(true);
        }

        if (!Bukkit.isPrimaryThread()) {
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

    private void handleSync(Player observer, WrapperPlayClientInteractEntity packet) {
        final Disguise disguise = DisguiseUtilities.getDisguise(observer, packet.getEntityId());

        if (disguise == null) {
            return;
        }

        if (disguise.getEntity() == observer) {
            // The type of interact, we don't care the difference with "Interact_At" however as it's not
            // useful
            // for self disguises
            final EquipmentSlot handUsed;
            final WrapperPlayClientInteractEntity.InteractAction interactType = packet.getAction();

            // Attack has a null hand, which throws an error if you attempt to fetch
            // If the hand used wasn't their main hand
            if (interactType != WrapperPlayClientInteractEntity.InteractAction.ATTACK && getHand(packet) == InteractionHand.OFF_HAND) {
                handUsed = EquipmentSlot.OFF_HAND;
            } else {
                handUsed = EquipmentSlot.HAND;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Fire self interact event
                    DisguiseInteractEvent selfEvent = new DisguiseInteractEvent((TargetedDisguise) disguise, handUsed,
                        interactType == WrapperPlayClientInteractEntity.InteractAction.ATTACK);

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
