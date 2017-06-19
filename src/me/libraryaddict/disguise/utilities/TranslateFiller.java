package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.inventory.ItemStack;

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

            if (info.getParamClass() == ItemStack.class || info.getParamClass() == ItemStack[].class)
                continue;

            for (String e : info.getEnums("")) {
                TranslateType.METHOD_PARAM.get(e, "Used as a disguise option for " + info.getName());
            }
        }

        for (DisguiseType type : DisguiseType.values()) {
            type.toReadable();

            for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(type.getWatcherClass())) {
                Class para = method.getParameterTypes()[0];
                String className = method.getDeclaringClass().getSimpleName().replace("Watcher", "");

                if (className.equals("Flag") || className.equals("Disguise"))
                    className = "Entity";
                else if (className.equals("Living"))
                    className = "Living Entity";
                else if (className.equals("AbstractHorse"))
                    className = "Horse";
                else if (className.equals("DroppedItem"))
                    className = "Item";
                else if (className.equals("IllagerWizard"))
                    className = "Illager";

                TranslateType.METHOD.get(method.getName(),
                        "Found in the disguise options for " + className + " and uses " + (para.isArray() ?
                                "multiple" + " " : "a ") + para.getSimpleName().replace("[]", "s"));
            }
        }
    }
}
