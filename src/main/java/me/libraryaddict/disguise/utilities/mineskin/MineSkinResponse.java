package me.libraryaddict.disguise.utilities.mineskin;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.UUID;

/**
 * Created by libraryaddict on 29/12/2019.
 */
public class MineSkinResponse {
    public class SkinData {
        public class SkinTexture {
            private String value;
            private String signature;
            private String url;
            private Map<String, String> urls;

            public String getValue() {
                return value;
            }

            public String getSignature() {
                return signature;
            }

            public String getUrl() {
                return url;
            }

            public Map<String, String> getUrls() {
                return urls;
            }
        }

        private String name;
        private UUID uuid;
        private SkinTexture texture;

        public String getName() {
            return name;
        }

        public SkinTexture getTexture() {
            return texture;
        }

        public UUID getUUID() {
            return uuid;
        }
    }

    private int id;
    private String name;
    private SkinData data;
    private double timestamp;
    private int duration;
    private int accountId;
    @SerializedName("private")
    private boolean privateSkin;
    private int views;
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

        GameProfile profile = new GameProfile(getData().getUUID(),
                StringUtils.stripToNull(getData().getName()) == null ? "Unknown" : getData().getName());

        if (getData().getTexture() != null) {
            Property property = new Property("textures", getData().getTexture().getValue(),
                    getData().getTexture().getSignature());
            profile.getProperties().put("textures", property);
        }

        return profile;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public int getAccountId() {
        return accountId;
    }

    public boolean isPrivate() {
        return privateSkin;
    }

    public int getViews() {
        return views;
    }

    public double getNextRequest() {
        return nextRequest;
    }
}
