package me.libraryaddict.disguise.utilities.json;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.google.gson.*;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Type;

/**
 * Created by libraryaddict on 5/12/2020.
 */
public class SerializerParticle implements JsonSerializer<WrappedParticle> {
    @Override
    public JsonElement serialize(WrappedParticle src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(ParamInfoManager.getParamInfo(WrappedParticle.class).toString(src));
    }
}
