package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by libraryaddict on 23/05/2020.
 */
public class ParamInfoSoundGroup extends ParamInfoEnum {
    public ParamInfoSoundGroup() {
        super(String.class, "SoundGroup", "A group of sounds", new HashMap<>());

        recalculate();
    }

    public void recalculate() {
        LinkedHashMap<String, String> possibleSoundGroups = new LinkedHashMap<>();

        ArrayList<String> list = new ArrayList<>(SoundGroup.getGroups().keySet());

        list.sort(String.CASE_INSENSITIVE_ORDER);

        for (String s : list) {
            possibleSoundGroups.put(s, s);
        }

        getValues().clear();
        getValues().putAll(possibleSoundGroups);
    }
}
