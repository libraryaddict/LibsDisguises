package me.libraryaddict.disguise.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SerializerItemStack implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> partialSerialize = src.serialize();

        if (partialSerialize.containsKey("meta")) {
            partialSerialize.put("meta", src.getItemMeta().serialize());
        }

        return context.serialize(partialSerialize);
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap map = context.deserialize(json, HashMap.class);

        if (map.containsKey("meta")) {
            Map meta = (Map) map.get("meta");

            if (meta.containsKey("meta-type")) {
                for (Object key : meta.keySet()) {
                    if (meta.get(key) instanceof Number) {
                        meta.put(key, ((Number) meta.get(key)).intValue());
                    }
                }

                ItemMeta itemMeta = ReflectionManager.getDeserializedItemMeta(meta);

                map.put("meta", itemMeta);
            }
        }

        return ItemStack.deserialize(map);
    }
}
