package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseParser;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import me.libraryaddict.disguise.utilities.LibsMsg;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers.ParamInfo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisguiseModifyPlayerCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map = getPermissions(sender);

        if (map.isEmpty()) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, map);
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
            sendCommandUsage(sender, map);
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

        if (!map.containsKey(new DisguisePerm(disguise.getType()))) {
            sender.sendMessage(LibsMsg.DMODPLAYER_NOPERM.get());
            return true;
        }

        try {
            DisguiseParser.callMethods(sender, disguise, map.get(new DisguisePerm(disguise.getType())),
                    new ArrayList<String>(), DisguiseParser.split(StringUtils.join(newArgs, " ")));
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

        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> perms = getPermissions(sender);

        if (perms.isEmpty())
            return tabs;

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

            for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];

                    if (!method.getName().equalsIgnoreCase(arg))
                        continue;

                    usedOptions.add(arg);
                }
            }

            if (passesCheck(sender, perms.get(disguiseType), usedOptions)) {
                boolean addMethods = true;

                if (args.length > 1) {
                    String prevArg = args[args.length - 1];

                    ParamInfo info = ReflectionFlagWatchers.getParamInfo(disguiseType, prevArg);

                    if (info != null) {
                        if (info.getParamClass() != boolean.class)
                            addMethods = false;

                        if (info.isEnums()) {
                            for (String e : info.getEnums(origArgs[origArgs.length - 1])) {
                                tabs.add(e);
                            }
                        } else {
                            if (info.getParamClass() == String.class) {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    tabs.add(p.getName());
                                }
                            }
                        }
                    }
                }

                if (addMethods) {
                    // If this is a method, add. Else if it can be a param of the previous argument, add.
                    for (Method method : ReflectionFlagWatchers
                            .getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
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
    protected void sendCommandUsage(CommandSender sender,
            HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(map);

        sender.sendMessage(LibsMsg.DMODPLAYER_HELP1.get());
        sender.sendMessage(LibsMsg.DMODIFY_HELP3
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));
    }
}
