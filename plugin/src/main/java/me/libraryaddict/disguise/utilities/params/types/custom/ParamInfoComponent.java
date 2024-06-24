package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.gson.JsonParseException;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import net.kyori.adventure.text.Component;

public class ParamInfoComponent extends ParamInfo<Component> {
    public ParamInfoComponent(Class<Component> paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    public Component fromString(String string) throws DisguiseParseException {
        try {
            return AdventureSerializer.getGsonSerializer().deserialize(string);
        } catch (JsonParseException ex) {
            // Lets assume it's not gson
            return DisguiseUtilities.getAdventureChat(string);
        }
    }

    @Override
    public String toString(Component object) {
        return AdventureSerializer.getGsonSerializer().serialize(object);
    }
}
