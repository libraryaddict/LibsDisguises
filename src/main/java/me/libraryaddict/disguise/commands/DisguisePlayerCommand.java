package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisguisePlayerCommand extends DisguiseBaseCommand implements TabCompleter {

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

        if (args.length == 1) {
            sender.sendMessage(LibsMsg.DPLAYER_SUPPLY.get());
            return true;
        }

        Entity entity = Bukkit.getPlayer(args[0]);

        if (entity == null) {
            if (args[0].contains("-")) {
                try {
                    entity = Bukkit.getEntity(UUID.fromString(args[0]));
                }
                catch (Exception ignored) {
                }
            }
        }

        if (entity == null) {
            sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[0]));
            return true;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        if (newArgs.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        Disguise disguise;

        try {
            disguise = DisguiseParser
                    .parseDisguise(sender, getPermNode(), DisguiseUtilities.split(StringUtils.join(newArgs, " ")),
                            permissions);
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
                disguise.getWatcher().setCustomName(getDisplayName(entity));

                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                    disguise.getWatcher().setCustomNameVisible(true);
                }
            }
        }
        disguise.setEntity(entity);

        if (!setViewDisguise(args)) {
            // They prefer to have the opposite of whatever the view disguises option is
            if (DisguiseAPI.hasSelfDisguisePreference(disguise.getEntity()) &&
                    disguise.isSelfDisguiseVisible() == DisguiseConfig.isViewDisguises())
                disguise.setViewSelfDisguise(!disguise.isSelfDisguiseVisible());
        }

        disguise.startDisguise();

        if (disguise.isDisguiseInUse()) {
            sender.sendMessage(LibsMsg.DISG_PLAYER_AS_DISG
                    .get(entity instanceof Player ? entity.getName() : DisguiseType.getType(entity).toReadable(),
                            disguise.getType().toReadable()));
        } else {
            sender.sendMessage(LibsMsg.DISG_PLAYER_AS_DISG_FAIL
                    .get(entity instanceof Player ? entity.getName() : DisguiseType.getType(entity).toReadable(),
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

        DisguisePermissions perms = getPermissions(sender);

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

                for (Method method : ParamInfoManager.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                    for (int i = disguiseType.getType() == DisguiseType.PLAYER ? 3 : 2; i < args.length; i++) {
                        String arg = args[i];

                        if (!method.getName().equalsIgnoreCase(arg))
                            continue;

                        usedOptions.add(arg);
                    }
                }

                if (perms.isAllowedDisguise(disguiseType, usedOptions)) {
                    boolean addMethods = true;

                    if (args.length > 2) {
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
