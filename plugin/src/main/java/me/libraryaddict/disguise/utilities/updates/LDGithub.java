package me.libraryaddict.disguise.utilities.updates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
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

    public DisguiseUpdate getLatestRelease() {
        try {
            String ourVersion = LibsDisguises.getInstance().getDescription().getVersion();

            if (!getChecker().isGoSilent()) {
                DisguiseUtilities.getLogger().info("Now looking for update on Github..");
            }

            // We're connecting to githubs REST api
            URL url = new URL("https://api.github.com/repos/libraryaddict/LibsDisguises/releases/latest");
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");

            // We believe we're on the latest version and know what the last etag was
            if (Objects.equals(ourVersion, DisguiseConfig.getLastPluginUpdateVersion()) && DisguiseConfig.getLastGithubUpdateETag() != null) {
                con.setRequestProperty("If-None-Match", DisguiseConfig.getLastGithubUpdateETag());
            }

            if (con.getResponseCode() == 304) {
                // Its the same as the last one we checked
                return null;
            }

            GithubData gitData;

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                gitData = new Gson().fromJson(json, GithubData.class);
            } catch (IOException ex) {
                try (InputStream error = con.getErrorStream()) {
                    String line = new BufferedReader(new InputStreamReader(error, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                    DisguiseUtilities.getLogger().severe("Error with Github! " + line);

                    if (line.contains("rate limit") && !DisguiseConfig.isHittingRateLimit()) {
                        DisguiseConfig.setHittingRateLimit(true);
                        DisguiseUtilities.getLogger().severe("Changed update checker to be every 36 hours due to rate limiting from this IP");
                        return null;
                    }
                } catch (Exception ex1) {
                    DisguiseUtilities.getLogger().severe("Error when trying to read error stream! Inception!");
                    ex1.printStackTrace();
                }

                return null;
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

            GithubUpdate update = new GithubUpdate(gitData.getTag_name().replace("v", ""), gitData.getBody().split("(\\r|\\n)+"), download);

            if (Objects.equals(update.getVersion(), ourVersion)) {
                DisguiseConfig.setLastGithubUpdateETag(con.getHeaderField("ETag"));
                DisguiseConfig.setLastPluginUpdateVersion(ourVersion);
                DisguiseConfig.saveInternalConfig();
            }

            return update;
        } catch (Exception ex) {
            DisguiseUtilities.getLogger().warning("Failed to check for a release on Github");
            ex.printStackTrace();
        }

        return null;
    }
}
