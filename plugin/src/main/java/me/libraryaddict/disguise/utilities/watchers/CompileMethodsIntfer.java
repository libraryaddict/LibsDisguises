package me.libraryaddict.disguise.utilities.watchers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CompileMethodsIntfer {
    String user() default "%%__USER__%%";
}