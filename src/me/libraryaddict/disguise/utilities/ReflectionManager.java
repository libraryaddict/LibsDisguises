package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReflectionManager {
    public enum LibVersion {
        V1_6, V1_7;
        private static LibVersion currentVersion;
        static {
            if (getBukkitVersion().startsWith("v1_")) {
                try {
                    int version = Integer.parseInt(getBukkitVersion().split("_")[1]);
                    if (version == 7) {
                        currentVersion = LibVersion.V1_7;
                    } else {
                        if (version < 7) {
                            currentVersion = LibVersion.V1_6;
                        } else {
                            currentVersion = LibVersion.V1_7;
                        }
                    }
                } catch (Exception ex) {

                }
            }
        }

        public static LibVersion getGameVersion() {
            return currentVersion;
        }

        public static boolean is1_6() {
            return getGameVersion() == V1_6;
        }

        public static boolean is1_7() {
            return getGameVersion() == V1_7;
        }
    }

    private static String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static Method damageAndIdleSoundMethod;
    private static Class itemClass;
    private static Field pingField;

    static {
        for (Method method : getNmsClass("EntityLiving").getDeclaredMethods()) {
            try {
                if (method.getReturnType() == float.class && Modifier.isProtected(method.getModifiers())
                        && method.getParameterTypes().length == 0) {
                    Object entity = createEntityInstance("Cow");
                    method.setAccessible(true);
                    float value = (Float) method.invoke(entity);
                    if (value == 0.4F) {
                        damageAndIdleSoundMethod = method;
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            itemClass = getCraftClass("inventory.CraftItemStack");
            pingField = getNmsClass("EntityPlayer").getField("ping");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object createEntityInstance(String entityName) {
        try {
            Class entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld(Bukkit.getWorlds().get(0));
            if (entityName.equals("Player")) {
                Object minecraftServer = getNmsClass("MinecraftServer").getMethod("getServer").invoke(null);
                Object playerinteractmanager = getNmsClass("PlayerInteractManager").getConstructor(getNmsClass("World"))
                        .newInstance(world);
                if (LibVersion.is1_7()) {
                    Object gameProfile = getGameProfile(null, "LibsDisguises");
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                            gameProfile.getClass(), playerinteractmanager.getClass()).newInstance(minecraftServer, world,
                            gameProfile, playerinteractmanager);
                } else {
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("World"), String.class,
                            playerinteractmanager.getClass()).newInstance(minecraftServer, world, "LibsDisguises",
                            playerinteractmanager);
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

    public static FakeBoundingBox getBoundingBox(Entity entity) {
        try {
            Object boundingBox = getNmsClass("Entity").getField("boundingBox").get(getNmsEntity(entity));
            double x = 0, y = 0, z = 0;
            int stage = 0;
            for (Field field : boundingBox.getClass().getFields()) {
                if (field.getType().getSimpleName().equals("double")) {
                    stage++;
                    switch (stage) {
                    case 1:
                        x -= field.getDouble(boundingBox);
                        break;
                    case 2:
                        y -= field.getDouble(boundingBox);
                        break;
                    case 3:
                        z -= field.getDouble(boundingBox);
                        break;
                    case 4:
                        x += field.getDouble(boundingBox);
                        break;
                    case 5:
                        y += field.getDouble(boundingBox);
                        break;
                    case 6:
                        z += field.getDouble(boundingBox);
                        break;
                    default:
                        throw new Exception("Error while setting the bounding box, more doubles than I thought??");
                    }
                }
            }
            return new FakeBoundingBox(x, y, z);

        } catch (Exception ex) {
            ex.printStackTrace();
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

    public static String getBukkitVersion() {
        return bukkitVersion;
    }

    public static Class getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getBukkitVersion() + "." + className);
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
            Class craftArt = Class.forName("org.bukkit.craftbukkit." + getBukkitVersion() + ".CraftArt");
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

    public static Object getGameProfile(UUID uuid, String playerName) {
        try {
            try {
                return Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile")
                        .getConstructor(UUID.class, String.class)
                        .newInstance(uuid != null ? uuid : DisguiseUtilities.getUUID(), playerName);
            } catch (NoSuchMethodException ex) {
                return Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile")
                        .getConstructor(String.class, String.class).newInstance(uuid != null ? uuid.toString() : "", playerName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + className);
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

    public static double getPing(Player player) {
        try {
            return (double) pingField.getInt(ReflectionManager.getNmsEntity(player));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0D;
    }

    public static float[] getSize(Entity entity) {
        try {
            float length = getNmsClass("Entity").getField("length").getFloat(getNmsEntity(entity));
            float width = getNmsClass("Entity").getField("width").getFloat(getNmsEntity(entity));
            float height = getNmsClass("Entity").getField("height").getFloat(getNmsEntity(entity));
            return new float[] { length, width, height };
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Float getSoundModifier(Object entity) {
        try {
            damageAndIdleSoundMethod.setAccessible(true);
            return (Float) damageAndIdleSoundMethod.invoke(entity);
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

    public static void setAllowSleep(Player player) {
        try {
            Object nmsEntity = getNmsEntity(player);
            Object connection = nmsEntity.getClass().getField("playerConnection").get(nmsEntity);
            Field check = connection.getClass().getField("checkMovement");
            check.setBoolean(connection, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setBoundingBox(Entity entity, FakeBoundingBox newBox, float[] entitySize) {
        try {
            Object boundingBox = getNmsClass("Entity").getField("boundingBox").get(getNmsEntity(entity));
            int stage = 0;
            Location loc = entity.getLocation();
            for (Field field : boundingBox.getClass().getFields()) {
                if (field.getType().getSimpleName().equals("double")) {
                    stage++;
                    switch (stage) {
                    case 1:
                        field.setDouble(boundingBox, loc.getX() - newBox.getX());
                        break;
                    case 2:
                        // field.setDouble(boundingBox, loc.getY() - newBox.getY());
                        break;
                    case 3:
                        field.setDouble(boundingBox, loc.getZ() - newBox.getZ());
                        break;
                    case 4:
                        field.setDouble(boundingBox, loc.getX() + newBox.getX());
                        break;
                    case 5:
                        field.setDouble(boundingBox, loc.getY() + newBox.getY());
                        break;
                    case 6:
                        field.setDouble(boundingBox, loc.getZ() + newBox.getZ());
                        break;
                    default:
                        throw new Exception("Error while setting the bounding box, more doubles than I thought??");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
