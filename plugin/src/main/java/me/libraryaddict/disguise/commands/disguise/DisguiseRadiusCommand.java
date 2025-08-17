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
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
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
    private final ArrayList<Class<? extends Entity>> validClasses = new ArrayList<>();

    public DisguiseRadiusCommand() {
        for (EntityType type : ReflectionManager.enumValues(EntityType.class)) {
            Class c = type.getEntityClass();

            while (c != null && Entity.class.isAssignableFrom(c) && !validClasses.contains(c)) {
                validClasses.add(c);

                c = c.getSuperclass();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sendIfNotPremium(sender)) {
            return true;
        }

        if (sender.getName().equals("CONSOLE")) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        DisguisePermissions permissions = getPermissions(sender);

        if (!permissions.hasPermissions()) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        if (hasHitRateLimit(sender)) {
            return true;
        }

        if (args[0].equalsIgnoreCase(TranslateType.DISGUISES.get("EntityType")) ||
            args[0].equalsIgnoreCase(TranslateType.DISGUISES.get("EntityType") + "s")) {
            ArrayList<String> classes = new ArrayList<>();

            for (Class c : validClasses) {
                classes.add(TranslateType.DISGUISES.get(c.getSimpleName()));
            }

            Collections.sort(classes);

            LibsMsg.DRADIUS_ENTITIES.send(sender, StringUtils.join(classes, LibsMsg.DRADIUS_JOINER.getRaw()));
            return true;
        }

        Class entityClass = Entity.class;
        EntityType type = null;
        int starting = 0;

        if (isNotInteger(args[0])) {
            for (Class c : validClasses) {
                if (TranslateType.DISGUISES.get(c.getSimpleName()).equalsIgnoreCase(args[0])) {
                    entityClass = c;
                    starting = 1;
                    break;
                }
            }

            if (starting == 0) {
                try {
                    type = ReflectionManager.fromEnum(EntityType.class, args[0]);
                } catch (Exception ignored) {
                }

                if (type == null) {
                    LibsMsg.UNRECOGNIZED_DISGUISE_TYPE.send(sender, args[0]);
                    return true;
                }
            }
        }

        if (args.length == starting + 1) {
            if (starting == 0) {
                LibsMsg.DRADIUS_NEEDOPTIONS.send(sender);
            } else {
                LibsMsg.DRADIUS_NEEDOPTIONS_ENTITY.send(sender);
            }

            return true;
        } else if (args.length < 2) {
            LibsMsg.DRADIUS_NEEDOPTIONS.send(sender);
            return true;
        }

        if (isNotInteger(args[starting])) {
            LibsMsg.NOT_NUMBER.send(sender, args[starting]);
            return true;
        }

        int radius = Integer.parseInt(args[starting]);

        if (radius > DisguiseConfig.getDisguiseRadiusMax()) {
            LibsMsg.LIMITED_RADIUS.send(sender, DisguiseConfig.getDisguiseRadiusMax());
            radius = DisguiseConfig.getDisguiseRadiusMax();
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

                if (testDisguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled() && entity instanceof LivingEntity) {
                    miscDisguises++;
                    continue;
                }

                Disguise disguise = DisguiseParser.parseDisguise(sender, entity, getPermNode(), disguiseArgs, permissions);

                if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise() &&
                    !entity.hasPermission("libsdisguises.hidename")) {
                    if (disguise.getWatcher() instanceof LivingWatcher) {
                        disguise.getWatcher().setCustomName(getDisplayName(disguise, entity));

                        if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                            disguise.getWatcher().setCustomNameVisible(true);
                        }
                    }
                }

                disguise.setEntity(entity);

                if (!setViewDisguise(args)) {
                    // They prefer to have the opposite of whatever the view disguises option is
                    if (DisguiseAPI.hasSelfDisguisePreference(disguise.getEntity()) &&
                        disguise.isSelfDisguiseVisible() == DisguiseConfig.isViewSelfDisguisesDefault()) {
                        disguise.setViewSelfDisguise(!disguise.isSelfDisguiseVisible());
                    }
                }

                disguise.startDisguise(sender);

                if (disguise.isDisguiseInUse()) {
                    disguisedEntitys++;
                }
            }

            if (disguisedEntitys > 0) {
                LibsMsg.DISRADIUS.send(sender, disguisedEntitys);
            } else {
                LibsMsg.DISRADIUS_FAIL.send(sender);
            }

            if (miscDisguises > 0) {
                LibsMsg.DRADIUS_MISCDISG.send(sender, miscDisguises);
            }
        } catch (DisguiseParseException ex) {
            ex.send(sender);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private boolean setViewDisguise(String[] strings) {
        for (String string : strings) {
            if (!string.equalsIgnoreCase("setSelfDisguiseVisible")) {
                continue;
            }

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

        if (isNotInteger(args[0])) {
            for (Class c : validClasses) {
                if (!TranslateType.DISGUISES.get(c.getSimpleName()).equalsIgnoreCase(args[0])) {
                    continue;
                }

                starting = 2;
                break;
            }

            // Not a valid radius
            if (starting == 1 || args.length == 1 || isNotInteger(args[1])) {
                return filterTabs(tabs, origArgs);
            }
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

        if (allowedDisguises.isEmpty()) {
            LibsMsg.NO_PERM.send(sender);
            return;
        }

        LibsMsg.DRADIUS_HELP1.send(sender, DisguiseConfig.getDisguiseRadiusMax());
        LibsMsg.CAN_USE_DISGS.send(sender, StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));

        if (allowedDisguises.stream().anyMatch(disguise -> disguise.equalsIgnoreCase("player"))) {
            LibsMsg.DRADIUS_HELP3.send(sender);
        }

        LibsMsg.DRADIUS_HELP4.send(sender);

        if (allowedDisguises.stream()
            .anyMatch(disguise -> disguise.equalsIgnoreCase("dropped_item") || disguise.equalsIgnoreCase("falling_block"))) {
            LibsMsg.DRADIUS_HELP5.send(sender);
        }

        LibsMsg.DRADIUS_HELP6.send(sender);
    }
}
