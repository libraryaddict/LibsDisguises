package me.libraryaddict.disguise.utilities.gson;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
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

public class SerializerItemProfile implements JsonDeserializer<ItemProfile> {
    private final Type listType = new TypeToken<List<ItemProfile.Property>>() {
    }.getType();

    @Override
    public ItemProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        String uuid = obj.has("id") ? obj.get("id").getAsString() : obj.has("uuid") ? obj.get("uuid").getAsString() : null;
        String name = obj.has("name") ? obj.get("name").getAsString() : null;
        List<ItemProfile.Property> properties;

        if (obj.has("textureProperties")) {
            properties = context.deserialize(obj.get("textureProperties"), listType);
        } else if (obj.has("properties")) {
            properties = context.deserialize(obj.get("properties"), listType);
        } else {
            properties = new ArrayList<>();
        }

        ItemProfile.SkinPatch skinPatch;

        if (obj.has("skinPatch")) {
            skinPatch = context.deserialize(obj.get("skinPatch"), ItemProfile.SkinPatch.class);
        } else {
            skinPatch = ItemProfile.SkinPatch.EMPTY;
        }

        return new ItemProfile(name, uuid == null ? null : UUID.fromString(uuid), properties, skinPatch);
    }
}
