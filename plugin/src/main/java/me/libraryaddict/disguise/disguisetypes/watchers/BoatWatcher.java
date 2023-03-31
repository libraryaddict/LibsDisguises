package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.TreeSpecies;

public class BoatWatcher extends FlagWatcher {
    public BoatWatcher(Disguise disguise) {
        super(disguise);

        setBoatType(TreeSpecies.GENERIC);
    }

    public float getDamage() {
        return getData(MetaIndex.BOAT_DAMAGE);
    }

    public void setDamage(float dmg) {
        setData(MetaIndex.BOAT_DAMAGE, dmg);
        sendData(MetaIndex.BOAT_DAMAGE);
    }

    public boolean isRightPaddling() {
        return getData(MetaIndex.BOAT_RIGHT_PADDLING);
    }

    public void setRightPaddling(boolean rightPaddling) {
        setData(MetaIndex.BOAT_RIGHT_PADDLING, rightPaddling);
        sendData(MetaIndex.BOAT_RIGHT_PADDLING);
    }

    public boolean isLeftPaddling() {
        return getData(MetaIndex.BOAT_LEFT_PADDLING);
    }

    public void setLeftPaddling(boolean leftPaddling) {
        setData(MetaIndex.BOAT_LEFT_PADDLING, leftPaddling);
        sendData(MetaIndex.BOAT_LEFT_PADDLING);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public int getBoatShake() {
        return getData(MetaIndex.BOAT_SHAKE);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public void setBoatShake(int number) {
        setData(MetaIndex.BOAT_SHAKE, number);
        sendData(MetaIndex.BOAT_SHAKE);
    }

    public TreeSpecies getBoatType() {
        return TreeSpecies.getByData(getData(MetaIndex.BOAT_TYPE).byteValue());
    }

    public void setBoatType(TreeSpecies boatType) {
        setData(MetaIndex.BOAT_TYPE, (int) boatType.getData());
        sendData(MetaIndex.BOAT_TYPE);
    }
}
