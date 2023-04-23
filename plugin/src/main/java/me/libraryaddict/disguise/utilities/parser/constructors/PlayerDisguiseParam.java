package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
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

public class PlayerDisguiseParam extends ExtraDisguiseParam<String> {
    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        return disguiseType == DisguiseType.PLAYER;
    }

    @Override
    public ParamInfo<String> getParamInfo() {
        return ParamInfoManager.getParamInfo(String.class);
    }

    @Override
    public String getParameterMethod() {
        return "setName";
    }

    @Override
    public String createParametervalue(CommandSender sender, String arg) {
        arg = arg.replace("\\_", " ");

        if (DisguiseConfig.isArmorstandsName() && sender != null && !sender.hasPermission("libsdisguises.multiname")) {
            arg = DisguiseUtilities.quoteNewLine(arg);
        }

        return DisguiseUtilities.translateAlternateColorCodes(arg);
    }

    @Override
    public String getParameterAsString(String param) {
        return param;
    }

    public void checkParameterPermission(CommandSender sender, DisguisePermissions permissions, HashMap<String, HashMap<String, Boolean>> disguiseOptions,
                                         ArrayList<String> usedOptions, DisguisePerm disguisePerm, String param) throws DisguiseParseException {
        // If they can't use this name, throw error
        if (sender != null && !DisguisePermissions.hasPermissionOption(disguiseOptions, "setname", param.toLowerCase(Locale.ENGLISH))) {
            if (!param.equalsIgnoreCase(sender.getName()) || !DisguisePermissions.hasPermissionOption(disguiseOptions, "setname", "themselves")) {
                throw new DisguiseParseException(LibsMsg.PARSE_NO_PERM_NAME);
            }
        }

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
