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
        Material material = Material.BROWN_HARNESS;

        if (isHarnesed && DisguiseConfig.isRandomDisguises()) {
            material = ReflectionManager.randomEnum(AnimalColor.class).getHarnessColor();
        }

        setItemStack(EquipmentSlot.BODY, new ItemStack(isHarnesed ? material : Material.AIR));
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
        Material material = Material.AIR;

        if (color != null) {
            material = AnimalColor.getColor(color).getHarnessColor();

            if (material == null) {
                material = Material.BROWN_HARNESS;
            }
        }

        setItemStack(EquipmentSlot.BODY, new ItemStack(material));
    }
}
