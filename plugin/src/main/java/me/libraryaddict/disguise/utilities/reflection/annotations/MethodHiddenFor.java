package me.libraryaddict.disguise.utilities.reflection.annotations;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MethodHiddenFor {
    /**
     * When empty, it means all DisguiseTypes
     */
    DisguiseType[] value();
}
