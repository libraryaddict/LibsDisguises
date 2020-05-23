package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;

import java.util.Map;

/**
 * Created by libraryaddict on 23/05/2020.
 */
public class ParamInfoSoundGroup extends ParamInfoEnum {
    public ParamInfoSoundGroup(Map<String, Object> possibleValues) {
        super(String.class, "SoundGroup", "A group of sounds", possibleValues);
    }
}
