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
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DisguiseCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isNotPremium(sender)) {
            return true;
        }

        if (!(sender instanceof Entity)) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_CONSOLE);
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, getPermissions(sender));
            return true;
        }

        Disguise disguise;

        try {
            disguise = DisguiseParser.parseDisguise(sender, (Entity) sender, getPermNode(),
                    DisguiseUtilities.split(StringUtils.join(args, " ")), getPermissions(sender));
        }
        catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                DisguiseUtilities.sendMessage(sender, ex.getMessage());
            }

            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }

        if (DisguiseConfig.isNameOfPlayerShownAboveDisguise() && !sender.hasPermission("libsdisguises.hidename")) {
            if (disguise.getWatcher() instanceof LivingWatcher) {
                disguise.getWatcher().setCustomName(getDisplayName(sender));

                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                    disguise.getWatcher().setCustomNameVisible(true);
                }
            }
        }

        disguise.setEntity((Player) sender);

        if (!setViewDisguise(args)) {
            // They prefer to have the opposite of whatever the view disguises option is
            if (DisguiseAPI.hasSelfDisguisePreference(disguise.getEntity()) &&
                    disguise.isSelfDisguiseVisible() == DisguiseConfig.isViewDisguises())
                disguise.setViewSelfDisguise(!disguise.isSelfDisguiseVisible());
        }

        if (!DisguiseAPI.isActionBarShown(disguise.getEntity())) {
            disguise.setNotifyBar(DisguiseConfig.NotifyBar.NONE);
        }

        disguise.startDisguise();

        if (disguise.isDisguiseInUse()) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.DISGUISED, disguise.getDisguiseName());
        } else {
            DisguiseUtilities.sendMessage(sender, LibsMsg.FAILED_DISGIUSE, disguise.getDisguiseName());
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

        return filterTabs(getTabDisguiseTypes(sender, perms, args, 0, getCurrentArg(origArgs)), origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        if (allowedDisguises.isEmpty()) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_PERM);
            return;
        }

        DisguiseUtilities.sendMessage(sender, LibsMsg.DISG_HELP1);
        DisguiseUtilities.sendMessage(sender, LibsMsg.CAN_USE_DISGS,
                StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));

        if (allowedDisguises.contains("player")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.DISG_HELP2);
        }

        DisguiseUtilities.sendMessage(sender, LibsMsg.DISG_HELP3);

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.DISG_HELP4);
        }
    }
}
