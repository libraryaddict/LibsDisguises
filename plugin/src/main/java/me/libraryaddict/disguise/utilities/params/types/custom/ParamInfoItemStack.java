package me.libraryaddict.disguise.utilities.params.types.custom;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoItemStack<I extends ItemStack> extends ParamInfoEnum<Object> {
    public ParamInfoItemStack(Class paramClass, String name, String valueType, String description, Enum[] possibleValues) {
        super(paramClass, name, valueType, description, possibleValues);

        if (this instanceof ParamInfoItemBlock) {
            return;
        }

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

        if (item.containsEnchantment(Enchantment.DURABILITY)) {
            temp.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        if (temp.isSimilar(item)) {
            String name = item.getType().name();

            if (item.getAmount() != 1) {
                name += ":" + item.getAmount();
            }

            if (item.containsEnchantment(Enchantment.DURABILITY)) {
                name += ":" + TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("glow");
            }

            return name;
        }

        // If its not a CraftItemStack
        if (!MinecraftReflection.isCraftItemStack(item) && item.hasItemMeta()) {
            item = ReflectionManager.getCraftItem(item);
        }

        String itemName = ReflectionManager.getItemName(item.getType());
        ArrayList<String> mcArray = new ArrayList<>();

        if (NmsVersion.v1_13.isSupported() && item.hasItemMeta()) {
            mcArray.add(itemName + DisguiseUtilities.serialize(NbtFactory.fromItemTag(item)));
        } else {
            mcArray.add(itemName);
        }

        if (item.getAmount() != 1) {
            mcArray.add(String.valueOf(item.getAmount()));
        }

        if (!NmsVersion.v1_13.isSupported()) {
            if (item.getDurability() != 0) {
                mcArray.add(String.valueOf(item.getDurability()));
            }

            if (item.hasItemMeta()) {
                mcArray.add(DisguiseUtilities.serialize(NbtFactory.fromItemTag(item)));
            }
        }

        return StringUtils.join(mcArray, "-");
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
        } else if (!string.matches("[a-zA-Z0-9_:,]+")) { // If it can't be simple parsed due to invalid chars
            String[] split;

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

            Material material = ReflectionManager.getMaterial(split[0].toLowerCase(Locale.ENGLISH));

            if (material == null) {
                material = Material.getMaterial(split[0].toUpperCase(Locale.ENGLISH));
            }

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

            if (split[split.length - 1].contains("{")) {
                Bukkit.getUnsafe().modifyItemStack(itemStack, split[split.length - 1]);
            }

            return itemStack;
        }

        return parseToItemstack(string.split("[:,]")); // Split on colon or comma
    }

    protected static ItemStack parseToItemstack(String[] split) {
        if (split[0].isEmpty() || split[0].equalsIgnoreCase(TranslateType.DISGUISE_OPTIONS_PARAMETERS.get("null"))) {
            return null;
        }

        Material material = Material.getMaterial(split[0].toUpperCase(Locale.ENGLISH));

        if (material == null || (material == Material.AIR && !split[0].equalsIgnoreCase("air"))) {
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

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }
}
