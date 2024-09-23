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
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DisguiseCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isNotPremium(sender)) {
            return true;
        }

        if (!(sender instanceof Entity)) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, getPermissions(sender));
            return true;
        }

        if (hasHitRateLimit(sender)) {
            return true;
        }

        Disguise disguise;

        try {
            disguise =
                DisguiseParser.parseDisguise(sender, (Entity) sender, getPermNode(), DisguiseUtilities.split(StringUtils.join(args, " ")),
                    getPermissions(sender));
        } catch (DisguiseParseException ex) {
            ex.send(sender);

            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return true;
        }

        if (DisguiseConfig.isNameOfPlayerShownAboveDisguise() && !sender.hasPermission("libsdisguises.hidename")) {
            if (disguise.getWatcher() instanceof LivingWatcher) {
                disguise.getWatcher().setCustomName(getDisplayName(disguise, sender));

                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                    disguise.getWatcher().setCustomNameVisible(true);
                }
            }
        }

        disguise.setEntity((Player) sender);

        if (!setViewDisguise(args)) {
            // They prefer to have the opposite of whatever the view disguises option is
            if (DisguiseAPI.hasSelfDisguisePreference(disguise.getEntity()) &&
                disguise.isSelfDisguiseVisible() == DisguiseConfig.isViewSelfDisguisesDefault()) {
                disguise.setViewSelfDisguise(!disguise.isSelfDisguiseVisible());
            }
        }

        if (!sender.isOp() && LibsPremium.isBisectHosted() &&
            !Bukkit.getIp().matches("((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!$)|$)){4}")) {
            disguise.setExpires(
                DisguiseConfig.isDynamicExpiry() ? 20 * 60 * 10 : System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
        }

        disguise.startDisguise(sender);

        if (disguise.isDisguiseInUse()) {
            LibsMsg.DISGUISED.send(sender, disguise.getDisguiseName());
        } else {
            LibsMsg.FAILED_DISGIUSE.send(sender, disguise.getDisguiseName());
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

        return filterTabs(getTabDisguiseTypes(sender, perms, args, 0, getCurrentArg(origArgs)), origArgs);
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

        LibsMsg.DISG_HELP1.send(sender);
        LibsMsg.CAN_USE_DISGS.send(sender, StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));

        if (allowedDisguises.stream().anyMatch(disguise -> disguise.equalsIgnoreCase("player"))) {
            LibsMsg.DISG_HELP2.send(sender);
        }

        LibsMsg.DISG_HELP3.send(sender);

        if (allowedDisguises.stream()
            .anyMatch(disguise -> disguise.equalsIgnoreCase("dropped_item") || disguise.equalsIgnoreCase("falling_block"))) {
            LibsMsg.DISG_HELP4.send(sender);
        }
    }
}
