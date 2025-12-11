package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariant;
import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariants;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.entity.ZombieNautilus;

public class ZombieNautilusWatcher extends NautilusWatcher {
    public ZombieNautilusWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            setVariant(ReflectionManager.randomRegistry(ZombieNautilusVariants.getRegistry()));
        }
    }

    public void setVariant(ZombieNautilusVariant variant) {
        sendData(MetaIndex.ZOMBIE_NAUTILUS_VARIANT, variant);
    }

    @RandomDefaultValue
    public void setVariant(ZombieNautilus.Variant variant) {
        setVariant(ZombieNautilusVariants.getRegistry().getByName(variant.getKey().getKey()));
    }

    public ZombieNautilus.Variant getVariant() {
        return ZombieNautilus.Variant.TEMPERATE;
    }
}
