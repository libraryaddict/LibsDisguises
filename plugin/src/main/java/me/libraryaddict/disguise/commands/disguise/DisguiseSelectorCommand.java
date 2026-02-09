package me.libraryaddict.disguise.commands.disguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisguiseSelectorCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!NmsVersion.v1_13.isSupported()) {
            sender.sendMessage(ChatColor.RED + "Entity selectors require 1.13+, this server is running an older version of Minecraft.");
            return true;
        }

        if (!LibsPremium.isPremium()) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, entity selector commands are limited to premium versions only!");
            return true;
        }

        if (sendIfNotPremium(sender)) {
            return true;
        }

        DisguisePermissions permissions = getPermissions(sender);

        if (!permissions.hasPermissions()) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (sender instanceof Player) {
            DisguiseUtilities.setCommandsUsed();
        } else {
            DisguiseUtilities.resetPluginTimer();
        }

        if (args.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        if (hasHitRateLimit(sender)) {
            return true;
        }

        String[] newArgs = DisguiseUtilities.split(StringUtils.join(args, " "));

        if (newArgs.length < 2) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        String[] disguiseArgs = Arrays.copyOfRange(newArgs, 1, newArgs.length);

        try {
            Disguise testDisguise = DisguiseParser.parseTestDisguise(sender, getPermNode(), disguiseArgs, permissions);

            // Time to use it!
            int disguisedEntitys = 0;
            int miscDisguises = 0;

            List<Entity> entities = Bukkit.selectEntities(sender, newArgs[0]);

            for (Entity entity : entities) {
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
                LibsMsg.DISGUISE_ENTITY_SELECTOR.send(sender, disguisedEntitys);
            } else {
                LibsMsg.DISGUISE_ENTITY_SELECTOR_NO_ENTITIES.send(sender);
            }

            if (miscDisguises > 0) {
                LibsMsg.DRADIUS_MISCDISG.send(sender, miscDisguises);
            }
        } catch (DisguiseParseException ex) {
            ex.send(sender);
        } catch (IllegalArgumentException ex) {
            LibsMsg.DISGUISE_ENTITY_SELECTOR_INVALID.send(sender, newArgs[0]);
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

        // If no args or the entity selector is unmatched quote
        if (args.length == 0 || (args[0].startsWith("\"") && Arrays.stream(args).noneMatch(s -> s.endsWith("\"")))) {
            return tabs;
        }

        tabs.addAll(getTabDisguiseTypes(sender, perms, args, 1, getCurrentArg(origArgs)));

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

        LibsMsg.DSELECTOR_HELP1.send(sender);
        LibsMsg.DSELECTOR_HELP2.send(sender);
        LibsMsg.CAN_USE_DISGS.send(sender, StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));
    }
}
