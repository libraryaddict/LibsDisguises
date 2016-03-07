package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FallingBlockWatcher extends FlagWatcher {

    private ItemStack block;

    //TODO: Check this watcher and make sure it works
    public FallingBlockWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public FallingBlockWatcher clone(Disguise disguise) {
        FallingBlockWatcher watcher = (FallingBlockWatcher) super.clone(disguise);
        watcher.setBlock(getBlock());
        return watcher;
    }

    public ItemStack getBlock() {
        return block;
    }

    public void setBlock(ItemStack block) {
        this.block = block;
        if (block.getType() == null || block.getType() == Material.AIR) {
            block.setType(Material.STONE);
        }
        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }
}
