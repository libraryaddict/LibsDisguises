package me.libraryaddict.disguise.utilities.watchers;

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
import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundEnums;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import org.bukkit.Sound;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CompileMethods {
    public static void main(String[] args) {
        Path zipFilePath = Paths.get(System.getProperty("jar.path"));

        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
            Files.write(fs.getPath("/METHOD_MAPPINGS.txt"), doMethods());
            Files.write(fs.getPath("/SOUND_MAPPINGS.txt"), doSounds());
            // Count after we write the mappings
            Files.write(fs.getPath("/plugin.yml"), doFileCount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getJarFileCount(File file, String... skipFiles) {
        try (JarFile jar = new JarFile(file)) {
            int count = 0;

            Enumeration<JarEntry> entries = jar.entries();

            loop:
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    continue;
                }

                for (String skipFile : skipFiles) {
                    if (!skipFile.equals(entry.getName())) {
                        continue;
                    }

                    continue loop;
                }

                count++;
            }

            return count;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] doFileCount() {
        int totalCount = getJarFileCount(new File(System.getProperty("jar.path")), "METHOD_MAPPINGS.txt", "SOUND_MAPPINGS.txt") + 2;

        try {
            Path path = new File(new File("build/resources/main"), "plugin.yml").toPath();
            String pluginYaml =
                Files.readString(path, StandardCharsets.UTF_8).replaceFirst("file-count: -?\\d+", "file-count: " + totalCount);
            return pluginYaml.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] doSounds() {
        List<String> list = new ArrayList<>();

        for (DisguiseSoundEnums e : DisguiseSoundEnums.values()) {
            StringBuilder sound = getSoundAsString(e);

            list.add(sound.toString());
        }

        return String.join("\n", list).getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> getMatchingFields(String pattern) {
        List<String> matches = new ArrayList<>();

        for (Field field : Sound.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != Sound.class) {
                continue;
            }

            if (!field.getName().matches(pattern)) {
                continue;
            }

            matches.add(field.getName());
        }

        return matches;
    }

    private static StringBuilder getSoundAsString(DisguiseSoundEnums e) {
        StringBuilder sound = new StringBuilder(e.name());

        for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
            sound.append("/");

            int i = 0;

            for (Map.Entry<String, SoundGroup.SoundType> entry : e.getSounds().entrySet()) {
                if (entry.getValue() != type) {
                    continue;
                }

                String soundValue = entry.getKey();

                if (soundValue.contains("*")) {
                    soundValue = String.join(",", getMatchingFields(soundValue));
                }

                if (i++ > 0) {
                    sound.append(",");
                }

                sound.append(soundValue);
            }
        }
        return sound;
    }

    private static void addClass(ArrayList<Class> classes, Class c) {
        if (classes.contains(c)) {
            return;
        }

        if (c != FlagWatcher.class) {
            addClass(classes, c.getSuperclass());
        }

        classes.add(c);
    }

    private static byte[] doMethods() {
        ArrayList<Class<?>> classes =
            ClassGetter.getClassesForPackage(FlagWatcher.class, "me.libraryaddict.disguise.disguisetypes.watchers");

        if (classes.isEmpty()) {
            throw new IllegalStateException("Classes were not found for FlagWatchers");
        }

        ArrayList<Class> sorted = new ArrayList<>();

        for (Class c : classes) {
            if (c.getName().contains("$")) {
                continue;
            }

            addClass(sorted, c);
        }

        ArrayList<WatcherInfo> methods = new ArrayList<>();

        for (Class c : sorted) {
            for (Method method : c.getMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()) ||
                    !FlagWatcher.class.isAssignableFrom(method.getDeclaringClass())) {
                    continue;
                } else if (method.getParameterCount() > 1/* && !method.isAnnotationPresent(NmsAddedIn.class) &&
                    !method.isAnnotationPresent(NmsRemovedIn.class)*/) {
                    continue;
                } else if (!(method.getName().startsWith("set") && method.getParameterCount() == 1) &&
                    !method.getName().startsWith("get") && !method.getName().startsWith("has") && !method.getName().startsWith("is")) {
                    continue;
                } else if (method.getName().equals("removePotionEffect")) {
                    continue;
                } else if ((LibsPremium.isPremium() || !LibsPremium.getUserID().contains("%")) && new Random().nextBoolean()) {
                    continue;
                } else if (method.getParameterCount() > 0 && method.getReturnType() != void.class) {
                    // At 04/06/2024, this only hit `getItemStack`, `getMetadata`, `hasValue` and `hasPotionEffect`
                    continue;
                }

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
                        "@RandomDefaultValue is intended for use only on setter methods, " + info.getMethod() + " on " + c.getSimpleName() +
                            " does not met this criteria!");
                }

                if (methods.contains(info)) {
                    continue;
                }

                methods.add(info);
            }
        }

        if (methods.isEmpty()) {
            throw new IllegalStateException("Methods were not compiled");
        }

        return new Gson().toJson(methods).getBytes(StandardCharsets.UTF_8);
    }

    static String getDescriptorForClass(final Class c) {
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

    static String getMethodDescriptor(Method m) {
        StringBuilder s = new StringBuilder("(");

        for (final Class c : (m.getParameterTypes())) {
            s.append(getDescriptorForClass(c));
        }

        return s.append(")") + getDescriptorForClass(m.getReturnType());
    }
}
