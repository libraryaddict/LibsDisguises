package me.libraryaddict.disguise.utilities;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private final String resourceID;
    private String latestVersion;
    private int latestSnapshot;

    public UpdateChecker(String resourceID) {
        this.resourceID = resourceID;
    }

    public void checkSnapshotUpdate(int buildNumber) {
        Map<String, Object> lastBuild = fetchLastSnapshotBuild();

        if (lastBuild == null || !lastBuild.containsKey("id") || !lastBuild.containsKey("timestamp")) {
            return;
        }

        int newBuildNumber = Integer.parseInt((String) lastBuild.get("id"));

        // If new build number is same or older
        if (newBuildNumber <= buildNumber) {
            return;
        }

        Date newBuildDate = new Date(((Number) lastBuild.get("timestamp")).longValue());

        // If the new snapshot is at least 3 days old
        /*if (newBuildDate.getTime() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)) {
            return;
        }*/

        latestSnapshot = newBuildNumber;
    }

    public void checkOfficialUpdate(String currentVersion) {
        String version = fetchSpigotVersion();

        if (version == null) {
            return;
        }

        if (!isNewerVersion(currentVersion, version)) {
            return;
        }

        latestVersion = version;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public int getLatestSnapshot() {
        return latestSnapshot;
    }

    /**
     * Asks spigot for the version
     */
    private String fetchSpigotVersion() {
        try {
            // We're connecting to spigot's API
            URL url = new URL("https://www.spigotmc.org/api/general.php");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // We're writing a body that contains the API access key (Not required and obsolete, but!)
            con.setDoOutput(true);

            // Can't think of a clean way to represent this without looking bad
            String body = "key" + "=" + "98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4" + "&" +
                    "resource=" + this.resourceID;

            // Get the output stream, what the site receives
            try (OutputStream stream = con.getOutputStream()) {
                // Write our body containing version and access key
                stream.write(body.getBytes(StandardCharsets.UTF_8));
            }

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String version = IOUtils.toString(input);

                // If the version is not empty, return it
                if (!version.isEmpty()) {
                    return version;
                }
            }
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Failed to check for a update on spigot.");
        }

        return null;
    }

    private boolean isNewerVersion(String currentVersion, String newVersion) {
        // Remove 'v' from string, split by decimal points
        String[] cSplit = currentVersion.replace("v", "").split("\\.");
        String[] nSplit = newVersion.replace("v", "").split("\\.");

        // Iterate over the versions from left to right
        for (int i = 0; i < Math.max(cSplit.length, nSplit.length); i++) {
            // If the current version doesn't have the next version, then it's older
            if (cSplit.length <= i) {
                return true;
            } else if (nSplit.length <= i) {
                // If the new version doesn't have the next version, then it's older
                return false;
            }

            // String compare the versions, should perform the same as an int compare
            int compareResult = cSplit[i].compareTo(nSplit[i]);

            // Same version
            if (compareResult == 0) {
                continue;
            }

            // Return if current version is inferior to new versio
            return compareResult < 0;
        }

        // Both versions should be the same, return false as it's not a newer version
        return false;
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
            Map<String, Object> jsonObject;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json = IOUtils.toString(input);

                jsonObject = new Gson().fromJson(json, Map.class);
            }

            return jsonObject;
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Failed to check for a snapshot update on jenkins.");
        }

        return null;
    }
}
