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
            LibsMsg.UPDATE_IN_PROGRESS.send(sender);
            return;
        }

        boolean releaseBuilds = checker.isUsingReleaseBuilds();

        if (args.length > 1) {
            boolean previous = releaseBuilds;

            if (args[1].equalsIgnoreCase("dev")) {
                releaseBuilds = false;
            } else if (args[1].equalsIgnoreCase("release")) {
                releaseBuilds = true;
            } else {
                LibsMsg.LIBS_UPDATE_UNKNOWN_BRANCH.send(sender);
                return;
            }

            DisguiseConfig.setUsingReleaseBuilds(releaseBuilds);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                LibsMsg updateResult = checker.doUpdateCheck();

                if (checker.getUpdate() == null) {
                    LibsMsg.UPDATE_FAILED.send(sender);
                    return;
                }

                if (checker.isOnLatestUpdate(true)) {
                    if (checker.getLastDownload() != null) {
                        LibsMsg.UPDATE_ALREADY_DOWNLOADED.send(sender);
                    } else {
                        LibsMsg.UPDATE_ON_LATEST.send(sender);
                    }

                    return;
                }

                /*if (!finalWantsDownload) {
                    if (updateResult != null) {
                        updateResult.send(sender);
                    } else {
                        for (String msg : checker.getUpdateMessage()) {
                            DisguiseUtilities.sendMessage(sender, msg);
                        }
                    }

                    return;
                }*/

                PluginInformation result = checker.doUpdate();

                if (result == null) {
                    LibsMsg.UPDATE_FAILED.send(sender);
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
