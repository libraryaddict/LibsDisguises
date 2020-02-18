package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class DisguiseModifyRadiusCommand extends DisguiseBaseCommand implements TabCompleter {
    private int maxRadius = 30;

    public DisguiseModifyRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    private Collection<Entity> getNearbyEntities(CommandSender sender, int radius) {
        Location center;

        if (sender instanceof Player) {
            center = ((Player) sender).getLocation();
        } else {
            center = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5, 0, 0.5);
        }

        return center.getWorld().getNearbyEntities(center, radius, radius, radius);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
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

        if (args[0].equalsIgnoreCase(TranslateType.DISGUISES.get("DisguiseType")) ||
                args[0].equalsIgnoreCase(TranslateType.DISGUISES.get("DisguiseType") + "s")) {
            ArrayList<String> classes = new ArrayList<>();

            for (DisguiseType type : DisguiseType.values()) {
                if (type.getEntityType() == null) {
                    continue;
                }

                classes.add(type.toReadable());
            }

            Collections.sort(classes);

            sender.sendMessage(LibsMsg.DMODRADIUS_USABLE
                    .get(ChatColor.GREEN + StringUtils.join(classes, ChatColor.DARK_GREEN + ", " + ChatColor.GREEN)));
            return true;
        }

        DisguiseType baseType = null;
        int starting = 0;

        if (!isInteger(args[0])) {
            for (DisguiseType t : DisguiseType.values()) {
                if (t.getEntityType() == null) {
                    continue;
                }

                if (t.toReadable().replaceAll(" ", "").equalsIgnoreCase(args[0].replaceAll("_", ""))) {
                    baseType = t;
                    starting = 1;
                    break;
                }
            }

            if (baseType == null) {
                sender.sendMessage(LibsMsg.DMODRADIUS_UNRECOGNIZED.get(args[0]));
                return true;
            }
        }

        if (args.length == starting + 1) {
            sender.sendMessage(
                    (starting == 0 ? LibsMsg.DMODRADIUS_NEEDOPTIONS : LibsMsg.DMODRADIUS_NEEDOPTIONS_ENTITY).get());
            return true;
        } else if (args.length < 2) {
            sender.sendMessage(LibsMsg.DMODRADIUS_NEEDOPTIONS.get());
            return true;
        }

        if (!isInteger(args[starting])) {
            sender.sendMessage(LibsMsg.NOT_NUMBER.get(args[starting]));
            return true;
        }

        int radius = Integer.parseInt(args[starting]);

        if (radius > maxRadius) {
            sender.sendMessage(LibsMsg.LIMITED_RADIUS.get(maxRadius));
            radius = maxRadius;
        }

        String[] newArgs = new String[args.length - (starting + 1)];
        System.arraycopy(args, starting + 1, newArgs, 0, newArgs.length);

        if (newArgs.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        // Time to use it!
        int modifiedDisguises = 0;
        int noPermission = 0;

        String[] disguiseArgs = DisguiseUtilities.split(StringUtils.join(newArgs, " "));

        for (Entity entity : getNearbyEntities(sender, radius)) {
            if (entity == sender) {
                continue;
            }

            if (baseType != null && !baseType.name().equalsIgnoreCase(entity.getType().name())) {
                continue;
            }

            Disguise disguise;

            if (sender instanceof Player)
                disguise = DisguiseAPI.getDisguise((Player) sender, entity);
            else
                disguise = DisguiseAPI.getDisguise(entity);

            DisguisePerm disguisePerm = new DisguisePerm(disguise.getType());

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                noPermission++;
                continue;
            }

            String[] tempArgs = Arrays.copyOf(disguiseArgs, disguiseArgs.length);
            tempArgs = DisguiseParser.parsePlaceholders(tempArgs, sender, entity);

            try {
                DisguiseParser.callMethods(sender, disguise, permissions, disguisePerm, new ArrayList<>(), tempArgs,
                        "DisguiseModifyRadius");
                modifiedDisguises++;
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
        }

        if (noPermission > 0) {
            sender.sendMessage(LibsMsg.DMODRADIUS_NOPERM.get(noPermission));
        }

        if (modifiedDisguises > 0) {
            sender.sendMessage(LibsMsg.DMODRADIUS.get(modifiedDisguises));
        } else {
            sender.sendMessage(LibsMsg.DMODRADIUS_NOENTS.get());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (args.length == 0) {
            for (DisguiseType type : DisguiseType.values()) {
                if (type.getEntityType() == null) {
                    continue;
                }

                tabs.add(type.toReadable().replaceAll(" ", "_"));
            }

            return filterTabs(tabs, origArgs);
        }

        int starting = 0;

        if (!isInteger(args[0])) {
            for (DisguiseType t : DisguiseType.values()) {
                if (t.getEntityType() == null) {
                    continue;
                }

                if (t.toReadable().replaceAll(" ", "").equalsIgnoreCase(args[0].replaceAll("_", ""))) {
                    starting = 2;
                    break;
                }
            }

            // Not a valid radius
            if (starting == 1 || args.length == 1 || !isInteger(args[1]))
                return filterTabs(tabs, origArgs);
        }

        if (args.length <= starting || !isInteger(args[starting])) {
            return filterTabs(tabs, origArgs);
        }

        int radius = Integer.parseInt(args[starting]);

        if (radius > maxRadius) {
            sender.sendMessage(LibsMsg.LIMITED_RADIUS.get(maxRadius));
            radius = maxRadius;
        }

        starting++;

        ArrayList<String> usedOptions = new ArrayList<>();

        for (Entity entity : getNearbyEntities(sender, radius)) {
            Disguise disguise = DisguiseAPI.getDisguise(entity);

            if (disguise == null)
                continue;

            DisguiseType disguiseType = disguise.getType();

            for (Method method : ParamInfoManager.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                for (String arg : args) {
                    if (!method.getName().equalsIgnoreCase(arg) || usedOptions.contains(arg))
                        continue;

                    usedOptions.add(arg);
                }
            }

            DisguisePerm perm = new DisguisePerm(disguiseType);

            if (perms.isAllowedDisguise(perm, usedOptions)) {
                tabs.addAll(getTabDisguiseSubOptions(sender, perms, perm, args, starting, getCurrentArg(args)));
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

        sender.sendMessage(LibsMsg.DMODRADIUS_HELP1.get(maxRadius));
        sender.sendMessage(LibsMsg.DMODIFY_HELP3
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));

        sender.sendMessage(LibsMsg.DMODRADIUS_HELP2.get());
        sender.sendMessage(LibsMsg.DMODRADIUS_HELP3.get());
    }
}
