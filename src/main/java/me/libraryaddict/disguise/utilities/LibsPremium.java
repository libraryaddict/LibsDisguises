package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by libraryaddict on 2/06/2017.
 */
public class LibsPremium {
    private static Boolean thisPluginIsPaidFor;
    /**
     * Information of the actively running plugin
     */
    private static PluginInformation pluginInformation;
    /**
     * Information of the plugin used to activate premium, if exists
     */
    private static PluginInformation paidInformation;

    public static PluginInformation getPluginInformation() {
        return pluginInformation;
    }

    public static PluginInformation getPaidInformation() {
        return paidInformation;
    }

    /**
     * @return Account ID if downloaded through SpigotMC
     */
    public static String getUserID() {
        return "%%__USER__%%";
    }

    /**
     * @return Resource ID if downloaded through SpigotMC
     */
    public static String getResourceID() {
        return "%%__RESOURCE__%%";
    }

    /**
     * @return Download ID if downloaded through SpigotMC
     */
    public static String getDownloadID() {
        return "%%__NONCE__%%";
    }

    /**
     * @param userID
     * @return true if userID does not contain __USER__
     */
    private static Boolean isPremium(String userID) {
        return !userID.contains("__USER__");
    }

    /**
     * Returns true if this plugin is premium
     */
    public static Boolean isPremium() {
        return thisPluginIsPaidFor == null ? isPremium(getUserID()) : thisPluginIsPaidFor;
    }

    /**
     * Checks if the premiumVersion can work on the current version
     */
    private static boolean isValidVersion(String currentVersion, String premiumVersion) {
        currentVersion = currentVersion.replaceAll("(v)|(-SNAPSHOT)", "");

        // Premium version must be using an accepted versioning system
        if (!premiumVersion.matches("[0-9]+(\\.[0-9]+)+")) {
            return false;
        }

        // If current version is not a number version, then the premium version cannot be checked
        if (!currentVersion.matches("[0-9]+(\\.[0-9]+)+")) {
            // Return true as the rest of the version check cannot be used
            return true;
        }

        // Split by decimal points
        String[] currentSplit = currentVersion.split("\\.");
        String[] premSplit = premiumVersion.split("\\.");

        // Comparing major versions
        // Current version must be the same, or lower than premium version
        return Integer.parseInt(currentSplit[0]) <= Integer.parseInt(premSplit[0]);

        // Snapshot must be of current version
        //return premiumVersion.equals(currentVersion);
    }

    private static PluginInformation getInformation(File file) throws Exception {
        try (URLClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
            Class c = cl.loadClass(LibsPremium.class.getName());

            boolean oldJarFile = true;

            try {
                // Error thrown if method doesn't exist
                c.getMethod("getUserID");
                // Method exists, is not older file
                oldJarFile = false;
            }
            catch (Exception ignored) {
            }

            // Fetch the plugin.yml from the jar file
            YamlConfiguration config = ReflectionManager.getPluginYaml(cl);
            // No checks for null config as the correct error will be thrown on access

            Boolean premium;
            String userId = null;
            String downloadId = null;
            String resourceId = null;

            if (oldJarFile) {
                premium = (Boolean) c.getMethod("isPremium").invoke(null);
            } else {
                userId = (String) c.getMethod("getUserID").invoke(null);
                resourceId = (String) c.getMethod("getResourceID").invoke(null);
                downloadId = (String) c.getMethod("getDownloadID").invoke(null);
                premium = isPremium(userId);
            }

            String pluginBuildDate = "??/??/????";

            // If plugin.yml contains a build-date
            if (config.contains("build-date")) {
                pluginBuildDate = config.getString("build-date");
            }

            String pluginBuildNumber = "???";

            // If plugin.yml contains a jenkins build number
            if (config.contains("build-number")) {
                pluginBuildNumber = config.getString("build-number");

                // If build number is composed of purely numbers, prepend with # for readability
                if (pluginBuildNumber.matches("[0-9]+")) {
                    pluginBuildNumber = "#" + pluginBuildNumber;
                }
            }

            String pluginVersion = config.getString("version");

            return new PluginInformation(userId, resourceId, downloadId, premium, pluginVersion, pluginBuildNumber,
                    pluginBuildDate);
        }
    }

    private static void doSecondaryCheck(String version) {
        File[] files = new File("plugins/LibsDisguises/").listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (!file.isFile())
                continue;

            if (!file.getName().endsWith(".jar"))
                continue;

            PluginInformation plugin;

            try {
                plugin = getInformation(file);
            }
            catch (ClassNotFoundException ex) {
                DisguiseUtilities.getLogger()
                        .warning("Found an unrecognized jar in the LibsDisguises folder (" + file.getName() + ")");
                continue;
            }
            catch (Exception ex) {
                DisguiseUtilities.getLogger().warning("Error while trying to handle the file " + file.getName());
                ex.printStackTrace();
                continue;
            }

            // Format into a string
            // v5.2.6, build #40, created 16/02/2019
            String fileInfo = String.format("v%s, build %s, created %s", plugin.getVersion(), plugin.getBuildNumber(),
                    plugin.getBuildDate());

            if (plugin.isPremium()) {
                if (!isValidVersion(version, plugin.getVersion()) || plugin.getUserID() == null ||
                        plugin.getDownloadID() == null || plugin.getUserID().equals("666666")) {
                    DisguiseUtilities.getLogger().warning(
                            "You have an old Lib's Disguises jar (" + file.getName() + " " + fileInfo +
                                    ") in the LibsDisguises folder! For security purposes, please replace this with a" +
                                    " new " +
                                    "version from SpigotMC - https://www.spigotmc.org/resources/libs-disguises.32453/");
                    continue;
                }

                paidInformation = plugin;

                thisPluginIsPaidFor = true;
                // Found a premium Lib's Disguises jar (v5.2.6, build #40, created 16/02/2019)
                DisguiseUtilities.getLogger().info("Found a premium Lib's Disguises jar (" + fileInfo + ")");
                DisguiseUtilities.getLogger().info("Registered to: " + getSanitizedUser(plugin.getUserID()));

                break;
            } else {
                // You have a non-premium Lib's Disguises jar (LibsDisguises.jar v5.2.6, build #40, created
                // 16/02/2019) in the LibsDisguises folder!
                DisguiseUtilities.getLogger().warning(
                        "You have a non-premium Lib's Disguises jar (" + file.getName() + " " + fileInfo +
                                ") in the LibsDisguises folder!");
            }
        }
    }

    private static String getSanitizedUser(String userID) {
        if (userID == null) {
            return "N/A";
        }

        if (!userID.matches("[0-9]+")) {
            return String.format("... %s? Am I reading this right?", userID);
        }

        int total = 0;

        for (char c : userID.toCharArray()) {
            total += Character.getNumericValue(c);
        }

        return String.format("%s (%s)", userID, total);
    }

    public static void check(String version, File file) {
        thisPluginIsPaidFor = isPremium();

        try {
            pluginInformation = getInformation(file);
        }
        catch (Exception e) {
            String pluginBuildDate = "??/??/????";

            YamlConfiguration config = new YamlConfiguration();

            try {
                config.loadFromString(IOUtils.toString(LibsDisguises.getInstance().getResource("plugin.yml"), "UTF-8"));

                // If plugin.yml contains a build-date
                if (config.contains("build-date")) {
                    pluginBuildDate = config.getString("build-date");
                }
            }
            catch (InvalidConfigurationException | IOException ex) {
                ex.printStackTrace();
            }

            String buildNo = LibsDisguises.getInstance().getBuildNo();

            if (buildNo != null && buildNo.matches("[0-9]+")) {
                buildNo = "#" + buildNo;
            }

            pluginInformation = new PluginInformation(getUserID(), getResourceID(), getDownloadID(),
                    isPremium(getUserID()), version, buildNo, pluginBuildDate);
        }

        if (!isPremium() || !LibsDisguises.getInstance().isReleaseBuild()) {
            doSecondaryCheck(version);
        } else {
            DisguiseUtilities.getLogger().info("Registered to: " + getSanitizedUser(getUserID()));
        }

        if (isPremium()) {
            boolean prem =
                    getPaidInformation() == null ? getPluginInformation().isLegit() : getPaidInformation().isLegit();

            DisguiseUtilities.getLogger()
                    .info("Premium enabled, thank you for supporting Lib's Disguises!" + (!prem ? "!" : ""));
        }
    }
}
