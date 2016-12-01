package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.entity.Llama;

import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class LlamaWatcher extends ChestedHorseWatcher {

    public LlamaWatcher(Disguise disguise) {
        super(disguise);
    }

    public void setColor(Llama.Color color) {
        setData(FlagType.LLAMA_COLOR, color.ordinal());
        sendData(FlagType.LLAMA_COLOR);
    }

    public Llama.Color getColor() {
        return Llama.Color.values()[getData(FlagType.LLAMA_COLOR)];
    }

    public void setCarpet(AnimalColor color) {
        setData(FlagType.LLAMA_CARPET, color.ordinal());
        sendData(FlagType.LLAMA_CARPET);
    }

    public AnimalColor getCarpet() {
        return AnimalColor.getColor(getData(FlagType.LLAMA_CARPET));
    }

    public void setStrength(int strength) {
        setData(FlagType.LLAMA_STRENGTH, strength);
        sendData(FlagType.LLAMA_STRENGTH);
    }

    public int getStrength() {
        return getData(FlagType.LLAMA_STRENGTH);
    }

}
