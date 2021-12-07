package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import org.bukkit.util.EulerAngle;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoEulerAngle extends ParamInfo {
    public ParamInfoEulerAngle(Class paramClass, String name, String valueType, String description) {
        super(paramClass, name, valueType, description);
    }

    @Override
    protected Object fromString(String string) {
        String[] split = string.split(",");

        if (split.length != 3) {
            return null;
        }

        return new EulerAngle(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }

    @Override
    public String toString(Object object) {
        EulerAngle angle = (EulerAngle) object;

        return String.format("%s,%s,%s", angle.getX(), angle.getY(), angle.getZ());
    }
}
