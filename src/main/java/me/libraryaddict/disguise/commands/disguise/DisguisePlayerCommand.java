package me.libraryaddict.disguise.commands.disguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
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
import java.util.UUID;

public class DisguisePlayerCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isNotPremium(sender)) {
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

        if (args.length == 1) {
            LibsMsg.DPLAYER_SUPPLY.send(sender);
            return true;
        }

        Entity entityTarget = Bukkit.getPlayer(args[0]);

        if (entityTarget == null) {
            if (args[0].contains("-")) {
                try {
                    entityTarget = Bukkit.getEntity(UUID.fromString(args[0]));
                }
                catch (Exception ignored) {
                }
            }
        }

        if (entityTarget == null) {
            LibsMsg.CANNOT_FIND_PLAYER.send(sender, args[0]);
            return true;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        if (newArgs.length == 0) {
            sendCommandUsage(sender, permissions);
            return true;
        }

        Disguise disguise;

        try {
            disguise = DisguiseParser.parseDisguise(sender, entityTarget, getPermNode(),
                    DisguiseUtilities.split(StringUtils.join(newArgs, " ")), permissions);
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

        if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            LibsMsg.DISABLED_LIVING_TO_MISC.send(sender);
            return true;
        }

        if (DisguiseConfig.isNameOfPlayerShownAboveDisguise() &&
                !entityTarget.hasPermission("libsdisguises.hidename")) {
            if (disguise.getWatcher() instanceof LivingWatcher) {
                disguise.getWatcher().setCustomName(getDisplayName(entityTarget));

                if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                    disguise.getWatcher().setCustomNameVisible(true);
                }
            }
        }

        disguise.setEntity(entityTarget);

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
            LibsMsg.DISG_PLAYER_AS_DISG.send(sender,
                    entityTarget instanceof Player ? entityTarget.getName() :
                            DisguiseType.getType(entityTarget).toReadable(), disguise.getDisguiseName());
        } else {
            LibsMsg.DISG_PLAYER_AS_DISG_FAIL.send(sender,
                    entityTarget instanceof Player ? entityTarget.getName() :
                            DisguiseType.getType(entityTarget).toReadable(), disguise.getDisguiseName());
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                // If command user cannot see player online, don't tab-complete name
                if (sender instanceof Player && !((Player) sender).canSee(player)) {
                    continue;
                }

                tabs.add(player.getName());
            }
        } else {
            tabs.addAll(getTabDisguiseTypes(sender, perms, args, 1, getCurrentArg(origArgs)));
        }

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

        LibsMsg.D_HELP1.send(sender);
        LibsMsg.CAN_USE_DISGS.send(sender,
                StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));

        if (allowedDisguises.contains("player")) {
            LibsMsg.D_HELP3.send(sender);
        }

        LibsMsg.D_HELP4.send(sender);

        if (allowedDisguises.contains("dropped_item") || allowedDisguises.contains("falling_block")) {
            LibsMsg.D_HELP5.send(sender);
        }
    }
}
