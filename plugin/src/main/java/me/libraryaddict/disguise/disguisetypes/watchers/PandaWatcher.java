package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodOnlyUsedBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import org.bukkit.entity.Panda;

import java.util.Random;

/**
 * Created by libraryaddict on 6/05/2019.
 */
@NmsAddedIn(NmsVersion.v1_14)
public class PandaWatcher extends AgeableWatcher {
    public PandaWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            // We don't do 'setGene' here so it's just as random as it would be as if it was natural.
            setMainGene(Panda.Gene.values()[new Random().nextInt(Panda.Gene.values().length)]);
            setHiddenGene(Panda.Gene.values()[new Random().nextInt(Panda.Gene.values().length)]);
        }
    }

    public Panda.Gene getGene() {
        return getMainGene();
    }

    @RandomDefaultValue
    public void setGene(Panda.Gene gene) {
        setMainGene(gene);
        setHiddenGene(gene);
    }

    public Panda.Gene getMainGene() {
        int id = getData(MetaIndex.PANDA_MAIN_GENE);

        for (Panda.Gene gene : Panda.Gene.values()) {
            if (gene.ordinal() != id) {
                continue;
            }

            return gene;
        }

        return Panda.Gene.NORMAL;
    }

    @MethodOnlyUsedBy(value = {}) // Hide from command
    public void setMainGene(Panda.Gene gene) {
        setData(MetaIndex.PANDA_MAIN_GENE, (byte) gene.ordinal());
        sendData(MetaIndex.PANDA_MAIN_GENE);
    }

    public Panda.Gene getHiddenGene() {
        int id = getData(MetaIndex.PANDA_HIDDEN_GENE);

        for (Panda.Gene gene : Panda.Gene.values()) {
            if (gene.ordinal() != id) {
                continue;
            }

            return gene;
        }

        return Panda.Gene.NORMAL;
    }

    @MethodOnlyUsedBy(value = {}) // Hide from command
    public void setHiddenGene(Panda.Gene gene) {
        setData(MetaIndex.PANDA_HIDDEN_GENE, (byte) gene.ordinal());
        sendData(MetaIndex.PANDA_HIDDEN_GENE);
    }

    public boolean isSneeze() {
        return getPandaFlag(2);
    }

    public void setSneeze(boolean value) {
        setPandaFlag(2, value);
    }

    public boolean isTumble() {
        return getPandaFlag(4);
    }

    public void setTumble(boolean value) {
        setPandaFlag(4, value);
    }

    public boolean isSitting() {
        return getPandaFlag(8);
    }

    public void setSitting(boolean value) {
        setPandaFlag(8, value);
    }

    public boolean isUpsideDown() {
        return getPandaFlag(16);
    }

    public void setUpsideDown(boolean value) {
        setPandaFlag(16, value);
    }

    public int getHeadShaking() {
        return getHeadShakingTicks();
    }

    public void setHeadShaking(int timeInTicks) {
        setData(MetaIndex.PANDA_HEAD_SHAKING, timeInTicks);
        sendData(MetaIndex.PANDA_HEAD_SHAKING);
    }

    @Deprecated
    public int getHeadShakingTicks() {
        return getData(MetaIndex.PANDA_HEAD_SHAKING);
    }

    private boolean getPandaFlag(int value) {
        return (getData(MetaIndex.PANDA_META) & value) != 0;
    }

    private void setPandaFlag(int no, boolean flag) {
        byte b1 = getData(MetaIndex.PANDA_META);

        if (flag) {
            b1 = (byte) (b1 | no);
        } else {
            b1 = (byte) (b1 & ~no);
        }

        setData(MetaIndex.PANDA_META, b1);
        sendData(MetaIndex.PANDA_META);
    }
}
