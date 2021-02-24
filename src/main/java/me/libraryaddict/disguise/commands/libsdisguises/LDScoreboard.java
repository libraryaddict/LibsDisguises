package me.libraryaddict.disguise.commands.libsdisguises;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDScoreboard implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("teams", "scoreboard", "board", "pushing");
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
                            sender.sendMessage(
                                    "The player disguise " + ((PlayerDisguise) disguise).getName() + " isn't using a scoreboard name? This is unexpected");
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
                                sender.sendMessage("The player disguise " + ((PlayerDisguise) disguise).getName().replace(ChatColor.COLOR_CHAR, '&') +
                                        " is missing a scoreboard team '" + scoreboardName.getTeamName() + "' on " + player.getName() +
                                        " and possibly more players!");
                            }

                            continue;
                        }

                        if (!team.getPrefix().equals("Colorize") &&
                                (!team.getPrefix().equals(scoreboardName.getPrefix()) || !team.getSuffix().equals(scoreboardName.getSuffix()))) {
                            if (issuesFound++ < 5) {
                                sender.sendMessage("The player disguise " + ((PlayerDisguise) disguise).getName().replace(ChatColor.COLOR_CHAR, '&') +
                                        " on scoreboard team '" + scoreboardName.getTeamName() + "' on " + player.getName() +
                                        " has an unexpected prefix/suffix of '" + team.getPrefix().replace(ChatColor.COLOR_CHAR, '&') + "' & '" +
                                        team.getSuffix().replace(ChatColor.COLOR_CHAR, '&') + "'! Expected '" +
                                        scoreboardName.getPrefix().replace(ChatColor.COLOR_CHAR, '&') + "' & '" +
                                        scoreboardName.getSuffix().replace(ChatColor.COLOR_CHAR, '&') + "'");
                            }
                            continue;
                        }

                        if (!team.hasEntry(scoreboardName.getPlayer())) {
                            if (issuesFound++ < 5) {
                                sender.sendMessage("The player disguise " + ((PlayerDisguise) disguise).getName().replace(ChatColor.COLOR_CHAR, '&') +
                                        " on scoreboard team '" + scoreboardName.getTeamName() + "' on " + player.getName() +
                                        " does not have the player entry expected! Instead has '" +
                                        StringUtils.join(team.getEntries(), ", ").replace(ChatColor.COLOR_CHAR, '&') + "'");
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

        List<PacketListener> listeners = ProtocolLibrary.getProtocolManager().getPacketListeners().stream()
                .filter(listener -> listener.getPlugin() != LibsDisguises.getInstance() && listener.getSendingWhitelist().getTypes().contains(PacketType.Play.Server.SCOREBOARD_TEAM)).collect(Collectors.toList());

        if (!listeners.isEmpty()) {
            ComponentBuilder builder =
                    new ComponentBuilder("");
            builder.append("The following plugins are listening for scoreboard teams using ProtocolLib, and could be modifying collisions: ");
            builder.color(net.md_5.bungee.api.ChatColor.BLUE);

            boolean comma = false;

            for (PacketListener listener : listeners) {
                if (comma) {
                    builder.reset();
                    builder.append(", ");
                    builder.color(net.md_5.bungee.api.ChatColor.BLUE);
                }

                comma = true;

                builder.reset();
                builder.append(listener.getPlugin().getName());
                builder.color(net.md_5.bungee.api.ChatColor.AQUA);

                String plugin = ChatColor.GOLD + "Plugin: " + ChatColor.YELLOW + listener.getPlugin().getName() + " v" +
                        listener.getPlugin().getDescription().getVersion();
                String listenerClass = ChatColor.GOLD + "Listener: " + ChatColor.YELLOW + listener.getClass().toString();
                String packets = ChatColor.GOLD + "Packets: " + ChatColor.YELLOW + StringUtils.join(listener.getSendingWhitelist().getTypes(), ", ");
                String priority = ChatColor.GOLD + "Priority: " + ChatColor.YELLOW + listener.getSendingWhitelist().getPriority();
                String options = ChatColor.GOLD + "Options: " + ChatColor.YELLOW + StringUtils.join(listener.getSendingWhitelist().getOptions(), ", ");

                builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        TextComponent.fromLegacyText(plugin + "\n" + listenerClass + "\n" + packets + "\n" + priority + "\n" + options)));
            }

            sender.spigot().sendMessage(builder.create());
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
