package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class EndermanWatcher extends InsentientWatcher {
    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    @Deprecated
    public ItemStack getItemInMainHand() {
        WrappedBlockState value = getHeldBlock();

        if (!NmsVersion.v1_13.isSupported()) {
            return ReflectionManager.getItemStackByCombinedId(value.getGlobalId());
        }

        MaterialData pair = SpigotConversionUtil.toBukkitMaterialData(value);

        return new ItemStack(pair.getItemType(), 1, pair.getData());
    }

    @Override
    @Deprecated
    public void setItemInMainHand(ItemStack itemstack) {
        setItemInMainHand(itemstack.getType());
    }

    @MethodMappedAs("setItemInMainHand")
    public void setHeldBlock(WrappedBlockState state) {
        setData(MetaIndex.ENDERMAN_ITEM, state);
        sendData(MetaIndex.ENDERMAN_ITEM);
    }

    @MethodMappedAs("getItemInMainHand")
    public WrappedBlockState getHeldBlock() {
        return getData(MetaIndex.ENDERMAN_ITEM);
    }

    @Deprecated
    public void setItemInMainHand(Material type) {
        if (!type.isBlock()) {
            return;
        }

        WrappedBlockState item = SpigotConversionUtil.fromBukkitMaterialData(new MaterialData(type));

        setHeldBlock(item);
    }

    @Deprecated
    public void setItemInMainHand(Material type, int data) {
        setItemInMainHand(type);
    }

    public boolean isAggressive() {
        return getData(MetaIndex.ENDERMAN_AGRESSIVE);
    }

    public void setAggressive(boolean isAggressive) {
        setData(MetaIndex.ENDERMAN_AGRESSIVE, isAggressive);
        sendData(MetaIndex.ENDERMAN_AGRESSIVE);
    }
}
