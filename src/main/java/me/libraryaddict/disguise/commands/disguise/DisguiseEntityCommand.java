package me.libraryaddict.disguise.commands.disguise;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.commands.interactions.DisguiseEntityInteraction;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class DisguiseEntityCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isNotPremium(sender)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LibsMsg.NO_CONSOLE.get());
            return true;
        }

        if (!getPermissions(sender).hasPermissions()) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, getPermissions(sender));
            return true;
        }

        String[] disguiseArgs = DisguiseUtilities.split(StringUtils.join(args, " "));
        Disguise testDisguise;

        try {
            testDisguise = DisguiseParser
                    .parseTestDisguise(sender, getPermNode(), disguiseArgs, getPermissions(sender));
        }
        catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }

            return true;
        }
        catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return true;
        }

        LibsDisguises.getInstance().getListener()
                .addInteraction(sender.getName(), new DisguiseEntityInteraction(disguiseArgs),
                        DisguiseConfig.getDisguiseEntityExpire());

        sender.sendMessage(LibsMsg.DISG_ENT_CLICK
                .get(DisguiseConfig.getDisguiseEntityExpire(), testDisguise.getType().toReadable()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return tabs;
        }

        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        return filterTabs(getTabDisguiseTypes(sender, perms, args, 0, getCurrentArg(origArgs)), origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        sender.sendMessage(LibsMsg.DISG_ENT_HELP1.get());
        sender.sendMessage(LibsMsg.CAN_USE_DISGS
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));

        if (allowedDisguises.contains("player")) {
            sender.sendMessage(LibsMsg.DISG_ENT_HELP3.get());
        }

        sender.sendMessage(LibsMsg.DISG_ENT_HELP4.get());

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            sender.sendMessage(LibsMsg.DISG_ENT_HELP5.get());
        }
    }
}
