package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.bukkit.entity.Rabbit;

public class RabbitWatcher extends AgeableWatcher {

    public RabbitWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setType(Rabbit.Type.values()[DisguiseUtilities.random.nextInt(Rabbit.Type.values().length)]);
        }
    }

    public Rabbit.Type getType() {
        return getData(MetaIndex.RABBIT_TYPE);
    }

    @RandomDefaultValue
    public void setType(Rabbit.Type type) {
        setData(MetaIndex.RABBIT_TYPE, type);
        sendData(MetaIndex.RABBIT_TYPE);
    }
}
