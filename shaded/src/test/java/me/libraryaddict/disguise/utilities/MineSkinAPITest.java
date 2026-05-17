package me.libraryaddict.disguise.utilities;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.Gson;
import me.libraryaddict.disguise.utilities.mineskin.MineSkinAPI;
import me.libraryaddict.disguise.utilities.mineskin.models.responses.MineSkinQueueResponse;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVariant;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class MineSkinAPITest {

    private MineSkinAPI mineSkinAPI;

    @BeforeEach
    public void setup() {
        mineSkinAPI = new MineSkinAPI();
        mineSkinAPI.setDebugging(true);
    }

    @Test
    @Disabled("We don't poke their backend for no reason")
    public void testGenerateFromSkinFile() {
        // Do note that MineSkin caches stuff, so the first run of a new skin and following runs can be different.
        File testSkin = new File("src/test/resources/skins/TemplateSkinWide.png");
        assertNotNull(testSkin, "Skin file should exist");

        SkinUtils.SkinCallback callback = new SkinUtils.SkinCallback() {
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

        MineSkinQueueResponse response = mineSkinAPI.generateFromFile(callback, testSkin, SkinVariant.CLASSIC);

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getSkin(), "Skin should be present");
        assertNotNull(response.getSkin().getTexture(), "Skin texture must not be null");
    }
}