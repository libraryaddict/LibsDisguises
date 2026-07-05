package me.libraryaddict.disguise.utilities.scoreboard.bukkit;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.scoreboard.AbstractScoreboardManager;
import me.libraryaddict.disguise.utilities.scoreboard.DisguiseScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BukkitScoreboardManager extends AbstractScoreboardManager {
    @Override
    public Collection<Scoreboard> getAllScoreboards() {
        List<Scoreboard> boards = new ArrayList<>();

        boards.add(Bukkit.getScoreboardManager().getMainScoreboard());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (boards.contains(player.getScoreboard())) {
                continue;
            }

            boards.add(player.getScoreboard());
        }

        return boards;
    }

    @Override
    protected void updateRegisteredTeam(PlayerDisguise disguise, DisguiseScoreboardTeam team) {
        for (Scoreboard board : getAllScoreboards()) {
            applyTeam(board, team, disguise.isNameVisible());
        }
    }

    @Override
    protected void removeRegisteredTeam(String teamName) {
        for (Scoreboard board : getAllScoreboards()) {
            Team t = board.getTeam(teamName);

            if (t == null) {
                continue;
            }

            for (String name : t.getEntries()) {
                board.resetScores(name);
            }

            t.unregister();
        }
    }

    @Override
    public void registerNoName(Player player) {
        registerNoName(player.getScoreboard());
    }

    public void registerNoName(Scoreboard scoreboard) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        Team mainTeam = scoreboard.getTeam(NO_NAME_TEAM);

        if (mainTeam == null) {
            mainTeam = scoreboard.registerNewTeam(NO_NAME_TEAM);
            mainTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            mainTeam.addEntry("§r");
        } else if (!mainTeam.hasEntry("§r")) {
            mainTeam.addEntry("§r");
        }
    }

    @Override
    public void registerAllExtendedNames(Player player) {
        registerAllExtendedNames(player.getScoreboard());
    }

    public void registerAllExtendedNames(Scoreboard scoreboard) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        for (Set<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
            for (Disguise disguise : disguises) {
                if (!disguise.isPlayerDisguise() || !disguise.isDisguiseInUse()) {
                    continue;
                }

                DisguiseScoreboardTeam name = ((PlayerDisguise) disguise).getScoreboardName();

                if (name == null || name.getTeamName() == null) {
                    continue;
                }

                applyTeam(scoreboard, name, ((PlayerDisguise) disguise).isNameVisible());
            }
        }
    }

    @Override
    public void setGlowColor(UUID uuid, @Nullable ChatColor color) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        String name = color == null ? "" : getTeamName(color);

        for (Scoreboard scoreboard : getAllScoreboards()) {
            Team team = scoreboard.getEntryTeam(uuid.toString());

            if (team != null) {
                if (!team.getName().startsWith(COLOR_TEAM_PREFIX) || name.equals(team.getName())) {
                    continue;
                }

                team.removeEntry(uuid.toString());

                if (team.getEntries().isEmpty() && team.getName().startsWith(COLOR_TEAM_PREFIX)) {
                    team.unregister();
                }
            }

            if (color == null) {
                continue;
            }

            team = scoreboard.getTeam(name);

            if (team == null) {
                team = scoreboard.registerNewTeam(name);
                team.setColor(color);
            }

            team.addEntry(uuid.toString());
        }
    }

    @Override
    public void registerColors(Player player) {
        registerColors(player.getScoreboard());
    }

    public void registerColors(Scoreboard scoreboard) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        for (ChatColor color : ChatColor.values()) {
            if (!color.isColor()) {
                continue;
            }

            String name = getTeamName(color);

            Team team = scoreboard.getTeam(name);

            if (team == null) {
                team = scoreboard.registerNewTeam(name);
            }

            team.setColor(color);

            if (DisguiseConfig.isModifyCollisions() && team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.NEVER) {
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        }
    }

    @Override
    public void onEnable() {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        for (Scoreboard board : getAllScoreboards()) {
            unregisterTeam(board);

            registerNoName(board);
            registerAllExtendedNames(board);
            registerColors(board);
        }
    }

    @Override
    public void onDisable() {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        for (Scoreboard board : getAllScoreboards()) {
            unregisterTeam(board);
        }
    }

    private void unregisterTeam(Scoreboard scoreboard) {
        for (Team team : scoreboard.getTeams()) {
            if (!team.getName().startsWith(LIBS_TEAM_PREFIX)) {
                continue;
            }

            for (String name : team.getEntries()) {
                scoreboard.resetScores(name);
            }

            team.unregister();
        }
    }

    public void applyTeam(Scoreboard board, DisguiseScoreboardTeam disguiseTeam, boolean nameVisible) {
        Disguise disguise = disguiseTeam.getDisguise();

        nameVisible = !disguise.getInternals().getNameDisplayType().isFakeEntity() && nameVisible;
        Team team = board.getTeam(disguiseTeam.getTeamName());

        if (team == null) {
            team = board.registerNewTeam(disguiseTeam.getTeamName());
            team.addEntry(disguiseTeam.getEntry());

            if (!nameVisible) {
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            }
        } else if (team.getOption(Team.Option.NAME_TAG_VISIBILITY) != (nameVisible ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER)) {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, nameVisible ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER);
        }

        if (DisguiseConfig.isModifyCollisions() && team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.NEVER) {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        if (disguise.getWatcher().getGlowColor() != null && disguise.getWatcher().getGlowColor() != team.getColor()) {
            team.setColor(disguise.getWatcher().getGlowColor());
        }

        String prefix = disguiseTeam.getPrefix();
        String suffix = disguiseTeam.getSuffix();

        if (!prefix.equals(team.getPrefix())) {
            team.setPrefix(NmsVersion.v1_13.isSupported() ? "" : prefix);
        }

        if (!suffix.equals(team.getSuffix())) {
            team.setSuffix(NmsVersion.v1_13.isSupported() ? "" : suffix);
        }
    }
}
