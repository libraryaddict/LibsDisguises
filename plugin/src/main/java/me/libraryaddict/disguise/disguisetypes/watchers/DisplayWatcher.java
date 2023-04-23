package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class DisplayWatcher extends FlagWatcher {
    public DisplayWatcher(Disguise disguise) {
        super(disguise);
    }

    public Transformation getTransformation() {
        Vector3f transformation = getData(MetaIndex.DISPLAY_TRANSLATION);
        Quaternionf leftRotation = getData(MetaIndex.DISPLAY_LEFT_ROTATION);
        Quaternionf rightRotation = getData(MetaIndex.DISPLAY_RIGHT_ROTATION);
        Vector3f scale = getData(MetaIndex.DISPLAY_SCALE);

        return new Transformation(transformation, leftRotation, scale, rightRotation);
    }

    // Because BlockDisplayWatcher modifies this on startup..
    @RandomDefaultValue
    public void setTransformation(Transformation transformation) {
        setData(MetaIndex.DISPLAY_TRANSLATION, transformation.getTranslation());
        setData(MetaIndex.DISPLAY_LEFT_ROTATION, transformation.getLeftRotation());
        setData(MetaIndex.DISPLAY_RIGHT_ROTATION, transformation.getRightRotation());
        setData(MetaIndex.DISPLAY_SCALE, transformation.getScale());

        sendData(MetaIndex.DISPLAY_TRANSLATION, MetaIndex.DISPLAY_LEFT_ROTATION, MetaIndex.DISPLAY_RIGHT_ROTATION, MetaIndex.DISPLAY_SCALE);
    }

    public Vector3f getTranslation() {
        return getData(MetaIndex.DISPLAY_TRANSLATION);
    }

    // Because BlockDisplayWatcher modifies this on startup..
    @RandomDefaultValue
    public void setTranslation(Vector3f translation) {
        setData(MetaIndex.DISPLAY_TRANSLATION, translation);
        sendData(MetaIndex.DISPLAY_TRANSLATION);
    }

    public Vector3f getScale() {
        return getData(MetaIndex.DISPLAY_SCALE);
    }

    public void setScale(Vector3f scale) {
        setData(MetaIndex.DISPLAY_SCALE, scale);
        sendData(MetaIndex.DISPLAY_SCALE);
    }

    public Quaternionf getLeftRotation() {
        return getData(MetaIndex.DISPLAY_LEFT_ROTATION);
    }

    public void setLeftRotation(Quaternionf rotation) {
        setData(MetaIndex.DISPLAY_LEFT_ROTATION, rotation);
        sendData(MetaIndex.DISPLAY_LEFT_ROTATION);
    }

    public Quaternionf getRightRotation() {
        return getData(MetaIndex.DISPLAY_LEFT_ROTATION);
    }

    public void setRightRotation(Quaternionf rotation) {
        setData(MetaIndex.DISPLAY_RIGHT_ROTATION, rotation);
        sendData(MetaIndex.DISPLAY_RIGHT_ROTATION);
    }

    public int getInterpolationDuration() {
        return getData(MetaIndex.DISPLAY_INTERPOLATION_DURATION);
    }

    public void setInterpolationDuration(int duration) {
        setData(MetaIndex.DISPLAY_INTERPOLATION_DURATION, duration);
        sendData(MetaIndex.DISPLAY_INTERPOLATION_DURATION);
    }

    public float getViewRange() {
        return getData(MetaIndex.DISPLAY_VIEW_RANGE);
    }

    public void setViewRange(float range) {
        setData(MetaIndex.DISPLAY_VIEW_RANGE, range);
        sendData(MetaIndex.DISPLAY_VIEW_RANGE);
    }

    public float getShadowRadius() {
        return getData(MetaIndex.DISPLAY_SHADOW_RADIUS);
    }

    public void setShadowRadius(float radius) {
        setData(MetaIndex.DISPLAY_SHADOW_RADIUS, radius);
        sendData(MetaIndex.DISPLAY_SHADOW_RADIUS);
    }

    public float getShadowStrength() {
        return getData(MetaIndex.DISPLAY_SHADOW_STRENGTH);
    }

    public void setShadowStrength(float strength) {
        setData(MetaIndex.DISPLAY_SHADOW_STRENGTH, strength);
        sendData(MetaIndex.DISPLAY_SHADOW_STRENGTH);
    }

    public float getDisplayWidth() {
        return getData(MetaIndex.DISPLAY_WIDTH);
    }

    public void setDisplayWidth(float width) {
        setData(MetaIndex.DISPLAY_WIDTH, width);
        sendData(MetaIndex.DISPLAY_WIDTH);
    }

    public float getDisplayHeight() {
        return getData(MetaIndex.DISPLAY_HEIGHT);
    }

    public void setDisplayHeight(float height) {
        setData(MetaIndex.DISPLAY_HEIGHT, height);
        sendData(MetaIndex.DISPLAY_HEIGHT);
    }

    public int getInterpolationDelay() {
        return getData(MetaIndex.DISPLAY_INTERPOLATION_START_DELTA_TICKS);
    }

    public void setInterpolationDelay(int ticks) {
        setData(MetaIndex.DISPLAY_INTERPOLATION_START_DELTA_TICKS, ticks);
        sendData(MetaIndex.DISPLAY_INTERPOLATION_START_DELTA_TICKS);
    }

    public Display.Billboard getBillboard() {
        return Display.Billboard.values()[getData(MetaIndex.DISPLAY_BILLBOARD_RENDER_CONSTRAINTS)];
    }

    // Because TextDisplayWatcher modifies this on startup..
    @RandomDefaultValue
    public void setBillboard(Display.Billboard billboard) {
        setData(MetaIndex.DISPLAY_BILLBOARD_RENDER_CONSTRAINTS, (byte) billboard.ordinal());
        sendData(MetaIndex.DISPLAY_BILLBOARD_RENDER_CONSTRAINTS);
    }

    public Color getGlowColorOverride() {
        int color = getData(MetaIndex.DISPLAY_GLOW_COLOR_OVERRIDE);
        return color == -1 ? null : Color.fromARGB(color);
    }

    public void setGlowColorOverride(Color color) {
        setData(MetaIndex.DISPLAY_GLOW_COLOR_OVERRIDE, color == null ? -1 : color.asARGB());
        sendData(MetaIndex.DISPLAY_GLOW_COLOR_OVERRIDE);
    }

    public Display.Brightness getBrightness() {
        int data = getData(MetaIndex.DISPLAY_BRIGHTNESS_OVERRIDE);

        if (data == -1) {
            return null;
        }

        int blockLight = data >> 4 & '\uffff';
        int skyLight = data >> 20 & '\uffff';

        return new Display.Brightness(blockLight, skyLight);
    }

    public void setBrightness(Display.Brightness brightness) {
        setData(MetaIndex.DISPLAY_BRIGHTNESS_OVERRIDE, brightness == null ? -1 : brightness.getBlockLight() << 4 | brightness.getSkyLight() << 20);
        sendData(MetaIndex.DISPLAY_BRIGHTNESS_OVERRIDE);
    }
}
