package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.EntityPose;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.VillagerData;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoParticle;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by libraryaddict on 1/06/2017.
 */
public class SerializerFlagWatcher implements JsonDeserializer<FlagWatcher>, InstanceCreator<FlagWatcher> {
    private final Gson gson;

    public SerializerFlagWatcher(Gson gson) {
        this.gson = gson;
    }

    @Override
    public FlagWatcher deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            FlagWatcher watcher = (FlagWatcher) gson.fromJson(json, Class.forName(((JsonObject) json).get("flagType").getAsString()));

            DisguiseType entity = DisguiseType.valueOf(((JsonObject) json).get("entityType").getAsString());

            correct(watcher, watcher.getClass(), "entityValues");
            correct(watcher, entity.getWatcherClass(), "backupEntityValues");

            return watcher;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void correct(FlagWatcher watcher, Class<? extends FlagWatcher> flagWatcher, String name)
        throws NoSuchFieldException, IllegalAccessException, DisguiseParseException {
        Field field = FlagWatcher.class.getDeclaredField(name);
        field.setAccessible(true);
        HashMap<Integer, Object> map = (HashMap<Integer, Object>) field.get(watcher);
        int count = 0;

        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            MetaIndex index = MetaIndex.getMetaIndex(flagWatcher, entry.getKey());

            if (entry.getValue() instanceof Double) {
                Object def = index.getDefault();

                if (def instanceof Long) {
                    entry.setValue(((Double) entry.getValue()).longValue());
                } else if (def instanceof Float) {
                    entry.setValue(((Double) entry.getValue()).floatValue());
                } else if (def instanceof Integer) {
                    entry.setValue(((Double) entry.getValue()).intValue());
                } else if (def instanceof Short) {
                    entry.setValue(((Double) entry.getValue()).shortValue());
                } else if (def instanceof Byte) {
                    entry.setValue(((Double) entry.getValue()).byteValue());
                }
            } else if (entry.getValue() instanceof String) {
                if (index.getDefault() instanceof WrappedParticle) {
                    entry.setValue(((ParamInfoParticle) ParamInfoManager.getParamInfo(WrappedParticle.class)).fromString((String) entry.getValue()));
                } else if (index.getDefault() instanceof EntityPose) {
                    entry.setValue(((ParamInfoEnum) ParamInfoManager.getParamInfo(EntityPose.class)).fromString((String) entry.getValue()));
                }
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
                        } catch (IllegalAccessException e) {
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
                                value = Optional.of(gson.fromJson(gson.toJson(((LinkedTreeMap) entry.getValue()).get("value")), val));
                            }

                            entry.setValue(value);
                        }
                    }
                } else if (index.getDefault() instanceof ItemStack) {
                    entry.setValue(gson.fromJson(gson.toJson(entry.getValue()), ItemStack.class));
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
            return (FlagWatcher) ((Class) type).getConstructor(Disguise.class).newInstance(new Object[]{null});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
