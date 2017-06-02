package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerGameProfile implements JsonSerializer<WrappedGameProfile>, JsonDeserializer<WrappedGameProfile> {

    @Override
    public JsonElement serialize(WrappedGameProfile src, Type typeOfSrc, JsonSerializationContext context) {
        System.out.println(src.getHandle().toString());
        return context.serialize(src.getHandle(), GameProfile.class);
    }

    @Override
    public WrappedGameProfile deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        return WrappedGameProfile.fromHandle(context.deserialize(json, GameProfile.class));
    }
}
