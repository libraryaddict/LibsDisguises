package me.libraryaddict.disguise.utilities.placeholderapi;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.WatcherGetterSetter;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderDisguiseFlag implements DPlaceholder {
    @Override
    public String getName() {
        return "disguise_flag";
    }

    @Override
    public String getStructure() {
        return "disguise_flag:[Disguise Arg]";
    }

    @Override
    public String parse(@Nullable Disguise disguise, String[] args) {
        if (args.length != 1) {
            return "???";
        }

        WatcherMethod method = getMethod(disguise, args[0]);

        if (method == null) {
            return "???";
        }

        Object ourValue;

        try {
            ourValue = DisguiseParser.parseToString(disguise, method);
        } catch (Throwable e) {
            if (LibsDisguises.getInstance().isDebuggingBuild()) {
                e.printStackTrace();
            }

            return "??Errored??";
        }

        String valueString;

        if (ourValue != null) {
            valueString = ParamInfoManager.getParamInfo(ourValue.getClass()).toString(ourValue);

            if (ourValue instanceof String) {
                valueString = TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet(valueString);
            }

            valueString = DisguiseUtilities.quote(valueString);
        } else {
            valueString = TranslateType.DISGUISE_OPTIONS_PARAMETERS.reverseGet("null");
        }

        return valueString;
    }

    private WatcherMethod getMethod(Disguise disguise, String param) {
        List<WatcherMethod> methods = ParamInfoManager.getDisguiseMethods().getMethods(disguise.getWatcher().getClass());

        for (WatcherMethod method : methods) {
            if (method.getOwner() == null) {
                continue;
            }

            WatcherGetterSetter owner = method.getOwner();
            String get = owner.getGetter().getMappedName();
            String set = owner.getSetter().getMappedName();

            if (!get.equalsIgnoreCase(param) && !set.equalsIgnoreCase(param) && !owner.getSharedName().equalsIgnoreCase(param)) {
                continue;
            }

            return method;
        }

        return null;
    }
}
