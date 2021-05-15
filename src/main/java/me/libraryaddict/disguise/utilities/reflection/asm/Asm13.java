package me.libraryaddict.disguise.utilities.reflection.asm;

import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by libraryaddict on 17/02/2020.
 */
public class Asm13 {
    public byte[] createClassWithoutMethods(String className, ArrayList<Map.Entry<String, String>> illegalMethods)
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

        return writer.toByteArray();
    }
}
