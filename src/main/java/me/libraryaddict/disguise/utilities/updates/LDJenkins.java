package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    /**
     * Fetches from jenkins, using the REST api the last snapshot build information
     */
    private Map<String, Object> fetchLastSnapshotBuild() {
        try {
            DisguiseUtilities.getLogger().info("Now looking for update on Jenkins..");
            // We're connecting to md_5's jenkins REST api
            URL url = new URL("https://ci.md-5.net/job/LibsDisguises/api/json?tree=builds[changeSet[items[msg]],id,result]");
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
        }
        catch (Exception ex) {
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

            if (!"SUCCESS".equalsIgnoreCase(result)) {
                continue;
            }

            if (changelog.isEmpty()) {
                version = (String) map.get("id");
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

                    release = release || msg.toLowerCase().matches("release.? .*");
                }
            }

            if (release) {
                break;
            }
        }

        if (changelog.isEmpty()) {
            return null;
        }

        return new JenkinsUpdate(version, changelog.toArray(new String[0]));
    }
}
