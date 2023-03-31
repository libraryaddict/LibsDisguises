package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import org.joml.Vector3f;

public class ParamInfoVector3f extends ParamInfo<Vector3f> {
    public ParamInfoVector3f(Class paramClass, String name, String valueType, String description) {
        super(paramClass, name, valueType, description);
    }

    @Override
    protected Vector3f fromString(String string) {
        String[] split = string.split(",");

        if (split.length != 3) {
            return null;
        }

        return new Vector3f(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]));
    }

    @Override
    public String toString(Vector3f transformation) {
        return String.format("%s,%s,%s", transformation.x(), transformation.y(), transformation.z());
    }
}
