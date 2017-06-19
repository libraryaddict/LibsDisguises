package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.*;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers.ParamInfo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class DisguiseModifyRadiusCommand extends DisguiseBaseCommand implements TabCompleter {
    private int maxRadius = 30;
    private ArrayList<Class<? extends Entity>> validClasses = new ArrayList<>();

    public DisguiseModifyRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;
        for (Class c : ClassGetter.getClassesForPackage("org.bukkit.entity")) {
            if (c != Entity.class && Entity.class.isAssignableFrom(c) && c.getAnnotation(Deprecated.class) == null) {
                validClasses.add(c);
            }
        }
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

        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map = getPermissions(sender);

        if (map.isEmpty()) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, map);
            return true;
        }

        if (args[0].equalsIgnoreCase("entitytype") || args[0].equalsIgnoreCase("entitytypes")) {
            ArrayList<String> classes = new ArrayList<>();

            for (Class c : validClasses) {
                classes.add(c.getSimpleName());
            }

            Collections.sort(classes);

            sender.sendMessage(LibsMsg.DMODRADIUS_USABLE
                    .get(ChatColor.GREEN + StringUtils.join(classes, ChatColor.DARK_GREEN + ", " + ChatColor.GREEN)));
            return true;
        }

        Class entityClass = Entity.class;
        EntityType type = null;
        int starting = 0;

        if (!isNumeric(args[0])) {
            for (Class c : validClasses) {
                if (c.getSimpleName().equalsIgnoreCase(args[0])) {
                    entityClass = c;
                    starting = 1;
                    break;
                }
            }

            if (starting == 0) {
                try {
                    type = EntityType.valueOf(args[0].toUpperCase());
                }
                catch (Exception ex) {
                }

                if (type == null) {
                    sender.sendMessage(LibsMsg.DMODRADIUS_UNRECOGNIZED.get(args[0]));
                    return true;
                }
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

        if (!isNumeric(args[starting])) {
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
            sendCommandUsage(sender, map);
            return true;
        }

        // Time to use it!
        int modifiedDisguises = 0;
        int noPermission = 0;

        for (Entity entity : getNearbyEntities(sender, radius)) {
            if (entity == sender) {
                continue;
            }

            if (type != null ? entity.getType() != type : !entityClass.isAssignableFrom(entity.getClass())) {
                continue;
            }

            Disguise disguise;

            if (sender instanceof Player)
                disguise = DisguiseAPI.getDisguise((Player) sender, entity);
            else
                disguise = DisguiseAPI.getDisguise(entity);

            if (!map.containsKey(new DisguisePerm(disguise.getType()))) {
                noPermission++;
                continue;
            }

            try {
                DisguiseParser.callMethods(sender, disguise, map.get(new DisguisePerm(disguise.getType())),
                        new ArrayList<String>(), newArgs);
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
        String[] args = getArgs(origArgs);

        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> perms = getPermissions(sender);

        if (args.length == 0) {
            for (Class<? extends Entity> entityClass : validClasses) {
                tabs.add(entityClass.getSimpleName());
            }

            return filterTabs(tabs, origArgs);
        }

        int starting = 1;

        if (!isNumeric(args[0])) {
            for (Class c : validClasses) {
                if (!c.getSimpleName().equalsIgnoreCase(args[0]))
                    continue;

                starting = 2;
                break;
            }

            // Not a valid radius
            if (starting == 1 || args.length == 1 || !isNumeric(args[1]))
                return filterTabs(tabs, origArgs);
        }

        int radius = Integer.parseInt(args[starting]);

        if (radius > maxRadius) {
            sender.sendMessage(LibsMsg.LIMITED_RADIUS.get(maxRadius));
            radius = maxRadius;
        }

        ArrayList<String> usedOptions = new ArrayList<>();

        for (Entity entity : getNearbyEntities(sender, radius)) {
            Disguise disguise = DisguiseAPI.getDisguise(entity);

            if (disguise == null)
                continue;

            DisguiseType disguiseType = disguise.getType();

            for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                for (String arg : args) {
                    if (!method.getName().equalsIgnoreCase(arg))
                        continue;

                    usedOptions.add(arg);
                }
            }

            if (passesCheck(sender, perms.get(new DisguisePerm(disguiseType)), usedOptions)) {
                boolean addMethods = true;

                if (args.length > 1 + starting) {
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

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender,
            HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(map);

        sender.sendMessage(LibsMsg.DMODRADIUS_HELP1.get(maxRadius));
        sender.sendMessage(LibsMsg.DMODRADIUS_HELP2
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));

        String optional = ChatColor.DARK_GREEN + "(" + ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")";

        if (allowedDisguises.contains("player")) {
            sender.sendMessage(LibsMsg.DMODRADIUS_HELP3.get());
        }

        sender.sendMessage(LibsMsg.DMODRADIUS_HELP4.get());

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            sender.sendMessage(LibsMsg.DMODRADIUS_HELP5.get());
        }

        sender.sendMessage(LibsMsg.DMODRADIUS_HELP6.get());
    }
}
