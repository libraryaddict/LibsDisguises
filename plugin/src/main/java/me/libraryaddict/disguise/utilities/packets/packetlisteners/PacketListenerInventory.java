package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PacketListenerInventory extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        try {
            if (event.isCancelled()) {
                return;
            }

            if (event.getPacketType() != Client.CLICK_WINDOW && event.getPacketType() != Client.CREATIVE_INVENTORY_ACTION) {
                return;
            }
            if (!Bukkit.isPrimaryThread()) {
                PacketPlayReceiveEvent cloned = event.clone();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        onPacketPlayReceive(cloned);
                    }
                }.runTask(LibsDisguises.getInstance());
                return;
            }

            final Player player = (Player) event.getPlayer();

            if (player == null || player.getVehicle() != null) {
                return;
            }

            if (!DisguiseConfig.isHidingCreativeEquipmentFromSelf() && player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            Disguise disguise = DisguiseAPI.getDisguise(player, player);

            // If player isn't disguise, isn't self disguised, or isn't hiding items from themselves
            // If player is disguised, views self disguises and has a inventory modifier
            if (disguise == null || !DisguiseUtilities.getSelfDisguised().contains(player.getUniqueId()) ||
                (!disguise.isHidingArmorFromSelf() && !disguise.isHidingHeldItemFromSelf())) {
                return;
            }

            // If they are in creative and clicked on a slot
            if (event.getPacketType() == Client.CREATIVE_INVENTORY_ACTION) {
                WrapperPlayClientCreativeInventoryAction wrapper = new WrapperPlayClientCreativeInventoryAction(event);

                int slot = wrapper.getSlot();

                if (slot >= 5 && slot <= 8) {
                    if (disguise.isHidingArmorFromSelf()) {
                        int armorSlot = Math.abs((slot - 5) - 3);

                        org.bukkit.inventory.ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                        if (DisguiseUtilities.shouldBeHiddenSelfDisguise(item) && item.getType() != Material.ELYTRA) {
                            int stateId = NmsVersion.v1_17.isSupported() ? ReflectionManager.getIncrementedStateId(player) : 0;

                            WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(0, stateId, slot,
                                com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);

                            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
                        }
                    }
                } else if (slot >= 36 && slot <= 45) {
                    if (disguise.isHidingHeldItemFromSelf()) {
                        int currentSlot = player.getInventory().getHeldItemSlot();

                        if (slot + 36 == currentSlot || slot == 45) {
                            org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                            if (DisguiseUtilities.shouldBeHiddenSelfDisguise(item)) {
                                int stateId = NmsVersion.v1_17.isSupported() ? ReflectionManager.getIncrementedStateId(player) : 0;

                                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(0, stateId, slot,
                                    com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);

                                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
                            }
                        }
                    }
                }
            } else if (event.getPacketType() == Client.CLICK_WINDOW) {
                WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);

                int slot = packet.getSlot();

                com.github.retrooper.packetevents.protocol.item.ItemStack clickedItem;
                WindowClickType type = packet.getWindowClickType();

                if (type == WindowClickType.QUICK_MOVE) {
                    // Its a shift click
                    clickedItem = packet.getCarriedItemStack();

                    // We don't look at if it should be hidden or not, we just want to prevent mis-synced inventory
                    if (clickedItem != null && !clickedItem.isEmpty()) {
                        // Rather than predict the clients actions
                        // Lets just update the entire inventory..
                        Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), new Runnable() {
                            public void run() {
                                player.updateInventory();
                            }
                        });
                    }

                    return;
                } else {
                    // If its not a player inventory click
                    // Shift clicking is exempted for the item in hand..
                    if (packet.getWindowId() != 0) {
                        return;
                    }

                    clickedItem = DisguiseUtilities.fromBukkitItemStack(player.getItemOnCursor());
                }

                if (DisguiseUtilities.shouldBeHiddenSelfDisguise(clickedItem) && clickedItem.getType() != ItemTypes.ELYTRA) {
                    // If the slot is a armor slot
                    if (slot >= 5 && slot <= 8) {
                        if (disguise.isHidingArmorFromSelf()) {
                            int stateId = NmsVersion.v1_17.isSupported() ? ReflectionManager.getIncrementedStateId(player) : 0;

                            WrapperPlayServerSetSlot newPacket = new WrapperPlayServerSetSlot(0, stateId, slot,
                                com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);

                            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, newPacket);
                        }
                        // Else if its a hotbar slot
                    } else if (slot >= 36 && slot <= 45) {
                        if (disguise.isHidingHeldItemFromSelf()) {
                            int currentSlot = player.getInventory().getHeldItemSlot();

                            // Check if the player is on the same slot as the slot that its setting
                            if (slot == currentSlot + 36 || slot == 45) {
                                int stateId = NmsVersion.v1_17.isSupported() ? ReflectionManager.getIncrementedStateId(player) : 0;

                                WrapperPlayServerSetSlot newPacket = new WrapperPlayServerSetSlot(0, stateId, slot,
                                    com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);

                                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, newPacket);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        try {
            if (event.isCancelled()) {
                return;
            }

            if (event.getPacketType() != Server.SET_SLOT && event.getPacketType() != Server.WINDOW_ITEMS) {
                return;
            }

            Player player = (Player) event.getPlayer();

            if (player == null || player.getVehicle() != null) {
                return;
            }

            if (!DisguiseConfig.isHidingCreativeEquipmentFromSelf() && player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            Disguise disguise = DisguiseAPI.getDisguise(player, player);

            if (disguise == null || !DisguiseUtilities.getSelfDisguised().contains(player.getUniqueId()) ||
                (!disguise.isHidingArmorFromSelf() && !disguise.isHidingHeldItemFromSelf())) {
                return;
            }

            // If the player is disguised, views self disguises and is hiding a item.

            // If the server is setting the slot
            // Need to set it to air if its in a place it shouldn't be.
            // Things such as picking up a item, spawned in item. Plugin sets the item. etc. Will fire this
            if (event.getPacketType() == Server.SET_SLOT) {
                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);

                // If the inventory is the players inventory
                if (packet.getWindowId() != 0) {
                    return;
                }

                // The raw slot
                // nms code has the start of the hotbar being 36.
                int slot = packet.getSlot();

                // If the slot is a armor slot
                if (slot >= 5 && slot <= 8) {
                    if (disguise.isHidingArmorFromSelf()) {
                        // Get the bukkit armor slot!
                        int armorSlot = Math.abs((slot - 5) - 3);

                        org.bukkit.inventory.ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                        if (DisguiseUtilities.shouldBeHiddenSelfDisguise(item) && item.getType() != Material.ELYTRA) {
                            packet.setItem(com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);
                            event.markForReEncode(true);
                        }
                    }
                    // Else if its a hotbar slot
                } else if (slot >= 36 && slot <= 45) {
                    if (disguise.isHidingHeldItemFromSelf()) {
                        int currentSlot = player.getInventory().getHeldItemSlot();

                        // Check if the player is on the same slot as the slot that its setting
                        if (slot == currentSlot + 36 || slot == 45) {
                            org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                            if (DisguiseUtilities.shouldBeHiddenSelfDisguise(item)) {
                                packet.setItem(com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);
                                event.markForReEncode(true);
                            }
                        }
                    }
                }
            } else if (event.getPacketType() == Server.WINDOW_ITEMS) {
                WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);

                // If the inventory is the players inventory
                if (packet.getWindowId() != 0) {
                    return;
                }

                List<com.github.retrooper.packetevents.protocol.item.ItemStack> items = packet.getItems();

                for (int slot = 0; slot < items.size(); slot++) {
                    if (slot >= 5 && slot <= 8) {
                        if (disguise.isHidingArmorFromSelf()) {
                            // Get the bukkit armor slot!
                            int armorSlot = Math.abs((slot - 5) - 3);

                            ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                            if (DisguiseUtilities.shouldBeHiddenSelfDisguise(item) && item.getType() != Material.ELYTRA) {
                                items.set(slot, com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);
                            }
                        }
                        // Else if its a hotbar slot
                    } else if (slot >= 36 && slot <= 45) {
                        if (disguise.isHidingHeldItemFromSelf()) {
                            int currentSlot = player.getInventory().getHeldItemSlot();

                            // Check if the player is on the same slot as the slot that its setting
                            if (slot == currentSlot + 36 || slot == 45) {
                                ItemStack item = player.getInventory().getItemInMainHand();

                                if (DisguiseUtilities.shouldBeHiddenSelfDisguise(item)) {
                                    items.set(slot, com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            event.setCancelled(true);
        }
    }
}
