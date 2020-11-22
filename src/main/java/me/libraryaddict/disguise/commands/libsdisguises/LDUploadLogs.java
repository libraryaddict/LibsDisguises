package me.libraryaddict.disguise.commands.libsdisguises;

import lombok.Data;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by libraryaddict on 18/06/2020.
 */
public class LDUploadLogs implements LDCommand {
    private long lastUsed;

    /**
     * Small modification of https://gist.github.com/jamezrin/12de49643d7be7150da362e86407113f
     */
    @Data
    public class GuestPaste {
        private String name = null;
        private final String text;

        public GuestPaste(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public URL paste() throws Exception {
            URL url = new URL("https://pastebin.com/api/api_post.php");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            List<SimpleEntry<String, String>> params = new LinkedList<>();
            // This doesn't give you access to my pastebin account ;)
            // You need to enter another key for that, this key is for pastebin's tracking metrics.
            // If you're using this code, please use your own pastebin dev key.
            // Overuse will get it banned, and you'll have to ship a new version with your own key anyways.
            // This is seperated into strings to prevent super easy scraping.
            if (getClass().getName().contains("me.libraryaddict")) {
                params.add(new SimpleEntry<>("api_dev_key", "62067f9d" + "cc1979a475105b529" + "eb453a5"));
            }
            params.add(new SimpleEntry<>("api_option", "paste"));
            params.add(new SimpleEntry<>("api_paste_name", name));
            params.add(new SimpleEntry<>("api_paste_code", text));

            params.add(new SimpleEntry<>("api_paste_format", "text"));
            params.add(new SimpleEntry<>("api_paste_expire_date", "1M"));
            params.add(new SimpleEntry<>("api_paste_private", "1"));

            StringBuilder output = new StringBuilder();
            for (SimpleEntry<String, String> entry : params) {
                if (output.length() > 0) {
                    output.append('&');
                }

                output.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                output.append('=');
                output.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            con.setDoOutput(true);
            try (DataOutputStream dos = new DataOutputStream(con.getOutputStream())) {
                dos.writeBytes(output.toString());
                dos.flush();
            }

            int status = con.getResponseCode();

            if (status >= 200 && status < 300) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }

                    return new URL(response.toString());
                }
            } else {
                throw new IllegalStateException("Unexpected response code " + status);
            }
        }
    }

    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("uploadlog", "uploadlogs", "uploadconfig", "uploadconfigs", "logs");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (lastUsed + TimeUnit.MINUTES.toMillis(3) > System.currentTimeMillis()) {
            sender.sendMessage(ChatColor.RED +
                    "You last used this command under 3 minutes ago! Restart the server or wait for this timer to " +
                    "disappear!");
            return;
        }

        File latest = new File("logs/latest.log");
        File disguises = new File(LibsDisguises.getInstance().getDataFolder(), "disguises.yml");
        File config = new File(LibsDisguises.getInstance().getDataFolder(), "config.yml");

        if (isTooBig(latest)) {
            sender.sendMessage(ChatColor.RED +
                    "Your latest.log file is too big! It should be less than 512kb! Please restart and run this " +
                    "command again!");
            return;
        }

        if (isTooBig(disguises)) {
            sender.sendMessage(ChatColor.RED +
                    "Your disguises.yml is too big! You'll need to trim that file down before using this command! It " +
                    "should be less than 512kb!");
            return;
        }

        if (isTooBig(config)) {
            sender.sendMessage(ChatColor.RED + "Your config.yml is too big! It should be less than 512kb!");
            return;
        }

        try {
            String latestText = new String(Files.readAllBytes(latest.toPath()));

            boolean valid = false;
            int lastFind = 0;

            for (int i = 0; i < 15; i++) {
                int nextLine = latestText.indexOf("\n", lastFind);

                if (nextLine == -1) {
                    break;
                }

                String str = latestText.substring(lastFind, nextLine);

                lastFind = nextLine + 2;

                if (!str.contains("Starting minecraft server version") && !str.contains("Loading properties") &&
                        !str.contains("This server is running")) {
                    continue;
                }

                valid = true;
                break;
            }

            if (!valid) {
                sender.sendMessage(
                        ChatColor.RED + "Your latest.log is too old! Please restart the server and try again!");
                return;
            }

            sender.sendMessage(ChatColor.GOLD + "Now creating pastebin links...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        String disguiseText = new String(Files.readAllBytes(disguises.toPath()));
                        StringBuilder configText = new StringBuilder(new String(Files.readAllBytes(config.toPath())));

                        configText.append("\n================\n");

                        ArrayList<String> modified =
                                DisguiseConfig.doOutput(LibsDisguises.getInstance().getConfig(), true, true);

                        for (String s : modified) {
                            configText.append("\n").append(s);
                        }

                        if (modified.isEmpty()) {
                            configText.append("\nUsing default config!");
                        }

                        URL latestPaste = new GuestPaste("latest.log", latestText).paste();
                        URL configPaste = new GuestPaste("LibsDisguises config.yml", configText.toString()).paste();
                        URL disguisesPaste = new GuestPaste("LibsDisguises disguises.yml", disguiseText).paste();

                        lastUsed = System.currentTimeMillis();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                sender.sendMessage(ChatColor.GOLD + "Upload successful!");

                                // Console can't click :(
                                if (sender instanceof Player) {
                                    sender.sendMessage(ChatColor.GOLD +
                                            "Click on the below message to have it appear in your chat input");
                                }

                                String text = "My log file: " + latestPaste + ", my config file: " + configPaste +
                                        " and my disguises file: " + disguisesPaste;

                                ComponentBuilder builder = new ComponentBuilder("");
                                builder.append(text);
                                builder.color(net.md_5.bungee.api.ChatColor.AQUA);
                                builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));

                                sender.spigot().sendMessage(builder.create());
                            }
                        }.runTask(LibsDisguises.getInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(ChatColor.RED + "Unexpected error! Upload failed! " + e.getMessage());
                    }
                }
            }.runTaskAsynchronously(LibsDisguises.getInstance());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTooBig(File file) {
        return file.exists() && file.length() >= 512 * 1024;
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_UPLOAD_LOGS;
    }
}
