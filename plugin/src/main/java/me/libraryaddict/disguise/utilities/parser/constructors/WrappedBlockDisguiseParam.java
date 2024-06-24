package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

/**
 * Returns true only if it is a valid block
 */
public class WrappedBlockDisguiseParam extends BlockStateDisguiseParam {
    public WrappedBlockDisguiseParam(DisguiseType... disguiseType) {
        super(disguiseType);
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        if (!super.isApplicable(disguiseType, arg)) {
            return false;
        }

        try {
            super.getParamInfo().fromString(arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getParameterMethod() {
        return "setBlock";
    }
}
