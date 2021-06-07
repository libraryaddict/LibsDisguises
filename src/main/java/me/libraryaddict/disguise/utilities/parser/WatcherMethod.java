package me.libraryaddict.disguise.utilities.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

import java.lang.invoke.MethodHandle;

/**
 * Created by libraryaddict on 21/05/2021.
 */
@RequiredArgsConstructor
@Getter
public class WatcherMethod {
    private final Class<? extends FlagWatcher> watcherClass;
    private final MethodHandle method;
    private final String name;
    private final Class returnType;
    private final Class param;
    private final boolean randomDefault;

    @Override
    public String toString() {
        return "WatcherMethod{" + "watcherClass=" + watcherClass + ", method=" + method + ", name='" + name + '\'' + ", returnType=" + returnType + ", param=" +
                param + ", randomDefault=" + randomDefault + '}';
    }
}
