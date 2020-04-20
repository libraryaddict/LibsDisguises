package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;

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

        if (DisguiseConfig.getPushingOption() == DisguiseConfig.DisguisePushing.IGNORE_SCOREBOARD) {
            sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_DISABLED.get());
        }

        Player player;

        if (args.length > 1) {
            player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[1]));
                return;
            }

            if (!DisguiseAPI.isDisguised(player)) {
                sender.sendMessage(LibsMsg.DMODPLAYER_NODISGUISE.get(player.getName()));
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;

            if (!DisguiseAPI.isDisguised(player)) {
                sender.sendMessage(LibsMsg.NOT_DISGUISED.get());
                return;
            }
        } else {
            sender.sendMessage(LibsMsg.NO_CONSOLE.get());
            return;
        }

        Scoreboard board = player.getScoreboard();

        Team team = board.getEntryTeam(sender.getName());

        if (team == null) {
            sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_NO_TEAM.get());
            return;
        }

        if (team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.NEVER &&
                team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.FOR_OTHER_TEAMS) {
            sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_NO_TEAM_PUSH.get(team.getName()));
            return;
        }

        sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_SUCCESS.get(team.getName()));
    }
}
