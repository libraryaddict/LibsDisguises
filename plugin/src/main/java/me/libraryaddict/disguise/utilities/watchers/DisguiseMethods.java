package me.libraryaddict.disguise.utilities.watchers;

import com.google.gson.Gson;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherInfo;
import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundCategory;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DisguiseMethods {
    private final Map<Class<? extends FlagWatcher>, List<WatcherMethod>> watcherMethods = new HashMap<>();
    private final Map<Class<? extends Disguise>, List<WatcherMethod>> disguiseMethods = new HashMap<>();
    @Getter
    private final List<WatcherMethod> methods = new ArrayList<>();

    public List<WatcherMethod> getMethods(Class c) {
        List<WatcherMethod> methods = new ArrayList<>();

        if (watcherMethods.containsKey(c)) {
            methods.addAll(watcherMethods.get(c));
        }

        if (c != FlagWatcher.class) {
            // Only adds a method if the method name is not being used already
            methods.addAll(
                getMethods(c.getSuperclass()).stream().filter(m -> methods.stream().noneMatch(m1 -> m1.getName().equals(m.getName())))
                    .collect(Collectors.toList()));
        }

        return methods;
    }

    public DisguiseMethods() {
        DisguiseConfig.loadPreConfig();
        loadMethods();
        validateMethods();
    }

    private void validateMethods() {
        for (Map.Entry<Class<? extends FlagWatcher>, List<WatcherMethod>> entry : watcherMethods.entrySet()) {
            for (WatcherMethod method : entry.getValue()) {
                // We want to only validate remapped methods
                // TODO Validate this, it's been broken for a while and no issues has seemingly arisen...
                if (method.getMappedName().equals(method.getMappedName())) {
                //if (method.getName().equals(method.getMappedName())) {
                    continue;
                }

                for (WatcherMethod method2 : entry.getValue()) {
                    if (method == method2 || !method.getMappedName().equalsIgnoreCase(method2.getMappedName())) {
                        continue;
                    }

                    throw new IllegalArgumentException(
                        "In " + entry.getKey() + ", " + method.getMappedName() + " wants to overload " + method2.getMappedName() + " but " +
                            method2.getMappedName() +
                            " well, exists. Which shouldn't be the case. It should either be unsupported version or unsupported parameter" +
                            ". Otherwise it shouldn't be trying to overload it");
                }
            }
        }
    }

    private void loadMethods() {
        List<String> notedSkippedParamTypes = new ArrayList<>();

        try (InputStream stream = LibsDisguises.getInstance().getResource("METHOD_MAPPINGS.txt")) {
            Map<String, Class<? extends FlagWatcher>> classes = new HashMap<>();
            classes.put(FlagWatcher.class.getSimpleName(), FlagWatcher.class);

            for (DisguiseType t : DisguiseType.values()) {
                if (t.getWatcherClass() == null) {
                    continue;
                }

                Class c = t.getWatcherClass();

                while (!classes.containsKey(c.getSimpleName())) {
                    classes.put(c.getSimpleName(), c);

                    if (c == FlagWatcher.class) {
                        break;
                    }

                    c = ReflectionManager.getSuperClass(c);
                }
            }

            WatcherInfo[] watcherInfos =
                new Gson().fromJson(new String(ReflectionManager.readFuzzyFully(stream), StandardCharsets.UTF_8), WatcherInfo[].class);
            int other = -1;

            for (int i = 0; i < watcherInfos.length; i++) {
                WatcherInfo info = watcherInfos[i];

                if (info == null || !info.isSupported()) {
                    continue;
                }

                if (info.isDeprecated() && info.getAdded() != 0 && info.getRemoved() < 0) {
                    continue;
                }

                Class<? extends FlagWatcher> watcher = classes.get(info.getWatcher());

                if (watcher == null) {
                    continue;
                }

                Class param = parseType(info.getParam());
                Class returnType = parseType(info.getReturnType());

                Class methodType = param == null || param == Void.TYPE ? returnType : param;
                ParamInfo paramType = ParamInfoManager.getParamInfo(methodType);

                if (paramType == null) {
                    String name = methodType.isArray() ? methodType.getComponentType().getName() + "[]" : methodType.getName();

                    if (!notedSkippedParamTypes.contains(name) && !LibsDisguises.getInstance().isJenkins()) {
                        notedSkippedParamTypes.add(name);
                        LibsDisguises.getInstance().getLogger()
                            .info("DEBUG: Skipped method using " + name + ", don't need it in experimental builds");
                    }

                    continue;
                }

                MethodType type =
                    param == null || param == Void.TYPE ? MethodType.methodType(returnType) : MethodType.methodType(returnType, param);

                MethodHandle method = MethodHandles.publicLookup().findVirtual(watcher, info.getMethod(), type);
                boolean[] unusableBy = new boolean[DisguiseType.values().length];
                boolean[] hiddenFor = new boolean[DisguiseType.values().length];

                for (int unusable : info.getUnusableBy()) {
                    unusableBy[unusable] = true;
                }

                for (int unusable : info.getHiddenFor()) {
                    hiddenFor[unusable] = true;
                }

                WatcherMethod m =
                    new WatcherMethod(watcher, method, info.getMappedAs(), info.getMethod(), returnType, param, info.isRandomDefault(),
                        info.isDeprecated() && info.getAdded() == 0, unusableBy, hiddenFor, info.getDescription(),
                        info.isNoVisibleDifference(), info.getAdded(), info.getRemoved());

                if (i % 2 == 0) {
                    other = i;
                } else if (i - 1 != other) {
                    throw new IllegalArgumentException("Expected companion parameter but it was missing");
                } else {
                    WatcherMethod setter = methods.get(methods.size() - 1);
                    m.setCompanionMethod(setter);
                    setter.setCompanionMethod(m);
                }

                methods.add(m);

                if (m.getMappedName().startsWith("get") || param == null || param == Void.TYPE) {
                    continue;
                }

                if (ParamInfoManager.getParamInfo(m) == null) {
                    continue;
                }

                watcherMethods.computeIfAbsent(watcher, (a) -> new ArrayList<>()).add(m);
            }

            addExtraMethods();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void addExtraMethods() {
        List<String> extraMethods = new ArrayList<>(
            Arrays.asList("setSelfDisguiseVisible", "setHideHeldItemFromSelf", "setHideArmorFromSelf", "setHearSelfDisguise",
                "setReplaceSounds", "setHidePlayer", "setExpires", "setNotifyBar", "setBossBarColor", "setBossBarStyle", "setDynamicName",
                "setSoundGroup", "setDisguiseName", "setDeadmau5Ears", "setTallSelfDisguise", "setPlayIdleSounds", "setSoundCategory"));

        // Methods hidden by default, mainly so it doesn't throw an exception if used
        // This means that any options in this will be set in a disguise, but it won't be copied when a disguise is parsed to string
        // And it won't appear in tab complete
        List<String> hiddenAndIgnored = new ArrayList<>(Arrays.asList("setTallDisguisesVisible", "setTallSelfDisguisesScaling"));

        if (NmsVersion.v1_20_R4.isSupported()) {
            extraMethods.add("setScalePlayerToDisguise");
        }

        extraMethods.addAll(hiddenAndIgnored);

        // Add these last as it's what we want to present to be called the least
        for (String methodName : extraMethods) {
            try {
                Class cl = boolean.class;
                Class disguiseClass = Disguise.class;
                boolean randomDefault = false;

                switch (methodName) {
                    case "setExpires":
                        cl = long.class;
                        break;
                    case "setNotifyBar":
                        cl = DisguiseConfig.NotifyBar.class;
                        break;
                    case "setBossBarColor":
                        cl = BarColor.class;
                        break;
                    case "setBossBarStyle":
                        cl = BarStyle.class;
                        break;
                    case "setDisguiseName":
                        randomDefault = true;
                        cl = String.class;
                        break;
                    case "setSoundGroup":
                        cl = String.class;
                        break;
                    case "setDeadmau5Ears":
                        disguiseClass = PlayerDisguise.class;
                        break;
                    case "setTallSelfDisguise":
                        cl = DisguiseConfig.TallSelfDisguise.class;
                        break;
                    case "setSoundCategory":
                        cl = DisguiseSoundCategory.class;
                        break;
                    default:
                        break;
                }

                for (Class returnType : new Class[]{Void.TYPE, disguiseClass}) {
                    try {
                        boolean hidden = hiddenAndIgnored.contains(methodName);

                        WatcherMethod setMethod = new WatcherMethod(disguiseClass,
                            MethodHandles.publicLookup().findVirtual(disguiseClass, methodName, MethodType.methodType(returnType, cl)),
                            methodName, methodName, null, cl, randomDefault, hidden, new boolean[DisguiseType.values().length],
                            new boolean[DisguiseType.values().length], null, false, 0, 0);

                        methods.add(setMethod);

                        watcherMethods.computeIfAbsent(disguiseClass == Disguise.class ? FlagWatcher.class : PlayerWatcher.class,
                            (a) -> new ArrayList<>()).add(setMethod);

                        String getName = (cl == boolean.class ? "is" : "get") + methodName.substring(3);

                        WatcherMethod getMethod = new WatcherMethod(disguiseClass,
                            MethodHandles.publicLookup().findVirtual(disguiseClass, getName, MethodType.methodType(cl)), getName, getName,
                            cl, null, randomDefault, hidden, new boolean[DisguiseType.values().length],
                            new boolean[DisguiseType.values().length], null, false, 0, 0);

                        methods.add(getMethod);

                        setMethod.setCompanionMethod(getMethod);
                        getMethod.setCompanionMethod(setMethod);
                        break;
                    } catch (NoSuchMethodException ex) {
                        if (returnType == disguiseClass) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Return the java {@link java.lang.Class} object with the specified class name.
     * <p>
     * This is an "extended" {@link java.lang.Class#forName(java.lang.String) } operation.
     * <p>
     * + It is able to return Class objects for primitive types
     * + Classes in name space `java.lang` do not need the fully qualified name
     * + It does not throw a checked Exception
     *
     * @param className The class name, never `null`
     * @throws IllegalArgumentException if no class can be loaded
     */
    public static Class<?> parseType(final String className) throws ClassNotFoundException {
        if (className == null) {
            return null;
        }

        if (className.contains(".")) {
            return Class.forName(className);
        }

        switch (className) {
            case "void":
                return Void.TYPE;
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "[I":
                return int[].class;
            case "[Z":
                return boolean[].class;
            default:
                throw new IllegalArgumentException("Class not found: " + className);
        }
    }
}
