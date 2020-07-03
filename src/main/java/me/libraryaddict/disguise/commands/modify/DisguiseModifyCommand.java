package me.libraryaddict.disguise.commands.modify;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
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

public class DisguiseModifyCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Entity)) {
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

        Disguise disguise = DisguiseAPI.getDisguise((Player) sender, (Entity) sender);

        if (disguise == null) {
            LibsMsg.NOT_DISGUISED.send(sender);
            return true;
        }

        DisguisePerm disguisePerm = new DisguisePerm(disguise.getType());

        if (!permissions.isAllowedDisguise(disguisePerm)) {
            LibsMsg.DMODIFY_NO_PERM.send(sender);
            return true;
        }

        String[] options = DisguiseUtilities.split(StringUtils.join(args, " "));

        options = DisguiseParser.parsePlaceholders(options, sender, sender);

        try {
            DisguiseParser.callMethods(sender, disguise, permissions, disguisePerm, new ArrayList<>(), options,
                    "DisguiseModify");
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

        LibsMsg.DMODIFY_MODIFIED.send(sender);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        if (!(sender instanceof Player))
            return new ArrayList<>();

        Disguise disguise = DisguiseAPI.getDisguise((Player) sender, (Entity) sender);

        if (disguise == null)
            return new ArrayList<>();

        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        DisguisePerm disguiseType = new DisguisePerm(disguise.getType());

        List<String> tabs = getTabDisguiseOptions(sender, perms, disguiseType, args, 0, getCurrentArg(origArgs));

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        LibsMsg.DMODIFY_HELP3.send(sender);
        LibsMsg.DMODIFY_HELP3.send(sender);
        LibsMsg.DMODIFY_HELP3.send(sender,
                StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));
    }
}
