package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by libraryaddict on 30/06/2020.
 */
public class LDUpdateProtocolLib implements LDCommand {
    private final AtomicBoolean updateInProgress = new AtomicBoolean(false);

    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("updateprotocollib", "updatepl", "protocollib");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("minecraft.command.op");
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (updateInProgress.get()) {
            sender.sendMessage(ChatColor.RED + "Update already in progress");
            return;
        }

        sender.sendMessage(ChatColor.RED + "Please hold, now downloading ProtocolLib..");

        new BukkitRunnable() {
            @Override
            public void run() {
                File protocolLibFile = null;

                try {
                    DisguiseUtilities.updateProtocolLib();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(ChatColor.RED + "Download success! Restart server to finish update!");
                        }
                    }.runTask(LibsDisguises.getInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(ChatColor.RED +
                                "Looks like ProtocolLib's site may be down! MythicCraft/MythicMobs has a discord server https://discord.gg/EErRhJ4qgx you" +
                                " can join. Check the pins in #libs-support for a ProtocolLib.jar you can download!");
                            sender.sendMessage(ChatColor.RED + "Update failed, " + ex.getMessage());
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
        return LibsMsg.LD_COMMAND_UPDATEPROTOCOLLIB;
    }
}
