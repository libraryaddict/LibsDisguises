package me.libraryaddict.disguise.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Type;

public class SerializerChatComponent implements JsonDeserializer<Component>, JsonSerializer<Component> {

    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return GsonComponentSerializer.gson().deserialize(json.getAsString());
        }

        return null;
    }

    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(GsonComponentSerializer.gson().serialize(src));
    }
}
