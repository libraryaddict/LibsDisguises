package me.libraryaddict.disguise.utilities;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.Gson;
import javax.imageio.ImageIO;
import me.libraryaddict.disguise.utilities.mineskin.MineSkinAPI;
import me.libraryaddict.disguise.utilities.mineskin.models.responses.MineSkinQueueResponse;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVariant;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled("We don't poke their backend for no reason")
public class MineSkinAPITest {
    private MineSkinAPI mineSkinAPI;

    @BeforeEach
    public void setup() {
        mineSkinAPI = new MineSkinAPI();
        mineSkinAPI.setDebugging(true);
    }

    private SkinUtils.SkinCallback createCallback() {
        return new SkinUtils.SkinCallback() {
            @Override
            public void onError(LibsMsg message, Object... args) {
                fail("API error: " + message + " " + new Gson().toJson(args));
            }

            @Override
            public void onInfo(LibsMsg message, Object... args) {
                System.out.println("INFO: " + message + " " + new Gson().toJson(args));
            }

            @Override
            public void onSuccess(UserProfile profile) {
                System.out.println("Success: " + new Gson().toJson(profile));
            }
        };
    }

    @Test
    public void testGenerateFromSkinFile() {
        // Do note that MineSkin caches stuff, so the first run of a new skin and following runs can be different.
        File testSkin = new File("src/test/resources/skins/TemplateSkinWide.png");
        assertNotNull(testSkin, "Skin file should exist");

        MineSkinQueueResponse response = mineSkinAPI.generateFromFile(createCallback(), testSkin, SkinVariant.CLASSIC);

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getSkin(), "Skin should be present");
        assertNotNull(response.getSkin().getTexture(), "Skin texture must not be null");
    }

    @Test
    public void testGenerateFromUniqueSkinFile() throws Exception {
        File original = new File("src/test/resources/skins/TemplateSkinWide.png");
        assertTrue(original.exists());

        BufferedImage img = ImageIO.read(original);
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);

        // Generate a unique skin not cached by MineSkin
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgba = img.getRGB(x, y);

                int a = (rgba >>> 24) & 0xff;
                if (a == 0) {
                    continue;
                }

                int r = random.nextInt(256);
                int g = random.nextInt(256);
                int b = random.nextInt(256);

                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        File temp = File.createTempFile("unique_skin_", ".png");
        ImageIO.write(img, "png", temp);

        MineSkinQueueResponse response = mineSkinAPI.generateFromFile(createCallback(), temp, SkinVariant.CLASSIC);

        assertNotNull(response);
        assertNotNull(response.getSkin());
        assertNotNull(response.getSkin().getTexture());
    }

    @Test
    public void testGenerateFromSkinName() {
        MineSkinQueueResponse response =
            mineSkinAPI.generateFromUUID(UUID.fromString("f84c6a79-0a4e-45e0-879b-cd49ebd4c4e2"), SkinVariant.CLASSIC);

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getSkin(), "Skin should be present");
        assertNotNull(response.getSkin().getTexture(), "Skin texture must not be null");
    }
}