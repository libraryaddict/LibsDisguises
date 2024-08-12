package me.libraryaddict.disguise.utilities.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class WatcherGetterSetter {
    private final WatcherMethod setter;
    private final WatcherMethod getter;
    private final Object defaultValue;
    /**
     * The name they both share. Eg, "setBurning" and "getBurning" both share "burning"
     */
    private final String sharedName;
}
