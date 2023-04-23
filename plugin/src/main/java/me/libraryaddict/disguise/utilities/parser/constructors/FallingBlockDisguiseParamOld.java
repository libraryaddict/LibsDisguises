package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class FallingBlockDisguiseParamOld extends ExtraDisguiseParam<ItemStack> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        return disguiseType == DisguiseType.FALLING_BLOCK && (!arg.contains("[") || !NmsVersion.v1_13.isSupported());
    }

    @Override
    public ParamInfo<ItemStack> getParamInfo() {
        return ParamInfoManager.getParamInfoItemBlock();
    }

    @Override
    public String getParameterMethod() {
        return "setBlock";
    }

    @Override
    public String getParameterAsString(ItemStack itemstack) {
        return itemstack.getType().name().toLowerCase(Locale.ENGLISH);
    }
}
