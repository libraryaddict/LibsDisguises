package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
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

import java.util.ArrayList;
import java.util.List;

public class PacketListenerInventory extends PacketAdapter {
    private final LibsDisguises libsDisguises;

    public PacketListenerInventory(LibsDisguises plugin) {
        super(plugin, ListenerPriority.HIGH, Server.SET_SLOT, Server.WINDOW_ITEMS, PacketType.Play.Client.SET_CREATIVE_SLOT,
            PacketType.Play.Client.WINDOW_CLICK);

        libsDisguises = plugin;
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        if (event.isCancelled() || event.isPlayerTemporary()) {
            return;
        }

        final Player player = event.getPlayer();

        if (player == null || player.getVehicle() != null) {
            return;
        }

        if (event.isAsync()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    onPacketReceiving(event);
                }
            }.runTask(LibsDisguises.getInstance());
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
        if (event.getPacketType() == PacketType.Play.Client.SET_CREATIVE_SLOT) {
            int slot = event.getPacket().getIntegers().read(0);

            if (slot >= 5 && slot <= 8) {
                if (disguise.isHidingArmorFromSelf()) {
                    int armorSlot = Math.abs((slot - 5) - 3);

                    org.bukkit.inventory.ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                    if (item != null && item.getType() != Material.AIR && item.getType() != Material.ELYTRA) {
                        PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                        StructureModifier<Object> mods = packet.getModifier();

                        mods.write(0, 0);
                        mods.write(NmsVersion.v1_17.isSupported() ? 2 : 1, slot);

                        if (NmsVersion.v1_17.isSupported()) {
                            mods.write(1, ReflectionManager.getIncrementedStateId(player));
                        }

                        packet.getItemModifier().write(0, new ItemStack(Material.AIR));

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                    }
                }
            } else if (slot >= 36 && slot <= 45) {
                if (disguise.isHidingHeldItemFromSelf()) {
                    int currentSlot = player.getInventory().getHeldItemSlot();

                    if (slot + 36 == currentSlot || slot == 45) {
                        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                        if (item != null && item.getType() != Material.AIR) {
                            PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                            StructureModifier<Object> mods = packet.getModifier();
                            mods.write(0, 0);
                            mods.write(NmsVersion.v1_17.isSupported() ? 2 : 1, slot);

                            if (NmsVersion.v1_17.isSupported()) {
                                mods.write(1, ReflectionManager.getIncrementedStateId(player));
                            }

                            packet.getItemModifier().write(0, new ItemStack(Material.AIR));

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                        }
                    }
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.WINDOW_CLICK) {
            int slot = event.getPacket().getIntegers().read(NmsVersion.v1_17.isSupported() ? 2 : 1);

            org.bukkit.inventory.ItemStack clickedItem;
            int type;

            if (NmsVersion.v1_17.isSupported()) {
                type = event.getPacket().getIntegers().read(3);
            } else {
                type = event.getPacket().getShorts().read(0);
            }

            if (type == 1) {
                // Its a shift click
                clickedItem = event.getPacket().getItemModifier().read(0);

                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    // Rather than predict the clients actions
                    // Lets just update the entire inventory..
                    Bukkit.getScheduler().runTask(libsDisguises, new Runnable() {
                        public void run() {
                            player.updateInventory();
                        }
                    });
                }

                return;
            } else {
                // If its not a player inventory click
                // Shift clicking is exempted for the item in hand..
                if (event.getPacket().getIntegers().read(0) != 0) {
                    return;
                }

                clickedItem = player.getItemOnCursor();
            }

            if (clickedItem != null && clickedItem.getType() != Material.AIR && clickedItem.getType() != Material.ELYTRA) {
                // If the slot is a armor slot
                if (slot >= 5 && slot <= 8) {
                    if (disguise.isHidingArmorFromSelf()) {
                        PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                        StructureModifier<Object> mods = packet.getModifier();

                        mods.write(0, 0);
                        mods.write(NmsVersion.v1_17.isSupported() ? 2 : 1, slot);

                        if (NmsVersion.v1_17.isSupported()) {
                            mods.write(1, ReflectionManager.getIncrementedStateId(player));
                        }

                        packet.getItemModifier().write(0, new ItemStack(Material.AIR));

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                    }
                    // Else if its a hotbar slot
                } else if (slot >= 36 && slot <= 45) {
                    if (disguise.isHidingHeldItemFromSelf()) {
                        int currentSlot = player.getInventory().getHeldItemSlot();

                        // Check if the player is on the same slot as the slot that its setting
                        if (slot == currentSlot + 36 || slot == 45) {
                            PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                            StructureModifier<Object> mods = packet.getModifier();
                            mods.write(0, 0);
                            mods.write(NmsVersion.v1_17.isSupported() ? 2 : 1, slot);

                            if (NmsVersion.v1_17.isSupported()) {
                                mods.write(1, ReflectionManager.getIncrementedStateId(player));
                            }

                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();

        // If the inventory is the players inventory
        if (event.isPlayerTemporary() || player.getVehicle() != null || event.getPacket().getIntegers().read(0) != 0) {
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
            // The raw slot
            // nms code has the start of the hotbar being 36.
            int slot = event.getPacket().getIntegers().read(NmsVersion.v1_17.isSupported() ? 2 : 1);

            // If the slot is a armor slot
            if (slot >= 5 && slot <= 8) {
                if (disguise.isHidingArmorFromSelf()) {
                    // Get the bukkit armor slot!
                    int armorSlot = Math.abs((slot - 5) - 3);

                    org.bukkit.inventory.ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                    if (item != null && item.getType() != Material.AIR && item.getType() != Material.ELYTRA) {
                        event.setPacket(event.getPacket().shallowClone());

                        event.getPacket().getItemModifier().write(0, new ItemStack(Material.AIR));
                    }
                }
                // Else if its a hotbar slot
            } else if (slot >= 36 && slot <= 45) {
                if (disguise.isHidingHeldItemFromSelf()) {
                    int currentSlot = player.getInventory().getHeldItemSlot();

                    // Check if the player is on the same slot as the slot that its setting
                    if (slot == currentSlot + 36 || slot == 45) {
                        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                        if (item != null && item.getType() != Material.AIR) {
                            event.setPacket(event.getPacket().shallowClone());

                            event.getPacket().getItemModifier().write(0, new ItemStack(Material.AIR));
                        }
                    }
                }
            }
        } else if (event.getPacketType() == Server.WINDOW_ITEMS) {
            event.setPacket(event.getPacket().shallowClone());

            StructureModifier<List<ItemStack>> mods = event.getPacket().getItemListModifier();
            List<ItemStack> items = new ArrayList<>(mods.read(0));

            for (int slot = 0; slot < items.size(); slot++) {
                if (slot >= 5 && slot <= 8) {
                    if (disguise.isHidingArmorFromSelf()) {
                        // Get the bukkit armor slot!
                        int armorSlot = Math.abs((slot - 5) - 3);

                        ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                        if (item != null && item.getType() != Material.AIR && item.getType() != Material.ELYTRA) {
                            items.set(slot, new ItemStack(Material.AIR));
                        }
                    }
                    // Else if its a hotbar slot
                } else if (slot >= 36 && slot <= 45) {
                    if (disguise.isHidingHeldItemFromSelf()) {
                        int currentSlot = player.getInventory().getHeldItemSlot();

                        // Check if the player is on the same slot as the slot that its setting
                        if (slot == currentSlot + 36 || slot == 45) {
                            ItemStack item = player.getInventory().getItemInMainHand();

                            if (item != null && item.getType() != Material.AIR) {
                                items.set(slot, new ItemStack(Material.AIR));
                            }
                        }
                    }
                }
            }

            mods.write(0, items);
        }
    }
}
