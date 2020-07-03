package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.updates.UpdateChecker;
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
        return Arrays.asList("update", "update dev", "update release", "update!");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.update";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UpdateChecker checker = LibsDisguises.getInstance().getUpdateChecker();

        if (checker.isDownloading()) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_IN_PROGRESS);
            return;
        }

        boolean releaseBuilds = checker.isUsingReleaseBuilds();
        boolean forceUpdate = args[0].endsWith("!");
        boolean forceCheck = args[0].endsWith("?") || args.length > 1 || forceUpdate;

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("dev")) {
                releaseBuilds = false;
            } else if (args[1].equalsIgnoreCase("release")) {
                releaseBuilds = true;
            } else {
                DisguiseUtilities.sendMessage(sender, LibsMsg.LIBS_UPDATE_UNKNOWN_BRANCH);
                return;
            }

            DisguiseConfig.setUsingReleaseBuilds(releaseBuilds);
        }

        if (checker.getUpdate() != null && checker.getUpdate().isReleaseBuild() == releaseBuilds && args.length <= 1 &&
                !forceCheck) {
            if (checker.isServerLatestVersion()) {
                DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_ON_LATEST);
                return;
            }

            if (checker.isOnLatestUpdate(true)) {
                DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_ALREADY_DOWNLOADED);
                return;
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                LibsMsg updateResult = null;

                if (checker.getUpdate() == null || args.length > 1 || checker.isOldUpdate() || forceCheck) {
                    updateResult = checker.doUpdateCheck();
                }

                if (checker.getUpdate() == null) {
                    DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_FAILED);
                    return;
                }

                if (checker.isOnLatestUpdate(true)) {
                    if (checker.getLastDownload() != null) {
                        DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_ALREADY_DOWNLOADED);
                    } else {
                        DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_ON_LATEST);
                    }

                    return;
                }

                if (!forceUpdate) {
                    if (updateResult != null) {
                        DisguiseUtilities.sendMessage(sender, updateResult);
                    } else {
                        for (String msg : checker.getUpdateMessage()) {
                            DisguiseUtilities.sendMessage(sender, msg);
                        }
                    }

                    return;
                }

                PluginInformation result = checker.doUpdate();

                if (result == null) {
                    DisguiseUtilities.sendMessage(sender, LibsMsg.UPDATE_FAILED);
                    return;
                }

                for (String msg : checker.getUpdateMessage()) {
                    DisguiseUtilities.sendMessage(sender, msg);
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
