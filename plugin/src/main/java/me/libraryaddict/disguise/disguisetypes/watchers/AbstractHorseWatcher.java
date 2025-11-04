package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractHorseWatcher extends AgeableWatcher {
    private static final int TAMED = 2, SADDLED = 4, REPRODUCED = 8, GRAZING = 16, REARING = 32, EATING = 64;

    public AbstractHorseWatcher(Disguise disguise) {
        super(disguise);
    }

    @NmsRemovedIn(NmsVersion.v1_19_R3)
    public UUID getOwner() {
        return getData(MetaIndex.HORSE_OWNER).orElse(null);
    }

    @NmsRemovedIn(NmsVersion.v1_19_R3)
    @MethodDescription
    public void setOwner(UUID uuid) {
        sendData(MetaIndex.HORSE_OWNER, uuid != null ? Optional.of(uuid) : Optional.empty());
    }

    @NmsRemovedIn(NmsVersion.v1_19_R3)
    @MethodDescription
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setOwner(Optional<UUID> owner) {
        sendData(MetaIndex.HORSE_OWNER, owner);
    }

    /**
     * If the horse can be breeded, no visible effect
     *
     * @return Is horse breedable
     */
    public boolean isReproduced() {
        return isHorseFlag(REPRODUCED);
    }

    @MethodDescription(noVisibleDifference = true)
    public void setReproduced(boolean reproduced) {
        setHorseFlag(REPRODUCED, reproduced);
    }

    /**
     * If the horse is grazing
     *
     * @return Is horse grazing
     */
    public boolean isGrazing() {
        return isHorseFlag(GRAZING);
    }

    @MethodDescription("Is the horse's head lowered?")
    public void setGrazing(boolean grazing) {
        setHorseFlag(GRAZING, grazing);
    }

    /**
     * If the horse has it's mouth open
     *
     * @return Horse has mouth open
     */
    public boolean isEating() {
        return isHorseFlag(EATING);
    }

    @MethodDescription("Is the horse's mouth open?")
    public void setEating(boolean mouthOpen) {
        setHorseFlag(EATING, mouthOpen);
    }

    public boolean isRearing() {
        return isHorseFlag(REARING);
    }

    @MethodDescription("Is the horse rearing in the air?")
    public void setRearing(boolean rear) {
        setHorseFlag(REARING, rear);
    }

    public boolean isSaddled() {
        if (!NmsVersion.v1_21_R4.isSupported()) {
            return isHorseFlag(SADDLED);
        }

        ItemStack item = getItemStack(EquipmentSlot.SADDLE);

        return item != null && item.getType() != Material.AIR;
    }

    @MethodDescription("Is the horse wearing a saddle?")
    public void setSaddled(boolean saddled) {
        if (!NmsVersion.v1_21_R4.isSupported()) {
            setHorseFlag(SADDLED, saddled);
            return;
        }

        setItemStack(EquipmentSlot.SADDLE, new ItemStack(saddled ? Material.SADDLE : Material.AIR));
    }

    public boolean isTamed() {
        return isHorseFlag(TAMED);
    }

    @MethodDescription("Is the horse tamed?")
    public void setTamed(boolean tamed) {
        setHorseFlag(TAMED, tamed);
    }

    private boolean isHorseFlag(int i) {
        return (getHorseFlag() & i) != 0;
    }

    private byte getHorseFlag() {
        return getData(MetaIndex.HORSE_META);
    }

    private void setHorseFlag(int i, boolean flag) {
        byte j = getData(MetaIndex.HORSE_META);

        if (flag) {
            j = (byte) (j | i);
        } else {
            j = (byte) (j & ~i);
        }

        sendData(MetaIndex.HORSE_META, j);
    }
}
