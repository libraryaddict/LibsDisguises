package me.libraryaddict.disguise.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.lang.reflect.Type;

public class SerializerMetaIndex implements JsonSerializer<MetaIndex>, JsonDeserializer<MetaIndex> {

    @Override
    public JsonElement serialize(MetaIndex src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("index", src.getIndex());
        obj.addProperty("flagwatcher", src.getFlagWatcher().getSimpleName());
        return obj;
    }

    @Override
    public MetaIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String name = obj.get("flagwatcher").getAsString();
        int index = obj.get("index").getAsInt();

        for (MetaIndex i : MetaIndex.values()) {
            if (i.getIndex() != index) {
                continue;
            }

            if (!i.getFlagWatcher().getSimpleName().equals(name)) {
                continue;
            }

            return i;
        }

        return null;
    }
}
