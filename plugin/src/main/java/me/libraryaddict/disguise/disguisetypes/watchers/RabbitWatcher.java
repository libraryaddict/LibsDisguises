package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.entity.Rabbit;

public class RabbitWatcher extends AgeableWatcher {

    public RabbitWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setType(ReflectionManager.randomEnum(Rabbit.Type.class));
        }
    }

    public Rabbit.Type getType() {
        return getData(MetaIndex.RABBIT_TYPE);
    }

    @RandomDefaultValue
    public void setType(Rabbit.Type type) {
        sendData(MetaIndex.RABBIT_TYPE, type);
    }
}
