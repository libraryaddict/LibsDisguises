package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.inventory.ItemStack;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public abstract class ThrowableWatcher extends FlagWatcher {
    public ThrowableWatcher(Disguise disguise) {
        super(disguise);

        setItemStack(getDefaultItemStack());
    }

    protected abstract ItemStack getDefaultItemStack();

    public ItemStack getItemStack() {
        return getData(MetaIndex.THROWABLE_ITEM);
    }

    @RandomDefaultValue
    public void setItemStack(ItemStack item) {
        setData(MetaIndex.THROWABLE_ITEM, item);
        sendData(MetaIndex.THROWABLE_ITEM);
    }
}
