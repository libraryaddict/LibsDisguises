package me.libraryaddict.disguise.utilities.updates;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateChecker {
    private final long started = System.currentTimeMillis();
    @Getter
    private PluginInformation lastDownload;
    private final AtomicBoolean downloading = new AtomicBoolean(false);
    @Getter
    private DisguiseUpdate update;
    private final LDGithub githubUpdater = new LDGithub(this);
    private final LDJenkins jenkinsUpdater = new LDJenkins();
    @Getter
    private String[] updateMessage = new String[0];
    @Getter
    @Setter
    private boolean goSilent;

    public boolean isServerLatestVersion() {
        return isOnLatestUpdate(false);
    }

    public boolean isOnLatestUpdate(boolean includeDownloaded) {
        if (getUpdate() == null) {
            return false;
        }

        boolean isRelease =
                includeDownloaded && getLastDownload() != null ? !getLastDownload().getVersion().contains("-SNAPSHOT") :
                        LibsDisguises.getInstance().isReleaseBuild();

        if (getUpdate().isReleaseBuild() != isRelease) {
            return false;
        }

        String version;

        if (getUpdate().isReleaseBuild()) {
            if (lastDownload != null && includeDownloaded) {
                version = lastDownload.getVersion();
            } else {
                version = LibsDisguises.getInstance().getDescription().getVersion();
            }
        } else {
            if (lastDownload != null && includeDownloaded) {
                version = lastDownload.getBuildNumber();
            } else {
                version = LibsDisguises.getInstance().getBuildNo();
            }
        }

        return getUpdate() != null && getUpdate().getVersion().equals(version);
    }

    public boolean isDownloading() {
        return downloading.get();
    }

    public boolean isOldUpdate() {
        return getUpdate() == null ||
                getUpdate().getFetched().before(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)));
    }

    public boolean isUsingReleaseBuilds() {
        DisguiseConfig.UpdatesBranch builds = DisguiseConfig.getUpdatesBranch();

        return builds == DisguiseConfig.UpdatesBranch.RELEASES ||
                (builds == DisguiseConfig.UpdatesBranch.SAME_BUILDS && DisguiseConfig.isUsingReleaseBuild());
    }

    public void notifyUpdate(CommandSender player) {
        if (isGoSilent() || !DisguiseConfig.isNotifyUpdate() || !player.hasPermission("libsdisguises.update")) {
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

    public void doAutoUpdateCheck() {
        try {
            DisguiseUpdate oldUpdate = getUpdate();

            updateMessage = new String[0];
            boolean alreadySilent = isGoSilent();

            doUpdateCheck();

            if (isOnLatestUpdate(true) ||
                    (oldUpdate != null && oldUpdate.getVersion().equals(getUpdate().getVersion()))) {
                return;
            }

            notifyUpdate(Bukkit.getConsoleSender());

            if (isGoSilent() ? !alreadySilent : DisguiseConfig.isAutoUpdate()) {
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
        DisguiseConfig.setLastUpdateRequest(System.currentTimeMillis());
        DisguiseConfig.saveInternalConfig();

        downloading.set(true);

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

            if (LibsDisguises.getInstance().isReleaseBuild() &&
                    !isNewerVersion(currentVersion, getUpdate().getVersion())) {
                return LibsMsg.UPDATE_ON_LATEST;
            }

            updateMessage = new String[]{LibsMsg.UPDATE_READY.get(currentVersion, getUpdate().getVersion()),
                    LibsMsg.UPDATE_HOW.get()};
        } else {
            if (!getUpdate().getVersion().matches("[0-9]+")) {
                return LibsMsg.UPDATE_FAILED;
            }

            int newBuild = Integer.parseInt(getUpdate().getVersion());

            if (newBuild <= LibsDisguises.getInstance().getBuildNumber()) {
                return LibsMsg.UPDATE_ON_LATEST;
            }

            String build = LibsDisguises.getInstance().getBuildNo();

            updateMessage = new String[]{
                    LibsMsg.UPDATE_READY_SNAPSHOT.get((build.matches("[0-9]+") ? "#" : "") + build, newBuild),
                    LibsMsg.UPDATE_HOW.get()};
        }

        return null;
    }

    private PluginInformation grabJarDownload(String urlString) {
        downloading.set(true);

        File dest = new File(Bukkit.getUpdateFolderFile(), LibsDisguises.getInstance().getFile().getName());

        if (!isGoSilent()) {
            DisguiseUtilities.getLogger()
                    .info("Now downloading build of Lib's Disguises from " + urlString + " to " + dest.getName());
        }

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
                Files.copy(input, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (!isGoSilent()) {
                DisguiseUtilities.getLogger().info("Download success!");
            }

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
