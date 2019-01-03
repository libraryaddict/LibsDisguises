package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.parser.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DisguiseModifyEntityCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LibsMsg.NO_CONSOLE.get());
            return true;
        }

        DisguisePermissions permissions = getPermissions(sender);

        if (!permissions.hasPermissions()) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        // TODO Validate if any disguises have this arg

        LibsDisguises.getInstance().getListener()
                .setDisguiseModify(sender.getName(), DisguiseUtilities.split(StringUtils.join(args, " ")));

        sender.sendMessage(LibsMsg.DMODIFYENT_CLICK.get(DisguiseConfig.getDisguiseEntityExpire()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return tabs;
        }

        String[] args = getArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (!perms.hasPermissions()) {
            return tabs;
        }

        for (DisguisePerm perm : perms.getAllowed()) {
            boolean addMethods = true;

            if (args.length > 0) {
                String prevArg = args[args.length - 1];

                ParamInfo info = ParamInfoManager.getParamInfo(perm.getType(), prevArg);

                if (info != null) {
                    if (!info.isParam(boolean.class)) {
                        addMethods = false;
                    }

                    if (info.hasValues()) {
                        tabs.addAll(info.getEnums(origArgs[origArgs.length - 1]));
                    } else if (info.isParam(String.class)) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            tabs.add(player.getName());
                        }
                    }
                }
            }

            if (addMethods) {
                // If this is a method, add. Else if it can be a param of the previous argument, add.
                for (Method method : ParamInfoManager.getDisguiseWatcherMethods(perm.getType().getWatcherClass())) {
                    tabs.add(method.getName());
                }
            }
        }

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        sender.sendMessage(LibsMsg.DMODENT_HELP1.get());
        sender.sendMessage(LibsMsg.DMODIFY_HELP3
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));
    }
}
