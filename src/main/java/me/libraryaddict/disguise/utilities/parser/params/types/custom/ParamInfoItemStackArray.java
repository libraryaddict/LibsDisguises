package me.libraryaddict.disguise.utilities.parser.params.types.custom;

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
        return DisguiseUtilities.getGson().toJson(object);
    }

    @Override
    public Object fromString(String string) {
        if (string.startsWith("{") && string.endsWith("}")) {
            try {
                return DisguiseUtilities.getGson().fromJson(string, ItemStack[].class);
            }
            catch (Exception ex) {
            }
        }

        String[] split = string.split(",", -1);

        if (split.length != 4) {
            return null;
        }

        // Parse to itemstack array
        ItemStack[] items = new ItemStack[4];

        for (int a = 0; a < 4; a++) {
            items[a] = parseToItemstack(split[a].split(":"));
        }

        return items;
    }
}
