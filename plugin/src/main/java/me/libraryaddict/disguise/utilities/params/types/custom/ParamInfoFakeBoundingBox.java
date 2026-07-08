package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.reflection.FakeBoundingBox;

public class ParamInfoFakeBoundingBox extends ParamInfo<FakeBoundingBox> {
    public ParamInfoFakeBoundingBox() {
        super(FakeBoundingBox.class, "Server Hitbox",
            "Sets the server-side hitbox half-extents as width,height,depth (full block sizes, not half-widths). Example: 1,2,1");
    }

    @Override
    protected FakeBoundingBox fromString(String string) throws DisguiseParseException {
        if (string == null || string.equalsIgnoreCase("null")) {
            return null;
        }

        String[] split = string.split(",");

        if (split.length != 3) {
            throw new DisguiseParseException("Expected width,height,depth for server bounding box");
        }

        try {
            double width = Double.parseDouble(split[0].trim());
            double height = Double.parseDouble(split[1].trim());
            double depth = Double.parseDouble(split[2].trim());

            return new FakeBoundingBox(width, height, depth);
        } catch (NumberFormatException ex) {
            throw new DisguiseParseException("Invalid number in server bounding box: " + string);
        }
    }

    @Override
    public String toString(FakeBoundingBox object) {
        if (object == null) {
            return "null";
        }

        return object.getX() + "," + object.getY() + "," + object.getZ();
    }
}