package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MannequinDisguiseParam extends ExtraDisguiseParam<String> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        return disguiseType == DisguiseType.MANNEQUIN;
    }

    @Override
    public ParamInfo<String> getParamInfo() {
        return ParamInfoManager.getParamInfo(String.class);
    }

    @Override
    public String getParameterMethod() {
        return "setSkin";
    }

    @Override
    public String getParameterAsString(String param) {
        return param;
    }

    public void checkParameterPermission(CommandSender sender, DisguisePermissions permissions,
                                         HashMap<String, HashMap<String, Boolean>> disguiseOptions, ArrayList<String> usedOptions,
                                         DisguisePerm disguisePerm, String param) throws DisguiseParseException {
        String itemName = param == null ? "null" : getParameterAsString(param);
        // If they can't use this name, throw error
        if (sender != null && !DisguisePermissions.hasPermissionOption(disguiseOptions, "setskin", param.toLowerCase(Locale.ENGLISH))) {
            if (!param.equalsIgnoreCase(sender.getName()) ||
                !DisguisePermissions.hasPermissionOption(disguiseOptions, "setskin", "themselves")) {
                throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, itemName, disguisePerm.toReadable());
            }
        }

        usedOptions.add(getParameterMethod().toLowerCase(Locale.ENGLISH));

        DisguiseParseException exception = permissions.getReasonNotAllowed(disguisePerm, usedOptions);

        if (exception != null) {
            throw exception;
        }

        if (!DisguisePermissions.hasPermissionOption(disguiseOptions, getParameterMethod(), itemName)) {
            throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_PARAM, itemName, disguisePerm.toReadable());
        }
    }
}
