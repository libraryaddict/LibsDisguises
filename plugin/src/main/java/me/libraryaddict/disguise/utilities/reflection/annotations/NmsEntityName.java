package me.libraryaddict.disguise.utilities.reflection.annotations;

import me.libraryaddict.disguise.utilities.reflection.NmsVersion;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = NmsEntityNames.class)
public @interface NmsEntityName {
    /**
     * Version this applies to
     */
    NmsVersion version() default NmsVersion.v1_12;

    /**
     * The new {name} for <code>minecraft:{name}</code>
     * @return
     */
    String value();
}
