package me.libraryaddict.disguise.commands.disguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisguiseRadiusCommand extends DisguiseBaseCommand implements TabCompleter {
    private int maxRadius = 30;
    private ArrayList<Class<? extends Entity>> validClasses = new ArrayList<>();

    public DisguiseRadiusCommand(int maxRadius) {
        this.maxRadius = maxRadius;

        for (EntityType type : EntityType.values()) {
            Class c = type.getEntityClass();

            while (c != null && Entity.class.isAssignableFrom(c) && !validClasses.contains(c)) {
                validClasses.add(c);

                c = c.getSuperclass();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isNotPremium(sender)) {
            return true;
        }

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

        if (!isInteger(args[0])) {
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

        String[] disguiseArgs = DisguiseUtilities.split(StringUtils.join(newArgs, " "));

        try {

            Disguise testDisguise = DisguiseParser.parseTestDisguise(sender, getPermNode(), disguiseArgs, permissions);

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

                if (type != null ? entity.getType() != type : !entityClass.isAssignableFrom(entity.getClass())) {
                    continue;
                }

                if (testDisguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled() &&
                        entity instanceof LivingEntity) {
                    miscDisguises++;
                    continue;
                }

                Disguise disguise = DisguiseParser
                        .parseDisguise(sender, entity, getPermNode(), disguiseArgs, permissions);

                if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise() &&
                        !entity.hasPermission("libsdisguises.hidename")) {
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
                    disguisedEntitys++;
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
        }
        catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private boolean setViewDisguise(String[] strings) {
        for (String string : strings) {
            if (!string.equalsIgnoreCase("setSelfDisguiseVisible"))
                continue;

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (args.length == 0) {
            for (Class<? extends Entity> entityClass : validClasses) {
                tabs.add(TranslateType.DISGUISES.get(entityClass.getSimpleName()));
            }

            return filterTabs(tabs, origArgs);
        }

        int starting = 1;

        if (!isInteger(args[0])) {
            for (Class c : validClasses) {
                if (!TranslateType.DISGUISES.get(c.getSimpleName()).equalsIgnoreCase(args[0]))
                    continue;

                starting = 2;
                break;
            }

            // Not a valid radius
            if (starting == 1 || args.length == 1 || !isInteger(args[1]))
                return filterTabs(tabs, origArgs);
        }

        tabs.addAll(getTabDisguiseTypes(sender, perms, args, starting, getCurrentArg(origArgs)));

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

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
