package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoBoatType;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodMappedAs;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;

public class BoatWatcher extends FlagWatcher {
    public BoatWatcher(Disguise disguise) {
        super(disguise);

        // As of 1.21.3, boat types are now different entity types
        if (NmsVersion.v1_21_R2.isSupported()) {
            return;
        }

        if (NmsVersion.v1_19_R1.isSupported()) {
            setType(Boat.Type.OAK);
        } else {
            setBoatType(TreeSpecies.GENERIC);
        }
    }

    public float getDamage() {
        return getData(MetaIndex.BOAT_DAMAGE);
    }

    @MethodDescription("No visible difference")
    public void setDamage(float dmg) {
        sendData(MetaIndex.BOAT_DAMAGE, dmg);
    }

    public boolean isRightPaddling() {
        return getData(MetaIndex.BOAT_RIGHT_PADDLING);
    }

    @MethodDescription("Is the boat's right paddle moving?")
    public void setRightPaddling(boolean rightPaddling) {
        sendData(MetaIndex.BOAT_RIGHT_PADDLING, rightPaddling);
    }

    public boolean isLeftPaddling() {
        return getData(MetaIndex.BOAT_LEFT_PADDLING);
    }

    @MethodDescription("Is the boat's left paddle moving?")
    public void setLeftPaddling(boolean leftPaddling) {
        sendData(MetaIndex.BOAT_LEFT_PADDLING, leftPaddling);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public int getBoatShake() {
        return getData(MetaIndex.BOAT_SHAKE);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    @MethodDescription("How violently does this boat shake when damaged?")
    public void setBoatShake(int number) {
        sendData(MetaIndex.BOAT_SHAKE, number);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    @NmsRemovedIn(NmsVersion.v1_21_R2)
    @MethodMappedAs("getBoatType")
    public Boat.Type getType() {
        return getData(MetaIndex.BOAT_TYPE_NEW);
    }

    @NmsAddedIn(NmsVersion.v1_19_R1)
    @NmsRemovedIn(NmsVersion.v1_21_R2)
    @MethodMappedAs("setBoatType")
    @MethodDescription("What type of wood is this boat made of?")
    public void setType(Boat.Type type) {
        sendData(MetaIndex.BOAT_TYPE_NEW, type);
    }

    @NmsRemovedIn(NmsVersion.v1_19_R1)
    public TreeSpecies getBoatType() {
        if (NmsVersion.v1_19_R1.isSupported()) {
            return ParamInfoBoatType.getSpeciesFromType(getType());
        }

        return getData(MetaIndex.BOAT_TYPE_OLD);
    }

    @NmsRemovedIn(NmsVersion.v1_19_R1)
    @MethodDescription("What type of wood is this boat made of?")
    public void setBoatType(TreeSpecies boatType) {
        if (NmsVersion.v1_19_R1.isSupported()) {
            setType(ParamInfoBoatType.getTypeFromSpecies(boatType));
            return;
        }

        sendData(MetaIndex.BOAT_TYPE_OLD, boatType);
    }
}
