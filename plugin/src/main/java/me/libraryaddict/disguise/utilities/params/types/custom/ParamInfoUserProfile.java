package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfo;

public class ParamInfoUserProfile extends ParamInfo<UserProfile> {
    public ParamInfoUserProfile(Class paramClass, String name, String description) {
        super(paramClass, name, description);

        setOtherValues("%user-skin%", "%target-skin%");
    }

    @Override
    public UserProfile fromString(String string) {
        return DisguiseUtilities.getGson().fromJson(string, UserProfile.class);
    }

    @Override
    public String toString(UserProfile object) {
        return DisguiseUtilities.getGson().toJson(object, UserProfile.class);
    }
}
