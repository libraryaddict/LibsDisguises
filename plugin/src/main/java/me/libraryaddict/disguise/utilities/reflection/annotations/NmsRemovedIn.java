package me.libraryaddict.disguise.utilities.reflection.annotations;

import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NmsRemovedIn {
    NmsVersion value();
}
