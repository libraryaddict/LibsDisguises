package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import me.libraryaddict.disguise.utilities.parser.params.types.ParamInfoEnum;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoItemStack extends ParamInfoEnum {
    public ParamInfoItemStack(Class paramClass, String name, String valueType, String description,
            Enum[] possibleValues) {
        super(paramClass, name, valueType, description, possibleValues);

        setOtherValues("null", "glow");
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
        return DisguiseUtilities.getGson().toJson(object);
    }

    protected static ItemStack parseToItemstack(String string) {
        if (string.startsWith("{") && string.endsWith("}")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, ItemStack.class);
            }
            catch (Exception ex) {
            }
        }

        return parseToItemstack(string.split("[:,]")); // Split on colon or comma
    }

    protected static ItemStack parseToItemstack(String[] split) {
        if (split[0].isEmpty() || split[0].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("null"))) {
            return null;
        }

        Material material = Material.getMaterial(split[0].toUpperCase());

        if (material == null) {
            throw new IllegalArgumentException();
        }

        Integer amount = null;
        boolean enchanted = false;

        for (int i = 1; i < split.length; i++) {
            String s = split[i];

            if (!enchanted && s.equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("glow"))) {
                enchanted = true;
            } else if (s.matches("\\d+") && amount == null) {
                amount = Integer.parseInt(s);
            } else {
                throw new IllegalArgumentException();
            }
        }

        ItemStack itemStack = new ItemStack(material, amount == null ? 1 : amount);

        if (enchanted) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        return itemStack;
    }

    public boolean isParam(Class paramClass) {
        return getParamClass().isAssignableFrom(paramClass);
    }
}
