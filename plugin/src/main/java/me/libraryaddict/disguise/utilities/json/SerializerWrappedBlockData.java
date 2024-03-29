package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Material;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerWrappedBlockData implements JsonSerializer<WrappedBlockData>, JsonDeserializer<WrappedBlockData> {

    @Override
    public JsonElement serialize(WrappedBlockData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", src.getType().name());
        obj.addProperty("data", src.getData());
        return obj;
    }

    @Override
    public WrappedBlockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        return WrappedBlockData.createData(Material.valueOf(obj.get("type").getAsString()), obj.get("data").getAsInt());
    }
}
