package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.protocol.player.UserProfile;

public interface LibsProfileLookup {
    void onLookup(UserProfile gameProfile);
}
