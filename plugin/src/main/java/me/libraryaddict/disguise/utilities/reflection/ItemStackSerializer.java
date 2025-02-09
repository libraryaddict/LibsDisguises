package me.libraryaddict.disguise.utilities.reflection;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackSerializer {

    public static List<String> serialize(ItemStack item) {
        // If its not a CraftItemStack
        if (!ReflectionManager.isCraftItem(item) && item.hasItemMeta()) {
            item = ReflectionManager.getCraftItem(item);
        }

        List<String> mcArray = new ArrayList<>();
        String type = ReflectionManager.getItemName(item.getType());

        if (item.hasItemMeta() && NmsVersion.v1_13.isSupported()) {
            String asString = ReflectionManager.getNmsReflection().getDataAsString(item);

            if (asString != null && asString.length() > 2) {
                // Vanilla seems to turn this into a string that contains for UUID
                // : [I; 772059800,
                // And there should be no space, so we must strip all spaces that are unneeded.
                if (NmsVersion.v1_17.isSupported()) {
                    asString = stripSpacesFromString(asString);
                }

                type += asString;
            }
        }

        mcArray.add(type);

        if (item.getAmount() != 1) {
            mcArray.add(String.valueOf(item.getAmount()));
        }

        if (!NmsVersion.v1_13.isSupported()) {
            if (item.getDurability() != 0) {
                mcArray.add(String.valueOf(item.getDurability()));
            }

            if (item.hasItemMeta()) {
                String asString = ReflectionManager.getNmsReflection().getDataAsString(item);

                if (asString != null && asString.length() > 2) {
                    mcArray.add(asString);
                }
            }
        }

        return mcArray;
    }

    private static String stripSpacesFromString(String string) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        boolean escaped = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inQuote = !inQuote;
            } else if (!inQuote && c == ' ') {
                continue; // Skip spaces outside quotes
            }

            result.append(c);
        }

        return result.toString();
    }
}
