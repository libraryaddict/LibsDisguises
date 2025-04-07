package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

public class ExperienceOrbWatcher extends FlagWatcher {
    public ExperienceOrbWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public void setExperience(int experience) {
        sendData(MetaIndex.EXPERIENCE_ORB_VALUE, experience);
    }

    @NmsAddedIn(NmsVersion.v1_21_R4)
    public int getExperience() {
        return getData(MetaIndex.EXPERIENCE_ORB_VALUE);
    }
}
