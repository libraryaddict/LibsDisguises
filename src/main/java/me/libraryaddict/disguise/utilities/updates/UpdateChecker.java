package me.libraryaddict.disguise.utilities.updates;

import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class UpdateChecker {
    private final String resourceID;
    private final long started = System.currentTimeMillis();
    @Getter
    private PluginInformation lastDownload;
    private final AtomicBoolean downloading = new AtomicBoolean(false);
    @Getter
    private DisguiseUpdate update;
    private LDGithub githubUpdater = new LDGithub();
    private LDJenkins jenkinsUpdater = new LDJenkins();
    @Getter
    private String[] updateMessage = new String[0];

    public UpdateChecker(String resourceID) {
        this.resourceID = resourceID;
    }

    public boolean isDownloading() {
        return downloading.get();
    }

    public boolean isUsingReleaseBuilds() {
        DisguiseConfig.UpdatesBranch builds = DisguiseConfig.getUpdatesBranch();

        return builds == DisguiseConfig.UpdatesBranch.RELEASES ||
                (builds == DisguiseConfig.UpdatesBranch.SAME_BUILDS && DisguiseConfig.isUsingReleaseBuild());
    }

    public void notifyUpdate(CommandSender player) {
        if (!DisguiseConfig.isNotifyUpdate() || !player.hasPermission("libsdisguises.update")) {
            return;
        }

        if (updateMessage == null || updateMessage.length == 0) {
            return;
        }

        if (player instanceof Player) {
            player.sendMessage(updateMessage);
        } else {
            for (String s : updateMessage) {
                DisguiseUtilities.getLogger().info(s);
            }
        }
    }

    public boolean isUpdateReady() {
        if (getUpdate() == null) {
            return false;
        }

        String version;

        if (getUpdate().isReleaseBuild()) {
            if (lastDownload != null) {
                version = lastDownload.getVersion();
            } else {
                version = LibsDisguises.getInstance().getDescription().getVersion();
            }
        } else {
            if (lastDownload != null) {
                version = lastDownload.getBuildNumber();
            } else {
                version = LibsDisguises.getInstance().getBuildNo();
            }
        }

        return getUpdate() != null && !getUpdate().getVersion().equals(version);
    }

    public void doAutoUpdateCheck() {
        try {
            DisguiseUpdate oldUpdate = getUpdate();

            updateMessage = new String[0];

            doUpdateCheck();

            if (!isUpdateReady() || (oldUpdate != null && oldUpdate.getVersion().equals(getUpdate().getVersion()))) {
                return;
            }

            notifyUpdate(Bukkit.getConsoleSender());

            if (DisguiseConfig.isAutoUpdate()) {
                // Update message changed by download
                grabJarDownload(getUpdate().getDownload());

                notifyUpdate(Bukkit.getConsoleSender());
            }

            Bukkit.getScheduler().runTask(LibsDisguises.getInstance(), () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    notifyUpdate(p);
                }
            });
        }
        catch (Exception ex) {
            DisguiseUtilities.getLogger().warning(String.format("Failed to check for update: %s", ex.getMessage()));
        }
    }

    public PluginInformation doUpdate() {
        // If no update on file, or more than 6 hours hold. Check for update
        if (getUpdate() == null ||
                getUpdate().getFetched().before(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(6)))) {
            doUpdateCheck();
        }

        if (getUpdate() == null) {
            return null;
        }

        return grabJarDownload(getUpdate().getDownload());
    }

    public LibsMsg doUpdateCheck() {
        downloading.set(false);

        try {
            update = null;

            if (isUsingReleaseBuilds()) {
                update = githubUpdater.getLatestRelease();
            } else {
                update = jenkinsUpdater.getLatestSnapshot();
            }
        }
        finally {
            downloading.set(false);
        }

        if (getUpdate() == null) {
            return LibsMsg.UPDATE_FAILED;
        }

        if (getUpdate().isReleaseBuild()) {
            String currentVersion = LibsDisguises.getInstance().getDescription().getVersion();

            if (!isNewerVersion(currentVersion, getUpdate().getVersion())) {
                return LibsMsg.UPDATE_ON_LATEST;
            }

            updateMessage = new String[]{LibsMsg.UPDATE_READY.get(currentVersion, getUpdate().getVersion())};
        } else {
            if (!getUpdate().getVersion().matches("[0-9]+")) {
                return LibsMsg.UPDATE_FAILED;
            }

            int newBuild = Integer.parseInt(getUpdate().getVersion());

            if (newBuild <= LibsDisguises.getInstance().getBuildNumber()) {
                return LibsMsg.UPDATE_ON_LATEST;
            }

            updateMessage = new String[]{
                    LibsMsg.UPDATE_READY_SNAPSHOT.get(LibsDisguises.getInstance().getBuildNo(), newBuild)};
        }

        return null;
    }

    private PluginInformation grabJarDownload(String urlString) {
        downloading.set(true);

        DisguiseUtilities.getLogger().info("Now downloading build of Lib's Disguises from " + urlString);

        File dest = new File(Bukkit.getUpdateFolderFile(), LibsDisguises.getInstance().getFile().getName());

        if (dest.exists()) {
            dest.delete();
        }

        dest.getParentFile().mkdirs();

        try {
            // We're connecting to spigot's API
            URL url = new URL(urlString);
            // Creating a connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDefaultUseCaches(false);

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                FileUtils.copyInputStreamToFile(input, dest);
            }

            DisguiseUtilities.getLogger().info("Download success!");

            PluginInformation result = LibsPremium.getInformation(dest);
            lastDownload = result;

            updateMessage = new String[]{LibsMsg.UPDATE_SUCCESS.get(),
                    LibsMsg.UPDATE_INFO.get(result.getVersion(), result.getBuildNumber(),
                            result.getParsedBuildDate().toString(), result.getSize() / 1024)};

            return result;
        }
        catch (Exception ex) {
            // Failed, set the last download back to previous build
            dest.delete();
            DisguiseUtilities.getLogger().warning("Failed to download snapshot build.");
            ex.printStackTrace();
        }
        finally {
            downloading.set(false);
        }

        return null;
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
            con.setDefaultUseCaches(false);

            // Get the input stream, what we receive
            try (InputStream input = con.getInputStream()) {
                // Read it to string
                String version = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));

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
        currentVersion = currentVersion.replaceAll("(v)|(-SNAPSHOT)", "");
        newVersion = newVersion.replaceAll("(v)|(-SNAPSHOT)", "");

        // If the server has been online for less than 6 hours and both versions are 1.1.1 kind of versions
        if (started + TimeUnit.HOURS.toMillis(6) > System.currentTimeMillis() &&
                currentVersion.matches("[0-9]+(\\.[0-9]+)*") && newVersion.matches("[0-9]+(\\.[0-9]+)*")) {

            int cVersion = Integer.parseInt(currentVersion.replace(".", ""));
            int nVersion = Integer.parseInt(newVersion.replace(".", ""));

            // If the current version is a higher version, and is only a higher version by 3 minor numbers
            // Then we have a cache problem
            if (cVersion > nVersion && nVersion + 3 > cVersion) {
                return false;
            }
        }

        // Lets just ignore all this fancy logic, and say that if you're not on the current release, you're outdated!
        return !currentVersion.equals(newVersion);
    }
}
