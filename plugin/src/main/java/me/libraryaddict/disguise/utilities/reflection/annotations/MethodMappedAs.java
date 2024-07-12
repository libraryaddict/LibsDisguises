package me.libraryaddict.disguise.utilities.reflection.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to provide multiple methods under the same name for string parsing.
 * Eg, "setType(Boat.Type.OAK)" and "setType(TreeSpecies.GENERIC")
 * Two possible uses, though only one is supported at the moment.
 * The supported one is simply that the method that's working in the current NmsVersion, will handle the parsing.
 * If more than one method claims to support it, we throw an error.
 * <p>
 * The other possible use is to provide fallback parsing where we call "setType GENERIC" and the ParamInfoParser will detect we're
 * talking about an old TreeSpecies and convert it to Boat.Type.OAK
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodMappedAs {
    /**
     * The full method name
     */
    String value();

    /**
     * If this NmsVersion is supported, this mapping applies
     */
    //NmsVersion mappingAdded() default NmsVersion.UNSUPPORTED;

    /**
     * If this NmsVersion is supported, this mapping is removed
     */
   // NmsVersion mappingRemoved() default NmsVersion.UNSUPPORTED;
}
