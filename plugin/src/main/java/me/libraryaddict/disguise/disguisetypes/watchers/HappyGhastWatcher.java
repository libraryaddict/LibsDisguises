package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HappyGhastWatcher extends AgeableWatcher {
    public HappyGhastWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setLeashHolder(boolean leashHolder) {
        sendData(MetaIndex.HAPPY_GHAST_IS_LEASH_HOLDER, leashHolder);
    }

    public boolean isLeashHolder() {
        return getData(MetaIndex.HAPPY_GHAST_IS_LEASH_HOLDER);
    }

    public boolean isHarnessed() {
        ItemStack item = getItemStack(EquipmentSlot.BODY);

        return item != null && AnimalColor.getColorByHarness(item.getType()) != null;
    }

    public void setHarnessed(boolean isHarnesed) {
        Material material = isHarnesed ? Material.BROWN_HARNESS : Material.AIR;

        if (isHarnesed && DisguiseConfig.isRandomDisguises()) {
            material = ReflectionManager.randomEnum(AnimalColor.class).getHarnessColor();
        }

        setItemStack(EquipmentSlot.BODY, new ItemStack(material));
    }

    public DyeColor getHarnessColor() {
        ItemStack saddle = getItemStack(EquipmentSlot.BODY);

        if (saddle == null) {
            return null;
        }

        AnimalColor color = AnimalColor.getColorByHarness(saddle.getType());

        return color == null ? null : color.getDyeColor();
    }

    public void setHarnessColor(DyeColor color) {
        if (color == null) {
            setItemStack(EquipmentSlot.BODY, null);
            return;
        }

        AnimalColor animalColor = AnimalColor.getColor(color);

        Material material = Material.BROWN_HARNESS;

        if (animalColor != null && animalColor.getHarnessColor() != null) {
            material = animalColor.getHarnessColor();
        }

        setItemStack(EquipmentSlot.BODY, new ItemStack(material));
    }
}
