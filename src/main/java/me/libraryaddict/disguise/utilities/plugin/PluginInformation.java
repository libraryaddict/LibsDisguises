package me.libraryaddict.disguise.utilities.plugin;

import me.libraryaddict.disguise.utilities.LibsPremium;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by libraryaddict on 20/06/2019.
 */
public class PluginInformation {
    private String userID;
    private String resourceID;
    private String downloadID;
    private boolean premium;
    private String version;
    private String buildNumber;
    private String buildDate;

    public PluginInformation(String userID, String resourceID, String downloadID, boolean premium, String version,
            String buildNumber, String buildDate) {
        this.userID = userID;
        this.resourceID = resourceID;
        this.downloadID = downloadID;
        this.premium = premium;
        this.version = version;
        this.buildNumber = buildNumber;
        this.buildDate = buildDate;
    }

    public String getUserID() {
        return userID;
    }

    public String getResourceID() {
        return resourceID;
    }

    public String getDownloadID() {
        return downloadID;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public Date getParsedBuildDate() {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(getBuildDate());
        }
        catch (Exception ex) {
        }

        return null;
    }

    public boolean isLegit() {
        return getUserID().matches("[0-9]+") && !getUserID().equals("12345") && getResourceID().equals("32453") && getDownloadID().matches("-?[0-9]+");
    }
}
