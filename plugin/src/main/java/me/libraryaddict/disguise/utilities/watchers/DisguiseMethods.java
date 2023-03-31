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
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherInfo;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by libraryaddict on 13/02/2020.
 */
public class DisguiseMethods {
    private final HashMap<Class<? extends FlagWatcher>, List<WatcherMethod>> watcherMethods = new HashMap<>();
    private final HashMap<Class<? extends Disguise>, List<WatcherMethod>> disguiseMethods = new HashMap<>();
    @Getter
    private final ArrayList<WatcherMethod> methods = new ArrayList<>();

    public ArrayList<WatcherMethod> getMethods(Class c) {
        ArrayList<WatcherMethod> methods = new ArrayList<>();

        if (watcherMethods.containsKey(c)) {
            methods.addAll(watcherMethods.get(c));
        }

        if (c != FlagWatcher.class) {
            methods.addAll(getMethods(c.getSuperclass()));
        }

        return methods;
    }

    public DisguiseMethods() {
        loadMethods();
    }

    private void loadMethods() {
        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCRYPTION")) {
            String[] lines = new String(ReflectionManager.readFuzzyFully(stream), StandardCharsets.UTF_8).split("\n");

            HashMap<String, Class<? extends FlagWatcher>> classes = new HashMap<>();
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

            for (String line : lines) {
                WatcherInfo info = new Gson().fromJson(line, WatcherInfo.class);

                if (!info.isSupported()) {
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

                String paramName = info.getParam();

                MethodType type = param == null || param == Void.TYPE ? MethodType.methodType(returnType) : MethodType.methodType(returnType, param);

                MethodHandle method = MethodHandles.publicLookup().findVirtual(watcher, info.getMethod(), type);

                WatcherMethod m =
                    new WatcherMethod(watcher, method, info.getMethod(), returnType, param, info.isRandomDefault(), info.isDeprecated() && info.getAdded() == 0,
                        info.getUnusableBy());

                methods.add(m);

                if (m.getName().startsWith("get") || m.getName().equals("hasPotionEffect") || param == null || param == Void.TYPE ||
                    ParamInfoManager.getParamInfo(m) == null) {
                    continue;
                }

                watcherMethods.computeIfAbsent(watcher, (a) -> new ArrayList<>()).add(m);
            }

            PlayerDisguise disguise = new PlayerDisguise("");

            // Add these last as it's what we want to present to be called the least
            for (String methodName : new String[]{"setSelfDisguiseVisible", "setHideHeldItemFromSelf", "setHideArmorFromSelf", "setHearSelfDisguise",
                "setHidePlayer", "setExpires", "setNotifyBar", "setBossBarColor", "setBossBarStyle", "setTallDisguisesVisible", "setDynamicName",
                "setSoundGroup", "setDisguiseName", "setDeadmau5Ears"}) {
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
                        case "setSoundGroup":
                            cl = String.class;
                            break;
                        case "setDeadmau5Ears":
                            disguiseClass = PlayerDisguise.class;
                            break;
                        default:
                            break;
                    }

                    for (Class returnType : new Class[]{Void.TYPE, disguiseClass}) {
                        try {
                            WatcherMethod method = new WatcherMethod(disguiseClass,
                                MethodHandles.publicLookup().findVirtual(disguiseClass, methodName, MethodType.methodType(returnType, cl)), methodName, null,
                                cl, randomDefault, false, new boolean[DisguiseType.values().length]);

                            methods.add(method);

                            watcherMethods.computeIfAbsent(disguiseClass == Disguise.class ? FlagWatcher.class : PlayerWatcher.class, (a) -> new ArrayList<>())
                                .add(method);

                            String getName = (cl == boolean.class ? "is" : "get") + methodName.substring(3);

                            WatcherMethod getMethod =
                                new WatcherMethod(disguiseClass, MethodHandles.publicLookup().findVirtual(disguiseClass, getName, MethodType.methodType(cl)),
                                    getName, cl, null, randomDefault, false, new boolean[DisguiseType.values().length]);

                            methods.add(getMethod);
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
        } catch (Throwable e) {
            e.printStackTrace();
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
