package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Art;

public class PaintingWatcher extends HangingWatcher {
    private Art painting;

    public PaintingWatcher(Disguise disguise) {
        super(disguise);
    }

    @Override
    public PaintingWatcher clone(Disguise disguise) {
        PaintingWatcher watcher = (PaintingWatcher) super.clone(disguise);
        watcher.setArt(getArt());

        return watcher;
    }

    public Art getArt() {
        if (!NmsVersion.v1_19_R1.isSupported()) {
            return painting;
        }

        return getData(MetaIndex.PAINTING);
    }

    public void setArt(Art newPainting) {
        if (NmsVersion.v1_19_R1.isSupported()) {
            sendData(MetaIndex.PAINTING, newPainting);
        } else {
            this.painting = newPainting;

            if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
                DisguiseUtilities.refreshTrackers(getDisguise());
            }
        }

        DisguiseParser.updateDisguiseName(getDisguise());
    }
}
