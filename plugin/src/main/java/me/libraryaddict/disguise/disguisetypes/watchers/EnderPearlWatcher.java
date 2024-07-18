package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EnderPearlWatcher extends ThrowableWatcher {
    public EnderPearlWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Material.ENDER_PEARL);
    }
}
