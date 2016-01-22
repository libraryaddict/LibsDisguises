package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.BaseDisguiseCommand;
import me.libraryaddict.disguise.utilities.ClassGetter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DisguiseRadiusCommand extends BaseDisguiseCommand {

    private int maxRadius = 30;
    private ArrayList<Class> validClasses = new ArrayList<>();

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
        HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map = getPermissions(sender);
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
                } catch (Exception ex) {
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
        try {
            disguise = parseDisguise(sender, newArgs, map);
        } catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            return true;
        } // Time to use it!
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
        } else {
            sender.sendMessage(ChatColor.RED + "Couldn't find any entities to disguise!");
        }
        if (miscDisguises > 0) {
            sender.sendMessage(ChatColor.RED + "Failed to disguise " + miscDisguises
                    + " entities because the option to disguise a living entity as a non-living has been disabled in the config");
        }
        return true;
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, HashMap<DisguiseType, HashMap<ArrayList<String>, Boolean>> map) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(map);
        sender.sendMessage(ChatColor.DARK_GREEN + "Disguise all entities in a radius! Caps at 30 blocks!");
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
                    + "> <Radius> <Dropped_Item/Falling_Block> <Id> <Durability" + optional + ">").replace("<",
                            "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">"));
        }
        sender.sendMessage(ChatColor.DARK_GREEN + "See the EntityType's usable by " + ChatColor.GREEN
                + "/disguiseradius EntityTypes");
    }

}
