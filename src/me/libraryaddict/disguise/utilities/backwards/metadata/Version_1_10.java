package me.libraryaddict.disguise.utilities.backwards.metadata;

import com.google.common.base.Optional;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by libraryaddict on 9/06/2017.
 * <p>
 * Supports 1.10.0 1.10.1 and 1.10.2
 */
public class Version_1_10 extends Version_1_11 {
    private MetaIndex ILLAGER_META;
    private MetaIndex ILLAGER_SPELL_TICKS;
    private MetaIndex<Integer> HORSE_VARIANT = new MetaIndex<>(HorseWatcher.class, 1, 0);
    private MetaIndex<Byte> SHULKER_COLOR;
    private MetaIndex<Optional<ItemStack>> DROPPED_ITEM = new MetaIndex<>(DroppedItemWatcher.class, 0,
            Optional.of(new ItemStack(Material.STONE)));
}
