package me.libraryaddict.disguise.commands.libsdisguises;

import com.comphenix.protocol.ProtocolLibrary;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

        sender.sendMessage(ChatColor.RED + "Please hold, now downloading..");

        new BukkitRunnable() {
            @Override
            public void run() {
                File protocolLibFile = null;

                try {
                    Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
                    getFile.setAccessible(true);
                    File theirFile = (File) getFile.invoke(ProtocolLibrary.getPlugin());
                    File dest = new File("plugins/update/" + theirFile.getName());

                    // We're connecting to spigot's API
                    URL url =
                            new URL("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target" +
                                    "/ProtocolLib" +
                                    ".jar");
                    // Creating a connection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");

                    // Get the input stream, what we receive
                    try (InputStream input = con.getInputStream()) {
                        Files.copy(input, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(ChatColor.RED + "Update success! Restart server to finish update!");
                        }
                    }.runTask(LibsDisguises.getInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
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
