package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import org.joml.Quaternionf;

public class ParamInfoQuaternionf extends ParamInfo<Quaternionf> {
    public ParamInfoQuaternionf(Class paramClass, String name, String valueType, String description) {
        super(paramClass, name, valueType, description);
    }

    @Override
    protected Quaternionf fromString(String string) {
        String[] split = string.split(",");

        if (split.length != 4) {
            return null;
        }

        return new Quaternionf(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]));
    }

    @Override
    public String toString(Quaternionf transformation) {
        return String.format("%s,%s,%s,%s", transformation.x(), transformation.y(), transformation.z(), transformation.w());
    }
}
