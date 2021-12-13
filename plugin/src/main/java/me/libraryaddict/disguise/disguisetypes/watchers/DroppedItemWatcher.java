package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DroppedItemWatcher extends FlagWatcher {
    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.DROPPED_ITEM);
    }

    public void setItemStack(ItemStack item) {
        setData(MetaIndex.DROPPED_ITEM, item);
        sendData(MetaIndex.DROPPED_ITEM);

        if (!getDisguise().isCustomDisguiseName()) {
            getDisguise().setDisguiseName(TranslateType.DISGUISES.get(DisguiseType.DROPPED_ITEM.toReadable()) + " " +
                    TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(ReflectionManager
                            .toReadable((item == null ? Material.AIR : item.getType()).name(), " ")));
        }
    }
}
