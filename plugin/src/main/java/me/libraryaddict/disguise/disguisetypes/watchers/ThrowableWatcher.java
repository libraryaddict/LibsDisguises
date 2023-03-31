package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.inventory.ItemStack;

/**
 * Created by libraryaddict on 6/05/2019.
 */
public abstract class ThrowableWatcher extends FlagWatcher {
    public ThrowableWatcher(Disguise disguise) {
        super(disguise);

        if (NmsVersion.v1_14.isSupported()) {
            setItemStack(getDefaultItemStack());
        }
    }

    protected abstract ItemStack getDefaultItemStack();

    @NmsAddedIn(NmsVersion.v1_14)
    public ItemStack getItemStack() {
        return getData(MetaIndex.THROWABLE_ITEM);
    }

    @RandomDefaultValue
    @NmsAddedIn(NmsVersion.v1_14)
    public void setItemStack(ItemStack item) {
        setData(MetaIndex.THROWABLE_ITEM, item);
        sendData(MetaIndex.THROWABLE_ITEM);
    }
}
