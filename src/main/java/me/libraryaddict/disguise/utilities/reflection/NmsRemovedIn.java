package me.libraryaddict.disguise.utilities.reflection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by libraryaddict on 6/02/2020.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NmsRemovedIn {
    NmsVersion val();
}
