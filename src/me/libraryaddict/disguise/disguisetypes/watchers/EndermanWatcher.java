package me.libraryaddict.disguise.disguisetypes.watchers;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class EndermanWatcher extends LivingWatcher
{

    public EndermanWatcher(Disguise disguise)
    {
        super(disguise);
    }

    @Override
    public ItemStack getItemInMainHand()
    {
        Optional<Integer> value = getValue(FlagType.ENDERMAN_ITEM);

        if (value.isPresent())
        {
            Pair<Integer, Integer> pair = ReflectionManager.getFromCombinedId(value.get());
            int id = pair.getLeft();
            int data = pair.getRight();

            return new ItemStack(id, 1, (short) data);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setItemInMainHand(ItemStack itemstack)
    {
        setItemInMainHand(itemstack.getTypeId(), itemstack.getDurability());
    }

    public void setItemInMainHand(int typeId)
    {
        setItemInMainHand(typeId, 0);
    }

    public void setItemInMainHand(int typeId, int data)
    {
        int combined = ReflectionManager.getCombinedId(typeId, data);

        setValue(FlagType.ENDERMAN_ITEM, Optional.of(combined));
    }

    public boolean isAggressive()
    {
        return getValue(FlagType.ENDERMAN_AGRESSIVE);
    }

    public void setAggressive(boolean isAggressive)
    {
        setValue(FlagType.ENDERMAN_AGRESSIVE, isAggressive);
        sendData(FlagType.ENDERMAN_AGRESSIVE);
    }

}
