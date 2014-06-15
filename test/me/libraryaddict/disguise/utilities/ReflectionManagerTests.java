package me.libraryaddict.disguise.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionManagerTests {

    @Test
    public void testParseSignatureArguments() throws Exception {
        Class<?>[] expect, actual;

        expect = new Class<?>[] {boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class};
        actual = ReflectionManager.parseSignatureArguments("ZBCSIJFD");
        assertEquals(expect, actual);

        expect = new Class<?>[] {int.class, String[].class, int.class, String.class};
        actual = ReflectionManager.parseSignatureArguments("I[Ljava/lang/String;ILjava/lang/String;");
        assertEquals(expect, actual);

        expect = new Class<?>[] {};
        actual = ReflectionManager.parseSignatureArguments("");
        assertEquals(expect, actual);

        expect = new Class<?>[] {boolean[][][][][][].class};
        actual = ReflectionManager.parseSignatureArguments("[[[[[[Z");
        assertEquals(expect, actual);
    }
}
