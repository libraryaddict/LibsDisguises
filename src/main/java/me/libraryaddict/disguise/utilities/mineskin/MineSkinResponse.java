package me.libraryaddict.disguise.utilities.mineskin;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * Created by libraryaddict on 29/12/2019.
 */
@Getter
public class MineSkinResponse {
    @Getter
    public class SkinTextureUrls {
        private String skin;
        private String cape;
    }

    @Getter
    public class SkinTexture {
        private String value;
        private String signature;
        private String url;
        private SkinTextureUrls urls;
    }

    @Getter
    public class SkinData {
        private String name;
        private UUID uuid;
        private SkinTexture texture;

        public UUID getUUID() {
            return uuid;
        }
    }

    public enum SkinVariant {
        UNKNOWN,
        CLASSIC,
        SLIM
    }

    private int id;
    private String idStr;
    private String uuid;
    private String name;
    private SkinVariant variant;
    private SkinData data;
    private double timestamp;
    private int duration;
    @SerializedName("account")
    private int accountId;
    private String server;
    @SerializedName("private")
    private boolean privateSkin;
    private int views;
    private boolean duplicate;
    private double nextRequest;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SkinData getData() {
        return data;
    }

    public GameProfile getGameProfile() {
        if (getData() == null) {
            return null;
        }

        GameProfile profile = new GameProfile(getData().getUUID(), StringUtils.stripToNull(getData().getName()) == null ? "Unknown" : getData().getName());

        if (getData().getTexture() != null) {
            Property property = new Property("textures", getData().getTexture().getValue(), getData().getTexture().getSignature());
            profile.getProperties().put("textures", property);
        }

        return profile;
    }
}
