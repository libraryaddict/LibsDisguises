package me.libraryaddict.disguise.utilities.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public class WatcherMethod {
    @Setter
    private WatcherGetterSetter owner;
    private final Class<? extends FlagWatcher> watcherClass;
    private final MethodHandle method;
    private final String mappedName;
    private final String name;
    private final Class returnType;
    private final Class param;
    private final boolean randomDefault;
    private final boolean hideFromTab;
    private final boolean[] unusableBy;
    private final boolean[] hiddenFor;
    private final String description;
    private final boolean noVisibleDifference;

    public boolean isUsable(DisguiseType type) {
        return !unusableBy[type.ordinal()];
    }

    public boolean isHidden(DisguiseType type) {
        return !isUsable(type) || hiddenFor[type.ordinal()];
    }

    @Override
    public String toString() {
        return "WatcherMethod{" + "watcherClass=" + watcherClass + ", method=" + method + ", mappedName='" + mappedName + '\'' +
            ", name='" + name + '\'' + ", returnType=" + returnType + ", param=" + param + ", randomDefault=" + randomDefault +
            ", hideFromTab=" + hideFromTab + ", unusableBy=" + Arrays.toString(unusableBy) + ", hiddenFor=" + Arrays.toString(hiddenFor) +
            '}';
    }
}
