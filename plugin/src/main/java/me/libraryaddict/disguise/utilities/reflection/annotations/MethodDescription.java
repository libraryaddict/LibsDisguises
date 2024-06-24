package me.libraryaddict.disguise.utilities.reflection.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Deprecated as this isn't supported yet, more of a planned feature
 */
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface MethodDescription {
    String value() default "";

    boolean noVisibleDifference() default false;
}
