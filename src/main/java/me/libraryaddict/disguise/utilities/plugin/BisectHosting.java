package me.libraryaddict.disguise.utilities.plugin;

import me.libraryaddict.disguise.LibsDisguises;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by libraryaddict on 6/03/2020.
 */
public class BisectHosting {
    public boolean isBisectHosted(String pluginName) {
        File configFile = new File("plugins/" + pluginName + "/internal.yml");
        boolean claimedHosted = false;
        String serverIp = Bukkit.getIp().replaceAll("[^:0-9.]", "");

        if (configFile.exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);

            if (configuration.contains("Bisect-Hosted") && configuration.contains("Server-IP")) {
                claimedHosted = configuration.getBoolean("Bisect-Hosted");

                // If not hosted by bisect
                if (!claimedHosted && configuration.getString("Server-IP").equals(serverIp)) {
                    return false;
                }
            }
        }

        String ip = Bukkit.getIp();
        boolean hostedBy = false;

        if (ip.matches("((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}")) {
            try {
                ip = getFinalURL("http://" + ip);

                if (ip.startsWith("https://www.bisecthosting.com/")) {
                    hostedBy = true;
                }
            }
            catch (IOException ignored) {
            }
        }

        // If config doesn't exist, or it's not a bisect server
        if (!configFile.exists()) {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            try (PrintWriter writer = new PrintWriter(configFile, "UTF-8")) {
                // This setting is if the server should check if you are using Bisect Hosting",
                writer.write("# If you're using BisectHosting, this will tell the server to enable premium for free!");
                writer.write("\n# However if you're not using BisectHosting, this is false so the server won't waste " +
                        "time");
                writer.write(
                        "\n# Coupon 'libraryaddict' for 25% off your first invoice on any of their gaming servers");
                writer.write("\n# Be sure to visit through this link! https://bisecthosting.com/libraryaddict");
                writer.write("\nBisect-Hosted: " + hostedBy);
                writer.write("\nServer-IP: " + serverIp);
            }
            catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (claimedHosted) {
            // Just a small message for those who tried to enable it
            LibsDisguises.getInstance().getLogger().severe("Check for BisectHosting failed! Connection error?");
        }

        return hostedBy;
    }

    private String getFinalURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        // Short as this shouldn't take long
        con.setReadTimeout(2500);
        con.setConnectTimeout(2500);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ||
                con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            return con.getHeaderField("Location");
        }

        return url;
    }
}
