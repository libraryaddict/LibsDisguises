package me.libraryaddict.disguise.utilities;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by libraryaddict on 25/10/2018.
 */
public class DisguiseUtilitiesTest {
    @Test
    public void testQuoteSplitter() {
        // Test if splits are correct
        Assert.assertArrayEquals(new String[]{"A", "simple", "string"}, DisguiseUtilities.split("A simple string"));

        Assert.assertArrayEquals(new String[]{"A quoted string"}, DisguiseUtilities.split("\"A quoted string\""));

        Assert.assertArrayEquals(new String[]{"\"A double quoted string\""},
                DisguiseUtilities.split("\"\"A double quoted string\"\""));

        Assert.assertArrayEquals(new String[]{"A", "string", "containing a", "quote"},
                DisguiseUtilities.split("A string \"containing a\" quote"));

        Assert.assertArrayEquals(new String[]{"A", "string", "fully", "split"},
                DisguiseUtilities.split("\"A\" string fully split"));

        Assert.assertArrayEquals(new String[]{"A", "string", "fully", "split"},
                DisguiseUtilities.split("\"A\" \"string\" fully split"));

        Assert.assertArrayEquals(new String[]{"A", "string", "fully", "split"},
                DisguiseUtilities.split("A \"string\" fully split"));

        // Test if quotes are ignored properly and included in result
        Assert.assertArrayEquals(new String[]{"A", "\"string", "fully", "split"},
                DisguiseUtilities.split("A \"string fully split"));

        Assert.assertArrayEquals(new String[]{"A", "\"string", "\"fully", "split"},
                DisguiseUtilities.split("A \"string \"fully split"));

        Assert.assertArrayEquals(new String[]{"\"A", "\"string", "\"fully", "split"},
                DisguiseUtilities.split("\"A \"string \"fully split"));

        Assert.assertArrayEquals(new String[]{"A", "string\"", "fully", "split"},
                DisguiseUtilities.split("A string\" fully split"));

        Assert.assertArrayEquals(new String[]{"A", "string\"", "fully\"", "split"},
                DisguiseUtilities.split("A string\" fully\" split"));

        Assert.assertArrayEquals(new String[]{"A", "string", "fully\"", "split"},
                DisguiseUtilities.split("A \"string\" fully\" split"));

        Assert.assertArrayEquals(new String[]{"A \"string", "with", "four", "splits"},
                DisguiseUtilities.split("\"A \"string\" with four splits"));

        // Test for quotes inside words
        Assert.assertArrayEquals(new String[]{"Fully", "split", "\"", "message"},
                DisguiseUtilities.split("Fully split \"\"\" message"));

        // Test to make sure space can be quoted, with an empty quote at the end
        Assert.assertArrayEquals(new String[]{" ", "\""}, DisguiseUtilities.split("\" \" \""));

        // Test to make sure empty quotes, are still quotes
        Assert.assertArrayEquals(new String[]{"Three", "", "split"}, DisguiseUtilities.split("Three \"\" split"));

        // Test to ensure single quotes, are still not quotes
        Assert.assertArrayEquals(new String[]{"'Three", "split", "message'"},
                DisguiseUtilities.split("'Three split message'"));

        // There is a quoted message inside the quoted message, however it was not escaped
        Assert.assertArrayEquals(new String[]{"A", "quoted message \"inside a quoted message\""},
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

        splitEquals(
                "Foobar is not 'Foo Bar' but is a single word 'foobar' or as some quote it, /'foobar/' and again, " +
                        "not /'foo bar/' - It is 'foobar'!",

                "Foobar", "is", "not", "Foo Bar", "but", "is", "a", "single", "word", "foobar", "or", "as", "some",
                "quote", "it,", "'foobar'", "and", "again,", "not", "'foo", "bar'", "-", "It", "is", "'foobar'!");

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

        Assert.assertEquals(1, split.length);
        Assert.assertEquals(string, split[0]);
    }

    private void splitEquals(String toSplit, String... expected) {
        String[] splitted = DisguiseUtilities.split(toSplit.replace("/", "\\").replace("'", "\""));
        String[] expect = Arrays.stream(expected).map(string -> string.replace("/", "\\").replace("'", "\""))
                .toArray(String[]::new);

        Assert.assertArrayEquals(expect, splitted);

        splitAndBack(toSplit);
    }
}
