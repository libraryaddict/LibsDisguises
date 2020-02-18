package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Created by libraryaddict on 16/02/2020.
 */
public class ParamInfoItemBlock extends ParamInfoItemStack {
    public ParamInfoItemBlock(Class paramClass, String name, String valueType, String description,
            Material[] possibleValues) {
        super(paramClass, name, valueType, description,
                Arrays.stream(possibleValues).filter(Material::isBlock).toArray(Material[]::new));
    }

    @Override
    public Object fromString(String string) {
        String[] split = string.split("[ -]", -1);

        if (split.length > (NmsVersion.v1_13.isSupported() ? 1 : 3)) {
            throw new IllegalArgumentException();
        }

        Material material = ReflectionManager.getMaterial(split[0].toLowerCase());

        if (material == null) {
            material = Material.getMaterial(split[0].toUpperCase());
        }

        if (material == null) {
            throw new IllegalArgumentException();
        }

        ItemStack itemStack;

        if (!NmsVersion.v1_13.isSupported() && split.length > 1 && split[split.length - 1].matches("[0-9]+")) {
            itemStack = new ItemStack(material, 1, Short.parseShort(split[split.length - 1]));
        } else {
            itemStack = new ItemStack(material, 1);
        }

        if (!itemStack.getType().isBlock()) {
            throw new IllegalArgumentException();
        }

        return itemStack;
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return false;
    }
}
