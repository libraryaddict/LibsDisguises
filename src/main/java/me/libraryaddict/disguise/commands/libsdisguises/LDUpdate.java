package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.updates.UpdateChecker;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
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
        return Arrays.asList("update", "update dev", "update release", "changelog", "update!");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.update";
    }

    private void doChangelog(CommandSender sender) {
        UpdateChecker checker = LibsDisguises.getInstance().getUpdateChecker();

        if (checker.getUpdate() == null) {
            sender.sendMessage(LibsMsg.UPDATE_REQUIRED.get());
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
    public void onCommand(CommandSender sender, String[] args) {
        UpdateChecker checker = LibsDisguises.getInstance().getUpdateChecker();

        if (checker.isDownloading()) {
            sender.sendMessage(LibsMsg.UPDATE_IN_PROGRESS.get());
            return;
        }

        if (args[0].equalsIgnoreCase("changelog")) {
            doChangelog(sender);
            return;
        }

        boolean releaseBuilds = checker.isUsingReleaseBuilds();

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("dev")) {
                releaseBuilds = false;
            } else if (args[1].equalsIgnoreCase("release")) {
                releaseBuilds = true;
            } else {
                sender.sendMessage(LibsMsg.LIBS_UPDATE_UNKNOWN_BRANCH.get());
                return;
            }

            DisguiseConfig.setUsingReleaseBuilds(releaseBuilds);
        }

        if (checker.getUpdate() != null && checker.getUpdate().isReleaseBuild() == releaseBuilds) {
            if (checker.isServerLatestVersion()) {
                sender.sendMessage(LibsMsg.UPDATE_ON_LATEST.get());
                return;
            }

            if (checker.isOnLatestUpdate(true)) {
                sender.sendMessage(LibsMsg.UPDATE_ALREADY_DOWNLOADED.get());
                return;
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                LibsMsg updateResult = null;

                if (checker.getUpdate() == null || args.length > 1 || checker.isOldUpdate()) {
                    updateResult = checker.doUpdateCheck();
                }

                if (checker.getUpdate() == null) {
                    sender.sendMessage(LibsMsg.UPDATE_FAILED.get());
                    return;
                }

                if (checker.isOnLatestUpdate(true)) {
                    if (checker.getLastDownload() != null) {
                        sender.sendMessage(LibsMsg.UPDATE_ALREADY_DOWNLOADED.get());
                    } else {
                        sender.sendMessage(LibsMsg.UPDATE_ON_LATEST.get());
                    }

                    return;
                }

                if (!args[0].endsWith("!")) {
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
