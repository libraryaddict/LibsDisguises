package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParamInfoTransformation extends ParamInfo<Transformation> {
    public ParamInfoTransformation(Class paramClass, String name, String valueType, String description) {
        super(paramClass, name, valueType, description);
    }

    @Override
    protected Transformation fromString(String string) {
        String[] split = string.split(",");

        if (split.length != 14) {
            return null;
        }

        Vector3f translation = new Vector3f(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]));
        Quaternionf leftRotation =
            new Quaternionf(Float.parseFloat(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]), Float.parseFloat(split[6]));
        Vector3f scale = new Vector3f(Float.parseFloat(split[7]), Float.parseFloat(split[8]), Float.parseFloat(split[9]));
        Quaternionf rightRotation =
            new Quaternionf(Float.parseFloat(split[10]), Float.parseFloat(split[11]), Float.parseFloat(split[12]), Float.parseFloat(split[13]));

        return new Transformation(translation, leftRotation, scale, rightRotation);
    }

    @Override
    public String toString(Transformation transformation) {
        Vector3f trans = transformation.getTranslation();
        Quaternionf lL = transformation.getLeftRotation();
        Vector3f scale = transformation.getScale();
        Quaternionf lR = transformation.getRightRotation();

        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", trans.x(), trans.y(), trans.z(), lL.x(), lL.y(), lL.z(), lL.w(), scale.x(), scale.y(),
            scale.z(), lR.x(), lR.y(), lR.z(), lR.w());
    }
}
