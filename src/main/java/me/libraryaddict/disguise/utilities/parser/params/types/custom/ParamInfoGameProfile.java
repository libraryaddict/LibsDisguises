package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

import java.lang.reflect.Method;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoGameProfile extends ParamInfo {
    public ParamInfoGameProfile(Class paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    protected Object fromString( String string) {
        return DisguiseUtilities.getGson().fromJson(string, WrappedGameProfile.class);
    }
}
