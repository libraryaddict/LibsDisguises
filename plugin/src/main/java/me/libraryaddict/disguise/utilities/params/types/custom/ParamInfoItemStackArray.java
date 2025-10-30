package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class ParamInfoItemStackArray extends ParamInfoItemStack {
    public ParamInfoItemStackArray(Class paramClass, String name, String valueType, String description, Enum[] possibleValues) {
        super(paramClass, name, valueType, description, possibleValues);

        setOtherValues("%armor%");
    }

    @Override
    public boolean canReturnNull() {
        return false;
    }

    @Override
    public Set<String> getEnums(String tabComplete) {
        ArrayList<String> split = split(tabComplete);

        Set<String> toReturn = new LinkedHashSet<>();

        if (split == null || split.stream().anyMatch(s -> s.equalsIgnoreCase("%armor%"))) {
            return toReturn;
        }

        String lastEntry = split.remove(split.size() - 1);

        for (String material : (Set<String>) super.getEnums(null)) {
            if (!split.isEmpty() && !material.toLowerCase(Locale.ENGLISH).startsWith(lastEntry.toLowerCase(Locale.ENGLISH))) {
                continue;
            }

            toReturn.add(StringUtils.join(split, ",") + (split.isEmpty() ? "" : ",") + material);
        }

        return toReturn;
    }

    @Override
    public String toString(Object object) {
        ItemStack[] stacks = (ItemStack[]) object;

        StringBuilder returns = new StringBuilder();

        for (int i = 0; i < stacks.length; i++) {
            if (i > 0) {
                returns.append(",");
            }

            if (stacks[i] == null) {
                continue;
            }

            String toString = super.toString(stacks[i]);

            // If we can't parse to simple
            if (toString.startsWith("{")) {
                return DisguiseUtilities.getGson().toJson(object);
            }

            returns.append(toString);
        }

        return returns.toString();
    }

    @Override
    public ItemStack[] fromString(String string) {
        if (string.startsWith("[") && string.endsWith("]")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, ItemStack[].class);
            } catch (Exception ignored) {
            }
        }

        ArrayList<String> split = split(string);

        if (split == null || split.size() != 4) {
            return null;
        }

        // Parse to itemstack array
        ItemStack[] items = new ItemStack[4];

        for (int a = 0; a < 4; a++) {
            items[a] = parseToItemstack(split.get(a));
        }

        return items;
    }

    private ArrayList<String> split(String string) {
        ArrayList<String> split = new ArrayList<>();

        char[] chars = string.toCharArray();
        boolean quote = false;
        int depth = 0;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            if (split.size() > 3 || depth < 0) {
                return null;
            }

            char c = chars[i];

            if (!quote && depth == 0 && c == ',') {
                split.add(builder.toString());
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
            } else if (!quote) {
                if (c == '{' || c == '[') {
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth--;
                }
            }
        }

        if (quote || depth != 0) {
            return null;
        }

        split.add(builder.toString());

        return split;
    }
}
