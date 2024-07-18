package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;

@NmsAddedIn(NmsVersion.v1_14)
public class TraderLlamaWatcher extends LlamaWatcher {
    public TraderLlamaWatcher(Disguise disguise) {
        super(disguise);
    }
}
