package me.libraryaddict.disguise.utilities.params.types.custom;

import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class ParamInfoPotionEffect extends ParamInfoEnum {
    public ParamInfoPotionEffect(Class paramClass, String name, String description, Map<String, Object> possibleValues) {
        super(paramClass, name, description, possibleValues);
    }

    public boolean isParam(Class paramClass) {
        return PotionEffectType.class.isAssignableFrom(paramClass);
    }

    @Override
    public String toString(Object object) {
        return ((PotionEffectType) object).getName();
    }
}
