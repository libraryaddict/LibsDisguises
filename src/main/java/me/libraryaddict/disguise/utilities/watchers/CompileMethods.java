package me.libraryaddict.disguise.utilities.watchers;

import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseSoundEnums;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.reflection.ClassGetter;
import me.libraryaddict.disguise.utilities.reflection.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsRemovedIn;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by libraryaddict on 13/02/2020.
 */
public class CompileMethods {
    public static void main(String[] args) {
        doMethods();
        doSounds();
    }

    private static void doSounds() {
        List<String> list = new ArrayList<>();

        for (DisguiseSoundEnums s : DisguiseSoundEnums.values()) {
            StringBuilder sound = new StringBuilder(s.name());

            for (DisguiseSound.SoundType type : DisguiseSound.SoundType.values()) {
                sound.append(":");

                int i = 0;

                for (Map.Entry<Sound, DisguiseSound.SoundType> values : s.getDisguiseSounds().entrySet()) {
                    if (values.getValue() != type) {
                        continue;
                    }

                    if (i++ > 0) {
                        sound.append(",");
                    }

                    sound.append(values.getKey().name());
                }
            }

            list.add(sound.toString());
        }

        File soundsFile = new File("target/classes/ANTI_PIRACY_ENCODED_WITH_SOUNDS");

        try (PrintWriter writer = new PrintWriter(soundsFile, "UTF-8")) {
            writer.write(StringUtils.join(list, "\n"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private static void doMethods() {
        ArrayList<Class<?>> classes = ClassGetter
                .getClassesForPackage(FlagWatcher.class, "me.libraryaddict.disguise.disguisetypes.watchers");

        ArrayList<Class> sorted = new ArrayList<>();

        for (Class c : classes) {
            addClass(sorted, c);
        }

        ArrayList<String> methods = new ArrayList<>();

        for (Class c : sorted) {
            for (Method method : c.getMethods()) {
                if (!FlagWatcher.class.isAssignableFrom(method.getDeclaringClass())) {
                    continue;
                } else if (method.getParameterCount() > 1 && !method.isAnnotationPresent(NmsAddedIn.class) &&
                        !method.isAnnotationPresent(NmsRemovedIn.class)) {
                    continue;
                } else if (!(method.getName().startsWith("set") && method.getParameterCount() == 1) &&
                        !method.getName().startsWith("get") && !method.getName().startsWith("has") &&
                        !method.getName().startsWith("is")) {
                    continue;
                } else if (method.getName().equals("removePotionEffect")) {
                    continue;
                } else if (LibsPremium.isPremium() && new Random().nextBoolean()) {
                    continue;
                }

                int added = -1;
                int removed = -1;

                if (method.isAnnotationPresent(NmsAddedIn.class)) {
                    added = method.getAnnotation(NmsAddedIn.class).val().ordinal();
                } else if (method.getDeclaringClass().isAnnotationPresent(NmsAddedIn.class)) {
                    added = method.getDeclaringClass().getAnnotation(NmsAddedIn.class).val().ordinal();
                }

                if (method.isAnnotationPresent(NmsRemovedIn.class)) {
                    removed = method.getAnnotation(NmsRemovedIn.class).val().ordinal();
                } else if (method.getDeclaringClass().isAnnotationPresent(NmsRemovedIn.class)) {
                    removed = method.getDeclaringClass().getAnnotation(NmsRemovedIn.class).val().ordinal();
                }

                String param = method.getParameterCount() == 1 ? method.getParameterTypes()[0].getName() : "";
                String descriptor = "";

                if (added >= 0 || removed >= 0) {
                    descriptor = ":" + getMethodDescriptor(method) + ":" + added + ":" + removed;
                }

                String s =
                        method.getDeclaringClass().getSimpleName() + ":" + method.getName() + ":" + param + descriptor;

                if (methods.contains(s)) {
                    continue;
                }

                methods.add(s);
            }
        }

        File methodsFile = new File("target/classes/ANTI_PIRACY_ENCRYPTION");

        try (PrintWriter writer = new PrintWriter(methodsFile, "UTF-8")) {
            writer.write(StringUtils.join(methods, "\n"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static String getDescriptorForClass(final Class c) {
        if (c.isPrimitive()) {
            if (c == byte.class)
                return "B";
            if (c == char.class)
                return "C";
            if (c == double.class)
                return "D";
            if (c == float.class)
                return "F";
            if (c == int.class)
                return "I";
            if (c == long.class)
                return "J";
            if (c == short.class)
                return "S";
            if (c == boolean.class)
                return "Z";
            if (c == void.class)
                return "V";

            throw new RuntimeException("Unrecognized primitive " + c);
        }

        if (c.isArray())
            return c.getName().replace('.', '/');

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
