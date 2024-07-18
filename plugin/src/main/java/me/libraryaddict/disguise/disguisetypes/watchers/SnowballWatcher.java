package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SnowballWatcher extends ThrowableWatcher {
    public SnowballWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Material.SNOWBALL);
    }
}
