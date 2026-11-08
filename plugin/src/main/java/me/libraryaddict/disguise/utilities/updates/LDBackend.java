package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.plugin.LibsDisgInfo;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class LDBackend {
    /**
     * Sent so it knows which update to give you.
     * <p>
     * If a new build drops support for your Java (18, 21, 25, etc), server version (1.12, 1.16, etc) or server brand (spigot,
     * megaspigot, ultimatespigot, spigotwithmissiles, etc) then it can try to send you only working jars, and if it needs a newer
     * PacketEvents, then that'll be downloaded too, instead of breaking on the next startup.
     * <p>
     * Premium info is sent so updates can give you a premium jar, instead of needing one kept in the Lib's Disguises folder.
     */
    @RequiredArgsConstructor
    static class SentData {
        private final LibsDisgInfo info1 = LibsPremium.getPluginInformation();
        private final LibsDisgInfo info2 = LibsPremium.getPaidInformation();
        private final String serverVersion = Bukkit.getVersion();
        private final String serverBrand = Bukkit.getName();
        private final String javaVersion = System.getProperty("java.version");
        private final String branch; // releases / snapshots
        private final boolean download;
        private final boolean bisect = LibsPremium.isBisectHosted();
        private final String packetEventsVersion = PacketEventsUpdater.getPacketEventsVersion();
        private final Long packetEventsBuild = PacketEventsUpdater.getPacketEventsBuildTimestamp();
    }

    @Data
    public static class PacketEventsDownload {
        private final String version;
        private final Long build;
        private final String download;
        private final String downloadName;
    }

    @Data
    public static class ReceivedData implements DisguiseUpdate {
        private final boolean success;
        private final boolean userOutdated;
        private final String branch;
        private final String version;
        private final String[] changelog;
        private final String download;
        private final String downloadName;
        private final long date;
        private final Integer retryIn;
        private final PacketEventsDownload packetEvents;

        @Override
        public boolean isReleaseBuild() {
            return "releases".equals(branch);
        }

        @Override
        public List<String> getDownloads() {
            return Collections.singletonList(download);
        }

        @Override
        public Date getFetched() {
            return new Date(date);
        }
    }

    static String postUpdate(Object sent, int timeout) throws IOException {
        URL url = new URL("https://api.lib.co.nz/v1/update");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        try {
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
            con.setRequestProperty("Content-Type", "application/json");

            try (OutputStream output = con.getOutputStream()) {
                output.write(new Gson().toJson(sent).getBytes(StandardCharsets.UTF_8));
            }

            try (InputStream input = con.getInputStream()) {
                return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            }
        } finally {
            con.disconnect();
        }
    }

    public static ReceivedData fetchUpdate(boolean releases, boolean download) throws IOException {
        String json = postUpdate(new SentData(releases ? "releases" : "snapshots", download), 10000);

        DisguiseConfig.setHashedData(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
        DisguiseConfig.saveInternalConfig();

        return new Gson().fromJson(json, ReceivedData.class);
    }

    public static ReceivedData getLastUpdate() {
        if (DisguiseConfig.getHashedData() == null) {
            return null;
        }

        try {
            return new Gson().fromJson(new String(Base64.getDecoder().decode(DisguiseConfig.getHashedData()), StandardCharsets.UTF_8),
                ReceivedData.class);
        } catch (Exception ignored) {
        }

        return null;
    }
}
