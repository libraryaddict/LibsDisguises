package me.libraryaddict.disguise.disguisetypes;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum AnimalColor {
    BLACK(DyeColor.BLACK, Material.INK_SAC),
    BLUE(DyeColor.BLUE, Material.LAPIS_LAZULI),
    BROWN(DyeColor.BROWN, Material.COCOA_BEANS),
    CYAN(DyeColor.CYAN, Material.CYAN_DYE),
    GRAY(DyeColor.GRAY, Material.GRAY_DYE),
    GREEN(DyeColor.GREEN, Material.GREEN_DYE),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, Material.LIGHT_BLUE_DYE),
    LIME(DyeColor.LIME, Material.LIME_DYE),
    MAGENTA(DyeColor.MAGENTA, Material.MAGENTA_DYE),
    ORANGE(DyeColor.ORANGE, Material.ORANGE_DYE),
    PINK(DyeColor.PINK, Material.PINK_DYE),
    PURPLE(DyeColor.PURPLE, Material.PURPLE_DYE),
    RED(DyeColor.RED, Material.RED_DYE),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, Material.LIGHT_GRAY_DYE),
    WHITE(DyeColor.WHITE, Material.BONE_MEAL),
    YELLOW(DyeColor.YELLOW, Material.YELLOW_DYE);

    public static AnimalColor getColorByWool(int woolId) {
        for (AnimalColor color : values()) {
            if (woolId != color.getDyeColor().getWoolData()) {
                continue;
            }

            return color;
        }

        return null;
    }

    public static AnimalColor getColorByWool(Material carpet) {
        if (carpet == null || (!carpet.name().endsWith("_WOOL") && !carpet.name().endsWith("_CARPET"))) {
            return null;
        }

        String name = carpet.name().replace("_CARPET", "").replace("_WOOL", "");

        for (AnimalColor color : AnimalColor.values()) {
            if (!color.name().equals(name)) {
                continue;
            }

            return color;
        }

        return null;
    }

    public static AnimalColor getColorByMaterial(Material material) {
        for (AnimalColor color : values()) {
            if (color.getDyeMaterial() != material) {
                continue;
            }

            return color;
        }

        return null;
    }

    public static AnimalColor getColorByDye(int dyeId) {
        for (AnimalColor color : values()) {
            if (dyeId != color.getDyeColor().getDyeData()) {
                continue;
            }

            return color;
        }

        return null;
    }

    public static AnimalColor getColor(DyeColor dyeColor) {
        for (AnimalColor color : values()) {
            if (dyeColor != color.getDyeColor()) {
                continue;
            }

            return color;
        }

        return null;
    }

    private DyeColor dyeColor;
    private Material material;

    AnimalColor(DyeColor color, Material material) {
        dyeColor = color;
        this.material = material;
    }

    public Material getDyeMaterial() {
        return material;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
