package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.mojang.authlib.properties.PropertyMap;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerFlagWatcher implements JsonDeserializer<FlagWatcher>, JsonSerializer<FlagWatcher>,
        InstanceCreator<FlagWatcher> {
    private Gson gson;

    public SerializerFlagWatcher(Gson gson) {
        this.gson = gson;
    }

    @Override
    public FlagWatcher deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        try {
            FlagWatcher watcher = (FlagWatcher) gson
                    .fromJson(json, Class.forName(((JsonObject) json).get("flagType").getAsString()));

            DisguiseType entity = DisguiseType.valueOf(((JsonObject) json).get("entityType").getAsString());

            correct(watcher, watcher.getClass(), "entityValues");
            correct(watcher, entity.getWatcherClass(), "backupEntityValues");

            return watcher;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void correct(FlagWatcher watcher, Class<? extends FlagWatcher> flagWatcher,
            String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = FlagWatcher.class.getDeclaredField(name);
        field.setAccessible(true);
        HashMap<Integer, Object> map = (HashMap<Integer, Object>) field.get(watcher);
        int count = 0;

        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            MetaIndex index = MetaIndex.getMetaIndex(flagWatcher, entry.getKey());

            if (entry.getValue() instanceof Double) {
                Object def = index.getDefault();

                if (def instanceof Long)
                    entry.setValue(((Double) entry.getValue()).longValue());
                else if (def instanceof Float)
                    entry.setValue(((Double) entry.getValue()).floatValue());
                else if (def instanceof Integer)
                    entry.setValue(((Double) entry.getValue()).intValue());
                else if (def instanceof Short)
                    entry.setValue(((Double) entry.getValue()).shortValue());
                else if (def instanceof Byte)
                    entry.setValue(((Double) entry.getValue()).byteValue());
            } else if (entry.getValue() instanceof LinkedTreeMap) { // If it's deserialized incorrectly as a map
                // If the default value is not VillagerData
                if (index.getDefault() instanceof VillagerData) {
                    entry.setValue(new Gson().fromJson(new Gson().toJson(entry.getValue()), VillagerData.class));
                } else if (index.getDefault() instanceof Optional) {

                    for (Field f : MetaIndex.class.getFields()) {
                        try {
                            if (f.get(null) != index) {
                                continue;
                            }
                        }
                        catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        Type type = f.getGenericType();
                        Type opt = ((ParameterizedType) type).getActualTypeArguments()[0];

                        if (opt instanceof ParameterizedType) {
                            Type val = ((ParameterizedType) opt).getActualTypeArguments()[0];

                            Optional value;

                            if (((LinkedTreeMap) entry.getValue()).isEmpty()) {
                                value = Optional.empty();
                            } else {
                                value = Optional
                                        .of(gson.fromJson(gson.toJson(((LinkedTreeMap) entry.getValue()).get("value")),
                                                val));
                            }

                            entry.setValue(value);
                        }
                    }
                }
            }

            // If the deserialized class is not the same class type as the default
            if (!index.getDefault().getClass().isInstance(entry.getValue())) {
                entry.setValue(index.getDefault());
                count++;
            }
        }

        if (count > 0) {
            DisguiseUtilities.getLogger().info("Fixed " + count + " incorrect disguise flags on saved disguise");
        }
    }

    @Override
    public FlagWatcher createInstance(Type type) {
        try {
            return (FlagWatcher) type.getClass().getConstructor(Disguise.class).newInstance(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonElement serialize(FlagWatcher src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = (JsonObject) gson.toJsonTree(src);

        obj.addProperty("flagType", src.getClass().getName());

        try {
            Method method = FlagWatcher.class.getDeclaredMethod("getDisguise");
            method.setAccessible(true);
            Disguise disguise = (Disguise) method.invoke(src);
            obj.addProperty("entityType", disguise.getType().name());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return obj;
    }
}
