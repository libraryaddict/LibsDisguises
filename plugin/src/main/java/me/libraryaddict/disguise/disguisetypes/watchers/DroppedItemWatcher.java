package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import org.bukkit.inventory.ItemStack;

public class DroppedItemWatcher extends FlagWatcher {
    public DroppedItemWatcher(Disguise disguise) {
        super(disguise);
    }

    public ItemStack getItemStack() {
        return getData(MetaIndex.DROPPED_ITEM);
    }

    @MethodDescription("What Item was dropped?")
    public void setItemStack(ItemStack item) {
        sendData(MetaIndex.DROPPED_ITEM, item);

        DisguiseParser.updateDisguiseName(getDisguise());
    }
}
