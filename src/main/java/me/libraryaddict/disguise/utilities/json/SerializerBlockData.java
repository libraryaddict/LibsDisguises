package me.libraryaddict.disguise.utilities.json;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 27/11/2018.
 */
public class SerializerBlockData implements JsonDeserializer<BlockData>,
        JsonSerializer<BlockData> {
    @Override
    public BlockData deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        return Bukkit.createBlockData(json.getAsString());
    }

    @Override
    public JsonElement serialize(BlockData src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getAsString());
    }
}
