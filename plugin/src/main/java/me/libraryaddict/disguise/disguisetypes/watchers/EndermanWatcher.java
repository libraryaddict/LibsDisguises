package me.libraryaddict.disguise.disguisetypes.watchers;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class EndermanWatcher extends InsentientWatcher {

    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public ItemStack getItemInMainHand() {
        Optional<WrappedBlockData> value = getData(MetaIndex.ENDERMAN_ITEM);

        if (value.isPresent()) {
            WrappedBlockData pair = value.get();
            return new ItemStack(pair.getType(), 1);
        } else {
            return null;
        }
    }

    @Override
    public void setItemInMainHand(ItemStack itemstack) {
        setItemInMainHand(itemstack.getType());
    }

    @Deprecated
    public void setItemInMainHand(Material type) {
        if (!type.isBlock()) {
            return;
        }

        Optional<WrappedBlockData> optional;

        if (type == null) {
            optional = Optional.empty();
        } else {
            optional = Optional.of(WrappedBlockData.createData(type));
        }

        setData(MetaIndex.ENDERMAN_ITEM, optional);
        sendData(MetaIndex.ENDERMAN_ITEM);
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
