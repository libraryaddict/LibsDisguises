package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
            // We're connecting to md_5's jenkins REST api
            URL url = new URL("https://ci.md-5.net/job/LibsDisguises/lastSuccessfulBuild/api/json");
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

        if (lastBuild == null || !lastBuild.containsKey("id") || !lastBuild.containsKey("timestamp")) {
            return null;
        }

        ArrayList<String> changelog = new ArrayList<>();

        if (lastBuild.get("changeSet") instanceof Map) {
            Object items = ((Map) lastBuild.get("changeSet")).get("items");

            if (items instanceof Map[]) {
                for (Map item : (Map[]) items) {
                    String msg = (String) item.get("msg");

                    if (msg == null) {
                        continue;
                    }

                    changelog.add(msg);
                }
            }
        }

        return new JenkinsUpdate((String) lastBuild.get("id"), changelog.toArray(new String[0]));
    }
}
