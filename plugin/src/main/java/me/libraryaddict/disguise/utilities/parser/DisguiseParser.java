package me.libraryaddict.disguise.utilities.parser;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.ModdedDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.modded.ModdedEntity;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.constructors.ArtPaintingDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.BlockStateDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.ExtraDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.IntegerPaintingDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.ItemDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.ItemFrameDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.PlayerDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.SplashPotionDisguiseParam;
import me.libraryaddict.disguise.utilities.parser.constructors.TextDisplayParam;
import me.libraryaddict.disguise.utilities.parser.constructors.WrappedBlockDisguiseParam;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import me.libraryaddict.disguise.utilities.watchers.DisguiseMethods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DisguiseParser {
    private static class InvalidSkinHits {
        private int occured;
        private long lastHit;

        public void addHit() {
            occured++;
            lastHit = System.currentTimeMillis();
        }

        /**
         * If long enough has passed that we'll let them start from fresh
         */
        public boolean isRemove() {
            return lastHit + TimeUnit.MILLISECONDS.toMillis(occured) < System.currentTimeMillis();
        }

        /**
         * If we can give them another try
         */
        public boolean hasExpired() {
            int expiresMinutes;

            if (occured <= 10) {
                expiresMinutes = occured * 3;
            } else {
                expiresMinutes = occured * 30;
            }

            return lastHit + TimeUnit.MINUTES.toMillis(expiresMinutes) < System.currentTimeMillis();
        }
    }

    private static final List<WatcherGetterSetter> watcherMethods = new ArrayList<>();
    private static final List<ExtraDisguiseParam> extraDisguiseParams = new ArrayList<>();
    private static final ConcurrentHashMap<String, List<Consumer<UserProfile>>> fetchingSkins = new ConcurrentHashMap<>();
    private static long lastPremiumMessage;
    private static final Queue<Long> failedSkins = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, InvalidSkinHits> invalidSkinFilesExpiresAt = new ConcurrentHashMap<>();

    public static void createDefaultMethods() {
        try {
            ArrayList<WatcherMethod> allMethods = ParamInfoManager.getDisguiseMethods().getMethods();

            for (DisguiseType type : DisguiseType.values()) {
                if (type.getEntityType() == null) {
                    continue;
                }

                Disguise disguise;

                if (type.isMisc()) {
                    disguise = new MiscDisguise(type);
                } else if (type.isMob()) {
                    disguise = new MobDisguise(type);
                } else if (type.isPlayer()) {
                    disguise = new PlayerDisguise("Foobar");
                } else {
                    continue;
                }

                FlagWatcher watcher = type.getWatcherClass().getConstructor(Disguise.class).newInstance(disguise);

                WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(watcher.getClass(), true);

                for (WatcherMethod setMethod : methods) {
                    // Invalidate methods that can't be handled normally
                    if (setMethod.getName().equals("addPotionEffect")) {
                        continue;
                    } else if (setMethod.getName().equals("setSkin") && setMethod.getParam() == String.class) {
                        continue;
                    } else if (setMethod.getName().equals("setTarget") && setMethod.getParam() != int.class) {
                        continue;
                    } else if ((setMethod.getName().equals("setCustomName") || setMethod.getName().equals("setCustomNameVisible")) &&
                        disguise.isPlayerDisguise()) {
                        // Player Disguise overrides the behavior of custom name, so we definitely don't want it judged on a global scale
                        continue;
                    } else if (setMethod.getName().equals("setItemInMainHand") && setMethod.getParam() == Material.class) {
                        continue;
                    } else if (setMethod.getName().matches("setArmor") && setMethod.getParam() == ItemStack[].class) {
                        continue;
                    }

                    String sharedName = setMethod.getMappedName().substring(3); // Remove 'set'
                    String getPrefix = "get";

                    if (sharedName.matches("^Has(Nectar|Stung)$") || sharedName.matches("^Has((Left)|(Right))Horn$")) {
                        sharedName = sharedName.substring(3);
                        getPrefix = "has";
                    } else if (setMethod.getParam().isAssignableFrom(boolean.class)) {
                        getPrefix = "is";
                    } else {
                        getPrefix = "get";
                    }

                    String getName = getPrefix + sharedName;
                    WatcherMethod getMethod = null;

                    for (WatcherMethod m : allMethods) {
                        if (m.getWatcherClass() != setMethod.getWatcherClass() || m.getParam() != null) {
                            continue;
                        }

                        if (!m.getMappedName().equals(getName)) {
                            continue;
                        }

                        getMethod = m;
                        break;
                    }

                    if (getMethod == null) {
                        LibsDisguises.getInstance().getLogger().severe(
                            String.format("No such method '%s' when looking for the companion of '%s' in '%s'", getName,
                                setMethod.getMappedName(), setMethod.getWatcherClass().getSimpleName()));
                        continue;
                    } else if (getMethod.getReturnType() != setMethod.getParam()) {
                        LibsDisguises.getInstance().getLogger().severe(
                            String.format("Invalid return type of '%s' when looking for the companion of '%s' in '%s'", getName,
                                setMethod.getMappedName(), setMethod.getWatcherClass().getSimpleName()));
                        continue;
                    }

                    if (getMethod.getAdded() != setMethod.getAdded() || getMethod.getRemoved() != setMethod.getRemoved()) {
                        LibsDisguises.getInstance().getLogger().severe(String.format(
                            "The methods %s and %s do not have matching NmsAdded and NmsRemoved, this is an oversight by the author of " +
                                "LibsDisguises", getMethod.getName(), setMethod.getName()));
                    }

                    Object defaultValue = null;

                    // Value is randomish so shouldn't be checked, should always specify value when setting
                    if (!setMethod.isRandomDefault()) {
                        Object invokeWith = watcher;

                        if (!ReflectionManager.isAssignableFrom(FlagWatcher.class, getMethod.getWatcherClass())) {
                            invokeWith = disguise;
                        }

                        defaultValue = getMethod.getMethod().bindTo(invokeWith).invoke();
                    }

                    addWatcherDefault(new WatcherGetterSetter(setMethod, getMethod, defaultValue, sharedName));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        List<DisguiseType> blockDisguises = new ArrayList<>();
        blockDisguises.add(DisguiseType.MINECART);
        // Although it wasn't usable until 1.19.3, it's also not a valid disguise when it wasn't usable
        blockDisguises.add(DisguiseType.BLOCK_DISPLAY);

        if (NmsVersion.v1_20_R3.isSupported()) {
            // Only in this version did it let you change block state
            blockDisguises.add(DisguiseType.PRIMED_TNT);
        }

        extraDisguiseParams.add(new BlockStateDisguiseParam(blockDisguises.toArray(new DisguiseType[0])));
        // Falling block is seperate as traditionally, it supported ItemStacks
        extraDisguiseParams.add(new WrappedBlockDisguiseParam(DisguiseType.FALLING_BLOCK));
        extraDisguiseParams.add(new ArtPaintingDisguiseParam());
        extraDisguiseParams.add(new IntegerPaintingDisguiseParam());
        extraDisguiseParams.add(new ItemDisguiseParam());
        extraDisguiseParams.add(new ItemFrameDisguiseParam());
        extraDisguiseParams.add(new PlayerDisguiseParam());
        extraDisguiseParams.add(new SplashPotionDisguiseParam());
        extraDisguiseParams.add(new TextDisplayParam());
    }

    /**
     * Return the player name, or the contents of setskin
     */
    private static String getSkin(String[] args) {
        if (args.length < 2 || !args[0].toLowerCase(Locale.ENGLISH).matches("p|player")) {
            return null;
        }

        for (int i = 0; i < args.length - 1; i++) {
            if (!args[i].equalsIgnoreCase("setskin")) {
                continue;
            }

            return args[i + 1];
        }

        return args[1];
    }

    private static void grabSkin(CommandSender sender, String skinFile, PlayerDisguise disguise) {
        // Only process if they are trying to use a skin pic
        if (skinFile == null || !skinFile.toLowerCase(Locale.ENGLISH).matches(".*\\.png($|\\W.*)")) {
            return;
        }

        if (DisguiseUtilities.hasUserProfile(skinFile, true)) {
            return;
        }

        // No point in translations as this message will only be seen if translations isn't available
        if (!LibsPremium.isPremium()) {
            if (sender == Bukkit.getConsoleSender()) {
                // Only send every 3 hours, don't spam users
                if (lastPremiumMessage + TimeUnit.HOURS.toMillis(3) > System.currentTimeMillis()) {
                    return;
                }

                lastPremiumMessage = System.currentTimeMillis();
            }

            DisguiseUtilities.sendMessage(sender,
                "<red>Using a skin file inline with a player disguise is a Lib's Disguises premium feature, you must use /saveskin and " +
                    "save as" + " that, or /savedisguise and disguise as the saved disguise.</red>");
            return;
        }

        // Remove all entries older than a hour
        while (!failedSkins.isEmpty() && failedSkins.peek() + TimeUnit.HOURS.toMillis(1) < System.currentTimeMillis()) {
            failedSkins.poll();
        }

        // We have 10 failures, lets not spam mineskin
        if (failedSkins.size() >= 10) {
            LibsMsg.SKIN_API_TOO_MANY_FAILURES.send(sender, skinFile);
            return;
        }

        InvalidSkinHits skinInvalidAt = invalidSkinFilesExpiresAt.get(skinFile);

        // Only players can bypass the limit as it's unlikely they'll spam it and they'll get messages. Hopefully we don't get fake
        // players..
        if (skinInvalidAt != null && !(sender instanceof Player)) {
            if (skinInvalidAt.isRemove()) {
                invalidSkinFilesExpiresAt.remove(skinFile);
            } else if (!skinInvalidAt.hasExpired()) {
                LibsMsg.SKIN_API_TOO_MANY_FAILURES_NON_PLAYER.send(sender, skinFile);
                return;
            }
        }

        String usable = SkinUtils.getUsableStatus();

        if (usable != null) {
            DisguiseUtilities.sendMessage(sender, usable);
            return;
        }

        Consumer<UserProfile> consumer = disguise::setSkin;
        List<Consumer<UserProfile>> list = fetchingSkins.get(skinFile);

        if (list != null) {
            list.add(consumer);
            return;
        }

        fetchingSkins.put(skinFile, list = new ArrayList<>());
        list.add(consumer);

        SkinUtils.grabSkin(sender, skinFile, new SkinUtils.SkinCallback() {
            @Override
            public void onError(LibsMsg msg, Object... args) {
                msg.send(sender, args);

                fetchingSkins.remove(skinFile);
                // Add the time
                failedSkins.add(System.currentTimeMillis());

                // On a failure where user error is likely
                switch (msg) {
                    case SKIN_API_403:
                    case SKIN_API_404:
                    case SKIN_API_FAIL:
                    case SKIN_API_BAD_FILE:
                    case SKIN_API_BAD_FILE_NAME:
                    case SKIN_API_BAD_URL:
                    case SKIN_API_FAILED_URL:
                    case SKIN_API_FAIL_TOO_FAST:
                    case SKIN_API_IN_USE:
                    case SKIN_API_IMAGE_HAS_ERROR:
                    case SKIN_API_INTERNAL_ERROR:
                    case SKIN_API_TIMEOUT_API_KEY_ERROR:
                    case SKIN_API_FAIL_CODE:
                    case SKIN_API_INVALID_NAME:
                        InvalidSkinHits hits = invalidSkinFilesExpiresAt.computeIfAbsent(skinFile, k -> new InvalidSkinHits());
                        hits.addHit();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onInfo(LibsMsg msg, Object... args) {
                msg.send(sender, args);
            }

            @Override
            public void onSuccess(UserProfile profile) {
                DisguiseUtilities.doSkinUUIDWarning(sender);

                DisguiseAPI.addGameProfile(skinFile, profile);

                fetchingSkins.remove(skinFile).forEach((a) -> a.accept(profile));
            }
        });
    }

    public static String parseToString(Disguise disguise) {
        return parseToString(disguise, true);
    }

    public static String parseToString(Disguise disguise, boolean outputSkinData) {
        return parseToString(disguise, outputSkinData, false);
    }

    public static String parseToString(Disguise disguise, WatcherMethod method) throws Throwable {
        // Ensure its a getter
        if (method.getOwner() != null) {
            method = method.getOwner().getGetter();
        }

        Object invokeWith = method.getWatcherClass().isInstance(disguise) ? disguise : disguise.getWatcher();

        Object ourValue = method.getMethod().bindTo(invokeWith).invoke();

        // Escape a hacky fix for custom names, disguised players with custom names don't want to show it
        // so it was set to an empty string.
        if ("".equals(ourValue) && method.getMappedName().equals("getCustomName")) {
            ourValue = null;
        }

        if (method.getMappedName().equals("getSkin") && disguise.isPlayerDisguise()) {
            PlayerDisguise pDisg = (PlayerDisguise) disguise;
            ourValue = pDisg.getName();

            if (pDisg.getSkin() != null) {
                if (pDisg.getSkin().length() <= 32) {
                    ourValue = pDisg.getSkin();
                }
            } else if (pDisg.getUserProfile() != null && pDisg.getUserProfile().getName() != null) {
                ourValue = pDisg.getUserProfile().getName();
            }
        }

        String valueString;

        if (ourValue != null) {
            ParamInfo paramInfo;

            if (ourValue instanceof String) {
                paramInfo = ParamInfoManager.getParamInfo(String.class);
            } else {
                paramInfo = ParamInfoManager.getParamInfo(method.getReturnType());
            }

            if (paramInfo == null) {
                LibsDisguises.getInstance().getLogger()
                    .info("Unhandled parameter for " + ourValue.getClass() + ", ParamInfo was not found");
                return "null";
            }

            valueString = paramInfo.toString(ourValue);

            if (ourValue instanceof String) {
                return TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet(valueString);
            }

            return valueString;
        }

        return TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet("null");

    }

    /**
     * Not outputting skin information is not garanteed to display the correct player name
     */
    public static String parseToString(Disguise disguise, boolean outputSkinData, boolean includeCustomData) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(TranslateType.DISGUISES.reverseGet(disguise.getType().toReadable().replace(" ", "_")));

            if (disguise.isPlayerDisguise()) {
                stringBuilder.append(" ").append(DisguiseUtilities.quote(((PlayerDisguise) disguise).getName()));
            }

            WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguise.getType().getWatcherClass());

            for (int i = methods.length - 1; i >= 0; i--) {
                WatcherGetterSetter getterSetter = methods[i].getOwner();

                if (getterSetter == null) {
                    continue;
                }

                WatcherMethod setter = getterSetter.getSetter();
                WatcherMethod getter = getterSetter.getGetter();

                // Special handling for this method
                if (getter.getMappedName().equals("getPotionEffects")) {
                    PotionEffectType[] types = (PotionEffectType[]) getter.getMethod().invoke();

                    for (PotionEffectType type : types) {
                        if (type == null) {
                            continue;
                        }

                        stringBuilder.append(" ").append(TranslateType.DISGUISE_OPTIONS.reverseGet(setter.getMappedName())).append(" ")
                            .append(TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet(type.getName()));
                    }

                    continue;
                }

                // Also for this method. You can't override it, so why output it
                if (setter.getMappedName().equals("setNoGravity")) {
                    continue;
                }

                if (disguise.isPlayerDisguise()) {
                    // Player disguises render this useless
                    if (setter.getMappedName().equals("setCustomName")) {
                        continue;
                        // If the name matches tablist, why output again?
                    } else if (setter.getMappedName().equals("setTablistName") &&
                        ((PlayerDisguise) disguise).getName().equals(((PlayerDisguise) disguise).getTablistName())) {
                        continue;
                        // Why output the skin again, when its the same as the name?
                    } else if (setter.getMappedName().equals("setSkin") &&
                        ((PlayerDisguise) disguise).getName().equals(((PlayerDisguise) disguise).getSkin())) {
                        continue;
                    }
                }

                // TODO Ideally we'd determine if the disguise name is the default and can be reconstructed
                // Realistically, probably not. Too much work?

                Object invokeWith = setter.getWatcherClass().isInstance(disguise) ? disguise : disguise.getWatcher();

                Object ourValue = getter.getMethod().bindTo(invokeWith).invoke();

                // Escape a hacky fix for custom names, disguised players with custom names don't want to show it
                // so it was set to an empty string.
                if ("".equals(ourValue) && setter.getMappedName().equals("setCustomName")) {
                    ourValue = null;
                }

                if (setter.getMappedName().equals("setSkin") && !outputSkinData) {
                    PlayerDisguise pDisg = (PlayerDisguise) disguise;
                    ourValue = pDisg.getName();

                    if (pDisg.getSkin() != null) {
                        ourValue = pDisg.getSkin();
                    } else if (pDisg.getUserProfile() != null && pDisg.getUserProfile().getName() != null) {
                        ourValue = pDisg.getUserProfile().getName();
                    }

                    if (ourValue.equals(pDisg.getName())) {
                        continue;
                    }
                } else {
                    // If its the same as default, continue
                    if (!setter.isRandomDefault() && Objects.deepEquals(getterSetter.getDefaultValue(), ourValue)) {
                        continue;
                    }
                }

                stringBuilder.append(" ").append(TranslateType.DISGUISE_OPTIONS.reverseGet(setter.getMappedName()));

                if (ourValue instanceof Boolean && (Boolean) ourValue) {
                    continue;
                }

                String valueString;

                if (ourValue != null) {
                    ParamInfo paramInfo;

                    if (ourValue instanceof String) {
                        paramInfo = ParamInfoManager.getParamInfo(String.class);
                    } else {
                        paramInfo = ParamInfoManager.getParamInfo(getter.getReturnType());
                    }

                    if (paramInfo == null) {
                        LibsDisguises.getInstance().getLogger()
                            .info("Unhandled parameter for " + ourValue.getClass() + ", ParamInfo was not found");
                        continue;
                    }

                    valueString = paramInfo.toString(ourValue);

                    if (ourValue instanceof String) {
                        valueString = TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet(valueString);
                    }

                    valueString = DisguiseUtilities.quote(valueString);
                } else {
                    valueString = TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet("null");
                }

                stringBuilder.append(" ").append(valueString);
            }

            if (includeCustomData) {
                HashMap<String, Object> meta = disguise.getCustomData();
                LinkedHashMap<String, String> serializedMeta = new LinkedHashMap<>();

                for (Entry<String, Object> entry : meta.entrySet()) {
                    Object val = entry.getValue();

                    try {
                        if (val == null) {
                            serializedMeta.put(entry.getKey(), "null");
                            continue;
                        }

                        String serialized = DisguiseUtilities.getGson().toJson(val);

                        serializedMeta.put(entry.getKey(), val.getClass().getName() + ":" + serialized);
                    } catch (Throwable throwable) {
                        LibsDisguises.getInstance().getLogger().warning(
                            "Unable to properly serialize the metadata on a disguise, the metadata was saved under name '" +
                                entry.getKey() + "'");

                        if (!(throwable instanceof StackOverflowError)) {
                            throwable.printStackTrace();
                        }
                    }
                }

                if (!serializedMeta.isEmpty()) {
                    String serialized = DisguiseUtilities.getGson().toJson(serializedMeta);

                    stringBuilder.append(" ").append("setCustomData").append(" ").append(DisguiseUtilities.quote(serialized));
                }
            }

            return stringBuilder.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void addWatcherDefault(WatcherGetterSetter watcherGetterSetter) {
        List<WatcherGetterSetter> existing =
            watcherMethods.stream().filter(method -> method.getSetter().equals(watcherGetterSetter.getSetter()))
                .collect(Collectors.toList());

        if (existing.isEmpty()) {
            if (watcherGetterSetter.getGetter().getOwner() != null || watcherGetterSetter.getSetter().getOwner() != null) {
                throw new IllegalStateException(
                    "Trying to register an older on an existing watcher! Lack of info as this shouldn't be called");
            }

            watcherGetterSetter.getSetter().setOwner(watcherGetterSetter);
            watcherGetterSetter.getGetter().setOwner(watcherGetterSetter);

            watcherMethods.add(watcherGetterSetter);
            return;
        }

        if (existing.size() > 1) {
            throw new IllegalStateException("Shouldn't have more than 1 getter/setter for " + watcherGetterSetter.getSetter().getName());
        }

        Object dObj = existing.get(0).getDefaultValue();

        if (!Objects.deepEquals(dObj, watcherGetterSetter.getDefaultValue())) {
            throw new IllegalStateException(String.format(
                "%s has conflicting values in class %s! This means it expected the same value again but " + "received a " +
                    "different value on a different disguise! %s is not the same as %s!", watcherGetterSetter.getSetter().toString(),
                watcherGetterSetter.getSetter(), watcherGetterSetter.getDefaultValue(), dObj));
        }
    }

    private static void doCheck(CommandSender sender, DisguisePermissions permissions, DisguisePerm disguisePerm,
                                Collection<String> usedOptions) throws DisguiseParseException {

        if (!permissions.isAllowedDisguise(disguisePerm, usedOptions)) {
            throw new DisguiseParseException(LibsMsg.D_PARSE_NOPERM, usedOptions.stream().reduce((first, second) -> second).orElse(null));
        }
    }

    public static DisguisePerm getDisguisePerm(String name) {
        name = name.replaceAll("[ |_]", "").toLowerCase(Locale.ENGLISH);

        for (DisguisePerm perm : getDisguisePerms()) {
            if (!perm.getRegexedName().equals(name)) {
                continue;
            }

            return perm;
        }

        if (name.equals("p")) {
            return getDisguisePerm(DisguiseType.PLAYER.toReadable());
        }

        return null;
    }

    public static DisguisePerm[] getDisguisePerms() {
        ArrayList<DisguisePerm> perms = new ArrayList<>();

        for (DisguiseType disguiseType : DisguiseType.values()) {
            if (disguiseType.getEntityType() == null || disguiseType.isCustom()) {
                continue;
            }

            perms.add(new DisguisePerm(disguiseType));
        }

        for (Entry<DisguisePerm, String> entry : DisguiseConfig.getCustomDisguises().entrySet()) {
            perms.add(entry.getKey());
        }

        perms.addAll(ModdedManager.getDisguiseTypes());

        return perms.toArray(new DisguisePerm[0]);
    }

    /**
     * Get perms for the node. Returns a hashmap of allowed disguisetypes and their options
     */
    public static DisguisePermissions getPermissions(CommandSender sender, String commandName) {
        return DisguisePermissions.getPermissions(sender, commandName);
    }

    private static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getName(CommandSender entity) {
        if (entity == null) {
            return "??";
        }

        if (entity instanceof Player) {
            return entity.getName();
        }

        if (entity instanceof Entity) {
            if (((Entity) entity).getCustomName() != null && ((Entity) entity).getCustomName().length() > 0) {
                return ((Entity) entity).getCustomName();
            }
        }

        return entity.getName();
    }

    private static String getSkin(CommandSender entity) {
        if (entity == null) {
            return "??";
        }

        if (entity instanceof Player) {
            UserProfile gameProfile = ReflectionManager.getUserProfile((Player) entity);

            if (gameProfile != null) {
                return DisguiseUtilities.getGson().toJson(gameProfile);
            }
        }

        return "{}";
    }

    public static String[] parsePlaceholders(String[] args, CommandSender user, CommandSender target) {
        return parsePlaceholders(args, getName(user), DisguiseUtilities.getDisplayName(user), getSkin(user), getName(target),
            DisguiseUtilities.getDisplayName(target), DisguiseParser.getSkin(target), getEntityEquipment(user), getEntityEquipment(target));
    }

    private static EntityEquipment getEntityEquipment(CommandSender entity) {
        return entity instanceof LivingEntity ? ((LivingEntity) entity).getEquipment() : null;
    }

    public static String[] parsePlaceholders(String[] args, String userName, String userSkin, String targetName, String targetSkin,
                                             EntityEquipment equip, EntityEquipment targetEquip) {
        return parsePlaceholders(args, userName, userName, userSkin, targetName, targetName, targetSkin, equip, targetEquip);
    }

    public static String[] parsePlaceholders(String[] args, String userName, String userDisplayname, String userSkin, String targetName,
                                             String targetDisplayname, String targetSkin, EntityEquipment equip,
                                             EntityEquipment targetEquip) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            arg = replace(arg, "%name%", userName);
            arg = replace(arg, "%displayname%", userDisplayname);
            arg = replace(arg, "%skin%", userSkin);
            arg = replace(arg, "%held-item%", equip == null ? null : equip.getItemInMainHand());
            arg = replace(arg, "%offhand-item%", equip == null ? null : equip.getItemInOffHand());
            arg = replace(arg, "%armor%", equip == null ? null : equip.getArmorContents());
            arg = replace(arg, "%helmet%", equip == null ? null : equip.getHelmet());
            arg = replace(arg, "%chestplate%", equip == null ? null : equip.getChestplate());
            arg = replace(arg, "%leggings%", equip == null ? null : equip.getLeggings());
            arg = replace(arg, "%boots%", equip == null ? null : equip.getBoots());

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!arg.contains("%" + p.getName() + "-")) {
                    continue;
                }

                String name = p.getName();

                arg = replace(arg, "%" + name + "-name%", name);
                arg = replace(arg, "%" + name + "-displayname%", DisguiseUtilities.getDisplayName(p));
                arg = replace(arg, "%" + name + "-skin%", getSkin(p));

                EntityEquipment pEquip = p.getEquipment();

                arg = replace(arg, "%" + name + "-held-item%", pEquip == null ? null : pEquip.getItemInMainHand());
                arg = replace(arg, "%" + name + "-offhand-item%", pEquip == null ? null : pEquip.getItemInOffHand());
                arg = replace(arg, "%" + name + "-armor%", pEquip == null ? null : pEquip.getArmorContents());
                arg = replace(arg, "%" + name + "-helmet%", pEquip == null ? null : pEquip.getHelmet());
                arg = replace(arg, "%" + name + "-chestplate%", pEquip == null ? null : pEquip.getChestplate());
                arg = replace(arg, "%" + name + "-leggings%", pEquip == null ? null : pEquip.getLeggings());
                arg = replace(arg, "%" + name + "-boots%", pEquip == null ? null : pEquip.getBoots());
            }

            arg = replace(arg, "%target-name%", targetName);
            arg = replace(arg, "%target-displayname%", targetDisplayname);
            arg = replace(arg, "%target-skin%", targetSkin);
            arg = replace(arg, "%target-held-item%", targetEquip == null ? null : targetEquip.getItemInMainHand());
            arg = replace(arg, "%target-offhand-item%", targetEquip == null ? null : targetEquip.getItemInOffHand());
            arg = replace(arg, "%target-armor%", targetEquip == null ? null : targetEquip.getArmorContents());
            arg = replace(arg, "%target-helmet%", targetEquip == null ? null : targetEquip.getHelmet());
            arg = replace(arg, "%target-chestplate%", targetEquip == null ? null : targetEquip.getChestplate());
            arg = replace(arg, "%target-leggings%", targetEquip == null ? null : targetEquip.getLeggings());
            arg = replace(arg, "%target-boots%", targetEquip == null ? null : targetEquip.getBoots());

            args[i] = arg;
        }

        return args;
    }

    private static String replace(String string, String value, Object toReplace) {
        if (!string.contains(value)) {
            return string;
        }

        String oValue;

        if (toReplace != null) {
            oValue = ParamInfoManager.toString(toReplace);
        } else {
            oValue = "null";
        }

        return string.replace(value, oValue);
    }

    public static long parseStringToTime(String string) throws DisguiseParseException {
        string = string.toLowerCase(Locale.ENGLISH);

        if (!string.matches("([0-9]+[a-z]+)+")) {
            throw new DisguiseParseException(LibsMsg.PARSE_INVALID_TIME_SEQUENCE, string);
        }

        String[] split = string.split("((?<=[a-zA-Z])(?=[0-9]))|((?<=[0-9])(?=[a-zA-Z]))");

        long time = 0;

        for (int i = 0; i < split.length; i += 2) {
            String t = split[i + 1];
            long v = Long.parseLong(split[i]);

            if (t.equals("s") || t.equals("sec") || t.equals("secs") || t.equals("seconds")) {
                time += v;
            } else if (t.equals("m") || t.equals("min") || t.equals("minute") || t.equals("minutes")) {
                time += TimeUnit.MINUTES.toSeconds(v);
            } else if (t.equals("h") || t.equals("hour") || t.equals("hours")) {
                time += TimeUnit.HOURS.toSeconds(v);
            } else if (t.equals("d") || t.equals("day") || t.equals("days")) {
                time += TimeUnit.DAYS.toSeconds(v);
            } else if (t.equals("w") || t.equals("week") || t.equals("weeks")) {
                time += TimeUnit.DAYS.toSeconds(v) * 7;
            } else if (t.equals("mon") || t.equals("month") || t.equals("months")) {
                time += TimeUnit.DAYS.toSeconds(v) * 31;
            } else if (t.equals("y") || t.equals("year") || t.equals("years")) {
                time += TimeUnit.DAYS.toSeconds(v) * 365;
            } else {
                throw new DisguiseParseException(LibsMsg.PARSE_INVALID_TIME, t);
            }
        }

        return time;
    }

    /**
     * Experimentally parses the arguments to test if this is a valid disguise
     *
     * @param sender
     * @param permNode
     * @param args
     * @param permissions
     * @return
     * @throws DisguiseParseException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Disguise parseTestDisguise(CommandSender sender, String permNode, String[] args, DisguisePermissions permissions)
        throws Throwable {

        // Clone array so original array isn't modified
        args = Arrays.copyOf(args, args.length);

        String skin = "{\"id\":\"a149f81bf7844f8987c554afdd4db533\",\"name\":\"libraryaddict\"," + "\"properties\":[]}";
        // Fill in fake data
        args = parsePlaceholders(args, "libraryaddict", skin, "libraryaddict", skin, null, null);

        // Parse disguise
        return parseDisguise(sender, null, permNode, args, permissions);
    }

    public static void modifyDisguise(Disguise disguise, Entity target, String[] params) throws Throwable {
        if (target != null) {
            params = DisguiseParser.parsePlaceholders(params, target, target);
        }

        DisguiseParser.callMethods(Bukkit.getConsoleSender(), disguise,
            DisguisePermissions.getPermissions(Bukkit.getConsoleSender(), "disguise"), new DisguisePerm(disguise.getType()),
            new ArrayList<>(), params, "Disguise");
    }

    public static void modifyDisguise(Disguise disguise, String[] params) throws Throwable {
        modifyDisguise(disguise, null, params);
    }

    public static void modifyDisguise(Disguise disguise, String params) throws Throwable {
        modifyDisguise(disguise, DisguiseUtilities.split(params));
    }

    public static void modifyDisguise(Disguise disguise, Entity target, String params) throws Throwable {
        modifyDisguise(disguise, target, DisguiseUtilities.split(params));
    }

    public static Disguise parseDisguise(String disguise) throws Throwable {
        return parseDisguise(Bukkit.getConsoleSender(), null, disguise);
    }

    public static Disguise parseDisguise(CommandSender sender, Entity target, String disguise) throws Throwable {
        return parseDisguise(sender, target, "disguise", DisguiseUtilities.split(disguise),
            DisguisePermissions.getPermissions(Bukkit.getConsoleSender(), "disguise"));
    }

    /**
     * Returns the disguise if it all parsed correctly. Returns a exception with a complete message if it didn't. The
     * commandsender is purely used for checking permissions. Would defeat the purpose otherwise. To reach this
     * point, the
     * disguise has been feed a proper disguisetype.
     */
    public static Disguise parseDisguise(CommandSender sender, Entity target, String permNode, String[] args,
                                         DisguisePermissions permissions) throws Throwable {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("DisguiseParser should not be called async!");
        }

        if (sender instanceof Player) {
            DisguiseUtilities.setCommandsUsed();
        }

        if (!permissions.hasPermissions()) {
            throw new DisguiseParseException(LibsMsg.NO_PERM);
        }

        if (args.length == 0) {
            throw new DisguiseParseException(LibsMsg.PARSE_NO_ARGS);
        }

        // How many args to skip due to the disugise being constructed
        // Time to start constructing the disguise.
        // We will need to check between all 3 kinds of disguises
        int toSkip = 1;
        ArrayList<String> usedOptions = new ArrayList<>();
        Disguise disguise = null;
        DisguisePerm disguisePerm;
        String name;
        boolean hasSetCustomName = false;

        if (args[0].startsWith("@")) {
            if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
                disguise = DisguiseUtilities.getClonedDisguise(args[0].toLowerCase(Locale.ENGLISH));

                if (disguise == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_NO_REF, args[0]);
                }
            } else {
                throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_REF);
            }

            disguisePerm = new DisguisePerm(disguise.getType());
            name = disguise.getDisguiseName();
            hasSetCustomName = disguise.isCustomDisguiseName();

            if (disguisePerm.isUnknown()) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
            }

            if (disguisePerm.getEntityType() == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_LOAD);
            }

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                throw new DisguiseParseException(LibsMsg.NO_PERM_DISGUISE);
            }
        } else {
            disguisePerm = getDisguisePerm(args[0]);

            if (disguisePerm == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_DISG_NO_EXIST, args[0]);
            }

            name = disguisePerm.toReadable();

            if (disguisePerm.getType().isCustom()) {
                ModdedEntity ent = ModdedManager.getModdedEntity(disguisePerm.toReadable());

                if (ent == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
                }

                disguise = new ModdedDisguise(ent);
                hasSetCustomName = true;
            }

            Entry<DisguisePerm, String> customDisguise = DisguiseConfig.getRawCustomDisguise(args[0]);

            if (customDisguise != null) {
                String[] oldArgs = Arrays.copyOfRange(args, 1, args.length);

                // Fill args with custom disguise instead
                args = DisguiseUtilities.split(customDisguise.getValue());

                // Expand the array for the old args
                args = Arrays.copyOf(args, args.length + oldArgs.length);

                // Copy the original args into the array
                for (int i = 0; i < oldArgs.length; i++) {
                    args[args.length - (oldArgs.length - i)] = oldArgs[i];
                }

                name = customDisguise.getKey().toReadable();
                hasSetCustomName = true;
            }

            args = parsePlaceholders(args, sender == null ? target : sender, target);

            if (disguisePerm.isUnknown()) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_DISG_UNKNOWN);
            }

            if (disguisePerm.getEntityType() == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_CANT_LOAD);
            }

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                throw new DisguiseParseException(LibsMsg.NO_PERM_DISGUISE);
            }

            HashMap<String, HashMap<String, Boolean>> disguiseOptions =
                DisguisePermissions.getDisguiseOptions(sender, permNode, disguisePerm);

            if (disguise == null) {
                WatcherMethod[] watcherMethods = ParamInfoManager.getDisguiseWatcherMethods(disguisePerm.getWatcherClass(), true);
                String method = null;
                Object param = null;

                if (args.length > 1) {
                    String[] argArray = args;

                    if (Arrays.stream(watcherMethods).noneMatch(m -> m.getMappedName().equalsIgnoreCase(argArray[1]))) {
                        for (ExtraDisguiseParam extra : extraDisguiseParams) {
                            if (!extra.isApplicable(disguisePerm.getType(), args[1])) {
                                continue;
                            }

                            method = extra.getParameterMethod();

                            try {
                                param = extra.createParametervalue(sender, args[1]);
                            } catch (DisguiseParseException ex) {
                                throw ex;
                            } catch (Throwable throwable) {
                                throw new DisguiseParseException(LibsMsg.PARSE_EXPECTED_RECEIVED, extra.getParamInfo().getDescriptiveName(),
                                    args[1], TranslateType.DISGUISE_OPTIONS.reverseGet(TranslateType.DISGUISE_OPTIONS.reverseGet(method)));
                            }

                            extra.checkParameterPermission(sender, permissions, disguiseOptions, usedOptions, disguisePerm, param);
                            toSkip++;
                            break;
                        }
                    }
                }

                if (disguisePerm.isPlayer()) {
                    // If he is doing a player disguise
                    if (args.length == 1) {
                        // He needs to give the player name
                        throw new DisguiseParseException(LibsMsg.PARSE_SUPPLY_PLAYER);
                    } else {
                        if (method == null) {
                            param = "Nameless Player";
                        } else if (!"setName".equalsIgnoreCase(method)) {
                            throw new IllegalStateException(
                                "Expected setName to be defined, this is an internal error, not a user error. Method was " + method);
                        }

                        // Construct the player disguise
                        disguise = new PlayerDisguise((String) param);

                        // Prevent this being set later
                        method = null;

                        if (!hasSetCustomName) {
                            name = ((PlayerDisguise) disguise).getName();
                        }
                    }
                } else if (disguisePerm.isMob()) {
                    // Its a mob, use the mob constructor
                    disguise = new MobDisguise(disguisePerm.getType());
                } else if (disguisePerm.isMisc()) {
                    // Its a misc, we are going to use the MiscDisguise constructor.
                    disguise = new MiscDisguise(disguisePerm.getType());
                }

                if (method != null && param != null) {
                    WatcherMethod m = null;

                    for (WatcherMethod method1 : watcherMethods) {
                        if (!method1.getMappedName().equalsIgnoreCase(method)) {
                            continue;
                        }

                        if (!method1.getParam().isAssignableFrom(param.getClass())) {
                            continue;
                        }

                        m = method1;
                        break;
                    }

                    if (m == null) {
                        throw new DisguiseParseException(LibsMsg.PARSE_CANT_LOAD_DETAILS, args[1], method);
                    }

                    MethodHandle handle = m.getMethod();

                    if (ReflectionManager.isAssignableFrom(FlagWatcher.class, m.getWatcherClass())) {
                        handle = handle.bindTo(disguise.getWatcher());
                    } else {
                        handle = handle.bindTo(disguise);
                    }

                    handle.invoke(param);
                }

                if (!hasSetCustomName && !disguisePerm.isPlayer()) {
                    name = disguise.getDisguiseName();
                }
            }
        }

        disguise.setDisguiseName(name);
        disguise.setCustomDisguiseName(hasSetCustomName);

        // Copy strings to their new range
        String[] newArgs = new String[args.length - toSkip];
        System.arraycopy(args, toSkip, newArgs, 0, args.length - toSkip);

        callMethods(sender, disguise, permissions, disguisePerm, usedOptions, newArgs, permNode);

        if (sender instanceof Player && target instanceof Player && "%%__USER__%%".equals("15" + "92") &&
            ThreadLocalRandom.current().nextBoolean()) {
            ((TargetedDisguise) disguise).setDisguiseTarget(TargetedDisguise.TargetType.HIDE_DISGUISE_TO_EVERYONE_BUT_THESE_PLAYERS);
            ((TargetedDisguise) disguise).addPlayer((Player) sender);
        }

        // Alright. We've constructed our disguise.
        return disguise;
    }

    public static void callMethods(CommandSender sender, Disguise disguise, DisguisePermissions disguisePermission,
                                   DisguisePerm disguisePerm, Collection<String> usedOptions, String[] args, String permNode)
        throws Throwable {
        WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguise.getWatcher().getClass(), true);
        List<String> list = new ArrayList<>(Arrays.asList(args));
        HashMap<String, HashMap<String, Boolean>> disguiseOptions = DisguisePermissions.getDisguiseOptions(sender, permNode, disguisePerm);

        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            // This is the method name they provided
            String methodNameProvided = list.remove(0);
            // Translate the name they provided, to a name we recognize
            String methodNameJava = TranslateType.DISGUISE_OPTIONS.reverseGet(methodNameProvided);
            // The method we'll use
            WatcherMethod methodToUse = null;
            Object valueToSet = null;
            DisguiseParseException parseException = null;

            if (!list.isEmpty() && methodNameProvided.equalsIgnoreCase("setCustomData") && (sender == null || sender.isOp())) {
                argIndex++;
                String data = list.remove(0);
                Map<String, String> deserial = DisguiseUtilities.getGson().fromJson(data, LinkedHashMap.class);

                for (Entry<String, String> entry : deserial.entrySet()) {
                    String val = entry.getValue();

                    if (!val.contains(":")) {
                        disguise.addCustomData(entry.getKey(), null);
                        continue;
                    }

                    String className = val.substring(0, val.indexOf(":"));
                    val = val.substring(className.length() + 1);

                    disguise.addCustomData(entry.getKey(), DisguiseUtilities.getGson().fromJson(val, DisguiseMethods.parseType(className)));
                }

                continue;
            }

            for (WatcherMethod method : methods) {
                if (!method.getMappedName().equalsIgnoreCase(methodNameJava)) {
                    continue;
                }

                ParamInfo paramInfo = ParamInfoManager.getParamInfo(method);

                try {
                    // Store how many args there were before calling the param
                    int argCount = list.size();

                    if (argCount < paramInfo.getMinArguments()) {
                        throw new DisguiseParseException(LibsMsg.PARSE_NO_OPTION_VALUE,
                            TranslateType.DISGUISE_OPTIONS.reverseGet(method.getMappedName()));
                    }

                    valueToSet = paramInfo.fromString(list);

                    if (valueToSet == null && !paramInfo.canReturnNull()) {
                        throw new IllegalStateException();
                    }

                    // Skip ahead as many args as were consumed on successful parse
                    argIndex += argCount - list.size();

                    methodToUse = method;
                    // We've found a method which will accept a valid value, break
                    break;
                } catch (DisguiseParseException ex) {
                    parseException = ex;
                } catch (Exception ex) {
                    parseException = new DisguiseParseException(LibsMsg.PARSE_EXPECTED_RECEIVED, paramInfo.getDescriptiveName(),
                        list.isEmpty() ? null : list.get(0), TranslateType.DISGUISE_OPTIONS.reverseGet(method.getMappedName()));
                }
            }

            if (methodToUse == null) {
                if (parseException != null) {
                    throw parseException;
                }

                throw new DisguiseParseException(LibsMsg.PARSE_OPTION_NA, methodNameProvided);
            }

            if (!usedOptions.contains(methodToUse.getMappedName().toLowerCase(Locale.ENGLISH))) {
                usedOptions.add(methodToUse.getMappedName().toLowerCase(Locale.ENGLISH));
            }

            doCheck(sender, disguisePermission, disguisePerm, usedOptions);

            if (!disguiseOptions.isEmpty()) {
                String stringValue = ParamInfoManager.toString(valueToSet);

                if (!DisguisePermissions.hasPermissionOption(disguiseOptions, methodToUse.getMappedName(), stringValue)) {
                    throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, stringValue, disguisePerm.toReadable());
                }
            }

            if (DisguiseConfig.isArmorstandsName() && ((methodToUse.getMappedName().equals("setName") && disguise.isPlayerDisguise()) ||
                (DisguiseConfig.isOverrideCustomNames() && methodToUse.getMappedName().equals("setCustomName"))) &&
                !sender.hasPermission("libsdisguises.multiname")) {
                valueToSet = DisguiseUtilities.quoteNewLine((String) valueToSet);
            }

            MethodHandle handle = methodToUse.getMethod();

            if (FlagWatcher.class.isAssignableFrom(methodToUse.getWatcherClass())) {
                handle = handle.bindTo(disguise.getWatcher());
            } else {
                handle = handle.bindTo(disguise);
            }

            handle.invoke(valueToSet);
        }

        if (disguise instanceof PlayerDisguise) {
            grabSkin(sender, getSkin(args), (PlayerDisguise) disguise);
        }

        if (disguise instanceof PlayerDisguise && args.length > 1) {
            for (int i = 0; i < args.length - 1; i++) {
                if (!args[i].equalsIgnoreCase("setSkin")) {
                    continue;
                }

                grabSkin(sender, args[i + 1], (PlayerDisguise) disguise);
                break;
            }
        }
    }
}
