package me.libraryaddict.disguise.utilities.reflection;

import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MushroomCowWatcher;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by libraryaddict on 13/02/2020.
 */
public class CompileMethods {
    public static void main(String[] args) {
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
                        method.getDeclaringClass().getSimpleName() + ":" + method.getName() + ":" +
                        param.getName();

                if (methods.contains(s)) {
                    continue;
                }

                methods.add(s);
            }
        }

        File methodsFile = new File("target/classes/methods.txt");

        try (PrintWriter writer = new PrintWriter(methodsFile, "UTF-8")) {
            writer.write(StringUtils.join(methods, "\n"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
