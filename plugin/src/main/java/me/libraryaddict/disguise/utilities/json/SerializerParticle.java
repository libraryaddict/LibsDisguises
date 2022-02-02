package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;

/**
 * Created by libraryaddict on 5/12/2020.
 */
public class SerializerParticle implements JsonSerializer<WrappedParticle> {
    @Override
    public JsonElement serialize(WrappedParticle src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(ParamInfoManager.getParamInfo(WrappedParticle.class).toString(src));
    }
}
