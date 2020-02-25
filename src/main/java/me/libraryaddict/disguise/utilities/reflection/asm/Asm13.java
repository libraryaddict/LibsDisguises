package me.libraryaddict.disguise.utilities.reflection.asm;

import com.google.gson.Gson;
import lombok.Getter;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by libraryaddict on 17/02/2020.
 */
public class Asm13 implements IAsm {
    @Getter
    private Method defineMethod;

    public Asm13() throws NoSuchMethodException {
        defineMethod = getDefineClassMethod();
    }

    public Class<?> createClassWithoutMethods(String className,
            ArrayList<Map.Entry<String, String>> illegalMethods) throws IOException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {
        ClassReader cr = new ClassReader(
                getClass().getClassLoader().getResourceAsStream(className.replace(".", "/") + ".class"));
        ClassWriter writer = new ClassWriter(cr, 0);

        cr.accept(new ClassVisitor(Opcodes.ASM5, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                    String[] exceptions) {

                Map.Entry<String, String> entry = illegalMethods.stream()
                        .filter(e -> e.getKey().equals(name) && e.getValue().equals(desc)).findFirst().orElse(null);

                if (entry != null) {
                    return null;
                }

                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);

        byte[] bytes = writer.toByteArray();

        ClassLoader loader = getClass().getClassLoader();
        Field field = loader.getClass().getDeclaredField("classes");
        field.setAccessible(true);
        Map<String, Class<?>> map = (Map<String, Class<?>>) field.get(loader);
        Class newClass = (Class<?>) defineMethod.invoke(getClass().getClassLoader(), className, bytes, 0, bytes.length);

        map.put(className, newClass);
        return newClass;
    }

    private Method getDefineClassMethod() throws NoSuchMethodException {
        Method defineClass = ClassLoader.class
                .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);

        return defineClass;
    }
}
