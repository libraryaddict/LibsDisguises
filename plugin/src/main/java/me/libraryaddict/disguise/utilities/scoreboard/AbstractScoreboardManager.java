package me.libraryaddict.disguise.utilities.scoreboard;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScoreboardManager implements ScoreboardManager {
    protected static final String LIBS_TEAM_PREFIX = "LD_";
    protected static final String NO_NAME_TEAM = "LD_NoName";
    protected static final String COLOR_TEAM_PREFIX = "LD_Color_";

    private final ConcurrentHashMap<String, DisguiseScoreboardTeam> teams = new ConcurrentHashMap<>();

    @Override
    public Map<String, DisguiseScoreboardTeam> getTeams() {
        return teams;
    }

    protected String getTeamName(ChatColor color) {
        return COLOR_TEAM_PREFIX + color.getChar();
    }

    @Override
    public void updateExtendedName(PlayerDisguise disguise) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        updateRegisteredTeam(disguise, getOrCreateTeamName(disguise));
    }

    @Override
    public void registerExtendedName(PlayerDisguise disguise) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        DisguiseScoreboardTeam team = getOrCreateTeamName(disguise);

        teams.put(team.getTeamName(), team);
        updateRegisteredTeam(disguise, team);
    }

    @Override
    public void unregisterExtendedName(PlayerDisguise disguise) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        DisguiseScoreboardTeam team = disguise.getScoreboardName();
        String teamName = team.getTeamName();

        if (teamName == null) {
            return;
        }

        removeRegisteredTeam(teamName);
        teams.remove(teamName);
        team.setTeamName(null);
    }

    @Override
    public DisguiseScoreboardTeam createScoreTeam(PlayerDisguise disguise, String[] split) {
        return new DisguiseScoreboardTeam(disguise, split);
    }

    protected abstract void updateRegisteredTeam(PlayerDisguise disguise, DisguiseScoreboardTeam team);

    protected abstract void removeRegisteredTeam(String teamName);

    private DisguiseScoreboardTeam getOrCreateTeamName(PlayerDisguise disguise) {
        DisguiseScoreboardTeam team = disguise.getScoreboardName();

        if (team.getTeamName() == null) {
            team.setTeamName(DisguiseUtilities.getUniqueTeam());
        }

        return team;
    }
}
