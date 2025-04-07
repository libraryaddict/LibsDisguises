package me.libraryaddict.disguise.utilities.reflection.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NmsEntityNames {
    NmsEntityName[] value();
}
