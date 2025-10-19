package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LibsProfileLookupCaller implements ProfileLookupCallback {
    private UserProfile userProfile;

    @Override
    public void onProfileLookupSucceeded(String s, UUID uuid) {
        userProfile = new UserProfile(uuid, s);
    }

    @Override
    public void onProfileLookupFailed(String s, Exception e) {
    }

    @Deprecated
    public void onProfileLookupFailed(GameProfile gameProfile, Exception arg1) {
    }

    @Deprecated
    public void onProfileLookupSucceeded(GameProfile profile) {
        userProfile = ReflectionManager.getUserProfile(profile);
    }
}
