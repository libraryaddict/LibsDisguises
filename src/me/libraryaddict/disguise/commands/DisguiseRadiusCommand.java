package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

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

        if (args[0].equalsIgnoreCase(TranslateType.DISGUISES.get("EntityType")) ||
                args[0].equalsIgnoreCase(TranslateType.DISGUISES.get("EntityType") + "s")) {
            ArrayList<String> classes = new ArrayList<>();

            for (Class c : validClasses) {
                classes.add(TranslateType.DISGUISES.get(c.getSimpleName()));
            }

            Collections.sort(classes);

            sender.sendMessage(LibsMsg.DRADIUS_ENTITIES
                    .get(ChatColor.GREEN + StringUtils.join(classes, ChatColor.DARK_GREEN + ", " + ChatColor.GREEN)));
            return true;
        }

        Class entityClass = Entity.class;
        EntityType type = null;
        int starting = 0;

        if (!isNumeric(args[0])) {
            for (Class c : validClasses) {
                if (TranslateType.DISGUISES.get(c.getSimpleName()).equalsIgnoreCase(args[0])) {
                    entityClass = c;
                    starting = 1;
                    break;
                }
            }

            if (starting == 0) {
                try {
                    type = EntityType.valueOf(args[0].toUpperCase());
                }
                catch (Exception ignored) {
                }

                if (type == null) {
                    sender.sendMessage(LibsMsg.DMODRADIUS_UNRECOGNIZED.get(args[0]));
                    return true;
                }
            }
        }

        if (args.length == starting + 1) {
            sender.sendMessage(
                    (starting == 0 ? LibsMsg.DRADIUS_NEEDOPTIONS : LibsMsg.DRADIUS_NEEDOPTIONS_ENTITY).get());
            return true;
        } else if (args.length < 2) {
            sender.sendMessage(LibsMsg.DRADIUS_NEEDOPTIONS.get());
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
        Disguise disguise;

        if (newArgs.length == 0) {
            sendCommandUsage(sender, map);
            return true;
        }

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

        // Time to use it!
        int disguisedEntitys = 0;
        int miscDisguises = 0;

        Location center;

        if (sender instanceof Player) {
            center = ((Player) sender).getLocation();
        } else {
            center = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5, 0, 0.5);
        }

        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity == sender) {
                continue;
            }

            if (type != null ? entity.getType() == type : entityClass.isAssignableFrom(entity.getClass())) {
                if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled() &&
                        entity instanceof LivingEntity) {
                    miscDisguises++;
                    continue;
                }

                disguise = disguise.clone();

                if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise()) {
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
                DisguiseAPI.disguiseEntity(entity, disguise);

                if (disguise.isDisguiseInUse()) {
                    disguisedEntitys++;
                }
            }
        }

        if (disguisedEntitys > 0) {
            sender.sendMessage(LibsMsg.DISRADIUS.get(disguisedEntitys));
        } else {
            sender.sendMessage(LibsMsg.DISRADIUS_FAIL.get());
        }

        if (miscDisguises > 0) {
            sender.sendMessage(LibsMsg.DRADIUS_MISCDISG.get(miscDisguises));
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
            for (Class<? extends Entity> entityClass : validClasses) {
                tabs.add(TranslateType.DISGUISES.get(entityClass.getSimpleName()));
            }

            return filterTabs(tabs, origArgs);
        }

        int starting = 1;

        if (!isNumeric(args[0])) {
            for (Class c : validClasses) {
                if (!TranslateType.DISGUISES.get(c.getSimpleName()).equalsIgnoreCase(args[0]))
                    continue;

                starting = 2;
                break;
            }

            // Not a valid radius
            if (starting == 1 || args.length == 1 || !isNumeric(args[1]))
                return filterTabs(tabs, origArgs);
        }

        if (args.length == starting) {
            tabs.addAll(getAllowedDisguises(perms));
        } else {

            DisguisePerm disguiseType = DisguiseParser.getDisguisePerm(args[starting]);

            if (disguiseType == null)
                return filterTabs(tabs, origArgs);

            if (args.length == 1 + starting && disguiseType.getType() == DisguiseType.PLAYER) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabs.add(player.getName());
                }
            } else {
                ArrayList<String> usedOptions = new ArrayList<>();

                for (Method method : ReflectionFlagWatchers.getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
                    for (int i = disguiseType.getType() == DisguiseType.PLAYER ? starting + 2 : starting + 1;
                         i < args.length; i++) {
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

        sender.sendMessage(LibsMsg.DRADIUS_HELP1.get(maxRadius));
        sender.sendMessage(LibsMsg.CAN_USE_DISGS
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));

        if (allowedDisguises.contains("player")) {
            sender.sendMessage(LibsMsg.DRADIUS_HELP3.get());
        }

        sender.sendMessage(LibsMsg.DRADIUS_HELP4.get());

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            sender.sendMessage(LibsMsg.DRADIUS_HELP5.get());
        }

        sender.sendMessage(LibsMsg.DRADIUS_HELP6.get());
    }
}
