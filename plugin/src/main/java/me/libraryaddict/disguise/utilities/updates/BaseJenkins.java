package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseJenkins {
    private final String baseUrl;

    public BaseJenkins(String baseUrl) {
        this.baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
    }

    @AllArgsConstructor
    @Getter
    static class JenkinsUpdate implements DisguiseUpdate {
        private final Date fetched = new Date();
        private final String version;
        private final String[] changelog;
        private final List<String> downloads;

        @Override
        public String getDownload() {
            if (downloads.size() != 1) {
                throw new IllegalStateException("We don't have any download information!");
            }

            return downloads.get(0);
        }

        @Override
        public boolean isReleaseBuild() {
            return false;
        }
    }

    /**
     * Fetches from jenkins, using the REST api the last snapshot build information
     */
    protected Map<String, Object> fetchLastSnapshotBuild() {
        try {
            // We're connecting to jenkins REST api
            URL url = new URL(baseUrl + "api/json?tree=builds[changeSet[items[msg]],id,result,artifacts[relativePath]]");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setUseCaches(false);
            con.setDefaultUseCaches(false);
            Map<String, Object> jsonObject;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json =
                    new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                jsonObject = new Gson().fromJson(json, Map.class);
            }

            con.disconnect();

            return jsonObject;
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger().warning("Failed to check for an update on jenkins.");
            ex.printStackTrace();
        }

        return null;
    }

    public DisguiseUpdate getLatestSnapshot() {
        Map<String, Object> lastBuild = fetchLastSnapshotBuild();

        if (lastBuild == null || !lastBuild.containsKey("builds")) {
            return null;
        }

        List<String> downloads = new ArrayList<>();
        ArrayList<String> changelog = new ArrayList<>();
        String version = null;

        for (Map map : (List<Map>) lastBuild.get("builds")) {
            List<Map> artifacts = (List<Map>) map.get("artifacts");

            if (artifacts == null || artifacts.isEmpty()) {
                continue;
            }

            if (downloads.isEmpty()) {
                for (Map artifact : artifacts) {
                    String relative = (String) artifact.get("relativePath");

                    if (relative == null || !relative.endsWith(".jar")) {
                        continue;
                    }

                    downloads.add(this.baseUrl + map.get("id") + "/artifact/" + relative);
                }
            }

            String result = (String) map.get("result");

            if (result == null || result.equalsIgnoreCase("null")) {
                if (version == null) {
                    LibsDisguises.getInstance().getLogger().info("Jenkins build is pending.. Sleeping and checking again in 10 seconds");

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

            if (!"SUCCESS".equalsIgnoreCase(result)) {
                continue;
            }

            if (version == null) {
                version = (String) map.get("id");
            }

            if (release) {
                break;
            }
        }

        if (downloads.isEmpty()) {
            return null;
        }

        return new JenkinsUpdate(version, changelog.toArray(new String[0]), downloads);
    }
}
