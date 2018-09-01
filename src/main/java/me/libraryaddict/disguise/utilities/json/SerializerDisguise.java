package me.libraryaddict.disguise.utilities.json;

import com.google.gson.*;
import me.libraryaddict.disguise.disguisetypes.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerDisguise implements JsonDeserializer<Disguise>, JsonSerializer<Disguise>, InstanceCreator<Disguise> {

    @Override
    public Disguise deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = (JsonObject) json;
        DisguiseType type = DisguiseType.valueOf(obj.get("disguiseType").getAsString());
        TargetedDisguise disg;

        if (type.isPlayer()) {
            disg = context.deserialize(json, PlayerDisguise.class);
        } else if (type.isMob()) {
            disg = context.deserialize(json, MobDisguise.class);
        } else if (type.isMisc()) {
            disg = context.deserialize(json, MiscDisguise.class);
        } else
            return null;

        try {
            Method method = FlagWatcher.class.getDeclaredMethod("setDisguise", TargetedDisguise.class);
            method.setAccessible(true);
            method.invoke(disg.getWatcher(), disg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return disg;
    }

    @Override
    public Disguise createInstance(Type type) {
        if (type == PlayerDisguise.class)
            return new PlayerDisguise("SaveDisgError");
        else if (type == MobDisguise.class)
            return new MobDisguise(DisguiseType.SHEEP);
        else if (type == MiscDisguise.class)
            return new MiscDisguise(DisguiseType.BOAT);

        return null;
    }

    @Override
    public JsonElement serialize(Disguise src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.isPlayerDisguise())
            return context.serialize(src, PlayerDisguise.class);
        else if (src.isMobDisguise())
            return context.serialize(src, MobDisguise.class);
        else if (src.isMiscDisguise())
            return context.serialize(src, MiscDisguise.class);

        return null;
    }
}
