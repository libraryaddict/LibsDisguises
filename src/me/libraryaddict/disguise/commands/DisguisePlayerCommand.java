package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class DisguisePlayerCommand extends DisguiseBaseCommand implements TabCompleter {

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

        if (args.length == 1) {
            sender.sendMessage(LibsMsg.DPLAYER_SUPPLY.get());
            return true;
        }

        Entity player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            if (args[0].contains("-")) {
                try {
                    player = Bukkit.getEntity(UUID.fromString(args[0]));
                }
                catch (Exception ignored) {
                }
            }
        }

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

        Disguise disguise;

        try {
            disguise = DisguiseParser
                    .parseDisguise(sender, getPermNode(), DisguiseParser.split(StringUtils.join(newArgs, " ")), map);
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

        if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            sender.sendMessage(LibsMsg.DISABLED_LIVING_TO_MISC.get());
            return true;
        }

        if (DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
            if (disguise.getWatcher() instanceof LivingWatcher) {
                disguise.getWatcher().setCustomName(getDisplayName(player));

                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                    disguise.getWatcher().setCustomNameVisible(true);
                }
            }
        }
        disguise.setEntity(player);

        if (!setViewDisguise(args)) {
            // They prefer to have the opposite of whatever the view disguises option is
            if (DisguiseAPI.hasSelfDisguisePreference(disguise.getEntity()) &&
                    disguise.isSelfDisguiseVisible() == DisguiseConfig.isViewDisguises())
                disguise.setViewSelfDisguise(!disguise.isSelfDisguiseVisible());
        }

        disguise.startDisguise();

        if (disguise.isDisguiseInUse()) {
            sender.sendMessage(LibsMsg.DISG_PLAYER_AS_DISG
                    .get(player instanceof Player ? player.getName() : DisguiseType.getType(player).toReadable(),
                            disguise.getType().toReadable()));
        } else {
            sender.sendMessage(LibsMsg.DISG_PLAYER_AS_DISG_FAIL
                    .get(player instanceof Player ? player.getName() : DisguiseType.getType(player).toReadable(),
                            disguise.getType().toReadable()));
        }

        return true;
    }

    private boolean setViewDisguise(String[] strings) {
        for (String string : strings) {
            if (!string.equalsIgnoreCase("setViewSelfDisguise"))
                continue;

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getArgs(origArgs);

        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> perms = getPermissions(sender);

        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tabs.add(player.getName());
            }
        } else if (args.length == 1) {
            tabs.addAll(getAllowedDisguises(perms));
        } else {
            DisguisePerm disguiseType = DisguiseParser.getDisguisePerm(args[1]);

            if (disguiseType == null)
                return filterTabs(tabs, origArgs);

            if (args.length == 2 && disguiseType.getType() == DisguiseType.PLAYER) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabs.add(player.getName());
                }
            } else {
                ArrayList<String> usedOptions = new ArrayList<>();

                for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                    for (int i = disguiseType.getType() == DisguiseType.PLAYER ? 3 : 2; i < args.length; i++) {
                        String arg = args[i];

                        if (!method.getName().equalsIgnoreCase(arg))
                            continue;

                        usedOptions.add(arg);
                    }
                }

                if (passesCheck(sender, perms.get(disguiseType), usedOptions)) {
                    boolean addMethods = true;

                    if (args.length > 2) {
                        String prevArg = args[args.length - 1];

                        ParamInfo info = ReflectionFlagWatchers.getParamInfo(disguiseType, prevArg);

                        if (info != null) {
                            if (info.getParamClass() != boolean.class)
                                addMethods = false;

                            if (info.isEnums()) {
                                tabs.addAll(Arrays.asList(info.getEnums(origArgs[origArgs.length - 1])));
                            } else {
                                if (info.getParamClass() == String.class) {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        tabs.add(player.getName());
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

        sender.sendMessage(LibsMsg.D_HELP1.get());
        sender.sendMessage(LibsMsg.CAN_USE_DISGS
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));

        if (allowedDisguises.contains("player")) {
            sender.sendMessage(LibsMsg.D_HELP3.get());
        }

        sender.sendMessage(LibsMsg.D_HELP4.get());

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            sender.sendMessage(LibsMsg.D_HELP5.get());
        }
    }
}
