package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.util.Vector3i;
import me.libraryaddict.disguise.utilities.params.ParamInfo;

public class ParamInfoBlockPosition extends ParamInfo {
    public ParamInfoBlockPosition(Class paramClass, String name, String valueType, String description) {
        super(paramClass, name, valueType, description);
    }

    @Override
    protected Object fromString(String string) {
        String[] split = string.split(",");

        if (split.length != 3) {
            return null;
        }

        return new Vector3i(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    @Override
    public String toString(Object object) {
        Vector3i position = (Vector3i) object;

        return String.format("%s,%s,%s", position.getX(), position.getY(), position.getZ());
    }
}
