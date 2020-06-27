package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.List;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDPermTest implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("permtest", "permissions", "permissiontest");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.permtest";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!LibsPremium.isPremium()) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_NON_PREM);
        }

        Permissible player;

        if (args.length > 1) {
            player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[1]));
                return;
            }
        } else {
            player = sender;
        }

        DisguisePermissions permissions = new DisguisePermissions(player, "disguise");
        DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_INFO_1);
        DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_INFO_2);

        if (player.hasPermission("libsdisguises.disguise.pig")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NORMAL_PERM_CHECK_SUCCESS);

            if (permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.PIG))) {
                DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_SUCCESS);
            } else {
                DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_FAIL);
            }
        } else {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NORMAL_PERM_CHECK_FAIL);
        }

        if (player.hasPermission("libsdisguises.disguise.zombie") ||
                permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.ZOMBIE))) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_ZOMBIE_PERMISSIONS);
        }

        PluginCommand command = LibsDisguises.getInstance().getCommand("disguise");

        if (command == null) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_CHECK_COMMAND_UNREGISTERED);
        } else if (player.hasPermission(command.getPermission())) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_COMMAND_SUCCESS, command.getPermission());
        } else {
            DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_PERM_COMMAND_FAIL, command.getPermission());
        }

        if (!sender.hasPermission("libsdisguises.seecmd.disguise")) {
            sender.sendMessage(LibsMsg.LIBS_PERM_COMMAND_FAIL.get("libsdisguises.seecmd.disguise"));
        } else {
            sender.sendMessage(LibsMsg.LIBS_PERM_COMMAND_SUCCESS.get("libsdisguises.seecmd.disguise"));
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_PERMTEST;
    }
}
