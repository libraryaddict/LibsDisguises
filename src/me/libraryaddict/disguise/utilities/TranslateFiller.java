package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

import java.lang.reflect.Method;

/**
 * Created by libraryaddict on 10/06/2017.
 */
public class TranslateFiller {
    public static void fillConfigs() {
        // Fill the configs

        for (ReflectionFlagWatchers.ParamInfo info : ReflectionFlagWatchers.getParamInfos()) {
            if (!info.isEnums())
                continue;

            for (String e : info.getEnums("")) {
                TranslateType.METHOD_PARAM.get(e, "Name for the param for " + info.getName());
            }
        }

        for (DisguiseType type : DisguiseType.values()) {
            type.toReadable();

            for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(type.getWatcherClass())) {
                TranslateType.METHOD.get(method.getName(),
                        "Found in " + method.getDeclaringClass().getSimpleName().replace("Watcher",
                                "") + " and accepts as a parameter " + TranslateType.METHOD_PARAM.get(
                                method.getParameterTypes()[0].getSimpleName()));
            }
        }
    }
}
