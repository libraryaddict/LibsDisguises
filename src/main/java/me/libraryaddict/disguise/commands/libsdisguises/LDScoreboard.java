package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDScoreboard implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("teams", "scoreboard", "board");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.scoreboard";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (DisguiseConfig.isScoreboardNames()) {
            int issuesFound = 0;
            int unexpected = 0;

            for (Set<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
                for (Disguise disguise : disguises) {
                    if (!disguise.isPlayerDisguise()) {
                        continue;
                    }

                    if (!((PlayerDisguise) disguise).hasScoreboardName()) {
                        if (unexpected++ < 3) {
                            sender.sendMessage("The player disguise " + ((PlayerDisguise) disguise).getName() +
                                    " isn't using a scoreboard name? This is unexpected");
                        }
                        continue;
                    }

                    ArrayList<Scoreboard> checked = new ArrayList<>();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Scoreboard board = player.getScoreboard();

                        if (checked.contains(board)) {
                            continue;
                        }

                        checked.add(board);
                        DisguiseUtilities.DScoreTeam scoreboardName = ((PlayerDisguise) disguise).getScoreboardName();

                        Team team = board.getTeam(scoreboardName.getTeamName());

                        if (team == null) {
                            if (issuesFound++ < 5) {
                                sender.sendMessage("The player disguise " +
                                        ((PlayerDisguise) disguise).getName().replace(ChatColor.COLOR_CHAR, '&') +
                                        " is missing a scoreboard team '" + scoreboardName.getTeamName() + "' on " +
                                        player.getName() + " and possibly more players!");
                            }

                            continue;
                        }

                        if (!team.getPrefix().equals("Colorize") &&
                                (!team.getPrefix().equals(scoreboardName.getPrefix()) ||
                                        !team.getSuffix().equals(scoreboardName.getSuffix()))) {
                            if (issuesFound++ < 5) {
                                sender.sendMessage("The player disguise " +
                                        ((PlayerDisguise) disguise).getName().replace(ChatColor.COLOR_CHAR, '&') +
                                        " on scoreboard team '" + scoreboardName.getTeamName() + "' on " +
                                        player.getName() + " has an unexpected prefix/suffix of '" +
                                        team.getPrefix().replace(ChatColor.COLOR_CHAR, '&') + "' & '" +
                                        team.getSuffix().replace(ChatColor.COLOR_CHAR, '&') + "'! Expected '" +
                                        scoreboardName.getPrefix().replace(ChatColor.COLOR_CHAR, '&') + "' & '" +
                                        scoreboardName.getSuffix().replace(ChatColor.COLOR_CHAR, '&') + "'");
                            }
                            continue;
                        }

                        if (!team.hasEntry(scoreboardName.getPlayer())) {
                            if (issuesFound++ < 5) {
                                sender.sendMessage("The player disguise " +
                                        ((PlayerDisguise) disguise).getName().replace(ChatColor.COLOR_CHAR, '&') +
                                        " on scoreboard team '" + scoreboardName.getTeamName() + "' on " +
                                        player.getName() + " does not have the player entry expected! Instead has '" +
                                        StringUtils.join(team.getEntries(), ", ").replace(ChatColor.COLOR_CHAR, '&') +
                                        "'");
                            }
                        }
                    }
                }
            }

            if (issuesFound == 0) {
                LibsMsg.LIBS_SCOREBOARD_NO_ISSUES.send(sender);
            } else {
                LibsMsg.LIBS_SCOREBOARD_ISSUES.send(sender, issuesFound);
            }
        } else {
            LibsMsg.LIBS_SCOREBOARD_NAMES_DISABLED.send(sender);
        }

        LibsMsg.LIBS_SCOREBOARD_IGNORE_TEST.send(sender);

        if (DisguiseConfig.getPushingOption() == DisguiseConfig.DisguisePushing.IGNORE_SCOREBOARD) {
            LibsMsg.LIBS_SCOREBOARD_DISABLED.send(sender);
        }

        Player player;

        if (args.length > 1) {
            player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                LibsMsg.CANNOT_FIND_PLAYER.send(sender, args[1]);
                return;
            }

            if (!DisguiseAPI.isDisguised(player)) {
                LibsMsg.DMODPLAYER_NODISGUISE.send(sender, player.getName());
                LibsMsg.DISGUISE_REQUIRED.send(sender);
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;

            if (!DisguiseAPI.isDisguised(player)) {
                LibsMsg.DISGUISE_REQUIRED.send(sender);
                return;
            }
        } else {
            LibsMsg.NO_CONSOLE.send(sender);
            return;
        }

        Scoreboard board = player.getScoreboard();

        Team team = board.getEntryTeam(sender.getName());

        if (team == null) {
            LibsMsg.LIBS_SCOREBOARD_NO_TEAM.send(sender);
            return;
        }

        if (team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.NEVER &&
                team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.FOR_OTHER_TEAMS) {
            LibsMsg.LIBS_SCOREBOARD_NO_TEAM_PUSH.send(sender, team.getName());
            return;
        }

        LibsMsg.LIBS_SCOREBOARD_SUCCESS.send(sender, team.getName());

        if (Bukkit.getPluginManager().getPlugin("TAB") != null) {
            LibsMsg.PLUGIN_TAB_DETECTED.send(sender);
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_SCOREBOARD;
    }
}
