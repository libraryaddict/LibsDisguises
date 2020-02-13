package me.libraryaddict.disguise.utilities;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateChecker {
    private final String resourceID;
    @Getter
    private String latestVersion;
    @Getter
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


    /**
     * Asks spigot for the version
     */
    private String fetchSpigotVersion() {
        try {
            // We're connecting to spigot's API
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceID);
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String version = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

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
        // Remove 'v' and '-SNAPSHOT' from string, split by decimal points
        String[] cSplit = currentVersion.replaceAll("(v)|(-SNAPSHOT)", "").split("\\.");
        String[] nSplit = newVersion.replaceAll("(v)|(-SNAPSHOT)", "").split("\\.");

        // Iterate over the versions from left to right
        for (int i = 0; i < Math.max(cSplit.length, nSplit.length); i++) {
            // If the current version doesn't have the next version, then it's older
            if (cSplit.length <= i) {
                return true;
            } else if (nSplit.length <= i) {
                // If the new version doesn't have the next version, then it's older
                return false;
            }

            // If both strings are numerical
            if (cSplit[i].matches("[0-9]+") && nSplit[i].matches("[0-9]+")) {
                int cInt = Integer.parseInt(cSplit[i]);
                int nInt = Integer.parseInt(nSplit[i]);

                // Same version
                if (cInt == nInt) {
                    continue;
                }

                // Return if current version is inferior to new version
                return cInt < nInt;
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
                String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

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
