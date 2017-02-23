package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.TreeSpecies;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

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

    public void setRightPaddling(boolean rightPaddling) {
        setData(MetaIndex.BOAT_RIGHT_PADDLING, rightPaddling);
        sendData(MetaIndex.BOAT_RIGHT_PADDLING);
    }

    public void setLeftPaddling(boolean leftPaddling) {
        setData(MetaIndex.BOAT_LEFT_PADDLING, leftPaddling);
        sendData(MetaIndex.BOAT_LEFT_PADDLING);
    }

    public boolean isRightPaddling() {
        return getData(MetaIndex.BOAT_RIGHT_PADDLING);
    }

    public boolean isLeftPaddling() {
        return getData(MetaIndex.BOAT_LEFT_PADDLING);
    }

    public void setBoatType(TreeSpecies boatType) {
        setData(MetaIndex.BOAT_TYPE, (int) boatType.getData());
        sendData(MetaIndex.BOAT_TYPE);
    }

    public TreeSpecies getBoatType() {
        return TreeSpecies.getByData(getData(MetaIndex.BOAT_TYPE).byteValue());
    }

}
