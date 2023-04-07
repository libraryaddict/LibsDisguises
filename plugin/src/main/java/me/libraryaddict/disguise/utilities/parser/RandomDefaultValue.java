package me.libraryaddict.disguise.utilities.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by libraryaddict on 31/12/2019.
 * <p>
 * This annotation should only be used on setter methods
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RandomDefaultValue {

}
