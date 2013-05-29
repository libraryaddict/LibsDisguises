package me.libraryaddict.disguise.DisguiseTypes;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public enum WatcherValues {
    ARROW(16, (byte) 0), BAT(16, (byte) 0), BLAZE(16, (byte) 0), BOAT(19, 40, 17, 10, 18, 0), CAVE_SPIDER(), CHICKEN(12, 0), COW(
            12, 0), CREEPER(16, (byte) 0, 17, (byte) 0), DROPPED_ITEM(10, CraftItemStack.asNMSCopy(new ItemStack(1))), EGG(), ENDER_CRYSTAL(), ENDER_DRAGON(
            16, 300), ENDER_PEARL(), ENDER_SIGNAL(), ENDERMAN(16, (byte) 0, 17, (byte) 1, 18, (byte) 0), EXPERIENCE_ORB(), FALLING_BLOCK(), FIREBALL(), FIREWORKS(), FISHING_HOOK(), GHAST(
            16, (byte) 0), GIANT(), IRON_GOLEM(), ITEM_FRAME(2, 5, 3, (byte) 0), MAGMA_CUBE(16, (byte) 0, 18, (byte) 0), MINECART_CHEST(
            16, (byte) 0, 17, 0, 18, 1, 19, 0, 20, 0, 21, 6, 22, (byte) 0), MINECART_FURNACE(16, (byte) 0, 17, 0, 18, 1, 19, 0,
            20, 0, 21, 6, 22, (byte) 0), MINECART_HOPPER(16, (byte) 0, 17, 0, 18, 1, 19, 0, 20, 0, 21, 6, 22, (byte) 0), MINECART_MOB_SPAWNER(
            16, (byte) 0, 17, 0, 18, 1, 19, 0, 20, 0, 21, 6, 22, (byte) 0), MINECART_RIDEABLE(16, (byte) 0, 17, 0, 18, 1, 19, 0,
            20, 0, 21, 6, 22, (byte) 0), MINECART_TNT(16, (byte) 0, 17, 0, 18, 1, 19, 0, 20, 0, 21, 6, 22, (byte) 0), MUSHROOM_COW(
            12, 0), OCELOT(12, 0, 16, (byte) 0, 17, "", 18, (byte) 0), PAINTING(), PIG(12, 0, 16, (byte) 0), PIG_ZOMBIE(12,
            (byte) 0), PLAYER(8, 0, 9, (byte) 0, 10, (byte) 0, 13, 0), PRIMED_TNT(), SHEEP(12, 0, 16, (byte) 0), SILVERFISH(), SKELETON(
            13, (byte) 0), SLIME(16, (byte) 0, 18, (byte) 0), SMALL_FIREBALL(), SNOWBALL(), SNOWMAN(), SPIDER(), SPLASH_POTION(), SQUID(), THROWN_EXP_BOTTLE(), VILLAGER(
            16, 0), WITCH(), WITHER(16, 300), WITHER_SKELETON(13, (byte) 1), WITHER_SKULL(), WOLF(16, (byte) 0, 17, "", 18, 8,
            19, (byte) 0, 20, (byte) 14), ZOMBIE(12, (byte) 0, 13, (byte) 0);
    private HashMap<Integer, Object> values = new HashMap<Integer, Object>();

    private WatcherValues(Object... obj) {
        for (int i = 0; i < obj.length; i += 2) {
            if (!values.containsKey(obj))
                values.put((Integer) obj[i], obj[i + 1]);
            else
                try {
                    throw new Exception("Values in WatcherValues already contains " + obj + " for " + this.name());
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    public Object getValue(int no) {
        return values.get(no);
    }

    public Set<Integer> getValues() {
        return values.keySet();
    }

}
