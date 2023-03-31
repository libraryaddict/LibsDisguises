package me.libraryaddict.disguise.disguisetypes;

import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AnimalColor {
    BLACK(DyeColor.BLACK, NmsVersion.v1_13.isSupported() ? Material.getMaterial("INK_SAC") : Material.getMaterial("INK_SACK")),
    BLUE(DyeColor.BLUE, NmsVersion.v1_13.isSupported() ? Material.getMaterial("LAPIS_LAZULI") : null),
    BROWN(DyeColor.BROWN, NmsVersion.v1_13.isSupported() ? Material.getMaterial("COCOA_BEANS") : null),
    CYAN(DyeColor.CYAN, NmsVersion.v1_13.isSupported() ? Material.getMaterial("CYAN_DYE") : null),
    GRAY(DyeColor.GRAY, NmsVersion.v1_13.isSupported() ? Material.getMaterial("GRAY_DYE") : null),
    GREEN(DyeColor.GREEN, NmsVersion.v1_14.isSupported() ? Material.getMaterial("GREEN_DYE") : Material.getMaterial("CACTUS_GREEN")),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, NmsVersion.v1_13.isSupported() ? Material.getMaterial("LIGHT_BLUE_DYE") : null),
    LIME(DyeColor.LIME, NmsVersion.v1_13.isSupported() ? Material.getMaterial("LIME_DYE") : null),
    MAGENTA(DyeColor.MAGENTA, NmsVersion.v1_13.isSupported() ? Material.getMaterial("MAGENTA_DYE") : null),
    ORANGE(DyeColor.ORANGE, NmsVersion.v1_13.isSupported() ? Material.getMaterial("ORANGE_DYE") : null),
    PINK(DyeColor.PINK, NmsVersion.v1_13.isSupported() ? Material.getMaterial("PINK_DYE") : null),
    PURPLE(DyeColor.PURPLE, NmsVersion.v1_13.isSupported() ? Material.getMaterial("PURPLE_DYE") : null),
    RED(DyeColor.RED, NmsVersion.v1_14.isSupported() ? Material.getMaterial("RED_DYE") : Material.getMaterial("ROSE_RED")),
    LIGHT_GRAY(DyeColor.valueOf(NmsVersion.v1_13.isSupported() ? "LIGHT_GRAY" : "SILVER"),
        NmsVersion.v1_13.isSupported() ? Material.getMaterial("LIGHT_GRAY_DYE") : null),
    WHITE(DyeColor.WHITE, NmsVersion.v1_13.isSupported() ? Material.getMaterial("BONE_MEAL") : null),
    YELLOW(DyeColor.YELLOW, NmsVersion.v1_14.isSupported() ? Material.getMaterial("YELLOW_DYE") : Material.getMaterial("DANDELION_YELLOW"));

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

    public static AnimalColor getColorByItem(ItemStack itemStack) {
        if (NmsVersion.v1_13.isSupported()) {
            return getColorByMaterial(itemStack.getType());
        }

        if (itemStack.getType().name().matches("(WOOL)|(CARPET)|(INK_SACK?)")) {
            return getColorByWool(itemStack.getDurability());
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

    private final DyeColor dyeColor;
    private final Material material;

    AnimalColor(DyeColor color, Material material) {
        dyeColor = color;
        this.material = NmsVersion.v1_13.isSupported() ? material : Material.getMaterial("INK_SACK");
    }

    public Material getDyeMaterial() {
        return material;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
