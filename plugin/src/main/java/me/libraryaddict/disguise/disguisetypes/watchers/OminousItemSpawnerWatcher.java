package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Navid
 */
public class OminousItemSpawnerWatcher extends FlagWatcher {
    public OminousItemSpawnerWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.OMINOUS_ITEM_SPAWNER_ITEM);
    }

    @MethodDescription("What item is displayed?")
    public void setItemStack(ItemStack item) {
        sendData(MetaIndex.OMINOUS_ITEM_SPAWNER_ITEM, item);

        if (!getDisguise().isCustomDisguiseName()) {
            getDisguise().setDisguiseName(TranslateType.DISGUISES.get(DisguiseType.OMINOUS_ITEM_SPAWNER.toReadable()) + " " +
                TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(
                    ReflectionManager.toReadable((item == null ? Material.AIR : item.getType()).name(), " ")));
        }
    }
}
