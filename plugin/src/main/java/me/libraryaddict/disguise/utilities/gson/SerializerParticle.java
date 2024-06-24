package me.libraryaddict.disguise.utilities.gson;

import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 5/12/2020.
 */
public class SerializerParticle implements JsonSerializer<Particle> {
    @Override
    public JsonElement serialize(Particle src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(ParamInfoManager.getParamInfo(Particle.class).toString(src));
    }
}
