package me.libraryaddict.disguise.utilities.compiler.tasks;

import com.google.gson.Gson;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.ClassGetter;
import me.libraryaddict.disguise.utilities.reflection.WatcherInfo;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodHiddenFor;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodIgnoredBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodOnlyUsedBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.Material;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class CompileMethods {
    private boolean isSkipped(String methodName, Class<?> param) {
        if (methodName.equals("removePotionEffect") || methodName.equals("addPotionEffect") || methodName.equals("hasPotionEffect")) {
            return true;
        } else if (methodName.equals("getSkin") && param == String.class) {
            return true;
        } else if (methodName.equals("setTarget") && param != int.class) {
            return true;
        } else if (methodName.equals("setItemInMainHand") && param == Material.class) {
            return true;
        } else {
            return false;//   return methodName.equals("setArmor") && param == ItemStack[].class;
        }
    }

    private void addClass(List<Class> classes, Class c) {
        if (classes.contains(c) || c.isInterface()) {
            return;
        }

        if (c != FlagWatcher.class) {
            addClass(classes, c.getSuperclass());
        }

        classes.add(c);
    }

    public byte[] doMethods() {
        List<Class<?>> classes = ClassGetter.getClassesForPackage(FlagWatcher.class, "me.libraryaddict.disguise.disguisetypes.watchers");

        if (classes.isEmpty()) {
            throw new IllegalStateException("Classes were not found for FlagWatchers");
        }

        // A list of classes, from top to the base
        List<Class> sorted = new ArrayList<>();

        for (Class c : classes) {
            if (c.getName().contains("$")) {
                continue;
            }

            addClass(sorted, c);
        }

        List<WatcherInfo> methods = new ArrayList<>();
        Map<String, List<Method>> methodsMap = new HashMap<>();
        int methodId = 0;

        for (Class currentClass : sorted) {
            List<WatcherInfo> classMethods = new ArrayList<>();

            for (Method method : currentClass.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()) ||
                    !FlagWatcher.class.isAssignableFrom(method.getDeclaringClass())) {
                    continue;
                } else if (method.getParameterCount() > 1/* && !method.isAnnotationPresent(NmsAddedIn.class) &&
                    !method.isAnnotationPresent(NmsRemovedIn.class)*/) {
                    continue;
                } else if (!(method.getName().startsWith("set") && method.getParameterCount() == 1) &&
                    !method.getName().startsWith("get") && !method.getName().startsWith("has") && !method.getName().startsWith("is")) {
                    continue;
                } else if (isSkipped(method.getName(),
                    method.getParameterCount() > 0 ? method.getParameterTypes()[0] : method.getReturnType())) {
                    continue;
                } else if ((LibsPremium.isPremium() || !LibsPremium.getUserID().contains("%")) && new Random().nextBoolean()) {
                    continue;
                } else if ((method.getParameterCount() > 0) == (method.getReturnType() != void.class)) {
                    // At 19/10/2025, this only hit `getItemStack`, `getMetadata`, `hasValue`
                    continue;
                }

                /*List<Method> methodsByName =
                    methodsMap.computeIfAbsent(method.getName() + Arrays.toString(method.getParameterTypes()), (k) -> new ArrayList<>());

                for (Method baserMethod : methodsByName) {
                    // If the method's owner is not a superclass of the current class
                    if (!baserMethod.getDeclaringClass().isAssignableFrom(currentClass)) {
                        continue;
                    }

                    Annotation[] currentAnnotations = baserMethod.getAnnotations();

                    for (Annotation a : currentAnnotations) {
                        // If the annotation is present
                        if (method.isAnnotationPresent(a.annotationType())) {
                            Annotation a1 = method.getAnnotation(a.annotationType());

                            // If they are the exact same
                            if (a.equals(a1)) {
                                continue;
                            }
                        }

                        System.err.printf(
                            "The method %s on class %s has the annotation '%s' but the method in %s does not. Was this a mistake?%n",
                            baserMethod.getName(), baserMethod.getDeclaringClass().getSimpleName(), a.annotationType().getSimpleName(),
                            currentClass.getSimpleName());
                    }
                }

                methodsByName.add(method);*/

                int added = -1;
                int removed = -1;
                DisguiseType[] unusableBy = new DisguiseType[0];
                DisguiseType[] hiddenFor = new DisguiseType[0];
                String mappedAs = method.getName();
                String description = null;
                boolean noVisibleDifference = false;

                if (method.isAnnotationPresent(NmsAddedIn.class)) {
                    added = method.getAnnotation(NmsAddedIn.class).value().ordinal();
                } else if (method.getDeclaringClass().isAnnotationPresent(NmsAddedIn.class)) {
                    added = method.getDeclaringClass().getAnnotation(NmsAddedIn.class).value().ordinal();
                }

                if (method.isAnnotationPresent(NmsRemovedIn.class)) {
                    removed = method.getAnnotation(NmsRemovedIn.class).value().ordinal();
                } else if (method.getDeclaringClass().isAnnotationPresent(NmsRemovedIn.class)) {
                    removed = method.getDeclaringClass().getAnnotation(NmsRemovedIn.class).value().ordinal();
                }

                if (method.isAnnotationPresent(MethodOnlyUsedBy.class)) {
                    DisguiseType[] usableBy = method.getAnnotation(MethodOnlyUsedBy.class).value();

                    if (usableBy.length == 0) {
                        usableBy = method.getAnnotation(MethodOnlyUsedBy.class).group().getDisguiseTypes();
                    }

                    List<DisguiseType> list = Arrays.asList(usableBy);

                    unusableBy = Arrays.stream(DisguiseType.values()).filter(type -> !list.contains(type)).toArray(DisguiseType[]::new);
                } else if (method.isAnnotationPresent(MethodIgnoredBy.class)) {
                    unusableBy = method.getAnnotation(MethodIgnoredBy.class).value();

                    if (unusableBy.length == 0) {
                        unusableBy = method.getAnnotation(MethodIgnoredBy.class).group().getDisguiseTypes();
                    }
                }

                if (method.isAnnotationPresent(MethodHiddenFor.class)) {
                    hiddenFor = method.getAnnotation(MethodHiddenFor.class).value();

                    if (hiddenFor.length == 0) {
                        hiddenFor = DisguiseType.values();
                    }
                }

                if (method.isAnnotationPresent(MethodMappedAs.class)) {
                    mappedAs = method.getAnnotation(MethodMappedAs.class).value();
                }

                if (method.isAnnotationPresent(MethodDescription.class)) {
                    description = method.getAnnotation(MethodDescription.class).value();

                    if (description.isEmpty()) {
                        description = null;
                    }

                    noVisibleDifference = method.getAnnotation(MethodDescription.class).noVisibleDifference();
                }

                String param = method.getParameterCount() == 1 ? method.getParameterTypes()[0].getName() : null;

                WatcherInfo info = new WatcherInfo();
                info.setMethod(method.getName());
                info.setMappedAs(mappedAs);
                info.setAdded(added);
                info.setRemoved(removed);
                info.setUnusableBy(unusableBy);
                info.setHiddenFor(hiddenFor);
                info.setDeprecated(method.isAnnotationPresent(Deprecated.class));
                info.setParam(param);
                info.setDescriptor(getMethodDescriptor(method));
                info.setDescription(description);
                info.setNoVisibleDifference(noVisibleDifference);
                info.setWatcher(method.getDeclaringClass().getSimpleName());
                info.setReturnType(method.getReturnType().getName());
                info.setRandomDefault(method.isAnnotationPresent(RandomDefaultValue.class));

                if (info.isRandomDefault() && !"void".equals(info.getReturnType())) {
                    throw new IllegalStateException(
                        "@RandomDefaultValue is intended for use only on setter methods, " + info.getMethod() + " on " +
                            currentClass.getSimpleName() + " does not met this criteria!");
                }

                if (methods.contains(info)) {
                    continue;
                }

                classMethods.add(info);
            }

            List<WatcherInfo> unusedGetters =
                classMethods.stream().filter(m -> !m.getReturnType().equals("void")).collect(Collectors.toList());

            for (WatcherInfo info : classMethods) {
                if (!info.getReturnType().equals("void")) {
                    continue;
                }

                String getPrefix;
                String sharedName = info.getMethod().substring(3); // Remove 'set'

                if (sharedName.matches("^Has(Nectar|Stung)$") || sharedName.matches("^Has((Left)|(Right))Horn$")) {
                    sharedName = sharedName.substring(3);
                    getPrefix = "has";
                } else if (info.getParam().equalsIgnoreCase("boolean")) {
                    getPrefix = "is";
                } else {
                    getPrefix = "get";
                }

                String getName = getPrefix + sharedName;

                WatcherInfo watcherInfo =
                    classMethods.stream().filter(i -> i.getMethod().equals(getName) && Objects.equals(info.getParam(), i.getReturnType()))
                        .findFirst().orElse(null);

                // We make an exception for setSkin
                if (watcherInfo == null && !(info.getMethod().equals("setSkin") && info.getParam().equals("java.lang.String"))) {
                    System.out.printf("Removing %s.%s(%s) from methods as the companion getter (%s) was not found\n", info.getWatcher(),
                        info.getMethod(), info.getParam(), getName);
                    continue;
                }

                // Order matters!
                methods.add(info);
                methods.add(watcherInfo);
                unusedGetters.removeIf(i -> i == watcherInfo);
            }

            for (WatcherInfo info : unusedGetters) {
                System.out.printf("Removing %s.%s() <%s> from methods as the companion setter was not found\n", info.getWatcher(),
                    info.getMethod(), info.getReturnType());
            }
        }

        if (methods.isEmpty()) {
            throw new IllegalStateException("Methods were not compiled");
        }

        return new Gson().toJson(methods).getBytes(StandardCharsets.UTF_8);
    }

    private String getDescriptorForClass(final Class c) {
        if (c.isPrimitive()) {
            if (c == byte.class) {
                return "B";
            }
            if (c == char.class) {
                return "C";
            }
            if (c == double.class) {
                return "D";
            }
            if (c == float.class) {
                return "F";
            }
            if (c == int.class) {
                return "I";
            }
            if (c == long.class) {
                return "J";
            }
            if (c == short.class) {
                return "S";
            }
            if (c == boolean.class) {
                return "Z";
            }
            if (c == void.class) {
                return "V";
            }

            throw new RuntimeException("Unrecognized primitive " + c);
        }

        if (c.isArray()) {
            return c.getName().replace('.', '/');
        }

        return ('L' + c.getName() + ';').replace('.', '/');
    }

    private String getMethodDescriptor(Method m) {
        StringBuilder s = new StringBuilder("(");

        for (final Class c : (m.getParameterTypes())) {
            s.append(getDescriptorForClass(c));
        }

        return s.append(")") + getDescriptorForClass(m.getReturnType());
    }
}
