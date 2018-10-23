package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsMsg;
import me.libraryaddict.disguise.utilities.parser.*;
import me.libraryaddict.disguise.utilities.parser.params.ParamInfo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisguiseEntityCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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

        Disguise disguise;

        try {
            disguise = DisguiseParser
                    .parseDisguise(sender, getPermNode(), DisguiseParser.split(StringUtils.join(args, " ")),
                            getPermissions(sender));
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

        LibsDisguises.getInstance().getListener().setDisguiseEntity(sender.getName(), disguise);

        sender.sendMessage(
                LibsMsg.DISG_ENT_CLICK.get(DisguiseConfig.getDisguiseEntityExpire(), disguise.getType().toReadable()));
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

        if (args.length == 0) {
            for (String type : getAllowedDisguises(perms)) {
                tabs.add(type);
            }
        } else {
            DisguisePerm disguiseType = DisguiseParser.getDisguisePerm(args[0]);

            if (disguiseType == null)
                return filterTabs(tabs, origArgs);

            if (args.length == 1 && disguiseType.getType() == DisguiseType.PLAYER) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabs.add(player.getName());
                }
            } else {
                ArrayList<String> usedOptions = new ArrayList<>();

                for (Method method : ParamInfoManager.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                    for (int i = disguiseType.getType() == DisguiseType.PLAYER ? 2 : 1; i < args.length; i++) {
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
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    tabs.add(player.getName());
                                }
                            }
                        }
                    }

                    if (addMethods) {
                        // If this is a method, add. Else if it can be a param of the previous argument, add.
                        for (Method method : ParamInfoManager
                                .getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                            tabs.add(method.getName());
                        }
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
