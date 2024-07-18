package me.libraryaddict.disguise.utilities.gson;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SerializerWrappedBlockData implements JsonSerializer<WrappedBlockState>, JsonDeserializer<WrappedBlockState> {

    @Override
    public JsonElement serialize(WrappedBlockState src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public WrappedBlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return WrappedBlockState.getByString(json.getAsString());
        }

        JsonObject obj = json.getAsJsonObject();
        String type = obj.get("type").getAsString();
        int data = obj.get("data").getAsInt();

        StateType.Mapped stateType = StateTypes.getMappedByName(type);

        if (stateType == null) {
            return null;
        }

        int combinedID = stateType.getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()) << 4 | data;

        return WrappedBlockState.getByGlobalId(combinedID);
    }
}
