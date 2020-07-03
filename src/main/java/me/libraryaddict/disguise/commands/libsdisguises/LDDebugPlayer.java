package me.libraryaddict.disguise.commands.libsdisguises;

import com.google.gson.Gson;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
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
                LibsMsg.TARGET_NOT_DISGUISED.send(player);
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

            if (DisguiseConfig.isArmorstandsName()) {
                player.sendMessage(
                        ChatColor.AQUA + "Oh! You're using armorstands! Lets give some debug for that too..");
                player.sendMessage(ChatColor.RED + String.format("Names: %s, Length: %s, Custom Name: '%s'",
                        new Gson().toJson(disg.getMultiName()).replace(ChatColor.COLOR_CHAR, '&'),
                        disg.getMultiNameLength(),
                        disg.getWatcher().getCustomName().replace(ChatColor.COLOR_CHAR, '&')));
            }

            Team team = player.getScoreboard().getTeam(name.getTeamName());

            if (team == null) {
                player.sendMessage(ChatColor.RED + "That team doesn't exist to you");

                if (Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name.getTeamName()) != null) {
                    player.sendMessage(ChatColor.RED + "But it does exist on the main scoreboard..");
                }
                return;
            }

            player.sendMessage(ChatColor.RED +
                    String.format("Prefix Matches: %s, Suffix Matches: %s, In Team: %s, Name Visibility: %s",
                            team.getPrefix().equals(name.getPrefix()), team.getSuffix().equals(name.getSuffix()),
                            team.hasEntry(name.getPlayer()), team.getOption(Team.Option.NAME_TAG_VISIBILITY)));
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
            LibsMsg.NO_PERM.send(sender);
        }

        LibsDisguises.getInstance().getListener().addInteraction(sender.getName(), new DebugInteraction(), 60);
        sender.sendMessage(ChatColor.DARK_GREEN + "Right click a disguised player to get some debug outta em");
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_DEBUG;
    }
}
