package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoGameProfile extends ParamInfo {
    public ParamInfoGameProfile(Class paramClass, String name, String description) {
        super(paramClass, name, description);
    }

    @Override
    protected Object fromString(String string) {
        return DisguiseUtilities.getGson().fromJson(string, WrappedGameProfile.class);
    }

    @Override
    public String toString(Object object) {
        return DisguiseUtilities.getGson().toJson(((WrappedGameProfile) object).getHandle(), GameProfile.class);
    }
}
