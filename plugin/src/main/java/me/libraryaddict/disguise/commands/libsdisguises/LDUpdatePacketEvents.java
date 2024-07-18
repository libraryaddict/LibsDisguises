package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.updates.PacketEventsUpdater;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LDUpdatePacketEvents implements LDCommand {
    private final AtomicBoolean updateInProgress = new AtomicBoolean(false);

    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("updatepacketevents", "updatepe", "packetevents");
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
    public void onCommand(CommandSender sender, String[] args) {
        if (updateInProgress.get()) {
            sendMessage(sender, "Update already in progress");
            return;
        }

        sendMessage(sender, "Please hold, now downloading PacketEvents..");

        new BukkitRunnable() {
            @Override
            public void run() {
                PacketEventsUpdater updater = new PacketEventsUpdater();

                try {
                    boolean outcome = updater.doUpdate();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (outcome) {
                                sendMessage(sender, "Download success! Restart server to finish update!");
                            } else {
                                sendMessage(sender, "Update failed, you may need to update PacketEvents");
                            }
                        }
                    }.runTask(LibsDisguises.getInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sendMessage(sender, "Update failed, " + ex.getMessage());
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
