package me.libraryaddict.disguise.utilities.scoreboard;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

public class DisguiseScoreboardTeam {
    @Getter
    @Setter
    private String teamName;
    private String[] nameParts;
    @Getter
    private final PlayerDisguise disguise;

    public DisguiseScoreboardTeam(PlayerDisguise disguise, String[] nameParts) {
        this.disguise = disguise;
        this.nameParts = nameParts;
    }

    public String getEntry() {
        return nameParts[1];
    }

    public synchronized String getPrefix() {
        return nameParts[0];
    }

    public synchronized String getSuffix() {
        return nameParts[2];
    }

    public synchronized void setNameParts(String[] nameParts) {
        this.nameParts = nameParts;
    }
}
