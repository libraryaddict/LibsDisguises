package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.apache.commons.lang.StringUtils;
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

            TranslateType.DISGUISE_OPTIONS_PARAMETERS.save(info.getRawName(), "Used as a disguise option");
            TranslateType.DISGUISE_OPTIONS_PARAMETERS
                    .save(info.getRawDescription(), "Description for the disguise option " + info.getRawName());

            for (String e : info.getEnums("")) {
                TranslateType.DISGUISE_OPTIONS_PARAMETERS.save(e, "Used for the disguise option " + info.getRawName());
            }
        }

        for (DisguiseType type : DisguiseType.values()) {
            String[] split = type.name().split("_");

            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
            }

            TranslateType.DISGUISES.save(StringUtils.join(split, " "), "Name for the " + type.name() + " disguise");

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

                TranslateType.DISGUISE_OPTIONS.save(method.getName(),
                        "Found in the disguise options for " + className + " and uses " + (para.isArray() ?
                                "multiple" + " " : "a ") + para.getSimpleName().replace("[]", "s"));
            }
        }

        for (LibsMsg msg : LibsMsg.values()) {
            TranslateType.MESSAGES.save(msg.getRaw());
        }

        if (!LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations()) {
            for (TranslateType type : TranslateType.values()) {
                type.wipeTranslations();
            }
        }
    }
}
