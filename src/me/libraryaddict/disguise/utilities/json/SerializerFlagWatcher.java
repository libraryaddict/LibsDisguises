package me.libraryaddict.disguise.utilities.json;

import com.google.gson.*;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerFlagWatcher implements JsonDeserializer<FlagWatcher>, JsonSerializer<FlagWatcher>, InstanceCreator<FlagWatcher> {

    @Override
    public FlagWatcher deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        try {
            FlagWatcher watcher = context.deserialize(json,
                    Class.forName(((JsonObject) json).get("flagType").getAsString()));

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

        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            if (!(entry.getValue() instanceof Double))
                continue;

            MetaIndex index = MetaIndex.getFlag(flagWatcher, entry.getKey());

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
        JsonObject obj = (JsonObject) context.serialize(src);

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
