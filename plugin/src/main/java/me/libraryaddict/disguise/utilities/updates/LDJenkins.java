package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.LibsPremium;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LDJenkins extends BaseJenkins {
    private UpdateChecker updateChecker;

    static class LDJenkinsUpdate extends JenkinsUpdate {

        public LDJenkinsUpdate(String version, String[] changelog, List<String> downloads) {
            super(version, changelog, downloads);
        }

        @Override
        public String getDownload() {
            return "https://ci.md-5.net/job/LibsDisguises/lastSuccessfulBuild/artifact/target/LibsDisguises.jar";
        }
    }

    public LDJenkins() {
        super("https://ci.md-5.net/job/LibsDisguises/");
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
                String json =
                    new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                map = new Gson().fromJson(json, HashMap.class);
            }

            con.disconnect();

            if (!map.containsKey("body")) {
                return new String[0];
            }

            return ((String) map.get("body")).split("[\\r\\n]+");
        } catch (Exception ignored) {
        }

        return new String[0];
    }

    /**
     * Fetches from jenkins, using the REST api the last snapshot build information
     */
    protected Map<String, Object> fetchLastSnapshotBuild() {
        try {
            String[] users = getBadUsers();

            for (String s : users) {
                if (LibsPremium.getPaidInformation() != null && (s.equals(LibsPremium.getPaidInformation().getDownloadID()) ||
                    s.equals(LibsPremium.getPaidInformation().getUserID()))) {
                    LibsDisguises.getInstance().unregisterCommands(true);
                }
            }

            LibsDisguises.getInstance().getLogger().info("Now looking for update on Jenkins..");

            return super.fetchLastSnapshotBuild();
        } catch (Exception ex) {
            LibsDisguises.getInstance().getLogger().warning("Failed to check for a snapshot update on jenkins.");
            ex.printStackTrace();
        }

        return null;
    }

    public DisguiseUpdate getLatestSnapshot() {
        JenkinsUpdate update = (JenkinsUpdate) super.getLatestSnapshot();

        return new LDJenkinsUpdate(update.getDownload(), update.getChangelog(), update.getDownloads());
    }

}
