package me.libraryaddict.disguise.utilities.watchers;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.asm.WatcherInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 13/02/2020.
 */
public class DisguiseMethods {
    private HashMap<Class<? extends FlagWatcher>, List<Method>> watcherMethods = new HashMap<>();

    public ArrayList<Method> getMethods(Class c) {
        ArrayList<Method> methods = new ArrayList<>();

        if (watcherMethods.containsKey(c)) {
            methods.addAll(watcherMethods.get(c));
        }

        if (c != FlagWatcher.class) {
            methods.addAll(getMethods(c.getSuperclass()));
        }

        return methods;
    }

    public ArrayList<Method> getMethods() {
        ArrayList<Method> methods = new ArrayList<>();

        this.watcherMethods.values().forEach(methods::addAll);

        return methods;
    }

    public DisguiseMethods() {
        try (InputStream stream = LibsDisguises.getInstance().getResource("ANTI_PIRACY_ENCRYPTION")) {
            List<String> lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.toList());

            HashMap<String, Class<? extends FlagWatcher>> classes = new HashMap<>();
            classes.put(FlagWatcher.class.getSimpleName(), FlagWatcher.class);

            for (DisguiseType t : DisguiseType.values()) {
                if (t.getWatcherClass() == null) {
                    continue;
                }

                Class c = t.getWatcherClass();

                while (!classes.containsKey(c.getSimpleName())) {
                    classes.put(c.getSimpleName(), c);
                    c = ReflectionManager.getSuperClass(c);
                }
            }

            for (String line : lines) {
                WatcherInfo info = new WatcherInfo(line);

                if (!info.isSupported() || info.getParam().isEmpty()) {
                    continue;
                }

                Class<? extends FlagWatcher> watcher = classes.get(info.getWatcher());

                if (watcher == null) {
                    continue;
                }

                String paramName = info.getParam();
                Class param;

                if (!paramName.contains(".")) {
                    param = parseType(paramName);
                } else {
                    param = Class.forName(paramName);
                }

                Method method = watcher.getMethod(info.getMethod(), param);

                if (method.getParameterCount() != 1) {
                    continue;
                } else if (method.getName().startsWith("get")) {
                    continue;
                } else if (method.isAnnotationPresent(Deprecated.class)) {
                    continue;
                } else if (!method.getReturnType().equals(Void.TYPE)) {
                    continue;
                } else if (ParamInfoManager.getParamInfo(method) == null) {
                    continue;
                }

                watcherMethods.computeIfAbsent(watcher, (a) -> new ArrayList<>()).add(method);
            }
        }
        catch (IOException | ClassNotFoundException | NoClassDefFoundError | NoSuchMethodException e) {
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
    private Class<?> parseType(final String className) {
        switch (className) {
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
            default:
                throw new IllegalArgumentException("Class not found: " + className);
        }
    }
}
