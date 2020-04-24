package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.List;

/**
 * Created by libraryaddict on 24/04/2020.
 */
public class LDDebugPlayer implements LDCommand {
    public class DebugInteraction implements LibsEntityInteract {
        @Override
        public void onInteract(Player player, Entity entity) {
            Disguise disguise = DisguiseAPI.getDisguise(player, entity);

            if (disguise == null) {
                player.sendMessage(LibsMsg.TARGET_NOT_DISGUISED.get());
                return;
            }

            if (!disguise.isPlayerDisguise()) {
                player.sendMessage(ChatColor.RED + "Meant to be used on player disguises!");
                return;
            }

            PlayerDisguise disg = (PlayerDisguise) disguise;

            player.sendMessage(ChatColor.RED + "Name: " + disg.getName().replace(ChatColor.COLOR_CHAR, '&'));

            if (!disg.hasScoreboardName()) {
                player.sendMessage(ChatColor.RED + "Disguise doesn't have scoreboard name, can't say more.");
                return;
            }

            DisguiseUtilities.DScoreTeam name = disg.getScoreboardName();

            player.sendMessage(ChatColor.RED +
                    String.format("Prefix: '%s', Suffix: '%s', Disguise Name: '%s', Team '%s'",
                            name.getPrefix().replace(ChatColor.COLOR_CHAR, '&'),
                            name.getSuffix().replace(ChatColor.COLOR_CHAR, '&'),
                            name.getPlayer().replace(ChatColor.COLOR_CHAR, '&'), name.getTeamName()));

            Team team = player.getScoreboard().getTeam(name.getTeamName());

            if (team == null) {
                player.sendMessage(ChatColor.RED + "That team doesn't exist to you");
                return;
            }

            player.sendMessage(ChatColor.RED + String.format("Prefix Matches: %s, Suffix Matches: %s, In Team: %s",
                    team.getPrefix().equals(name.getPrefix()), team.getSuffix().equals(name.getSuffix()),
                    team.hasEntry(name.getPlayer())));
        }
    }

    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("debug");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public String getPermission() {
        return "libsdisguises.debug";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
        }

        LibsDisguises.getInstance().getListener().addInteraction(sender.getName(), new DebugInteraction(), 60);
        sender.sendMessage(ChatColor.DARK_GREEN + "Right click a disguised player to get some debug outta em");
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_DEBUG;
    }
}
