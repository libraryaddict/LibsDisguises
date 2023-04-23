package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import org.bukkit.block.data.BlockData;

import java.util.Locale;

public class BlockDisplayDisguiseParam extends ExtraDisguiseParam<BlockData> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        return disguiseType == DisguiseType.BLOCK_DISPLAY;
    }

    @Override
    public ParamInfo<BlockData> getParamInfo() {
        return ParamInfoManager.getParamInfo(BlockData.class);
    }

    @Override
    public String getParameterMethod() {
        return "setBlock";
    }

    @Override
    public String getParameterAsString(BlockData blockData) {
        return blockData.getAsString().toLowerCase(Locale.ENGLISH);
    }
}
