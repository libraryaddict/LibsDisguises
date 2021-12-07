package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.updates.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Created by libraryaddict on 27/04/2020.
 */
public class LDChangelog implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("changelog");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public String getPermission() {
        return "libsdisguises.update";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UpdateChecker checker = LibsDisguises.getInstance().getUpdateChecker();

        if (checker.isDownloading()) {
            LibsMsg.UPDATE_IN_PROGRESS.send(sender);
            return;
        }

        if (checker.getUpdate() == null) {
            LibsMsg.UPDATE_REQUIRED.send(sender);
            return;
        }

        if (!checker.getUpdate().isReleaseBuild()) {
            sender.sendMessage(
                    ChatColor.GOLD + "You are on build " + (LibsDisguises.getInstance().isNumberedBuild() ? "#" : "") +
                            LibsDisguises.getInstance().getBuildNo());
        }

        for (String msg : checker.getUpdate().getChangelog()) {
            sender.sendMessage(ChatColor.GOLD + msg);
        }
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_CHANGELOG;
    }
}
