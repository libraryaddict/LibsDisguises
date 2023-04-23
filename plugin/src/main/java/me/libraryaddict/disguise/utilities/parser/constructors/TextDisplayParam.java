package me.libraryaddict.disguise.utilities.parser.constructors;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.TextDisplayWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class TextDisplayParam extends ExtraDisguiseParam<String> {
    private final WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(TextDisplayWatcher.class, true);

    @Override
    public boolean isApplicable(DisguiseType disguiseType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isApplicable(DisguiseType disguiseType, String arg) {
        if (disguiseType != DisguiseType.TEXT_DISPLAY) {
            return false;
        }

        return Arrays.stream(methods).noneMatch(m -> m.getParam() != null && m.getName().equalsIgnoreCase(arg));
    }

    @Override
    public ParamInfo<String> getParamInfo() {
        return ParamInfoManager.getParamInfo(String.class);
    }

    @Override
    public String getParameterMethod() {
        return "setText";
    }

    @Override
    public String createParametervalue(CommandSender sender, String arg) {
        return DisguiseUtilities.translateAlternateColorCodes(arg);
    }

    @Override
    public String getParameterAsString(String param) {
        return param;
    }
}
