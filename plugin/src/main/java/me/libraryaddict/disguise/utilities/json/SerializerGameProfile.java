package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerGameProfile implements JsonSerializer<WrappedGameProfile>, JsonDeserializer<WrappedGameProfile> {

    @Override
    public JsonElement serialize(WrappedGameProfile src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getHandle(), GameProfile.class);
    }

    @Override
    public WrappedGameProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("id") && !obj.get("id").getAsString().contains("-")) {
            obj.addProperty("id",
                Pattern.compile("([\\da-fA-F]{8})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]{4})([\\da-fA-F]+)").matcher(obj.get("id").getAsString())
                    .replaceFirst("$1-$2-$3-$4-$5"));
        }

        return WrappedGameProfile.fromHandle(context.deserialize(json, GameProfile.class));
    }
}
