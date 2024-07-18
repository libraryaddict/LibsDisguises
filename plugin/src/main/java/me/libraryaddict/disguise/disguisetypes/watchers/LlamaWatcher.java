package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.DyeColor;
import org.bukkit.entity.Llama;

public class LlamaWatcher extends ChestedHorseWatcher {

    public LlamaWatcher(Disguise disguise) {
        super(disguise);
    }

    public Llama.Color getColor() {
        return getData(MetaIndex.LLAMA_COLOR);
    }

    public void setColor(Llama.Color color) {
        sendData(MetaIndex.LLAMA_COLOR, color);
    }

    public DyeColor getCarpet() {
        if (!hasValue(MetaIndex.LLAMA_CARPET) || getData(MetaIndex.LLAMA_CARPET) == -1) {
            return null;
        }

        return AnimalColor.getColorByWool(getData(MetaIndex.LLAMA_CARPET)).getDyeColor();
    }

    public void setCarpet(DyeColor dyeColor) {
        sendData(MetaIndex.LLAMA_CARPET, dyeColor == null ? -1 : (int) dyeColor.getWoolData());
    }

    @Deprecated
    public void setCarpet(AnimalColor color) {
        setCarpet(color.getDyeColor());
    }

    public int getStrength() {
        return getData(MetaIndex.LLAMA_STRENGTH);
    }

    public void setStrength(int strength) {
        sendData(MetaIndex.LLAMA_STRENGTH, strength);
    }
}
