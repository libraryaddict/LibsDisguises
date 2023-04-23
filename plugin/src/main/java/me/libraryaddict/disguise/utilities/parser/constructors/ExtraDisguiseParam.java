package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public abstract class ExtraDisguiseParam<T> {
    public abstract boolean isApplicable(DisguiseType disguiseType);

    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        return isApplicable(disguiseType);
    }

    public abstract ParamInfo<T> getParamInfo();

    public abstract String getParameterMethod();

    public T createParametervalue(CommandSender sender, String arg) throws DisguiseParseException {
        return getParamInfo().fromString(new ArrayList<>(Collections.singletonList(arg)));
    }

    public abstract String getParameterAsString(T param);

    public void checkParameterPermission(CommandSender sender, DisguisePermissions permissions, HashMap<String, HashMap<String, Boolean>> disguiseOptions,
                                         ArrayList<String> usedOptions, DisguisePerm disguisePerm, T param) throws DisguiseParseException {
        checkParameterPermission(permissions, disguiseOptions, usedOptions, disguisePerm, param);
    }

    public void checkParameterPermission(DisguisePermissions permissions, HashMap<String, HashMap<String, Boolean>> disguiseOptions,
                                         ArrayList<String> usedOptions, DisguisePerm disguisePerm, T param) throws DisguiseParseException {
        usedOptions.add(getParameterMethod().toLowerCase());

        if (!permissions.isAllowedDisguise(disguisePerm, usedOptions)) {
            throw new DisguiseParseException(LibsMsg.D_PARSE_NOPERM, usedOptions.stream().reduce((first, second) -> second).orElse(null));
        }

        String itemName = param == null ? "null" : getParameterAsString(param);

        if (!DisguisePermissions.hasPermissionOption(disguiseOptions, getParameterMethod(), itemName)) {
            throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, itemName, disguisePerm.toReadable());
        }
    }
}
