package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisguiseUtilitiesTest {
    @Test
    public void testNewlineSplitter() {
        assertArrayEquals(new String[]{"Name 1", "Name 2"}, DisguiseUtilities.splitNewLine("Name 1\nName 2"));
        assertArrayEquals(new String[]{"Name 1", "Name 2"}, DisguiseUtilities.splitNewLine("Name 1\\nName 2"));
        assertArrayEquals(new String[]{"Name 1\\", "Name 2"}, DisguiseUtilities.splitNewLine("Name 1\\\nName 2"));
        assertArrayEquals(new String[]{"Name 1\\nName 2"}, DisguiseUtilities.splitNewLine("Name 1\\\\nName 2"));
        assertArrayEquals(new String[]{"Name 1\\", "Name 2"}, DisguiseUtilities.splitNewLine("Name 1\\\\\\nName 2"));
        assertArrayEquals(new String[]{"Name 1\\\\nName 2"}, DisguiseUtilities.splitNewLine("Name 1\\\\\\\\nName 2"));
    }

    @Test
    public void testQuoteSplitter() {
        // Test if splits are correct
        assertArrayEquals(new String[]{"A", "simple", "string"}, DisguiseUtilities.split("A simple string"));

        assertArrayEquals(new String[]{"A quoted string"}, DisguiseUtilities.split("\"A quoted string\""));

        assertArrayEquals(new String[]{"\"A double quoted string\""}, DisguiseUtilities.split("\"\"A double quoted string\"\""));

        assertArrayEquals(new String[]{"A", "string", "containing a", "quote"}, DisguiseUtilities.split("A string \"containing a\" quote"));

        assertArrayEquals(new String[]{"A", "string", "fully", "split"}, DisguiseUtilities.split("\"A\" string fully split"));

        assertArrayEquals(new String[]{"A", "string", "fully", "split"}, DisguiseUtilities.split("\"A\" \"string\" fully split"));

        assertArrayEquals(new String[]{"A", "string", "fully", "split"}, DisguiseUtilities.split("A \"string\" fully split"));

        // Test if quotes are ignored properly and included in result
        assertArrayEquals(new String[]{"A", "\"string", "fully", "split"}, DisguiseUtilities.split("A \"string fully split"));

        assertArrayEquals(new String[]{"A", "\"string", "\"fully", "split"}, DisguiseUtilities.split("A \"string \"fully split"));

        assertArrayEquals(new String[]{"\"A", "\"string", "\"fully", "split"}, DisguiseUtilities.split("\"A \"string \"fully split"));

        assertArrayEquals(new String[]{"A", "string\"", "fully", "split"}, DisguiseUtilities.split("A string\" fully split"));

        assertArrayEquals(new String[]{"A", "string\"", "fully\"", "split"}, DisguiseUtilities.split("A string\" fully\" split"));

        assertArrayEquals(new String[]{"A", "string", "fully\"", "split"}, DisguiseUtilities.split("A \"string\" fully\" split"));

        assertArrayEquals(new String[]{"A \"string", "with", "four", "splits"}, DisguiseUtilities.split("\"A \"string\" with four splits"));

        // Test for quotes inside words
        assertArrayEquals(new String[]{"Fully", "split", "\"", "message"}, DisguiseUtilities.split("Fully split \"\"\" message"));

        // Test to make sure space can be quoted, with an empty quote at the end
        assertArrayEquals(new String[]{" ", "\""}, DisguiseUtilities.split("\" \" \""));

        // Test to make sure empty quotes, are still quotes
        assertArrayEquals(new String[]{"Three", "", "split"}, DisguiseUtilities.split("Three \"\" split"));

        // Test to ensure single quotes, are still not quotes
        assertArrayEquals(new String[]{"'Three", "split", "message'"}, DisguiseUtilities.split("'Three split message'"));

        // There is a quoted message inside the quoted message, however it was not escaped
        assertArrayEquals(new String[]{"A", "quoted message \"inside a quoted message\""},
            DisguiseUtilities.split("A \"quoted message \"inside a quoted message\"\""));

        // Now test for escaped quotes, however as escaped quotes look different inside editors, I'll be replacing \
        // with / and " with '

        // Test for escaped quotes, they should be ignored
        splitEquals("/'Escaped quotes/'", "'Escaped", "quotes'");

        // Test with one quote escaped
        splitEquals("'Escaped quotes/'", "'Escaped", "quotes'");

        // Test with no quotes escaped, where the escape was escaped
        splitEquals("'Unescaped quotes/'", "'Unescaped", "quotes'");

        // Test with three escaped slashes, then unescaped quote
        splitEquals("'Unescaped quotes//////'", "Unescaped quotes///");

        // Test with three escaped slashes, then escaped quote
        splitEquals("'Escaped quotes///////'", "'Escaped", "quotes///'");

        // Test with strings of escapes and quotes only
        splitEquals("////", "////");

        splitEquals("////'", "//'");

        splitEquals("'////'", "//");

        splitEquals("'/////'", "'//'");

        splitEquals("'// //'", "// /");

        splitEquals("'//// ////'", "//// //");

        splitEquals("Foobar is not 'Foo Bar' but is a single word 'foobar' or as some quote it, /'foobar/' and again, " +
                "not /'foo bar/' - It is 'foobar'!",

            "Foobar", "is", "not", "Foo Bar", "but", "is", "a", "single", "word", "foobar", "or", "as", "some", "quote", "it,", "'foobar'",
            "and", "again,", "not", "'foo", "bar'", "-", "It", "is", "'foobar'!");

        splitAndBack("Hi \" bye");
        splitAndBack("Hi\\\" I'm Sam");
        splitAndBack("\"Hi\\\" I'm Sam");
        splitAndBack("\"Hi\\\\\" I'm Sam");
        splitAndBack("\"Hi\\\\\\\" I'm Sam");
        splitAndBack("\"Hi\\\\\\\" \"I'm Sam");
    }

    private void splitAndBack(String string) {
        String quoted = DisguiseUtilities.quote(string);
        String[] split = DisguiseUtilities.split(quoted);

        assertEquals(1, split.length);
        assertEquals(string, split[0]);
    }

    private void splitEquals(String toSplit, String... expected) {
        String[] splitted = DisguiseUtilities.split(toSplit.replace("/", "\\").replace("'", "\""));
        String[] expect = Arrays.stream(expected).map(string -> string.replace("/", "\\").replace("'", "\"")).toArray(String[]::new);

        assertArrayEquals(expect, splitted);

        splitAndBack(toSplit);
    }

    @Test
    public void testVersioning() {
        Assertions.assertTrue(DisguiseUtilities.isOlderThan("1.0", "0.9"));
        Assertions.assertTrue(DisguiseUtilities.isOlderThan("3.0", "2.9"));
        Assertions.assertTrue(DisguiseUtilities.isOlderThan("3.0", "2.99.9"));
        Assertions.assertTrue(DisguiseUtilities.isOlderThan("3", "2.99.9"));

        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3", "4.99.9"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.0", "4.99.9"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("1.0", "1"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("1.0", "1.0"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("1", "1.0"));

        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.0.0", "3.1.0"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.0.0", "3.0.1"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.0.0", "3.0.1-SNAPSHOT"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.1.0", "3.1.1-SNAPSHOT"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.1.1-SNAPSHOT", "3.4.0"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("3.1.1-SNAPSHOT", "3.4.0-SNAPSHOT"));
        Assertions.assertFalse(DisguiseUtilities.isOlderThan("2.3.1", "2.4.0-SNAPSHOT"));
    }

    @Test
    public void testYaw() {
        assertEquals(180, DisguiseUtilities.getYaw(DisguiseType.MINECART, DisguiseType.PLAYER, 90));
    }

    @Test
    public void testQuoter() {
        assertEquals("\"&c10.0 ❤ &8| &5Prokurator &8(&7poz. 1&8)\"", DisguiseUtilities.quote("&c10.0 ❤ &8| &5Prokurator &8(&7poz. 1&8)"));
        assertEquals("String", DisguiseUtilities.quote("String"));
        assertEquals("\"\"String\"", DisguiseUtilities.quote("\"String"));
        assertEquals("\"String 2\"", DisguiseUtilities.quote("String 2"));
    }
}
