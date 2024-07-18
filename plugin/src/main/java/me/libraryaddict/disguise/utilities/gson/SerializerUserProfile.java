package me.libraryaddict.disguise.utilities.gson;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class SerializerUserProfile implements JsonDeserializer<UserProfile> {
    private final Type listType = new TypeToken<List<TextureProperty>>() {
    }.getType();

    @Override
    public UserProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        final UUID uuid;
        final String name = obj.has("name") ? obj.get("name").getAsString() : null;
        List<TextureProperty> properties = new ArrayList<>();

        // Deserialize via UserProfile
        if (obj.has("uuid")) {
            uuid = UUID.fromString(obj.get("uuid").getAsString());

            if (obj.has("textureProperties")) {
                properties = context.deserialize(obj.get("textureProperties"), listType);
            }
        } else if (obj.has("id")) {
            String id = obj.get("id").getAsString();

            // Conversion from old old data
            if (!id.contains("-")) {
                id = Pattern.compile("([\\da-fA-F]{8})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]+)")
                    .matcher(obj.get("id").getAsString()).replaceFirst("$1-$2-$3-$4-$5");
            }

            uuid = UUID.fromString(id);

            if (obj.has("properties")) {
                properties = context.deserialize(obj.get("properties"), listType);
            }
        } else {
            return null;
        }

        return new UserProfile(uuid, name, properties);
    }
}
