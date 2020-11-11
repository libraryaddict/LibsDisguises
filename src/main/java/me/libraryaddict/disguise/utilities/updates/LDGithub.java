package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 26/04/2020.
 */
@AllArgsConstructor
public class LDGithub {
    @Getter
    @AllArgsConstructor
    private class GithubUpdate implements DisguiseUpdate {
        private String version;
        private String[] changelog;
        private String download;
        private final Date fetched = new Date();

        @Override
        public boolean isReleaseBuild() {
            return true;
        }
    }

    @Getter
    private class GithubData {
        @Getter
        class Asset {
            String browser_download_url;
            String name;
            String content_type;
        }

        String name;
        String tag_name;
        String body;
        Date published_at;
        Asset[] assets;
    }

    @Getter
    private UpdateChecker checker;

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

    public DisguiseUpdate getLatestRelease() {
        try {
            String[] users = getBadUsers();

            for (String s : users) {
                if (LibsPremium.getPaidInformation() != null &&
                        (s.equals(LibsPremium.getPaidInformation().getDownloadID()) ||
                                s.equals(LibsPremium.getPaidInformation().getUserID()))) {
                    LibsDisguises.getInstance().unregisterCommands(true);
                } else {
                    if (LibsPremium.getUserID() == null ||
                            (!s.equals(LibsPremium.getUserID()) && !s.equals(LibsPremium.getDownloadID()))) {
                        continue;
                    }

                    getChecker().setGoSilent(true);
                }
            }

            if (!getChecker().isGoSilent()) {
                DisguiseUtilities.getLogger().info("Now looking for update on Github..");
            }

            // We're connecting to md_5's jenkins REST api
            URL url = new URL("https://api.github.com/repos/libraryaddict/LibsDisguises/releases/latest");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            GithubData gitData;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));

                gitData = new Gson().fromJson(json, GithubData.class);
            } catch (IOException ex) {
                try (InputStream error = con.getErrorStream()) {
                    String line = new BufferedReader(new InputStreamReader(error, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));

                    DisguiseUtilities.getLogger().severe("Error with Github! " + line);

                    if (line.contains("rate limit") && !DisguiseConfig.isHittingRateLimit()) {
                        DisguiseConfig.setHittingRateLimit(true);
                        DisguiseUtilities.getLogger().severe("Changed update checker to be every 36 hours due to rate limiting from this IP");
                    }
                } catch (Exception ex1) {
                    DisguiseUtilities.getLogger().severe("Error when trying to read error stream! Inception!");
                    ex1.printStackTrace();
                }

                throw ex;
            }

            String download = null;

            for (GithubData.Asset asset : gitData.getAssets()) {
                if (!asset.getName().endsWith(".jar")) {
                    continue;
                }

                download = asset.getBrowser_download_url();
                break;
            }

            if (download == null) {
                throw new IllegalStateException("Download url is missing");
            }

            return new GithubUpdate(gitData.getTag_name().replace("v", ""), gitData.getBody().split("(\\r|\\n)+"),
                    download);
        } catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Failed to check for a release on Github");
            ex.printStackTrace();
        }

        return null;
    }
}
