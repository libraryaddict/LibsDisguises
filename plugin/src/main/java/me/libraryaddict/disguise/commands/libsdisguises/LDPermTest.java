package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
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
            LibsMsg.LIBS_PERM_CHECK_NON_PREM.send(sender);
        }

        Permissible player;

        if (args.length > 1) {
            player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                LibsMsg.CANNOT_FIND_PLAYER.send(sender, args[1]);
                return;
            }

            LibsMsg.LIBS_PERM_CHECK_USING_TARGET.send(sender, args[1]);
        } else {
            player = sender;
            LibsMsg.LIBS_PERM_CHECK_CAN_TARGET.send(sender);
        }

        DisguisePermissions permissions = new DisguisePermissions(player, "disguise");
        LibsMsg.LIBS_PERM_CHECK_INFO_1.send(sender);
        LibsMsg.LIBS_PERM_CHECK_INFO_2.send(sender);

        if (player.hasPermission("libsdisguises.disguise.pig")) {
            LibsMsg.NORMAL_PERM_CHECK_SUCCESS.send(sender);

            if (permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.PIG))) {
                LibsMsg.LIBS_PERM_CHECK_SUCCESS.send(sender);
            } else {
                LibsMsg.LIBS_PERM_CHECK_FAIL.send(sender);
            }
        } else {
            LibsMsg.NORMAL_PERM_CHECK_FAIL.send(sender);
        }

        if (player.hasPermission("libsdisguises.disguise.zombie") ||
                permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.ZOMBIE))) {
            LibsMsg.LIBS_PERM_CHECK_ZOMBIE_PERMISSIONS.send(sender);
        }

        PluginCommand command = LibsDisguises.getInstance().getCommand("disguise");

        if (command == null) {
            LibsMsg.LIBS_PERM_CHECK_COMMAND_UNREGISTERED.send(sender);
        } else if (player.hasPermission(command.getPermission())) {
            LibsMsg.LIBS_PERM_COMMAND_SUCCESS.send(sender, command.getPermission());
        } else {
            LibsMsg.LIBS_PERM_COMMAND_FAIL.send(sender, command.getPermission());
        }

        if (!sender.hasPermission("libsdisguises.seecmd.disguise")) {
            LibsMsg.LIBS_PERM_COMMAND_FAIL.send(sender, "libsdisguises.seecmd.disguise");
        } else {
            LibsMsg.LIBS_PERM_COMMAND_SUCCESS.send(sender, "libsdisguises.seecmd.disguise");
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
