package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Llama;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class LlamaWatcher extends ChestedHorseWatcher {

    public LlamaWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setColor(Llama.Color color) {
        setData(MetaIndex.LLAMA_COLOR, color.ordinal());
        sendData(MetaIndex.LLAMA_COLOR);
    }

    public Llama.Color getColor() {
        return Llama.Color.values()[getData(MetaIndex.LLAMA_COLOR)];
    }

    public void setCarpet(AnimalColor color) {
        setData(MetaIndex.LLAMA_CARPET, color.getId());
        sendData(MetaIndex.LLAMA_CARPET);
    }

    public AnimalColor getCarpet() {
        return AnimalColor.getColor(getData(MetaIndex.LLAMA_CARPET));
    }

    public void setStrength(int strength) {
        setData(MetaIndex.LLAMA_STRENGTH, strength);
        sendData(MetaIndex.LLAMA_STRENGTH);
    }

    public int getStrength() {
        return getData(MetaIndex.LLAMA_STRENGTH);
    }

}
