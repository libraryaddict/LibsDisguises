package me.libraryaddict.disguise.commands.libsdisguises;

import com.google.gson.Gson;
import javax.net.ssl.HttpsURLConnection;
import lombok.Data;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.config.ConfigLoader;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LDUploadLogs implements LDCommand {
    static class MCLogs {
        boolean success;
        String id;
        String url;
        String raw;
    }

    private long lastUsed;

    @Data
    public static class GuestPaste {
        private String name;
        private final String text;

        public GuestPaste(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public URL paste() throws Exception {
            URL url = new URL("https://api.mclo.gs/1/log");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            try {
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                StringBuilder output = new StringBuilder();

                output.append(URLEncoder.encode("content", "UTF-8"));
                output.append('=');
                output.append(URLEncoder.encode(text, "UTF-8"));

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

                        MCLogs logs = new Gson().fromJson(response.toString(), MCLogs.class);

                        return new URL(logs.url);
                    }
                } else {
                    throw new IllegalStateException("Unexpected response code " + status);
                }
            } finally {
                con.disconnect();
            }
        }
    }

    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("uploadlog", "uploadlogs", "uploadconfig", "uploadconfigs", "logs", "log");
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
            sender.sendMessage(
                ChatColor.RED + "You last used this command under 3 minutes ago! Restart the server or wait for this timer to " +
                    "disappear!");
            return;
        }

        File latest = new File("logs/latest.log");
        File disguises = new File(LibsDisguises.getInstance().getDataFolder(), "configs/disguises.yml");

        List<File> configs = new ConfigLoader().getConfigs().stream().map(f -> new File(LibsDisguises.getInstance().getDataFolder(), f))
            .collect(Collectors.toList());

        StringBuilder configText = new StringBuilder();

        for (File config : configs) {
            if (configText.length() != 0) {
                configText.append("\n\n================\n\n");
            }

            try {
                String text = new String(Files.readAllBytes(config.toPath()));
                text = text.replaceAll("\n? *#[^\n]*", "").replaceAll("[\n\r]+", "\n");

                configText.append("File: ").append(config.getName()).append("\n\n").append(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isTooBig(latest)) {
            sender.sendMessage(
                ChatColor.RED + "Your latest.log file is too big! It should be less than 10mb! Please restart and run this " +
                    "command again!");
            return;
        }

        if (isTooBig(disguises)) {
            sender.sendMessage(
                ChatColor.RED + "Your disguises.yml is too big! You'll need to trim that file down before using this command! It " +
                    "should be less than 10mb!");
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
                    !str.contains("This server is running") && !str.contains(" Running Java ") &&
                    !str.contains("[ServerMain/INFO]: [bootstrap] Loading ")) {
                    continue;
                }

                valid = true;
                break;
            }

            if (!valid) {
                sender.sendMessage(ChatColor.RED + "Your latest.log is too old! Please restart the server and try again!");
                return;
            }

            sender.sendMessage(ChatColor.GOLD + "Now creating mclo.gs links...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        String disguiseText = disguises.exists() ? new String(Files.readAllBytes(disguises.toPath())) : null;

                        configText.append("\n\n================\n");

                        ArrayList<String> modified = DisguiseConfig.doOutput(true, true);

                        for (String s : modified) {
                            configText.append("\n").append(s);
                        }

                        if (modified.isEmpty()) {
                            configText.append("\nUsing default config!");
                        }

                        URL latestPaste = new GuestPaste("latest.log", latestText).paste();
                        URL configPaste = new GuestPaste("LibsDisguises config.yml", configText.toString()).paste();
                        URL disguisesPaste =
                            disguiseText != null ? new GuestPaste("LibsDisguises disguises.yml", disguiseText).paste() : null;

                        lastUsed = System.currentTimeMillis();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                sender.sendMessage(ChatColor.GOLD + "Upload successful!");

                                // Console can't click :(
                                if (sender instanceof Player) {
                                    sender.sendMessage(ChatColor.GOLD + "Click on the below message to have it appear in your chat input");
                                }

                                String text = "My log file: " + latestPaste + ", my combined config files: " + configPaste +
                                    " and my disguises file: " +
                                    (disguisesPaste != null ? disguisesPaste : "disguises.yml doesn't exist..");

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
        return file.exists() && isTooBig(file.length());
    }

    private boolean isTooBig(long length) {
        return length > 10_048_000;
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_UPLOAD_LOGS;
    }
}
