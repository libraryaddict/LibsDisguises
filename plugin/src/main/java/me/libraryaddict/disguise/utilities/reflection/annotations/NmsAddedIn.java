package me.libraryaddict.disguise.utilities.reflection.annotations;

import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by libraryaddict on 6/02/2020.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NmsAddedIn {
    NmsVersion value();
}
