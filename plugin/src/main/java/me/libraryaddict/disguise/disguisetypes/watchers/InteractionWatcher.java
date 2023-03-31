package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class InteractionWatcher extends FlagWatcher {
    public InteractionWatcher(Disguise disguise) {
        super(disguise);
    }

    public float getInteractionWidth() {
        return getData(MetaIndex.INTERACTION_WIDTH);
    }

    public void setInteractionWidth(float width) {
        setData(MetaIndex.INTERACTION_WIDTH, width);
        sendData(MetaIndex.INTERACTION_WIDTH);
    }

    public float getInteractionHeight() {
        return getData(MetaIndex.INTERACTION_HEIGHT);
    }

    public void setInteractionHeight(float height) {
        setData(MetaIndex.INTERACTION_HEIGHT, height);
        sendData(MetaIndex.INTERACTION_HEIGHT);
    }

    public boolean isResponsive() {
        return getData(MetaIndex.INTERACTION_RESPONSIVE);
    }

    public void setResponsive(boolean responsive) {
        setData(MetaIndex.INTERACTION_RESPONSIVE, responsive);
        sendData(MetaIndex.INTERACTION_RESPONSIVE);
    }
}
