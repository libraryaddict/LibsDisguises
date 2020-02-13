package me.libraryaddict.disguise.utilities.reflection;

import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MushroomCowWatcher;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseSoundEnums;
import me.libraryaddict.disguise.utilities.LibsPremium;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    private static void doMethods() {
        ArrayList<Class<?>> classes = ClassGetter
                .getClassesForPackage(FlagWatcher.class, "me.libraryaddict.disguise.disguisetypes.watchers");
        classes.add(FlagWatcher.class);
        classes.add(MushroomCowWatcher.class);

        ArrayList<String> methods = new ArrayList<>();

        for (Class c : classes) {
            for (Method method : c.getMethods()) {
                if (method.getParameterTypes().length != 1) {
                    continue;
                } else if (method.getName().startsWith("get")) {
                    continue;
                } else if (method.isAnnotationPresent(Deprecated.class) &&
                        !method.isAnnotationPresent(NmsRemovedIn.class)) {
                    continue;
                } else if (!method.getReturnType().equals(Void.TYPE)) {
                    continue;
                } else if (method.getName().equals("removePotionEffect")) {
                    continue;
                } else if (!FlagWatcher.class.isAssignableFrom(method.getDeclaringClass())) {
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

                Class<?> param = method.getParameterTypes()[0];

                String s = ((added >= 0 || removed >= 0) ? added + ":" + removed + ":" : "") +
                        method.getDeclaringClass().getSimpleName() + ":" + method.getName() + ":" + param.getName();

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
}
