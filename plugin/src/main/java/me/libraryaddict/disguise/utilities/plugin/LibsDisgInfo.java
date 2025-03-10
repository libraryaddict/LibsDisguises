package me.libraryaddict.disguise.utilities.plugin;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Getter
public class LibsDisgInfo {
    private final long size;
    private final String userID;
    private final String resourceID;
    private final String downloadID;
    private final boolean premium;
    private final String version;
    private final String buildNumber;
    private final String buildDate;

    public LibsDisgInfo(long size, String userID, String resourceID, String downloadID, boolean premium, String version, String buildNumber,
                        String buildDate) {
        this.size = size;
        this.userID = userID;
        this.resourceID = resourceID;
        this.downloadID = downloadID;
        this.premium = premium;
        this.version = version;
        this.buildNumber = buildNumber;
        this.buildDate = buildDate;
    }

    public Date getParsedBuildDate() {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(getBuildDate());
        } catch (Exception ignored) {
        }

        return null;
    }

    public boolean isPaid() {
        return !"12345".equals(getUserID()) && getResourceID().equals("32453") && getUserID().matches("\\d+") &&
            getDownloadID().matches("-?\\d+");
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LibsDisgInfo that = (LibsDisgInfo) object;
        return premium == that.premium && Objects.equals(userID, that.userID) && Objects.equals(resourceID, that.resourceID) &&
            Objects.equals(downloadID, that.downloadID) && Objects.equals(version, that.version) &&
            Objects.equals(buildNumber, that.buildNumber) && Objects.equals(buildDate, that.buildDate);
    }
}
