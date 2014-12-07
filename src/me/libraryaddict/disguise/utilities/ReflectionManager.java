package me.libraryaddict.disguise.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

public class ReflectionManager {

    public enum LibVersion {
        V1_6, V1_7, V1_7_10, V1_7_6, V1_8;
        private static LibVersion currentVersion;
        static {
            String mcVersion = Bukkit.getVersion().split("MC: ")[1].replace(")", "");
            if (mcVersion.startsWith("1.")) {
                if (mcVersion.compareTo("1.7") < 0) {
                    currentVersion = LibVersion.V1_6;
                } else if (mcVersion.startsWith("1.7")) {
                    if (mcVersion.equals("1.7.10")) {
                        currentVersion = LibVersion.V1_7_10;
                    } else {
                        currentVersion = mcVersion.compareTo("1.7.6") < 0 ? LibVersion.V1_7 : LibVersion.V1_7_6;
                    }
                } else {
                    currentVersion = V1_8;
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
            return getGameVersion() == V1_7 || is1_7_6();
        }

        public static boolean is1_7_10() {
            return getGameVersion() == V1_7_10 || is1_8();
        }

        public static boolean is1_7_6() {
            return getGameVersion() == V1_7_6 || is1_7_10();
        }

        public static boolean is1_8() {
            return getGameVersion() == V1_8;
        }
    }

    private static final String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static final Class<?> craftItemClass;
    private static Method damageAndIdleSoundMethod;
    private static final Field entitiesField;
    /**
     * Map of mc-dev simple class name to fully qualified Forge class name.
     */
    private static Map<String, String> ForgeClassMappings;
    /**
     * Map of Forge fully qualified class names to a map from mc-dev field names to Forge field names.
     */
    private static Map<String, Map<String, String>> ForgeFieldMappings;

    /**
     * Map of Forge fully qualified class names to a map from mc-dev method names to a map from method signatures to Forge method
     * names.
     */
    private static Map<String, Map<String, Map<String, String>>> ForgeMethodMappings;
    private static final Method ihmGet;
    private static HashMap<String, Boolean> is1_8 = new HashMap<String, Boolean>();
    private static final boolean isForge = Bukkit.getServer().getName().contains("Cauldron")
            || Bukkit.getServer().getName().contains("MCPC-Plus");
    private static final Field pingField;
    private static Map<Class<?>, String> primitiveTypes;
    private static final Field trackerField;

    /*
     * This portion of code is originally Copyright (C) 2014-2014 Kane York.
     *
     * In addition to the implicit license granted to libraryaddict to redistribuite the code, the
     * code is also licensed to the public under the BSD 2-clause license.
     *
     * The publicly licensed version may be viewed here: https://gist.github.com/riking/2f330f831c30e2276df7
     */
    static {
        final String nameseg_class = "a-zA-Z0-9$_";
        final String fqn_class = nameseg_class + "/";

        primitiveTypes = ImmutableMap.<Class<?>, String> builder().put(boolean.class, "Z").put(byte.class, "B")
                .put(char.class, "C").put(short.class, "S").put(int.class, "I").put(long.class, "J").put(float.class, "F")
                .put(double.class, "D").put(void.class, "V").build();

        if (isForge) {
            // Initialize the maps by reading the srg file
            ForgeClassMappings = new HashMap<String, String>();
            ForgeFieldMappings = new HashMap<String, Map<String, String>>();
            ForgeMethodMappings = new HashMap<String, Map<String, Map<String, String>>>();
            try {
                InputStream stream = Class.forName("net.minecraftforge.common.MinecraftForge").getClassLoader()
                        .getResourceAsStream("mappings/" + getBukkitVersion() + "/cb2numpkg.srg");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                // 1: cb-simpleName
                // 2: forge-fullName (Needs dir2fqn())
                Pattern classPattern = Pattern.compile("^CL: net/minecraft/server/([" + nameseg_class + "]+) ([" + fqn_class
                        + "]+)$");
                // 1: cb-simpleName
                // 2: cb-fieldName
                // 3: forge-fullName (Needs dir2fqn())
                // 4: forge-fieldName
                Pattern fieldPattern = Pattern.compile("^FD: net/minecraft/server/([" + nameseg_class + "]+)/([" + nameseg_class
                        + "]+) ([" + fqn_class + "]+)/([" + nameseg_class + "]+)$");
                // 1: cb-simpleName
                // 2: cb-methodName
                // 3: cb-signature-args
                // 4: cb-signature-ret
                // 5: forge-fullName (Needs dir2fqn())
                // 6: forge-methodName
                // 7: forge-signature-args
                // 8: forge-signature-ret
                Pattern methodPattern = Pattern.compile("^MD: net/minecraft/server/([" + fqn_class + "]+)/([" + nameseg_class
                        + "]+) \\(([;\\[" + fqn_class + "]*)\\)([;\\[" + fqn_class + "]+) " + "([" + fqn_class + "]+)/(["
                        + nameseg_class + "]+) \\(([;\\[" + fqn_class + "]*)\\)([;\\[" + fqn_class + "]+)$");

                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher classMatcher = classPattern.matcher(line);
                    if (classMatcher.matches()) {
                        // by CB class name
                        ForgeClassMappings.put(classMatcher.group(1), dir2fqn(classMatcher.group(2)));
                        continue;
                    }
                    Matcher fieldMatcher = fieldPattern.matcher(line);
                    if (fieldMatcher.matches()) {
                        // by CB class name
                        Map<String, String> innerMap = ForgeFieldMappings.get(dir2fqn(fieldMatcher.group(3)));
                        if (innerMap == null) {
                            innerMap = new HashMap<String, String>();
                            ForgeFieldMappings.put(dir2fqn(fieldMatcher.group(3)), innerMap);
                        }
                        // by CB field name to Forge field name
                        innerMap.put(fieldMatcher.group(2), fieldMatcher.group(4));
                        continue;
                    }
                    Matcher methodMatcher = methodPattern.matcher(line);
                    if (methodMatcher.matches()) {
                        // get by CB class name
                        Map<String, Map<String, String>> middleMap = ForgeMethodMappings.get(dir2fqn(methodMatcher.group(5)));
                        if (middleMap == null) {
                            middleMap = new HashMap<String, Map<String, String>>();
                            ForgeMethodMappings.put(dir2fqn(methodMatcher.group(5)), middleMap);
                        }
                        // get by CB method name
                        Map<String, String> innerMap = middleMap.get(methodMatcher.group(2));
                        if (innerMap == null) {
                            innerMap = new HashMap<String, String>();
                            middleMap.put(methodMatcher.group(2), innerMap);
                        }
                        // store the parameter strings
                        innerMap.put(methodMatcher.group(3), methodMatcher.group(6));
                        innerMap.put(methodMatcher.group(7), methodMatcher.group(6));
                    }
                }
                System.out.println("[LibsDisguises] Loaded in Cauldron/Forge mode");
                System.out.println("[LibsDisguises] Loaded " + ForgeClassMappings.size() + " Cauldron class mappings");
                System.out.println("[LibsDisguises] Loaded " + ForgeFieldMappings.size() + " Cauldron field mappings");
                System.out.println("[LibsDisguises] Loaded " + ForgeMethodMappings.size() + " Cauldron method mappings");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.err
                        .println("Warning: Running on Cauldron server, but couldn't load mappings file. LibsDisguises will likely crash!");
            } catch (IOException e) {
                e.printStackTrace();
                System.err
                        .println("Warning: Running on Cauldron server, but couldn't load mappings file. LibsDisguises will likely crash!");
            }
        }
    }

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
        craftItemClass = getCraftClass("inventory.CraftItemStack");
        pingField = getNmsField("EntityPlayer", "ping");
        trackerField = getNmsField("WorldServer", "tracker");
        entitiesField = getNmsField("EntityTracker", "trackedEntities");
        ihmGet = getNmsMethod("IntHashMap", "get", int.class);
    }

    public static Object createEntityInstance(String entityName) {
        try {
            Class<?> entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld(Bukkit.getWorlds().get(0));
            if (entityName.equals("Player")) {
                Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);
                Object playerinteractmanager = getNmsClass("PlayerInteractManager").getConstructor(getNmsClass("World"))
                        .newInstance(world);
                if (LibVersion.is1_7()) {
                    WrappedGameProfile gameProfile = getGameProfile(null, "LibsDisguises");
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                            gameProfile.getHandleType(), playerinteractmanager.getClass()).newInstance(minecraftServer, world,
                            gameProfile.getHandle(), playerinteractmanager);
                } else {
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("World"), String.class,
                            playerinteractmanager.getClass()).newInstance(minecraftServer, world, "LibsDisguises",
                            playerinteractmanager);
                }
            } else if (LibVersion.is1_8() && entityName.equals("EnderPearl")) {
                entityObject = entityClass.getConstructor(getNmsClass("World"), getNmsClass("EntityLiving"))
                        .newInstance(world, createEntityInstance("Sheep"));
            } else {
                entityObject = entityClass.getConstructor(getNmsClass("World")).newInstance(world);
            }
            return entityObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String dir2fqn(String s) {
        return s.replaceAll("/", ".");
    }

    public static FakeBoundingBox getBoundingBox(Entity entity) {
        try {
            Object boundingBox;
            if (LibVersion.is1_8()) {
                boundingBox = getNmsMethod("Entity", "getBoundingBox").invoke(getNmsEntity(entity));
            } else {
                boundingBox = getNmsField("Entity", "boundingBox").get(getNmsEntity(entity));
            }
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
            return (Entity) getNmsMethod("Entity", "getBukkitEntity").invoke(nmsEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) craftItemClass.getMethod("asBukkitCopy", getNmsClass("ItemStack")).invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getBukkitVersion() {
        return bukkitVersion;
    }

    public static Class<?> getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getBukkitVersion() + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCraftSound(Sound sound) {
        try {
            return (String) getCraftClass("CraftSound").getMethod("getSound", Sound.class).invoke(null, sound);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getEntityTrackerEntry(Entity target) throws Exception {
        Object world = getWorld(target.getWorld());
        Object tracker = trackerField.get(world);
        Object trackedEntities = entitiesField.get(tracker);
        return ihmGet.invoke(trackedEntities, target.getEntityId());
    }

    public static String getEnumArt(Art art) {
        try {
            Object enumArt = getCraftClass("CraftArt").getMethod("BukkitToNotch", Art.class).invoke(null, art);
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

    public static WrappedGameProfile getGameProfile(Player player) {
        if (LibVersion.is1_7() || LibVersion.is1_8()) {
            return WrappedGameProfile.fromPlayer(player);
        }
        return null;
    }

    public static WrappedGameProfile getGameProfile(UUID uuid, String playerName) {
        try {
            return new WrappedGameProfile(uuid != null ? uuid : UUID.randomUUID(), playerName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static WrappedGameProfile getGameProfileWithThisSkin(UUID uuid, String playerName, WrappedGameProfile profileWithSkin) {
        try {
            WrappedGameProfile gameProfile = new WrappedGameProfile(uuid != null ? uuid : UUID.randomUUID(), playerName);
            if (LibVersion.is1_7_6()) {
                gameProfile.getProperties().putAll(profileWithSkin.getProperties());
            }
            return gameProfile;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class getNmsClass(String className) {
        if (isForge) {
            String forgeName = ForgeClassMappings.get(className);
            if (forgeName != null) {
                try {
                    return Class.forName(forgeName);
                } catch (ClassNotFoundException ignored) {
                }
            } else {
                // Throw, because the default cannot possibly work
                throw new RuntimeException("Missing Forge mapping for " + className);
            }
        }
        try {
            return Class.forName("net.minecraft.server." + getBukkitVersion() + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
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

    public static Field getNmsField(Class clazz, String fieldName) {
        if (isForge) {
            try {
                return clazz.getField(ForgeFieldMappings.get(clazz.getName()).get(fieldName));
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ignored) {
            }
        }
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getNmsField(String className, String fieldName) {
        return getNmsField(getNmsClass(className), fieldName);
    }

    public static Object getNmsItem(ItemStack itemstack) {
        try {
            return craftItemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getNmsMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        if (isForge) {
            try {
                Map<String, String> innerMap = ForgeMethodMappings.get(clazz.getName()).get(methodName);
                StringBuilder sb = new StringBuilder();
                for (Class<?> cl : parameters) {
                    sb.append(methodSignaturePart(cl));
                }
                return clazz.getMethod(innerMap.get(sb.toString()), parameters);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
            }
        }
        try {
            return clazz.getMethod(methodName, parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getNmsMethod(String className, String methodName, Class<?>... parameters) {
        return getNmsMethod(getNmsClass(className), methodName, parameters);
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
            float length = getNmsField("Entity", "length").getFloat(getNmsEntity(entity));
            float width = getNmsField("Entity", "width").getFloat(getNmsEntity(entity));
            float height;
            if (LibVersion.is1_8()) {
                height = (Float) getNmsMethod("Entity", "getHeadHeight").invoke(getNmsEntity(entity));
            } else {
                height = getNmsField("Entity", "height").getFloat(getNmsEntity(entity));
            }
            return new float[] { length, width, height };
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static WrappedGameProfile getSkullBlob(WrappedGameProfile gameProfile) {
        try {
            Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);
            for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                if (method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                    Object session = method.invoke(minecraftServer);
                    return WrappedGameProfile.fromHandle(session.getClass()
                            .getMethod("fillProfileProperties", gameProfile.getHandleType(), boolean.class)
                            .invoke(session, gameProfile.getHandle(), true));
                }
            }
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

    public static WrappedGameProfile grabProfileAddUUID(String playername) {
        try {
            Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);
            for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                if (method.getReturnType().getSimpleName().equals("GameProfileRepository")) {
                    Object profileRepo = method.invoke(minecraftServer);
                    Object agent = Class.forName("net.minecraft.util.com.mojang.authlib.Agent").getField("MINECRAFT").get(null);
                    LibsProfileLookupCaller callback = new LibsProfileLookupCaller();
                    profileRepo
                            .getClass()
                            .getMethod("findProfilesByNames", String[].class, agent.getClass(),
                                    Class.forName("net.minecraft.util.com.mojang.authlib.ProfileLookupCallback"))
                            .invoke(profileRepo, new String[] { playername }, agent, callback);
                    if (callback.getGameProfile() != null) {
                        return callback.getGameProfile();
                    }
                    return getGameProfile(null, playername);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean is1_8(Player player) {
        if (LibVersion.is1_8()) {
            if (is1_8.containsKey(player.getName())) {
                return is1_8.get(player.getName());
            }
            try {
                Object nmsEntity = getNmsEntity(player);
                Object connection = getNmsField(nmsEntity.getClass(), "playerConnection").get(nmsEntity);
                Field networkManager = getNmsField(connection.getClass(), "networkManager");
                Method getVersion = getNmsMethod(networkManager.getType(), "getVersion");
                boolean is18 = (Integer) getVersion.invoke(networkManager.get(connection)) >= 28;
                is1_8.put(player.getName(), is18);
                return is18;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isForge() {
        return isForge;
    }

    private static String methodSignaturePart(Class<?> param) {
        if (param.isArray()) {
            return "[" + methodSignaturePart(param.getComponentType());
        } else if (param.isPrimitive()) {
            return primitiveTypes.get(param);
        } else {
            return "L" + param.getName().replaceAll("\\.", "/") + ";";
        }
    }

    public static void removePlayer(Player player) {
        is1_8.remove(player.getName());
    }

    public static void setAllowSleep(Player player) {
        try {
            Object nmsEntity = getNmsEntity(player);
            Object connection = getNmsField(nmsEntity.getClass(), "playerConnection").get(nmsEntity);
            Field check = getNmsField(connection.getClass(), "checkMovement");
            check.setBoolean(connection, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setBoundingBox(Entity entity, FakeBoundingBox newBox) {
        try {
            Object boundingBox;
            if (LibVersion.is1_8()) {
                boundingBox = getNmsMethod("Entity", "getBoundingBox").invoke(getNmsEntity(entity));
            } else {
                boundingBox = getNmsField("Entity", "boundingBox").get(getNmsEntity(entity));
            }
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
