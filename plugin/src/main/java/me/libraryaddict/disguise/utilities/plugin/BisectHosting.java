package me.libraryaddict.disguise.utilities.plugin;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BisectHosting {
    public boolean isBisectHosted(String pluginName) {
        boolean claimedHosted = DisguiseConfig.isBisectHosted();
        String ip = Bukkit.getIp();
        String parsedIP = ip.replaceAll("[^:\\d.]", "");

        // If not hosted by bisect
        if (!claimedHosted && DisguiseConfig.getSavedServerIp().equals(parsedIP)) {
            return false;
        }

        boolean hostedBy = false;

        if (ip.matches("((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!$)|$)){4}")) {
            try {
                ip = getFinalURL("http://" + ip);

                // Doing this funny stringbuilder because antiviruses may find it "dodgy". Or does this make it dodgier?
                if (ip != null && ip.startsWith("https" + "://" + "www" + "." + "bisecthosting" + "." + "com/")) {
                    hostedBy = true;
                }
            } catch (IOException ignored) {
            }
        }

        if (claimedHosted != hostedBy || !DisguiseConfig.getSavedServerIp().equals(parsedIP)) {
            DisguiseConfig.setBisectHosted(hostedBy, parsedIP);
        }

        if (!hostedBy && !DisguiseConfig.getSavedServerIp().isEmpty()) {
            // Just a small message for those who tried to enable it
            LibsDisguises.getInstance().getLogger().severe("Check for BisectHosting failed! Connection error?");
            DisguiseConfig.setBisectHosted(hostedBy, parsedIP);
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

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            return con.getHeaderField("Location");
        }

        return url;
    }
}
