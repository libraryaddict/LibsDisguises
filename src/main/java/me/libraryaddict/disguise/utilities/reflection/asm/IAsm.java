package me.libraryaddict.disguise.utilities.reflection.asm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by libraryaddict on 17/02/2020.
 */
public interface IAsm {
    Class<?> createClassWithoutMethods(String className, ArrayList<Map.Entry<String, String>> illegalMethods) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException;
}


