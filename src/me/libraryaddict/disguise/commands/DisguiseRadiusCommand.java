package me.libraryaddict.disguise.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.ClassGetter;
import me.libraryaddict.disguise.utilities.DisguiseParser;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers;
import me.libraryaddict.disguise.utilities.ReflectionFlagWatchers.ParamInfo;

public class DisguiseRadiusCommand extends DisguiseBaseCommand implements TabCompleter {
    private int maxRadius = 30;
    private ArrayList<Class<? extends Entity>> validClasses = new ArrayList<>();

    public DisguiseRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;
        for (Class c : ClassGetter.getClassesForPackage("org.bukkit.entity")) {
            if (c != Entity.class && Entity.class.isAssignableFrom(c) && c.getAnnotation(Deprecated.class) == null) {
                validClasses.add(c);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(ChatColor.RED + "You may not use this command from the console!");
            return true;
        }

        HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map = getPermissions(sender);

        if (map.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
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

            sender.sendMessage(ChatColor.DARK_GREEN + "EntityTypes usable are: " + ChatColor.GREEN
                    + StringUtils.join(classes, ChatColor.DARK_GREEN + ", " + ChatColor.GREEN) + ChatColor.DARK_GREEN + ".");
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
                    sender.sendMessage(ChatColor.RED + "Unrecognised EntityType " + args[0]);
                    return true;
                }
            }
        }

        if (args.length == starting + 1) {
            sender.sendMessage(ChatColor.RED + "You need to supply a disguise as well as the radius"
                    + (starting != 0 ? " and EntityType" : ""));
            return true;
        }
        else if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "You need to supply a radius as well as the disguise");
            return true;
        }

        if (!isNumeric(args[starting])) {
            sender.sendMessage(ChatColor.RED + args[starting] + " is not a number");
            return true;
        }

        int radius = Integer.parseInt(args[starting]);

        if (radius > maxRadius) {
            sender.sendMessage(ChatColor.RED + "Limited radius to " + maxRadius + "! Don't want to make too much lag right?");
            radius = maxRadius;
        }

        String[] newArgs = new String[args.length - (starting + 1)];
        System.arraycopy(args, starting + 1, newArgs, 0, newArgs.length);
        Disguise disguise;

        if (newArgs.length == 0) {
            sendCommandUsage(sender, map);
            return true;
        }

        try {
            disguise = DisguiseParser.parseDisguise(sender, getPermNode(), newArgs, map);
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

        // Time to use it!
        int disguisedEntitys = 0;
        int miscDisguises = 0;

        for (Entity entity : ((Player) sender).getNearbyEntities(radius, radius, radius)) {
            if (entity == sender) {
                continue;
            }

            if (type != null ? entity.getType() == type : entityClass.isAssignableFrom(entity.getClass())) {
                if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled()
                        && entity instanceof LivingEntity) {
                    miscDisguises++;
                    continue;
                }

                disguise = disguise.clone();

                if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
                    if (disguise.getWatcher() instanceof LivingWatcher) {
                        disguise.getWatcher().setCustomName(((Player) entity).getDisplayName());
                        if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                            disguise.getWatcher().setCustomNameVisible(true);
                        }
                    }
                }

                DisguiseAPI.disguiseToAll(entity, disguise);

                if (disguise.isDisguiseInUse()) {
                    disguisedEntitys++;
                }
            }
        }

        if (disguisedEntitys > 0) {
            sender.sendMessage(ChatColor.RED + "Successfully disguised " + disguisedEntitys + " entities!");
        }
        else {
            sender.sendMessage(ChatColor.RED + "Couldn't find any entities to disguise!");
        }

        if (miscDisguises > 0) {
            sender.sendMessage(ChatColor.RED + "Failed to disguise " + miscDisguises
                    + " entities because the option to disguise a living entity as a non-living has been disabled in the config");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<String>();
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

        if (args.length == starting) {
            for (String type : getAllowedDisguises(perms)) {
                tabs.add(type);
            }
        }
        else {

            DisguisePerm disguiseType = DisguiseParser.getDisguisePerm(args[starting]);

            if (disguiseType == null)
                return filterTabs(tabs, origArgs);

            if (args.length == 1 + starting && disguiseType.getType() == DisguiseType.PLAYER) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabs.add(player.getName());
                }
            }
            else {
                ArrayList<String> usedOptions = new ArrayList<String>();

                for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                    for (int i = disguiseType.getType() == DisguiseType.PLAYER ? starting + 2
                            : starting + 1; i < args.length; i++) {
                        String arg = args[i];

                        if (!method.getName().equalsIgnoreCase(arg))
                            continue;

                        usedOptions.add(arg);
                    }
                }

                if (passesCheck(sender, perms.get(disguiseType), usedOptions)) {
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
                            }
                            else {
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
                        for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
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
    protected void sendCommandUsage(CommandSender sender, HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(map);

        sender.sendMessage(ChatColor.DARK_GREEN + "Disguise all entities in a radius! Caps at " + maxRadius + " blocks!");
        sender.sendMessage(ChatColor.DARK_GREEN + "You can use the disguises: " + ChatColor.GREEN
                + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN));

        String optional = ChatColor.DARK_GREEN + "(" + ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")";

        if (allowedDisguises.contains("player")) {
            sender.sendMessage((ChatColor.DARK_GREEN + "/disguiseradius <EntityType" + optional + "> <Radius> player <Name>")
                    .replace("<", "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">"));
        }

        sender.sendMessage((ChatColor.DARK_GREEN + "/disguiseradius <EntityType" + optional + "> <Radius> <DisguiseType> <Baby"
                + optional + ">").replace("<", "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">"));

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            sender.sendMessage((ChatColor.DARK_GREEN + "/disguiseradius <EntityType" + optional
                    + "> <Radius> <Dropped_Item/Falling_Block> <Id> <Durability" + optional + ">")
                            .replace("<", "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">"));
        }

        sender.sendMessage(
                ChatColor.DARK_GREEN + "See the EntityType's usable by " + ChatColor.GREEN + "/disguiseradius EntityTypes");
    }

}
