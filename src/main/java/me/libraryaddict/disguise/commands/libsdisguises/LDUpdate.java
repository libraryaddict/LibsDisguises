package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.UpdateChecker;
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
        return Arrays.asList("update", "update!");
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

        boolean force = args[0].endsWith("!");

        if (!force) {
            if (checker.getLatestSnapshot() <= 0) {
                sender.sendMessage(LibsMsg.UPDATE_NOT_READY.get());
                return;
            }

            if (checker.getLatestSnapshot() == LibsDisguises.getInstance().getBuildNumber()) {
                sender.sendMessage(LibsMsg.UPDATE_ON_LATEST.get());
                return;
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                PluginInformation result;

                if (force) {
                    result = checker.grabLatestSnapshot();
                } else {
                    result = checker.grabSnapshotBuild();
                }

                if (result == null) {
                    sender.sendMessage(LibsMsg.UPDATE_FAILED.get());
                    return;
                }

                sender.sendMessage(LibsMsg.UPDATE_SUCCESS.get()); // Update success, please restart to update
                sender.sendMessage(LibsMsg.UPDATE_INFO
                        .get(result.getVersion(), result.getBuildNumber(), result.getParsedBuildDate().toString(),
                                result.getSize() / 1024));

                if (sender instanceof Player) {
                    Bukkit.getConsoleSender().sendMessage(LibsMsg.UPDATE_SUCCESS.get());
                    Bukkit.getConsoleSender().sendMessage(LibsMsg.UPDATE_INFO
                            .get(result.getVersion(), result.getBuildNumber(), result.getParsedBuildDate().toString(),
                                    result.getSize() / 1024));
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
