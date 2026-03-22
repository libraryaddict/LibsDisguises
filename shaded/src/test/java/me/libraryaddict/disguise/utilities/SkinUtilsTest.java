package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVariant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class SkinUtilsTest {
    private ByteArrayOutputStream output;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private File getFile(String name) {
        return new File(getClass().getClassLoader().getResource("skins/" + name).getFile());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TemplateSkinSlim.png", "TemplateSkinSlim_Solid.png"})
    public void testSlimSkins(String filename) {
        SkinVariant variant = SkinUtils.detectSkinVariant(getFile(filename));

        Assertions.assertEquals(SkinVariant.SLIM, variant);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TemplateSkinWide.png", "TemplateSkinWide_Solid.png"})
    public void testClassicSkins(String filename) {
        SkinVariant variant = SkinUtils.detectSkinVariant(getFile(filename));

        Assertions.assertEquals(SkinVariant.CLASSIC, variant);
    }

    @Test
    public void testInvalidFirstBlock() {
        SkinVariant variant = SkinUtils.detectSkinVariant(getFile("TemplateSkinInvalidFirstBlock.png"));

        Assertions.assertEquals(SkinVariant.UNKNOWN, variant);
        Assertions.assertTrue(output.toString().contains("first 8x8 block of pixels"));
    }

    @Test
    public void testInvalidRightArm() {
        SkinVariant variant = SkinUtils.detectSkinVariant(getFile("TemplateSkinInvalidRightArm.png"));

        Assertions.assertEquals(SkinVariant.UNKNOWN, variant);
        Assertions.assertTrue(output.toString().contains("in the right arm"));
    }

    @Test
    public void testInvalidReusedColor() {
        SkinVariant variant = SkinUtils.detectSkinVariant(getFile("TemplateSkinSolidInvalidReused.png"));

        Assertions.assertEquals(SkinVariant.UNKNOWN, variant);
        Assertions.assertTrue(output.toString().contains("color is used in the skin"));
    }
}
