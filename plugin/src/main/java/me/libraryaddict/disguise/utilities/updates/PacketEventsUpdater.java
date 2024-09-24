package me.libraryaddict.disguise.utilities.updates;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.PEVersions;
import com.google.gson.Gson;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
public class PacketEventsUpdater {
    private static class ModrinthFile {
        private String url;
        private String filename;
        private boolean primary;
        private long size;
    }

    private static class ModrinthVersion {
        private String name;
        private String version_number; // Which is a string!
        private String version_type;
        private String status;
        private String requested_status;
        private String id;
        private ModrinthFile[] files;
    }

    private @Nullable File destination;
    @Getter
    private static boolean notedShadedPlugin;

    /**
     * Returns the min required version, as in any older version will just not work.
     */
    public static String getMinimumPacketEventsVersion() {
        // At time of writing, release is 2.4.0, and snapshot builds are all "2.5.0-SNAPSHOT"
        // This means we'll always fail a release check!
        // But now that 2.5.0 has released, it'll update to the release build
        return "2.5.0";
    }

    /**
     * PacketEvents must have a build timestamp of this or more recent
     */
    public static Instant getMinimumPacketEventsBuildTimestamp() {
        // As taken from the most recent packetevents compiled jar
        return Instant.ofEpochMilli(1726865049970L);
    }

    public static boolean isPacketEventsOutdated(Instant requiredTime) {
        try {
            return PEVersions.BUILD_TIMESTAMP.isBefore(requiredTime);
        } catch (Throwable ignored) {
            // If error is thrown, then the field is missing and we're definitely outdated
        }

        return true;
    }

    private boolean isNotBukkitPlugin(String name) {
        return !name.toLowerCase(Locale.ENGLISH).matches(".*(bukkit|spigot|paper).*");
    }

    /**
     * Returns true if success
     */
    public boolean doUpdate() throws IOException {
        if (notedShadedPlugin) {
            LibsDisguises.getInstance().getLogger().warning(
                "Unable to update PacketEvents, it is not installed as a plugin and instead has likely been shaded into another plugin.");
            return false;
        }

        boolean outcome = doReleaseUpdate(getMinimumPacketEventsVersion());

        if (!outcome) {
            LibsDisguises.getInstance().getLogger().info(
                "Release builds of PacketEvents didn't match criteria for Lib's Disguises, now looking at PacketEvents snapshot builds");
            outcome = doSnapshotUpdate();
        }

        return outcome;
    }

    public boolean doSnapshotUpdate() {
        try {
            return doJenkinsUpdate();
        } catch (IOException e) {
            LibsDisguises.getInstance().getLogger().warning("Error while trying to retrieve snapshot builds for PacketEvents");
            e.printStackTrace();
        }

        return false;
    }

    public static void doShadedWarning() {
        if (isNotedShadedPlugin()) {
            return;
        }

        try {
            CodeSource src = PacketEvents.class.getProtectionDomain().getCodeSource();

            if (src == null) {
                return;
            }

            URL resource = src.getLocation();
            String path = resource.getPath().toLowerCase(Locale.ENGLISH);

            // If it's not in a jar file, who knows but we're not going to fuss over it
            if (!path.endsWith(".jar") && !(path.endsWith(".class") && !path.endsWith(".jar!"))) {
                return;
            }
            File file = Paths.get(resource.toURI()).toFile();
            YamlConfiguration yaml = ReflectionManager.getPluginYAML(file);
            String pluginName = yaml.getString("name");

            if (pluginName != null && pluginName.equalsIgnoreCase("packetevents")) {
                return;
            }

            if (pluginName == null) {
                LibsDisguises.getInstance().getLogger().info(
                    "Your installation of PacketEvents is curious, it's apparently in the file '" + file.getName() +
                        "' and not installed as a plugin as expected. If everything works, then don't worry about it.");
            } else {
                LibsDisguises.getInstance().getLogger().warning(
                    "PacketEvents looks like it has been shaded into the plugin '" + pluginName + "' which is in the file '" +
                        file.getName() +
                        "' which is not 'packetevents', this may be fine but it also means that Lib's Disguises can't help you if you're " +
                        "experiencing issues with packetevents because there's no good way for Lib's Disguises to help you update. You " +
                        "must instead ask the author of that plugin to either update or relocate their shaded libraries.");
            }

            notedShadedPlugin = true;
        } catch (Throwable ignored) {
        }
    }

    private boolean doJenkinsUpdate() throws IOException {
        DisguiseUpdate jenkins = new BaseJenkins("https://ci.codemc.io/job/retrooper/job/packetevents/").getLatestSnapshot();

        if (jenkins == null || jenkins.getDownloads().isEmpty()) {
            return false;
        }

        jenkins.getDownloads().removeIf(this::isNotBukkitPlugin);

        if (jenkins.getDownloads().size() != 1) {
            LibsDisguises.getInstance().getLogger().severe(
                "Failed to find jenkins update for PacketEvents, expected 1 download remaining but got: " +
                    String.join(", ", jenkins.getDownloads()));
            return false;
        }

        String url = jenkins.getDownload();
        String file = url.substring(url.lastIndexOf("/") + 1);

        downloadFile(url, file);
        return true;
    }

    public boolean doReleaseUpdate(@Nullable String requiredVersion) throws IOException {
        URL url = new URL("https://api.modrinth.com/v2/project/packetevents/version");
        // Creating a connection
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
        con.setRequestProperty("Accept", "application/json");

        // Get the input stream, what we receive
        try (InputStream input = con.getInputStream()) {
            // Read it to string
            String json =
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

            con.disconnect();

            ModrinthVersion[] versions = new Gson().fromJson(json, ModrinthVersion[].class);

            for (ModrinthVersion version : versions) {
                // If not listed, or its not a release
                if (version.files == null || !version.status.equals("listed") || !version.version_type.equals("release")) {
                    continue;
                }

                if (isNotBukkitPlugin(version.name)) {
                    continue;
                }

                // Weird, shouldn't get into this state
                if (requiredVersion != null && isOlderThan(requiredVersion, version.version_number)) {
                    continue;
                }

                for (ModrinthFile file : version.files) {
                    if (!file.filename.endsWith(".jar") || isNotBukkitPlugin(file.filename)) {
                        continue;
                    }

                    downloadFile(file.url, file.filename);
                    return true;
                }

            }
        }

        LibsDisguises.getInstance().getLogger().warning(
            "Failed to find a " + requiredVersion + " or higher release build of PacketEvents, we probably want the snapshot builds.");

        return false;
    }

    private File getDestination(String preferredName) {
        File dest = new File(LibsDisguises.getInstance().getDataFolder().getAbsoluteFile().getParentFile(), preferredName);

        try {
            Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
            getFile.setAccessible(true);

            File theirFile = (File) getFile.invoke(PacketEvents.getAPI().getPlugin());
            dest = new File(Bukkit.getUpdateFolderFile(), theirFile.getName());
        } catch (Throwable throwable) {
            // Fall back to trying to scanning files
            if (Bukkit.getPluginManager().getPlugin("packetevents") != null) {
                List<File> plJars = ReflectionManager.getFilesByPlugin("packetevents");

                if (plJars.size() > 1) {
                    // Its probably the first file regardless, Bukkit seems to use folder.listFiles() and use the order provided
                    LibsDisguises.getInstance().getLogger().warning(
                        "You have multiple PacketEvents jars in your plugin folder, you may need to update PacketEvents yourself.");
                }

                if (!plJars.isEmpty()) {
                    preferredName = plJars.get(0).getName();
                }

                dest = new File(Bukkit.getUpdateFolderFile(), preferredName);
            }
        }

        return dest;
    }

    private void downloadFile(String fileUrl, String filename) throws IOException {
        LibsDisguises.getInstance().getLogger().info("Now downloading " + filename);

        URL url = new URL(fileUrl);
        // Creating a connection
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "libraryaddict/LibsDisguises");
        con.setRequestProperty("Accept", "application/json");

        con.setDefaultUseCaches(false);

        File dest = this.destination = getDestination(filename);

        if (!dest.exists()) {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
        }

        // Get the input stream, what we receive
        try (InputStream input = con.getInputStream()) {
            Files.copy(input, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        con.disconnect();

        LibsDisguises.getInstance().getLogger().info(filename + " successfully downloaded and saved to " + dest.getPath());
        LibsDisguises.getInstance().setPacketEventsUpdateDownloaded(true);
    }

    /**
     * Copied from DisguiseUtilities
     */
    public static boolean isOlderThan(String requiredVersion, String theirVersion) {
        int[] required = getNumericVersion(requiredVersion);
        int[] has = getNumericVersion(theirVersion);

        for (int i = 0; i < Math.min(required.length, has.length); i++) {
            if (required[i] == has[i]) {
                continue;
            }

            return required[i] >= has[i];
        }

        return false;
    }

    /**
     * Copied from DisguiseUtilities
     */
    public static int[] getNumericVersion(String version) {
        int[] v = new int[0];
        for (String split : version.split("[.\\-]")) {
            if (!split.matches("\\d+")) {
                return v;
            }

            v = Arrays.copyOf(v, v.length + 1);
            v[v.length - 1] = Integer.parseInt(split);
        }

        return v;
    }

    public static boolean isPacketEventsOutdated() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("packetevents");

        if (plugin == null) {
            LibsDisguises.getInstance().getLogger().severe("PacketEvents not installed on server (as a plugin), must be missing!");
            return true;
        }

        String packetEventsVersion;

        try {
            packetEventsVersion = plugin.getDescription().getVersion();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return true;
        }

        return isOlderThan(PacketEventsUpdater.getMinimumPacketEventsVersion(), packetEventsVersion) ||
            isPacketEventsOutdated(getMinimumPacketEventsBuildTimestamp());
    }
}
