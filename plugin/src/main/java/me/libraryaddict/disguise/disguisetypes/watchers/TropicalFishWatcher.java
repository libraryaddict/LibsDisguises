package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by libraryaddict on 6/08/2018.
 */
public class TropicalFishWatcher extends FishWatcher {
    private enum CraftPattern {
        KOB("KOB", 0, 0, false),
        SUNSTREAK("SUNSTREAK", 1, 1, false),
        SNOOPER("SNOOPER", 2, 2, false),
        DASHER("DASHER", 3, 3, false),
        BRINELY("BRINELY", 4, 4, false),
        SPOTTY("SPOTTY", 5, 5, false),
        FLOPPER("FLOPPER", 6, 0, true),
        STRIPEY("STRIPEY", 7, 1, true),
        GLITTER("GLITTER", 8, 2, true),
        BLOCKFISH("BLOCKFISH", 9, 3, true),
        BETTY("BETTY", 10, 4, true),
        CLAYFISH("CLAYFISH", 11, 5, true);

        private final int variant;
        private final boolean large;
        private static final Map<Integer, TropicalFish.Pattern> BY_DATA;

        static {
            BY_DATA = new HashMap<>();
            CraftPattern[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final CraftPattern type = values[i];
                CraftPattern.BY_DATA.put(type.getDataValue(), TropicalFish.Pattern.values()[type.ordinal()]);
            }
        }

        static TropicalFish.Pattern fromData(final int data) {
            return CraftPattern.BY_DATA.get(data);
        }

        CraftPattern(final String s, final int n, final int variant, final boolean large) {
            this.variant = variant;
            this.large = large;
        }

        public int getDataValue() {
            return this.variant << 8 | (this.large ? 1 : 0);
        }
    }

    public TropicalFishWatcher(Disguise disguise) {
        super(disguise);

        if (DisguiseConfig.isRandomDisguises()) {
            this.setPattern(TropicalFish.Pattern.values()[RandomUtils.nextInt(TropicalFish.Pattern.values().length)]);
            this.setBodyColor(DyeColor.values()[RandomUtils.nextInt(DyeColor.values().length)]);
            this.setPatternColor(DyeColor.values()[RandomUtils.nextInt(DyeColor.values().length)]);
        }
    }

    public DyeColor getPatternColor() {
        return DyeColor.getByWoolData((byte) (getVariant() >> 24 & 0xFF));
    }

    @RandomDefaultValue
    public void setPatternColor(DyeColor dyeColor) {
        setVariant(getData(dyeColor, getBodyColor(), getPattern()));
    }

    private int getData(final DyeColor patternColor, final DyeColor bodyColor, final TropicalFish.Pattern type) {
        return patternColor.getWoolData() << 24 | bodyColor.getWoolData() << 16 |
                CraftPattern.values()[type.ordinal()].getDataValue();
    }

    public DyeColor getBodyColor() {
        return DyeColor.getByWoolData((byte) (getVariant() >> 16 & 0xFF));
    }

    @RandomDefaultValue
    public void setBodyColor(DyeColor dyeColor) {
        setVariant(getData(getPatternColor(), dyeColor, getPattern()));
    }

    public TropicalFish.Pattern getPattern() {
        return CraftPattern.fromData(getVariant() & 0xFFFF);
    }

    @RandomDefaultValue
    public void setPattern(TropicalFish.Pattern pattern) {
        setVariant(getData(getPatternColor(), getBodyColor(), pattern));
    }

    @Deprecated
    public int getVariant() {
        return getData(MetaIndex.TROPICAL_FISH_VARIANT);
    }

    @Deprecated
    public void setVariant(int variant) {
        setData(MetaIndex.TROPICAL_FISH_VARIANT, variant);
        sendData(MetaIndex.TROPICAL_FISH_VARIANT);
    }
}
