package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ReflectionManager {
    private static boolean after17 = true;
    private static String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static Class itemClass;
    private static Method soundMethod;
    static {
        for (Method method : getNmsClass("EntityLiving").getDeclaredMethods()) {
            try {
                if (method.getReturnType() == float.class && Modifier.isProtected(method.getModifiers())
                        && method.getParameterTypes().length == 0) {
                    Object entity = createEntityInstance("Pig");
                    method.setAccessible(true);
                    method.invoke(entity);
                    Field random = getNmsClass("Entity").getDeclaredField("random");
                    random.setAccessible(true);
                    random.set(entity, null);
                    method.setAccessible(true);
                    try {
                        method.invoke(entity);
                    } catch (Exception ex) {
                        soundMethod = method;
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            itemClass = getCraftClass("inventory.CraftItemStack");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bukkitVersion.startsWith("1.")) {
            try {
                if (Integer.parseInt(bukkitVersion.split("\\.")[1]) < 7) {
                    after17 = false;
                }
            } catch (Exception ex) {

            }
        }
    }public static Object createEntityInstance(String entityName) {
        try {
            Class entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld(Bukkit.getWorlds().get(0));
            if (entityName.equals("Player")) {
                Object minecraftServer = getNmsClass("MinecraftServer").getMethod("getServer").invoke(null);
                Object playerinteractmanager = getNmsClass("PlayerInteractManager").getConstructor(getNmsClass("World"))
                        .newInstance(world);
                if (!isAfter17()) {
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("World"), String.class,
                            playerinteractmanager.getClass()).newInstance(minecraftServer, world, "LibsDisguises",
                            playerinteractmanager);
                } else {
                    Object gameProfile = getGameProfile("LibsDisguises");
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                            gameProfile.getClass(), playerinteractmanager.getClass()).newInstance(minecraftServer, world,
                            gameProfile, playerinteractmanager);
                }
            } else {
                entityObject = entityClass.getConstructor(getNmsClass("World")).newInstance(world);
            }
            return entityObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        try {
            Entity bukkitEntity = (Entity) ReflectionManager.getNmsClass("Entity").getMethod("getBukkitEntity").invoke(nmsEntity);
            return bukkitEntity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) itemClass.getMethod("asBukkitCopy", getNmsClass("ItemStack")).invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + bukkitVersion + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCraftSound(Sound sound) {
        try {
            Class c = getCraftClass("CraftSound");
            return (String) c.getMethod("getSound", Sound.class).invoke(null, sound);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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

    public static Object getGameProfile(String playerName) {
        try {
            return Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile").getConstructor(String.class, String.class)
                    .newInstance(playerName, playerName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + bukkitVersion + "." + className);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return null;
    }

    public static Object getNmsEntity(Entity entity) {
        try {
            return getCraftClass("entity.CraftEntity").getMethod("getHandle").invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getNmsItem(ItemStack itemstack) {
        try {
            return itemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Float getSoundModifier(Object entity) {
        try {
            soundMethod.setAccessible(true);
            return (Float) soundMethod.invoke(entity);
        } catch (Exception ex) {
        }
        return null;
    }

    public static Object getWorld(World world) {
        try {
            return getCraftClass("CraftWorld").getMethod("getHandle").invoke(world);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAfter17() {
        return after17;
    }

}
