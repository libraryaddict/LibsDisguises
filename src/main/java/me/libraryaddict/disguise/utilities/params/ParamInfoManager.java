package me.libraryaddict.disguise.utilities.params;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoItemBlock;
import me.libraryaddict.disguise.utilities.watchers.DisguiseMethods;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ParamInfoManager {
    private static List<ParamInfo> paramList;
    private static DisguiseMethods disguiseMethods;
    @Getter
    private static ParamInfoItemBlock paramInfoItemBlock;

    public static List<ParamInfo> getParamInfos() {
        return paramList;
    }

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        }

        ParamInfo info = getParamInfo(object.getClass());

        if (info == null) {
            throw new IllegalArgumentException(object.getClass() + " is not handled by ParamInfo!");
        }

        return info.toString(object);
    }

    public static ParamInfo getParamInfo(Method method) {
        if (method.getDeclaringClass() == FallingBlockWatcher.class &&
                method.getParameterTypes()[0] == ItemStack.class) {
            return getParamInfoItemBlock();
        }

        return getParamInfo(method.getParameterTypes()[0]);
    }

    public static ParamInfo getParamInfo(Class c) {
        for (ParamInfo info : getParamInfos()) {
            if (!info.isParam(c)) {
                continue;
            }

            return info;
        }

        return null;
    }

    public static ParamInfo getParamInfo(DisguisePerm disguiseType, String methodName) {
        return getParamInfo(disguiseType.getType(), methodName);
    }

    public static ParamInfo getParamInfo(DisguiseType disguiseType, String methodName) {
        for (Method method : getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
            if (!method.getName().toLowerCase().equals(methodName.toLowerCase()))
                continue;

            return getParamInfo(method);
        }

        return null;
    }

    static {
        ParamInfoTypes infoTypes = new ParamInfoTypes();
        paramList = infoTypes.getParamInfos();
        paramInfoItemBlock = infoTypes.getParamInfoBlock();
        disguiseMethods = new DisguiseMethods();

        //paramList.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
    }

    public static Method[] getDisguiseWatcherMethods(@Nullable Class<? extends FlagWatcher> watcherClass) {
        if (watcherClass == null) {
            return new Method[0];
        }

        ArrayList<Method> methods = new ArrayList<>(disguiseMethods.getMethods(watcherClass));

        // Order first by their declaring class, the top class (SheepWatcher) goes before (FlagWatcher)
        // Order methods in the same watcher by their name from A to Z
        methods.sort((m1, m2) -> {
            int v1 = getValue(m1);
            int v2 = getValue(m2);

            if (v1 != v2) {
                return v1 - v2;
            }

            return String.CASE_INSENSITIVE_ORDER.compare(m1.getName(), m2.getName());
        });

        // Add these last as it's what we want to present to be called the least
        for (String methodName : new String[]{"setSelfDisguiseVisible", "setHideHeldItemFromSelf",
                "setHideArmorFromSelf", "setHearSelfDisguise", "setHidePlayer", "setExpires"}) {
            try {
                methods.add(Disguise.class
                        .getMethod(methodName, methodName.equals("setExpires") ? long.class : boolean.class));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (watcherClass == PlayerWatcher.class) {
            try {
                methods.add(PlayerDisguise.class.getMethod("setNameVisible", boolean.class));
                methods.add(PlayerDisguise.class.getMethod("setDynamicName", boolean.class));
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return methods.toArray(new Method[0]);
    }

    /**
     * Value of the method, used namely for ordering the more unique methods to a disguise
     */
    public static int getValue(Method method) {
        ChatColor methodColor = ChatColor.YELLOW;

        Class<?> declaring = method.getDeclaringClass();

        if (declaring == LivingWatcher.class) {
            return 1;
        } else if (!(FlagWatcher.class.isAssignableFrom(declaring)) || declaring == FlagWatcher.class) {
            return 2;
        }

        return 0;
    }
}
