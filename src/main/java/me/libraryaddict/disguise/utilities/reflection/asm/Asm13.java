package me.libraryaddict.disguise.utilities.reflection.asm;

import lombok.Getter;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by libraryaddict on 17/02/2020.
 */
public class Asm13 implements IAsm {
    @Getter
    private final LibsJarFile libsJarFile;

    public Asm13() throws Throwable {
        ClassLoader pluginClassLoader = getClass().getClassLoader();
        Class c = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
        Field file = c.getDeclaredField("file");
        file.setAccessible(true);

        libsJarFile = new LibsJarFile((File) file.get(pluginClassLoader));

        Field field = c.getDeclaredField("jar");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(pluginClassLoader, libsJarFile);
    }

    public void createClassWithoutMethods(String className, ArrayList<Map.Entry<String, String>> illegalMethods)
            throws IOException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        className = className.replace(".", "/") + ".class";

        ClassReader cr = new ClassReader(getClass().getClassLoader().getResourceAsStream(className));
        ClassWriter writer = new ClassWriter(cr, 0);

        cr.accept(new ClassVisitor(Opcodes.ASM5, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

                Map.Entry<String, String> entry =
                        illegalMethods.stream().filter(e -> e.getKey().equals(name) && e.getValue().equals(desc)).findFirst().orElse(null);

                if (entry != null) {
                    return null;
                }

                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);

        libsJarFile.addClass(className, writer.toByteArray());
    }
}
