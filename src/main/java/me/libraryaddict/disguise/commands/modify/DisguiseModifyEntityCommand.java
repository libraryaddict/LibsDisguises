package me.libraryaddict.disguise.commands.modify;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.commands.interactions.DisguiseModifyInteraction;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DisguiseModifyEntityCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
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

        // TODO Validate if any disguises have this arg

        LibsDisguises.getInstance().getListener().addInteraction(sender.getName(),
                new DisguiseModifyInteraction(DisguiseUtilities.split(StringUtils.join(args, " "))),
                DisguiseConfig.getDisguiseEntityExpire());

        sender.sendMessage(LibsMsg.DMODIFYENT_CLICK.get(DisguiseConfig.getDisguiseEntityExpire()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        String[] args = getPreviousArgs(origArgs);

        DisguisePermissions perms = getPermissions(sender);

        if (!perms.hasPermissions()) {
            return new ArrayList<>();
        }

        List<String> tabs = new ArrayList<>();

        for (DisguisePerm perm : perms.getAllowed()) {
            tabs.addAll(getTabDisguiseOptions(sender, perms, perm, args, 0, getCurrentArg(origArgs)));
        }

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = getAllowedDisguises(permissions);

        sender.sendMessage(LibsMsg.DMODENT_HELP1.get());
        sender.sendMessage(LibsMsg.DMODIFY_HELP3
                .get(ChatColor.GREEN + StringUtils.join(allowedDisguises, ChatColor.RED + ", " + ChatColor.GREEN)));
    }
}
