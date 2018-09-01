package me.libraryaddict.disguise.utilities.packetlisteners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class PacketListenerInventory extends PacketAdapter {
    private LibsDisguises libsDisguises;

    public PacketListenerInventory(LibsDisguises plugin) {
        super(plugin, ListenerPriority.HIGH, Server.SET_SLOT, Server.WINDOW_ITEMS, PacketType.Play.Client.HELD_ITEM_SLOT,
                PacketType.Play.Client.SET_CREATIVE_SLOT, PacketType.Play.Client.WINDOW_CLICK);

        libsDisguises = plugin;
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        if (event.isCancelled())
            return;

        final Player player = event.getPlayer();

        if (player.getName().contains("UNKNOWN[")) // If the player is temporary
            return;

        if (player instanceof com.comphenix.net.sf.cglib.proxy.Factory || player.getVehicle() != null) {
            return;
        }

        Disguise disguise = DisguiseAPI.getDisguise(player, player);

        // If player is disguised, views self disguises and has a inventory modifier
        if (disguise != null && disguise.isSelfDisguiseVisible()
                && (disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
            // If they are in creative and clicked on a slot
            if (event.getPacketType() == PacketType.Play.Client.SET_CREATIVE_SLOT) {
                int slot = event.getPacket().getIntegers().read(0);

                if (slot >= 5 && slot <= 8) {
                    if (disguise.isHidingArmorFromSelf()) {
                        int armorSlot = Math.abs((slot - 5) - 3);

                        org.bukkit.inventory.ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                        if (item != null && item.getType() != Material.AIR) {
                            PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                            StructureModifier<Object> mods = packet.getModifier();

                            mods.write(0, 0);
                            mods.write(1, slot);
                            mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));

                            try {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                            }
                            catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else if (slot >= 36 && slot <= 45) {
                    if (disguise.isHidingHeldItemFromSelf()) {
                        int currentSlot = player.getInventory().getHeldItemSlot();

                        if (slot + 36 == currentSlot || slot == 45) {
                            org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                            if (item != null && item.getType() != Material.AIR) {
                                PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                                StructureModifier<Object> mods = packet.getModifier();
                                mods.write(0, 0);
                                mods.write(1, slot);
                                mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));

                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                                }
                                catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            // If the player switched item, aka he moved from slot 1 to slot 2
            else if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_SLOT) {
                if (disguise.isHidingHeldItemFromSelf()) {
                    // From logging, it seems that both bukkit and nms uses the same thing for the slot switching.
                    // 0 1 2 3 - 8
                    // If the packet is coming, then I need to replace the item they are switching to
                    // As for the old item, I need to restore it.
                    org.bukkit.inventory.ItemStack currentlyHeld = player.getItemInHand();
                    // If his old weapon isn't air
                    if (currentlyHeld != null && currentlyHeld.getType() != Material.AIR) {
                        PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                        StructureModifier<Object> mods = packet.getModifier();

                        mods.write(0, 0);
                        mods.write(1, player.getInventory().getHeldItemSlot() + 36);
                        mods.write(2, ReflectionManager.getNmsItem(currentlyHeld));

                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                        }
                        catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                    org.bukkit.inventory.ItemStack newHeld = player.getInventory()
                            .getItem(event.getPacket().getIntegers().read(0));

                    // If his new weapon isn't air either!
                    if (newHeld != null && newHeld.getType() != Material.AIR) {
                        PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                        StructureModifier<Object> mods = packet.getModifier();

                        mods.write(0, 0);
                        mods.write(1, event.getPacket().getIntegers().read(0) + 36);
                        mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));

                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                        }
                        catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else if (event.getPacketType() == PacketType.Play.Client.WINDOW_CLICK) {
                int slot = event.getPacket().getIntegers().read(1);

                org.bukkit.inventory.ItemStack clickedItem;

                if (event.getPacket().getShorts().read(0) == 1) {
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
                }
                else {
                    // If its not a player inventory click
                    // Shift clicking is exempted for the item in hand..
                    if (event.getPacket().getIntegers().read(0) != 0) {
                        return;
                    }

                    clickedItem = player.getItemOnCursor();
                }

                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    // If the slot is a armor slot
                    if (slot >= 5 && slot <= 8) {
                        if (disguise.isHidingArmorFromSelf()) {
                            PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                            StructureModifier<Object> mods = packet.getModifier();

                            mods.write(0, 0);
                            mods.write(1, slot);
                            mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));

                            try {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                            }
                            catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                        // Else if its a hotbar slot
                    }
                    else if (slot >= 36 && slot <= 45) {
                        if (disguise.isHidingHeldItemFromSelf()) {
                            int currentSlot = player.getInventory().getHeldItemSlot();

                            // Check if the player is on the same slot as the slot that its setting
                            if (slot == currentSlot + 36 || slot == 45) {
                                PacketContainer packet = new PacketContainer(Server.SET_SLOT);

                                StructureModifier<Object> mods = packet.getModifier();
                                mods.write(0, 0);
                                mods.write(1, slot);
                                mods.write(2, ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));

                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                                }
                                catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
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
        if (player instanceof com.comphenix.net.sf.cglib.proxy.Factory || player.getVehicle() != null
                || event.getPacket().getIntegers().read(0) != 0) {
            return;
        }

        Disguise disguise = DisguiseAPI.getDisguise(player, player);

        if (disguise == null || !disguise.isSelfDisguiseVisible()
                || (!disguise.isHidingArmorFromSelf() && !disguise.isHidingHeldItemFromSelf())) {
            return;
        }

        // If the player is disguised, views self disguises and is hiding a item.

        // If the server is setting the slot
        // Need to set it to air if its in a place it shouldn't be.
        // Things such as picking up a item, spawned in item. Plugin sets the item. etc. Will fire this
        /**
         * Done
         */
        if (event.getPacketType() == Server.SET_SLOT) {
            // The raw slot
            // nms code has the start of the hotbar being 36.
            int slot = event.getPacket().getIntegers().read(1);

            // If the slot is a armor slot
            if (slot >= 5 && slot <= 8) {
                if (disguise.isHidingArmorFromSelf()) {
                    // Get the bukkit armor slot!
                    int armorSlot = Math.abs((slot - 5) - 3);

                    org.bukkit.inventory.ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                    if (item != null && item.getType() != Material.AIR) {
                        event.setPacket(event.getPacket().shallowClone());

                        event.getPacket().getModifier().write(2,
                                ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));
                    }
                }
                // Else if its a hotbar slot
            }
            else if (slot >= 36 && slot <= 45) {
                if (disguise.isHidingHeldItemFromSelf()) {
                    int currentSlot = player.getInventory().getHeldItemSlot();

                    // Check if the player is on the same slot as the slot that its setting
                    if (slot == currentSlot + 36 || slot == 45) {
                        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                        if (item != null && item.getType() != Material.AIR) {
                            event.setPacket(event.getPacket().shallowClone());
                            event.getPacket().getModifier().write(2,
                                    ReflectionManager.getNmsItem(new org.bukkit.inventory.ItemStack(Material.AIR)));
                        }
                    }
                }
            }
        }
        else if (event.getPacketType() == Server.WINDOW_ITEMS) {
            event.setPacket(event.getPacket().shallowClone());

            StructureModifier<List<ItemStack>> mods = event.getPacket().getItemListModifier();
            List<ItemStack> items = new ArrayList<>(mods.read(0));

            for (int slot = 0; slot < items.size(); slot++) {
                if (slot >= 5 && slot <= 8) {
                    if (disguise.isHidingArmorFromSelf()) {
                        // Get the bukkit armor slot!
                        int armorSlot = Math.abs((slot - 5) - 3);

                        ItemStack item = player.getInventory().getArmorContents()[armorSlot];

                        if (item != null && item.getType() != Material.AIR) {
                            items.set(slot, new ItemStack(Material.AIR));
                        }
                    }
                    // Else if its a hotbar slot
                }
                else if (slot >= 36 && slot <= 45) {
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
