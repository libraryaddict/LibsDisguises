package me.libraryaddict.disguise;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ReflectionManager {
    private static String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static Class itemClass;
    static {
        try {
            itemClass = Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".inventory.CraftItemStack");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getEntityInstance(String entityName) {
        try {
            Class entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld();
            if (entityName.equals("Human")) {
                entityObject = entityClass.getConstructor(world.getClass(), String.class).newInstance(world, "LibsDisguises");
            } else {
                entityObject = entityClass.getConstructor(world.getClass()).newInstance(world);
            }
            return entityObject;
        } catch (Exception e) {
        }
        return null;
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + bukkitVersion + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Float getSoundModifier(Object entity) {
        try {
            Method soundStrength = getNmsClass("EntityLiving").getDeclaredMethod("ba");
            // TODO Update this each update!
            soundStrength.setAccessible(true);
            return (Float) soundStrength.invoke(entity);
        } catch (Exception ex) {
        }
        return null;
    }

    private static Object getWorld() {
        try {
            return World.class.getMethod("getHandle").invoke(Bukkit.getWorlds().get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getNmsItem(ItemStack itemstack) {
        try {
            return itemClass.getMethod("asNMSCopy", getNmsClass("ItemStack")).invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) itemClass.getMethod("asBukkitCopy", ItemStack.class).invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
