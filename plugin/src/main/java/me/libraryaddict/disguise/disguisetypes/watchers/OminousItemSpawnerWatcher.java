package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
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

        DisguiseParser.updateDisguiseName(getDisguise());
    }
}
