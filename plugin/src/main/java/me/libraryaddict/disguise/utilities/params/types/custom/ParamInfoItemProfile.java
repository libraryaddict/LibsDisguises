package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfo;

public class ParamInfoItemProfile extends ParamInfo<ItemProfile> {
    public ParamInfoItemProfile(Class paramClass, String name, String description) {
        super(paramClass, name, description);

        setOtherValues("%user-skin%", "%target-skin%");
    }

    @Override
    public ItemProfile fromString(String string) {
        return DisguiseUtilities.getGson().fromJson(string, ItemProfile.class);
    }

    @Override
    public String toString(ItemProfile object) {
        return DisguiseUtilities.getGson().toJson(object, ItemProfile.class);
    }
}
