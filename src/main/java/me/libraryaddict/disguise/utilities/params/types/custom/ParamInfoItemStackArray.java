package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoItemStackArray extends ParamInfoItemStack {
    public ParamInfoItemStackArray(Class paramClass, String name, String valueType, String description,
            Enum[] possibleValues) {
        super(paramClass, name, valueType, description, possibleValues);
    }

    @Override
    public boolean canReturnNull() {
        return false;
    }

    @Override
    public Set<String> getEnums(String tabComplete) {
        String beginning = tabComplete.substring(0, tabComplete.contains(",") ? tabComplete.lastIndexOf(",") + 1 : 0);
        String end = tabComplete.substring(tabComplete.contains(",") ? tabComplete.lastIndexOf(",") + 1 : 0);

        Set<String> toReturn = new LinkedHashSet<>();

        for (String material : super.getEnums(null)) {
            if (!material.toLowerCase().startsWith(end.toLowerCase()))
                continue;

            toReturn.add(beginning + material);
        }

        return toReturn;
    }

    @Override
    public String toString(Object object) {
        ItemStack[] stacks = (ItemStack[]) object;

        String returns = "";

        for (int i = 0; i < stacks.length; i++) {
            if (i > 0) {
                returns += ",";
            }

            if (stacks[i] == null) {
                continue;
            }

            String toString = super.toString(stacks[i]);

            // If we can't parse to simple
            if (toString.startsWith("{")) {
                return DisguiseUtilities.getGson().toJson(object);
            }

            returns += toString;
        }

        return returns;
    }

    @Override
    public Object fromString(String string) {
        if (string.startsWith("[") && string.endsWith("]")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, ItemStack[].class);
            }
            catch (Exception ex) {
            }
        }

        String[] split = split(string);

        if (split == null || split.length != 4) {
            return null;
        }

        // Parse to itemstack array
        ItemStack[] items = new ItemStack[4];

        for (int a = 0; a < 4; a++) {
            items[a] = parseToItemstack(split[a]);
        }

        return items;
    }

    private static String[] split(String string) {
        String[] split = new String[4];

        char[] chars = string.toCharArray();
        boolean quote = false;
        int depth = 0;
        int splitNo = 0;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            if (splitNo > 3 || depth < 0) {
                return null;
            }

            char c = chars[i];

            if (!quote && depth == 0 && c == ',') {
                split[splitNo++] = builder.toString();
                builder = new StringBuilder();
                continue;
            }

            builder.append(c);

            if (c == '\\' && i + 1 < chars.length) {
                builder.append(chars[++i]);
                continue;
            }

            if (c == '"') {
                quote = !quote;
            }

            if (!quote) {
                if (c == '{' || c == '[') {
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth--;
                }
            }
        }

        if (splitNo != 3 || quote || depth != 0) {
            return null;
        }

        split[splitNo] = builder.toString();

        return split;
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }
}
