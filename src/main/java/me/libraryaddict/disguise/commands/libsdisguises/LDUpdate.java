package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.updates.UpdateChecker;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDUpdate implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        // Update by download
        // Update check
        // Update to latest dev build
        return Arrays.asList("update", "update?", "update!");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.update";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UpdateChecker checker = LibsDisguises.getInstance().getUpdateChecker();

        if (checker.isDownloading()) {
            sender.sendMessage(LibsMsg.UPDATE_IN_PROGRESS.get());
            return;
        }

        boolean check = args[0].endsWith("?");
        boolean force = args[0].endsWith("!");

        if (!check && !force && checker.getUpdate() != null) {
            if (checker.getUpdate().getVersion().equals(checker.getUpdate().isReleaseBuild() ?
                    LibsDisguises.getInstance().getDescription().getDescription() :
                    LibsDisguises.getInstance().getBuildNumber())) {
                sender.sendMessage(LibsMsg.UPDATE_ON_LATEST.get());
                return;
            }

            if (checker.getLastDownload() != null && checker.getUpdate().getVersion()
                    .equals(checker.isUsingReleaseBuilds() ? checker.getLastDownload().getVersion() :
                            checker.getLastDownload().getBuildNumber())) {
                sender.sendMessage(LibsMsg.UPDATE_ALREADY_DOWNLOADED.get());
                return;
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                LibsMsg updateResult = null;

                if (check || checker.getUpdate() == null || force) {
                    updateResult = checker.doUpdateCheck();
                }

                if (checker.getUpdate() == null) {
                    sender.sendMessage(LibsMsg.UPDATE_FAILED.get());
                    return;
                }

                if (!checker.isUpdateReady()) {
                    sender.sendMessage(LibsMsg.UPDATE_ON_LATEST.get());
                    return;
                }

                if (check) {
                    if (updateResult != null) {
                        sender.sendMessage(updateResult.get());
                    } else {
                        for (String msg : checker.getUpdateMessage()) {
                            sender.sendMessage(msg);
                        }
                    }

                    return;
                }

                PluginInformation result = checker.doUpdate();

                if (result == null) {
                    sender.sendMessage(LibsMsg.UPDATE_FAILED.get());
                    return;
                }

                for (String msg : checker.getUpdateMessage()) {
                    sender.sendMessage(msg);
                }

                if (sender instanceof Player) {
                    for (String msg : checker.getUpdateMessage()) {
                        DisguiseUtilities.getLogger().info(msg);
                    }
                }
            }
        }.runTaskAsynchronously(LibsDisguises.getInstance());
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_UPDATE;
    }
}
