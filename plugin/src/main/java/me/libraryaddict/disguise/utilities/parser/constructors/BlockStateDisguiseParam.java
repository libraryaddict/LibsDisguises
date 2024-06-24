package me.libraryaddict.disguise.utilities.parser.constructors;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoWrappedBlockData;

public class BlockStateDisguiseParam extends ExtraDisguiseParam<WrappedBlockState> {
    private final boolean[] forDisguise = new boolean[DisguiseType.values().length];

    public BlockStateDisguiseParam(DisguiseType... disguiseTypes) {
        for (DisguiseType type : disguiseTypes) {
            forDisguise[type.ordinal()] = true;
        }
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        return forDisguise[disguiseType.ordinal()];
    }

    @Override
    public ParamInfoWrappedBlockData getParamInfo() {
        return (ParamInfoWrappedBlockData) ParamInfoManager.getParamInfo(WrappedBlockState.class);
    }

    @Override
    public String getParameterMethod() {
        return "setBlock";
    }

    @Override
    public String getParameterAsString(WrappedBlockState blockData) {
        return getParamInfo().toString(blockData);
    }
}
