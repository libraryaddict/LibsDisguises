package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import lombok.Getter;

@Getter
public class LibsProfileLookupCaller implements ProfileLookupCallback {
    private UserProfile userProfile;

    @Override
    public void onProfileLookupFailed(String s, Exception e) {
    }

    @Deprecated
    public void onProfileLookupFailed(GameProfile gameProfile, Exception arg1) {
    }

    @Override
    public void onProfileLookupSucceeded(GameProfile profile) {
        userProfile = ReflectionManager.getUserProfile(profile);
    }
}
