package me.libraryaddict.disguise.utilities;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ReflectionManagerTests {

    @Test
    public void testParseSignatureArguments() throws Exception {
        List<Class<?>> expect, actual;

        expect = ImmutableList.<Class<?>>of(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class);
        actual = ReflectionManager.parseSignatureArguments("ZBCSIJFD");
        assertEquals(expect, actual);

        expect = ImmutableList.<Class<?>>of(int.class, String[].class, int.class);
        actual = ReflectionManager.parseSignatureArguments("I[Ljava/lang/String;I");
        assertEquals(expect, actual);

        expect = ImmutableList.<Class<?>>of();
        actual = ReflectionManager.parseSignatureArguments("");
        assertEquals(expect, actual);

        expect = ImmutableList.<Class<?>>of(boolean[][][][][][].class);
        actual = ReflectionManager.parseSignatureArguments("[[[[[[Z");
        assertEquals(expect, actual);
    }
}
