package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FallingBlockWatcher extends FlagWatcher {
    private ItemStack block = new ItemStack(Material.STONE);

    public FallingBlockWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public FallingBlockWatcher clone(Disguise disguise) {
        FallingBlockWatcher watcher = (FallingBlockWatcher) super.clone(disguise);
        watcher.setBlock(getBlock().clone());

        return watcher;
    }

    public ItemStack getBlock() {
        return block;
    }

    public void setBlock(ItemStack block) {
        if (block == null || block.getType() == null || block.getType() == Material.AIR) {
            block = new ItemStack(Material.STONE);
        }

        this.block = block;

        if (DisguiseAPI.isDisguiseInUse(getDisguise()) && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise());
        }
    }
}
