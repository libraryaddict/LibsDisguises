package me.libraryaddict.disguise.utilities.updates;

import java.util.Date;

/**
 * Created by libraryaddict on 26/04/2020.
 */
public interface DisguiseUpdate {
    /**
     * Null if invalid
     */
    String getVersion();

    boolean isReleaseBuild();

    String getDownload();

    String[] getChangelog();

    /**
     * When was this update fetched?
     */
    Date getFetched();
}
