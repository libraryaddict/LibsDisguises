package me.libraryaddict.disguise.utilities.parser.params.types.custom;

import me.libraryaddict.disguise.utilities.parser.params.types.ParamInfoEnum;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class ParamInfoPotionType extends ParamInfoEnum {
    public ParamInfoPotionType(Class paramClass, String name, String description, String[] possibleValues) {
        super(paramClass, name, description, possibleValues);
    }

    @Override
    public Object fromString(String string) {
        return PotionEffectType.getByName(string);
    }
}
