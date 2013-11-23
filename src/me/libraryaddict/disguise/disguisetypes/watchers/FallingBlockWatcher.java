package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class FallingBlockWatcher extends FlagWatcher {
    private ItemStack block;

    public FallingBlockWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setBlock(ItemStack block) {
        this.block = block;
        if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise().getEntity());
        }
    }

    public ItemStack getBlock() {
        return block;
    }

    @Override
    public FallingBlockWatcher clone(Disguise disguise) {
        FallingBlockWatcher watcher = (FallingBlockWatcher) super.clone(disguise);
        watcher.setBlock(getBlock());
        return watcher;
    }
}
