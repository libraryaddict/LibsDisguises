package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseConfig.UpdatesBranch;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.updates.PacketEventsUpdater;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class LDUpdatePacketEvents implements LDCommand {
    private final AtomicBoolean updateInProgress = new AtomicBoolean(false);

    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("updatepacketevents", "updatepe", "packetevents", "pe");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("libsdisguises.update") || sender.hasPermission("minecraft.command.op");
    }

    @Override
    public String getPermission() {
        return null;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.DARK_RED + "[LibsDisguises] " + ChatColor.RED + message);
    }

    @Override
    public List<String> onTabComplete(String[] args) {
        if (args.length == 2) {
            return Arrays.asList("snapshots", "releases");
        }

        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (updateInProgress.get()) {
            sendMessage(sender, "Update already in progress");
            return;
        }

        UpdatesBranch branch;

        if (args.length > 1) {
            if (args[1].toLowerCase(Locale.ENGLISH).contains("snapshot")) {
                branch = UpdatesBranch.SNAPSHOTS;
            } else if (args[1].toLowerCase(Locale.ENGLISH).contains("release")) {
                branch = UpdatesBranch.RELEASES;
            } else {
                sendMessage(sender,
                    "Unrecognized third argument, you can provide 'snapshots' and 'release' to force an update to one of those");
                return;
            }
        } else {
            branch = UpdatesBranch.SAME_BUILDS;
        }

        sendMessage(sender, "Please hold, now downloading " + (branch == UpdatesBranch.RELEASES ? "latest release for " :
            branch == UpdatesBranch.SNAPSHOTS ? "latest snapshot build for " : "") + "PacketEvents..");

        new BukkitRunnable() {
            @Override
            public void run() {
                PacketEventsUpdater updater = new PacketEventsUpdater();

                try {
                    boolean outcome;

                    if (branch == UpdatesBranch.SNAPSHOTS) {
                        outcome = updater.doSnapshotUpdate();
                    } else if (branch == UpdatesBranch.RELEASES) {
                        outcome = updater.doReleaseUpdate(null);
                    } else {
                        outcome = updater.doUpdate();
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (outcome) {
                                sendMessage(sender, "Packetevents download success! Restart server to finish update!");
                            } else {
                                sendMessage(sender, "Packetevents update failed, you may need to update PacketEvents");
                            }
                        }
                    }.runTask(LibsDisguises.getInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sendMessage(sender, "Packetevents update failed, " + ex.getMessage());
                        }
                    }.runTask(LibsDisguises.getInstance());
                } finally {
                    updateInProgress.set(false);
                }
            }
        }.runTaskAsynchronously(LibsDisguises.getInstance());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_UPDATE_PACKET_EVENTS;
    }
}
