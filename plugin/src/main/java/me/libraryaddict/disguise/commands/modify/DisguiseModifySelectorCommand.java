package me.libraryaddict.disguise.commands.modify;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DisguiseModifySelectorCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!NmsVersion.v1_13.isSupported()) {
            sender.sendMessage(ChatColor.RED + "Entity selectors require 1.13+, this server is running an older version of Minecraft.");
            return true;
        }

        DisguisePermissions permissions = getPermissions(sender);

        if (!permissions.hasPermissions()) {
            LibsMsg.NO_PERM.send(sender);
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

        String[] disguiseArgs = DisguiseUtilities.split(StringUtils.join(args, " "));

        if (disguiseArgs.length < 2) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        // Time to use it!
        int modifiedDisguises = 0;
        int noPermission = 0;

        List<Entity> entities;

        try {
            entities = Bukkit.selectEntities(sender, disguiseArgs[0]);
        } catch (IllegalArgumentException ex) {
            LibsMsg.DISGUISE_ENTITY_SELECTOR_INVALID.send(sender, disguiseArgs[0]);
            return true;
        }

        for (Entity entity : entities) {
            if (sender instanceof Player && entity instanceof Player && !((Player) sender).canSee((Player) entity)) {
                continue;
            }

            Disguise disguise;

            if (sender instanceof Player) {
                disguise = DisguiseAPI.getDisguise((Player) sender, entity);
            } else {
                disguise = DisguiseAPI.getDisguise(entity);
            }

            if (disguise == null) {
                continue;
            }

            DisguisePerm disguisePerm = new DisguisePerm(disguise.getType());

            if (!permissions.isAllowedDisguise(disguisePerm)) {
                noPermission++;
                continue;
            }

            String[] tempArgs = Arrays.copyOfRange(disguiseArgs, 1, disguiseArgs.length);
            tempArgs = DisguiseParser.parsePlaceholders(tempArgs, sender, entity);

            try {
                DisguiseParser.callMethods(sender, disguise, permissions, disguisePerm, new ArrayList<>(), tempArgs,
                    "DisguiseModifyRadius");
                modifiedDisguises++;
            } catch (DisguiseParseException ex) {
                ex.send(sender);

                return true;
            } catch (Throwable ex) {
                ex.printStackTrace();
                return true;
            }
        }

        if (noPermission > 0) {
            LibsMsg.DMODRADIUS_NOPERM.send(sender, noPermission);
        }

        if (modifiedDisguises > 0) {
            LibsMsg.DMODRADIUS.send(sender, modifiedDisguises);
        } else {
            LibsMsg.DMODRADIUS_NOENTS.send(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (args.length <= 1) {
            return tabs;
        }

        for (DisguisePerm perm : perms.getAllowed()) {
            tabs.addAll(getTabDisguiseOptions(sender, perms, perm, args, 1, getCurrentArg(origArgs)));
        }

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        LibsMsg.DMODSELECTOR_HELP1.send(sender);
        LibsMsg.DMODSELECTOR_HELP2.send(sender);
        LibsMsg.DMODSELECTOR_HELP3.send(sender, StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));
    }
}
