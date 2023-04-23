package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class ItemDisguiseParam extends ExtraDisguiseParam<ItemStack> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        return disguiseType == DisguiseType.DROPPED_ITEM || disguiseType == DisguiseType.ITEM_DISPLAY;
    }

    @Override
    public ParamInfo<ItemStack> getParamInfo() {
        return ParamInfoManager.getParamInfo(ItemStack.class);
    }

    @Override
    public String getParameterMethod() {
        return "setItemStack";
    }

    @Override
    public String getParameterAsString(ItemStack itemstack) {
        return itemstack.getType().name().toLowerCase(Locale.ENGLISH);
    }
}
