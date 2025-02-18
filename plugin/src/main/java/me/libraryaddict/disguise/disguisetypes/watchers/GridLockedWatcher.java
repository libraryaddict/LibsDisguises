package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.util.NumberConversions;

public interface GridLockedWatcher {
    /**
     * Get the width of the block / 2
     */
    double getWidthX();

    /**
     * Get the length of the block / 2
     */
    double getWidthZ();

    /**
     * If the plugin should try to center this block
     */
    boolean isGridLocked();

    /**
     * Make the plugin start centering this block
     *
     * @param gridLocked
     */
    void setGridLocked(boolean gridLocked);

    static double center(double origin, double width) {
        // Covered by junit tests because I confuse myself - DisguiseGridLockedTest
        double floored = NumberConversions.floor(origin);

        if (width < 1) {
            floored += Math.floor((origin - floored) / width) * width;
        }

        return floored + (width / 2) % 1;
    }
}
