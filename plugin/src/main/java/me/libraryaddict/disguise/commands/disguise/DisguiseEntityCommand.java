package me.libraryaddict.disguise.commands.disguise;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.commands.interactions.DisguiseEntityInteraction;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DisguiseEntityCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sendIfNotPremium(sender)) {
            return true;
        }

        if (!(sender instanceof Player)) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        if (!getPermissions(sender).hasPermissions()) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (args.length == 0) {
            sendCommandUsage(sender, getPermissions(sender));
            return true;
        }

        if (hasHitRateLimit(sender)) {
            return true;
        }

        String[] disguiseArgs = DisguiseUtilities.split(StringUtils.join(args, " "));
        Disguise testDisguise;

        try {
            testDisguise = DisguiseParser.parseTestDisguise(sender, getPermNode(), disguiseArgs, getPermissions(sender));
        } catch (DisguiseParseException ex) {
            ex.send(sender);

            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return true;
        }

        LibsDisguises.getInstance().getListener()
            .addInteraction(sender.getName(), new DisguiseEntityInteraction(disguiseArgs), DisguiseConfig.getDisguiseEntityExpire());

        LibsMsg.DISG_ENT_CLICK.send(sender, DisguiseConfig.getDisguiseEntityExpire(), testDisguise.getDisguiseName());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return tabs;
        }

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

        LibsMsg.DISG_ENT_HELP1.send(sender);
        LibsMsg.CAN_USE_DISGS.send(sender, StringUtils.join(allowedDisguises, LibsMsg.CAN_USE_DISGS_SEPERATOR.get()));

        if (allowedDisguises.stream().anyMatch(disguise -> disguise.equalsIgnoreCase("player"))) {
            LibsMsg.DISG_ENT_HELP3.send(sender);
        }

        LibsMsg.DISG_ENT_HELP4.send(sender);

        if (allowedDisguises.stream()
            .anyMatch(disguise -> disguise.equalsIgnoreCase("dropped_item") || disguise.equalsIgnoreCase("falling_block"))) {
            LibsMsg.DISG_ENT_HELP5.send(sender);
        }
    }
}
