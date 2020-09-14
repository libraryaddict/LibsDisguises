package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 26/04/2020.
 */
public class LDJenkins {
    private UpdateChecker updateChecker;

    @AllArgsConstructor
    @Getter
    private class JenkinsUpdate implements DisguiseUpdate {
        private final Date fetched = new Date();
        private final String version;
        private final String[] changelog;

        @Override
        public String getDownload() {
            return "https://ci.md-5.net/job/LibsDisguises/" + getVersion() + "/artifact/target/LibsDisguises.jar";
        }

        @Override
        public boolean isReleaseBuild() {
            return false;
        }
    }

    private String[] getBadUsers() {
        // List of bad users that need to redownload Libs Disguises

        try {
            // We're connecting to md_5's jenkins REST api
            URL url = new URL("https://api.github.com/repos/libraryaddict/libsdisguises/issues/469");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            HashMap<String, Object> map;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));

                map = new Gson().fromJson(json, HashMap.class);
            }

            if (!map.containsKey("body")) {
                return new String[0];
            }

            return ((String) map.get("body")).split("(\\r|\\n)+");
        } catch (Exception ignored) {
        }

        return new String[0];
    }

    /**
     * Fetches from jenkins, using the REST api the last snapshot build information
     */
    private Map<String, Object> fetchLastSnapshotBuild() {
        try {
            String[] users = getBadUsers();

            for (String s : users) {
                if (LibsPremium.getPaidInformation() != null &&
                        (s.equals(LibsPremium.getPaidInformation().getDownloadID()) ||
                                s.equals(LibsPremium.getPaidInformation().getUserID()))) {
                    LibsDisguises.getInstance().unregisterCommands(true);
                }
            }

            DisguiseUtilities.getLogger().info("Now looking for update on Jenkins..");
            // We're connecting to md_5's jenkins REST api
            URL url = new URL("https://ci.md-5.net/job/LibsDisguises/api/json?tree=builds[changeSet[items[msg]],id," +
                    "result]");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDefaultUseCaches(false);
            Map<String, Object> jsonObject;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));

                jsonObject = new Gson().fromJson(json, Map.class);
            }

            return jsonObject;
        } catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Failed to check for a snapshot update on jenkins.");
            ex.printStackTrace();
        }

        return null;
    }

    public DisguiseUpdate getLatestSnapshot() {
        Map<String, Object> lastBuild = fetchLastSnapshotBuild();

        if (lastBuild == null || !lastBuild.containsKey("builds")) {
            return null;
        }

        ArrayList<String> changelog = new ArrayList<>();
        String version = null;

        for (Map map : (List<Map>) lastBuild.get("builds")) {
            String result = (String) map.get("result");

            if (result == null || result.equalsIgnoreCase("null")) {
                if (version == null) {
                    DisguiseUtilities.getLogger()
                            .info("Jenkins build is pending.. Sleeping and checking again in 10 seconds");

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return getLatestSnapshot();
                }
            }

            Object items = ((Map) map.get("changeSet")).get("items");
            boolean release = false;

            if (items instanceof List) {
                for (Map item : (List<Map>) items) {
                    String msg = (String) item.get("msg");

                    if (msg == null) {
                        continue;
                    }

                    changelog.add("#" + map.get("id") + ": " + ChatColor.YELLOW + msg);

                    release = release || msg.toLowerCase(Locale.ENGLISH).matches("(re)?.?release.? .*");
                }
            }

            if ("SUCCESS".equalsIgnoreCase(result)) {
                if (version == null) {
                    version = (String) map.get("id");
                }

                if (release) {
                    break;
                }
            }
        }

        if (changelog.isEmpty()) {
            return null;
        }

        return new JenkinsUpdate(version, changelog.toArray(new String[0]));
    }
}
