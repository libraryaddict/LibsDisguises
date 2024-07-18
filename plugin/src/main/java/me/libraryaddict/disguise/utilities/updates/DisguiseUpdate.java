package me.libraryaddict.disguise.utilities.updates;

import java.util.Date;
import java.util.List;

public interface DisguiseUpdate {
    /**
     * Null if invalid
     */
    String getVersion();

    boolean isReleaseBuild();

    List<String> getDownloads();

    String getDownload();

    String[] getChangelog();

    /**
     * When was this update fetched?
     */
    Date getFetched();
}
