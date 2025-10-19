package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

@Getter
public class MineSkinSkin {
    @Getter
    public static class Texture {
        @Getter
        public static class TextureData {
            private String value;
            private String signature;
        }

        @Getter
        public static class TextureHash {
            private String skin;
            private @Nullable String cape;
        }

        @Getter
        public static class TextureUrl {
            private String skin;
            private @Nullable String cape;
        }

        private TextureData data;
        private TextureHash hash;
        private TextureUrl url;
    }

    @Getter
    public static class Generator {
        private String version;
        private long timestamp;
        private long duration;
        private String account;
        private String server;
    }

    private String uuid;
    private String name;
    private SkinVisibility visibility;
    private SkinVariant variant;
    private Texture texture;
    private Generator generator;
    private long views;
    private boolean duplicate;

    public GameProfile getGameProfile() {
        String id = getUuid();

        // If uuid doesn't contain dashes, inject them
        if (!id.contains("-")) {
            id = Pattern.compile("([\\da-fA-F]{8})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]+)").matcher(id)
                .replaceFirst("$1-$2-$3-$4-$5");
        }

        UserProfile profile = new UserProfile(UUID.fromString(id), StringUtils.stripToNull(getName()) == null ? "Unknown" : getName());

        if (getTexture() != null && getTexture().getData() != null) {
            TextureProperty property =
                new TextureProperty("textures", getTexture().getData().getValue(), getTexture().getData().getSignature());
            profile.setTextureProperties(Collections.singletonList(property));
        }

        return ReflectionManager.convertProfile(profile);
    }
}
