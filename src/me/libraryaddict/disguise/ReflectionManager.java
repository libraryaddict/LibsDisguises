package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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

    public static String getEnumArt(Art art) {
        try {
            Class craftArt = Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".CraftArt");
            Object enumArt = craftArt.getMethod("BukkitToNotch", Art.class).invoke(null, art);
            for (Field field : enumArt.getClass().getFields()) {
                if (field.getType() == String.class) {
                    return (String) field.get(enumArt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getCraftSound(Sound sound) {
        try {
            Class c = Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".CraftSound");
            return (String) c.getMethod("getSound", Sound.class).invoke(null, sound);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getEntityInstance(String entityName) {
        try {
            Class entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld();
            if (entityName.equals("Human")) {
                entityObject = entityClass.getConstructor(getNmsClass("World"), String.class).newInstance(world, "LibsDisguises");
            } else {
                entityObject = entityClass.getConstructor(getNmsClass("World")).newInstance(world);
            }
            return entityObject;
        } catch (Exception e) {
            e.printStackTrace();
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
            return Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".CraftWorld").getMethod("getHandle")
                    .invoke(Bukkit.getWorlds().get(0));

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
