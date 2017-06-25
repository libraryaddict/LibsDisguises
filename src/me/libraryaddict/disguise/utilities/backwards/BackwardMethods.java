package me.libraryaddict.disguise.utilities.backwards;

import me.libraryaddict.disguise.utilities.DisguiseSound;

import java.util.HashMap;

/**
 * Created by libraryaddict on 8/06/2017.
 */
public class BackwardMethods {

    public boolean isOrderedIndexes() {
        return true;
    }

    public void doReplaceSounds() {
    }

    public void replace(String old, String newString) {
        DisguiseSound.replace(old, newString);
    }
}
