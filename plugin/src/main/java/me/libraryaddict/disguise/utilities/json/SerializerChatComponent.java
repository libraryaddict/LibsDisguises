package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 27/11/2018.
 */
public class SerializerChatComponent implements JsonDeserializer<WrappedChatComponent>,
        JsonSerializer<WrappedChatComponent> {

    @Override
    public WrappedChatComponent deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return WrappedChatComponent.fromJson(json.getAsString());
        }

        return null;
    }

    @Override
    public JsonElement serialize(WrappedChatComponent src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getJson());
    }
}
