package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
        sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_INFO_1.get());
        sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_INFO_2.get());

        if (player.hasPermission("libsdisguises.disguise.pig")) {
            sender.sendMessage(LibsMsg.NORMAL_PERM_CHECK_SUCCESS.get());

            if (permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.PIG))) {
                sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_SUCCESS.get());
            } else {
                sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_FAIL.get());
            }
        } else {
            sender.sendMessage(LibsMsg.NORMAL_PERM_CHECK_FAIL.get());
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
