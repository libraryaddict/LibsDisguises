package me.libraryaddict.disguise.disguisetypes.watchers;

import com.google.common.base.Optional;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.inventory.ItemStack;

public class EndermanWatcher extends LivingWatcher {

    public EndermanWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public ItemStack getItemInMainHand() {
        Optional<Integer> value = (Optional<Integer>) getValue(11, Optional.of(1));
        if (value.isPresent()) {
            Pair<Integer, Integer> pair = ReflectionManager.getFromCombinedId(value.get());
            int id = pair.getLeft();
            int data = pair.getRight();
            return new ItemStack(id, 1, (short) data);
        } else {
            return null;
        }
    }

    @Override
    public void setItemInMainHand(ItemStack itemstack) {
        setItemInMainHand(itemstack.getTypeId(), itemstack.getDurability());
    }

    public void setItemInMainHand(int typeId) {
        setItemInMainHand(typeId, 0);
    }

    public void setItemInMainHand(int typeId, int data) {
        int combined = ReflectionManager.getCombinedId(typeId, data);
        setValue(11, Optional.of(combined));
    }

    public boolean isAggressive() {
        return (boolean) getValue(12, false);
    }

    public void setAggressive(boolean isAggressive) {
        setValue(12, isAggressive);
        sendData(12);
    }

}
