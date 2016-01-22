package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionManager {

    public enum LibVersion {

        V1_8;
        private static LibVersion currentVersion;

        static {
            //String mcVersion = Bukkit.getVersion().split("MC: ")[1].replace(")", "");
            currentVersion = V1_8;
        }

        public static LibVersion getGameVersion() {
            return currentVersion;
        }
    }

    private static final String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static final Class<?> craftItemClass;
    private static Method damageAndIdleSoundMethod;
    private static final Field entitiesField;
    private static final Constructor<?> boundingBoxConstructor;
    private static final Method setBoundingBoxMethod;
    /**
     * Map of mc-dev simple class name to fully qualified Forge class name.
     */
    private static Map<String, String> ForgeClassMappings;
    /**
     * Map of Forge fully qualified class names to a map from mc-dev field names to Forge field names.
     */
    private static Map<String, Map<String, String>> ForgeFieldMappings;

    /**
     * Map of Forge fully qualified class names to a map from mc-dev method names to a map from method signatures to Forge method names.
     */
    private static Map<String, Map<String, Map<String, String>>> ForgeMethodMappings;
    private static final Method ihmGet;
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

        primitiveTypes = ImmutableMap.<Class<?>, String>builder().put(boolean.class, "Z").put(byte.class, "B")
                .put(char.class, "C").put(short.class, "S").put(int.class, "I").put(long.class, "J").put(float.class, "F")
                .put(double.class, "D").put(void.class, "V").build();

        if (isForge) {
            // Initialize the maps by reading the srg file
            ForgeClassMappings = new HashMap<>();
            ForgeFieldMappings = new HashMap<>();
            ForgeMethodMappings = new HashMap<>();
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
                            innerMap = new HashMap<>();
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
                            middleMap = new HashMap<>();
                            ForgeMethodMappings.put(dir2fqn(methodMatcher.group(5)), middleMap);
                        }
                        // get by CB method name
                        Map<String, String> innerMap = middleMap.get(methodMatcher.group(2));
                        if (innerMap == null) {
                            innerMap = new HashMap<>();
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
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace(System.out);
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
                ex.printStackTrace(System.out);
            }
        }
        craftItemClass = getCraftClass("inventory.CraftItemStack");
        pingField = getNmsField("EntityPlayer", "ping");
        trackerField = getNmsField("WorldServer", "tracker");
        entitiesField = getNmsField("EntityTracker", "trackedEntities");
        ihmGet = getNmsMethod("IntHashMap", "get", int.class);
        boundingBoxConstructor = getNmsConstructor("AxisAlignedBB", double.class, double.class, double.class,
                double.class, double.class, double.class);
        setBoundingBoxMethod = getNmsMethod("Entity", "a", getNmsClass("AxisAlignedBB"));
    }

    public static Object createEntityInstance(String entityName) {
        try {
            Class<?> entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld(Bukkit.getWorlds().get(0));
            switch (entityName) {
                case "Player":
                    Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);
                    Object playerinteractmanager = getNmsClass("PlayerInteractManager").getConstructor(getNmsClass("World"))
                            .newInstance(world);
                    WrappedGameProfile gameProfile = getGameProfile(null, "LibsDisguises");
                    entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                            gameProfile.getHandleType(), playerinteractmanager.getClass()).newInstance(minecraftServer, world,
                            gameProfile.getHandle(), playerinteractmanager);
                    break;
                case "EnderPearl":
                    entityObject = entityClass.getConstructor(getNmsClass("World"), getNmsClass("EntityLiving"))
                            .newInstance(world, createEntityInstance("Cow"));
                    break;
                default:
                    entityObject = entityClass.getConstructor(getNmsClass("World")).newInstance(world);
                    break;
            }
            return entityObject;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static Object createMobEffect(int id, int duration, int amplification, boolean ambient, boolean particles) {
        try {
            return getNmsClass("MobEffect").getConstructor(int.class, int.class, int.class, boolean.class, boolean.class)
                    .newInstance(id, duration, amplification, ambient, particles);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static Object createMobEffect(PotionEffect effect) {
        return createMobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());
    }

    private static String dir2fqn(String s) {
        return s.replaceAll("/", ".");
    }

    public static FakeBoundingBox getBoundingBox(Entity entity) {
        try {
            Object boundingBox = getNmsMethod("Entity", "getBoundingBox").invoke(getNmsEntity(entity));
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
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        try {
            return (Entity) getNmsMethod("Entity", "getBukkitEntity").invoke(nmsEntity);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) craftItemClass.getMethod("asBukkitCopy", getNmsClass("ItemStack")).invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace(System.out);
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
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static String getCraftSound(Sound sound) {
        try {
            return (String) getCraftClass("CraftSound").getMethod("getSound", Sound.class).invoke(null, sound);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
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
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static Object getBlockPosition(int x, int y, int z) {
        try {
            return getNmsClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(x, y, z);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static Enum getEnumDirection(int direction) {
        try {
            return (Enum) getNmsMethod("EnumDirection", "fromType2", int.class).invoke(null, direction);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static Enum getEnumPlayerInfoAction(int action) {
        try {
            return (Enum) getNmsClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getEnumConstants()[action];
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static Object getPlayerInfoData(Object playerInfoPacket, WrappedGameProfile gameProfile) {
        try {
            Object playerListName = getNmsClass("ChatComponentText").getConstructor(String.class)
                    .newInstance(gameProfile.getName());
            return getNmsClass("PacketPlayOutPlayerInfo$PlayerInfoData").getConstructor(getNmsClass("PacketPlayOutPlayerInfo"),
                    gameProfile.getHandleType(), int.class, getNmsClass("WorldSettings$EnumGamemode"), getNmsClass("IChatBaseComponent"))
                    .newInstance(playerInfoPacket, gameProfile.getHandle(), 0,
                            getNmsClass("WorldSettings$EnumGamemode").getEnumConstants()[1], playerListName);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static WrappedGameProfile getGameProfile(Player player) {
        return WrappedGameProfile.fromPlayer(player);
    }

    public static WrappedGameProfile getGameProfile(UUID uuid, String playerName) {
        try {
            return new WrappedGameProfile(uuid != null ? uuid : UUID.randomUUID(), playerName);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static WrappedGameProfile getGameProfileWithThisSkin(UUID uuid, String playerName, WrappedGameProfile profileWithSkin) {

        try {
            WrappedGameProfile gameProfile = new WrappedGameProfile(uuid != null ? uuid : UUID.randomUUID(), playerName);
            gameProfile.getProperties().putAll(profileWithSkin.getProperties());
            return gameProfile;
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
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
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static Constructor getNmsConstructor(Class clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static Constructor getNmsConstructor(String className, Class<?>... parameters) {
        return getNmsConstructor(getNmsClass(className), parameters);
    }

    public static Object getNmsEntity(Entity entity) {
        try {
            return getCraftClass("entity.CraftEntity").getMethod("getHandle").invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

    public static Field getNmsField(Class clazz, String fieldName) {
        if (isForge) {
            try {
                return clazz.getField(ForgeFieldMappings.get(clazz.getName()).get(fieldName));
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace(System.out);
            } catch (NullPointerException ignored) {
            }
        }
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace(System.out);
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
            e.printStackTrace(System.out);
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
                e.printStackTrace(System.out);
            } catch (NullPointerException ignored) {
            }
        }
        try {
            return clazz.getMethod(methodName, parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace(System.out);
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
            ex.printStackTrace(System.out);
        }
        return 0D;
    }

    public static float[] getSize(Entity entity) {
        try {
            float length = getNmsField("Entity", "length").getFloat(getNmsEntity(entity));
            float width = getNmsField("Entity", "width").getFloat(getNmsEntity(entity));
            float height = (Float) getNmsMethod("Entity", "getHeadHeight").invoke(getNmsEntity(entity));
            return new float[]{length, width, height};
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
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
            ex.printStackTrace(System.out);
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
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static WrappedGameProfile grabProfileAddUUID(String playername) {
        try {
            Object minecraftServer = getNmsMethod("MinecraftServer", "getServer").invoke(null);
            for (Method method : getNmsClass("MinecraftServer").getMethods()) {
                if (method.getReturnType().getSimpleName().equals("GameProfileRepository")) {
                    Object profileRepo = method.invoke(minecraftServer);
                    Object agent = Class.forName("com.mojang.authlib.Agent").getField("MINECRAFT").get(null);
                    LibsProfileLookupCaller callback = new LibsProfileLookupCaller();
                    profileRepo
                            .getClass()
                            .getMethod("findProfilesByNames", String[].class, agent.getClass(),
                                    Class.forName("com.mojang.authlib.ProfileLookupCallback"))
                            .invoke(profileRepo, new String[]{playername}, agent, callback);
                    if (callback.getGameProfile() != null) {
                        return callback.getGameProfile();
                    }
                    return getGameProfile(null, playername);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
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

    }

    public static void setAllowSleep(Player player) {
        try {
            /**
             * Object nmsEntity = getNmsEntity(player); Object connection = getNmsField(nmsEntity.getClass(), "playerConnection").get(nmsEntity); Field check = getNmsField(connection.getClass(), "checkMovement"); check.setBoolean(connection, true); *
             */
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static void setBoundingBox(Entity entity, FakeBoundingBox newBox) {
        try {
            Location loc = entity.getLocation();
            Object boundingBox = boundingBoxConstructor.newInstance(loc.getX() - newBox.getX(), loc.getY() - newBox.getY(),
                    loc.getZ() - newBox.getZ(), loc.getX() + newBox.getX(), loc.getY() + newBox.getY(), loc.getZ() + newBox.getZ());
            setBoundingBoxMethod.invoke(getNmsEntity(entity), boundingBox);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

}
