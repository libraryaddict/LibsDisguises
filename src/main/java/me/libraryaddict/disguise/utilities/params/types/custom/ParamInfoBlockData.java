package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

/**
 * Created by libraryaddict on 12/08/2020.
 */
public class ParamInfoBlockData extends ParamInfo {
    public ParamInfoBlockData(Class paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    protected Object fromString(String string) throws DisguiseParseException {
        if (string == null || string.equals("null")) {
            return null;
        }

        return Bukkit.createBlockData(string);
    }

    @Override
    public String toString(Object object) {
        if (object == null) {
            return "null";
        }

        return ((BlockData) object).getAsString();
    }

    @Override
    public boolean isParam(Class paramClass) {
        return getParamClass().isAssignableFrom(paramClass);
    }
}
