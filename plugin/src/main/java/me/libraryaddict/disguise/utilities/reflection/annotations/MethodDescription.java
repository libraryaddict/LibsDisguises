package me.libraryaddict.disguise.utilities.reflection.annotations;

import com.google.errorprone.annotations.DoNotCall;
import io.netty.util.internal.UnstableApi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Deprecated as this isn't supported yet, more of a planned feature
 */
@Retention(RetentionPolicy.RUNTIME)
@UnstableApi
public @interface MethodDescription {
    String value() default "";

    boolean noVisibleDifference() default false;
}
