package me.libraryaddict.disguise.utilities.updates;

import java.util.Date;
import java.util.List;

/**
 * Created by libraryaddict on 26/04/2020.
 */
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
