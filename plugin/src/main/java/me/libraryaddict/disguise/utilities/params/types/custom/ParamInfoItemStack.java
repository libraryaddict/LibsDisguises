package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.reflection.ItemStackSerializer;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public class ParamInfoItemStack<I extends ItemStack> extends ParamInfoEnum<Object> {
    public ParamInfoItemStack(Class paramClass, String name, String valueType, String description, Enum[] possibleValues) {
        super(paramClass, name, valueType, description, possibleValues);

        setOtherValues("null", "%held-item%", "%offhand-item%", "%helmet%", "%chestplate%", "%leggings%", "%boots%");
    }

    @Override
    public boolean canTranslateValues() {
        return false;
    }

    @Override
    public boolean canReturnNull() {
        return true;
    }

    @Override
    public Object fromString(String string) {
        return parseToItemstack(string);
    }

    @Override
    public String toString(Object object) {
        ItemStack item = (ItemStack) object;
        ItemStack temp = new ItemStack(item.getType(), item.getAmount());

        if (item.containsEnchantment(DisguiseUtilities.getDurabilityEnchantment())) {
            temp.addUnsafeEnchantment(DisguiseUtilities.getDurabilityEnchantment(), 1);
        }

        if (temp.isSimilar(item)) {
            String name = item.getType().name();

            if (item.getAmount() != 1) {
                name += ":" + item.getAmount();
            }

            if (item.containsEnchantment(DisguiseUtilities.getDurabilityEnchantment())) {
                name += ":" + TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("glow");
            }

            if (NmsVersion.v1_14.isSupported() && item.getItemMeta().hasCustomModelData()) {
                name += ":custom_model_" + item.getItemMeta().getCustomModelData();
            }

            return name;
        }

        return StringUtils.join(ItemStackSerializer.serialize(item), "-");
    }

    protected static ItemStack parseToItemstack(String string) {
        if (string.isEmpty()) {
            return null;
        } else if (string.startsWith("{") && string.endsWith("}")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, ItemStack.class);
            } catch (Exception ex) {
                throw new IllegalArgumentException();
            }
        } else if (!string.matches("[a-zA-Z0-9_:,]+")) {
            // If it can't be simple parsed due to invalid chars
            String[] split;
            String materialName;

            if (!NmsVersion.v1_20_R4.isSupported()) {
                // If it matches /give @p stone {data}
                if (string.matches("^[^{]+?[ -]\\{[.].+?}$")) {
                    split = string.substring(0, string.indexOf("{") - 1).split("[ -]");
                    split = Arrays.copyOf(split, split.length + 1);
                    split[split.length - 1] = string.substring(string.indexOf("{"));
                } else if (string.matches("^[^{ -]+?\\{.+?}([ -][0-9]+)?$")) { // /give @p stone[data] <amount?>
                    split = new String[string.endsWith("}") ? 2 : 3];
                    split[0] = string.substring(0, string.indexOf("{"));
                    split[string.endsWith("}") ? 1 : 2] = string.substring(string.indexOf("{"), string.lastIndexOf("}") + 1);

                    if (!string.endsWith("}")) {
                        split[1] = string.substring(string.lastIndexOf(" ") + 1);
                    }
                } else {
                    split = string.split("[ -]");
                }

                materialName = split[0];
            } else {
                split = string.split(" (?=\\d+)");
                materialName = split[0];

                if (materialName.contains("[")) {
                    materialName = materialName.substring(0, materialName.indexOf("["));
                }
            }

            Material material = ReflectionManager.getMaterial(materialName.toUpperCase(Locale.ENGLISH));

            if (material == null) {
                material = Material.getMaterial(materialName.toUpperCase(Locale.ENGLISH));
            }

            ItemStack itemStack = getItemStack(material, split);

            String s = split[split.length - 1];

            // Because now we have to provide the item, lets just hope the user gave the right args
            if (NmsVersion.v1_20_R4.isSupported()) {
                s = split[0];
            }

            if (s.contains("{") || s.contains("[")) {
                ItemStack clone = itemStack.clone();
                Bukkit.getUnsafe().modifyItemStack(itemStack, s);

                if (DisguiseUtilities.isDebuggingMode() && itemStack.equals(clone)) {
                    LibsDisguises.getInstance().getLogger().info(
                        "Potential error when trying to modify an item via Bukkit Unsafe. Item Type: " + clone.getType() + ", Data: '" + s +
                            "'");
                }
            }

            return itemStack;
        }

        return parseToItemstack(string.split("[:,]")); // Split on colon or comma
    }

    @NotNull
    private static ItemStack getItemStack(Material material, String[] split) {
        if (material == null || (material == Material.AIR && !split[0].equalsIgnoreCase("air"))) {
            throw new IllegalArgumentException();
        }

        int amount = split.length > 1 && split[1].matches("[0-9]+") ? Integer.parseInt(split[1]) : 1;
        ItemStack itemStack;

        if (!NmsVersion.v1_13.isSupported() && split.length > 2 && split[2].matches("[0-9]+")) {
            itemStack = new ItemStack(material, amount, Short.parseShort(split[2]));
        } else {
            itemStack = new ItemStack(material, amount);
        }

        return itemStack;
    }

    protected static ItemStack parseToItemstack(String[] split) {
        if (split[0].isEmpty() || split[0].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("null"))) {
            return null;
        }
        Material material = ReflectionManager.getMaterial(split[0].toUpperCase(Locale.ENGLISH));

        if (material == null) {
            material = Material.getMaterial(split[0].toUpperCase(Locale.ENGLISH));
        }

        if (material == null || (material == Material.AIR && !split[0].equalsIgnoreCase("air"))) {
            throw new IllegalArgumentException();
        }

        Integer amount = null;
        boolean enchanted = false;
        Integer customModel = null;

        for (int i = 1; i < split.length; i++) {
            String s = split[i];

            if (!enchanted && s.equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("glow"))) {
                enchanted = true;
            } else if (s.matches("\\d+") && amount == null) {
                amount = Integer.parseInt(s);
            } else if (NmsVersion.v1_14.isSupported() && s.toLowerCase(Locale.ENGLISH).matches("^custom_model_-?\\d+$")) {
                customModel = Integer.parseInt(s.split("_", 3)[2]);
            } else {
                throw new IllegalArgumentException();
            }
        }

        ItemStack itemStack = new ItemStack(material, amount == null ? 1 : amount);

        if (enchanted) {
            itemStack.addUnsafeEnchantment(DisguiseUtilities.getDurabilityEnchantment(), 1);
        }

        if (customModel != null) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setCustomModelData(customModel);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    public boolean isParam(Class paramClass) {
        return getParamClass().isAssignableFrom(paramClass);
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }
}
