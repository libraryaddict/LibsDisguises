package me.libraryaddict.disguise.utilities.reflection.annotations;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MethodOnlyUsedBy {
    DisguiseType[] value();

    MethodGroupType group() default MethodGroupType.NONE;
}
