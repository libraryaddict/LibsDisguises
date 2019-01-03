package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.parser.*;
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

public class DisguiseModifyPlayerCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        DisguisePermissions permissions = getPermissions(sender);

        if (!permissions.hasPermissions()) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[0]));
            return true;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        if (newArgs.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        Disguise disguise = null;

        if (sender instanceof Player)
            disguise = DisguiseAPI.getDisguise((Player) sender, player);

        if (disguise == null)
            disguise = DisguiseAPI.getDisguise(player);

        if (disguise == null) {
            sender.sendMessage(LibsMsg.DMODPLAYER_NODISGUISE.get(player.getName()));
            return true;
        }

        DisguisePerm disguisePerm = new DisguisePerm(disguise.getType());

        if (!permissions.isAllowedDisguise(disguisePerm)) {
            sender.sendMessage(LibsMsg.DMODPLAYER_NOPERM.get());
            return true;
        }

        try {
            DisguiseParser.callMethods(sender, disguise, permissions, disguisePerm, new ArrayList<>(),
                    DisguiseUtilities.split(StringUtils.join(newArgs, " ")));
        }
        catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
            return true;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }

        sender.sendMessage(LibsMsg.DMODPLAYER_MODIFIED.get(player.getName()));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (!perms.hasPermissions()) {
            return tabs;
        }

        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tabs.add(player.getName());
            }
        } else {
            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[0]));
                return tabs;
            }

            Disguise disguise = null;

            if (sender instanceof Player)
                disguise = DisguiseAPI.getDisguise((Player) sender, player);

            if (disguise == null)
                disguise = DisguiseAPI.getDisguise(player);

            if (disguise == null) {
                sender.sendMessage(LibsMsg.DMODPLAYER_NODISGUISE.get(player.getName()));
                return tabs;
            }

            DisguisePerm disguiseType = new DisguisePerm(disguise.getType());

            ArrayList<String> usedOptions = new ArrayList<>();

            for (Method method : ParamInfoManager.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];

                    if (!method.getName().equalsIgnoreCase(arg))
                        continue;

                    usedOptions.add(arg);
                }
            }

            if (perms.isAllowedDisguise(disguiseType, usedOptions)) {
                boolean addMethods = true;

                if (args.length > 1) {
                    String prevArg = args[args.length - 1];

                    ParamInfo info = ParamInfoManager.getParamInfo(disguiseType, prevArg);

                    if (info != null) {
                        if (!info.isParam(boolean.class)) {
                            addMethods = false;
                        }

                        if (info.hasValues()) {
                            tabs.addAll(info.getEnums(origArgs[origArgs.length - 1]));
                        } else if (info.isParam(String.class)) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                tabs.add(p.getName());
                            }
                        }
                    }
                }

                if (addMethods) {
                    // If this is a method, add. Else if it can be a param of the previous argument, add.
                    for (Method method : ParamInfoManager.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                        tabs.add(method.getName());
                    }
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

        sender.sendMessage(LibsMsg.DMODPLAYER_HELP1.get());
        sender.sendMessage(LibsMsg.DMODIFY_HELP3
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));
    }
}
