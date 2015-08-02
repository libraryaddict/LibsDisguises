package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;

public class LibsProfileLookupCaller implements ProfileLookupCallback {

    private WrappedGameProfile gameProfile;

    public WrappedGameProfile getGameProfile() {
        return gameProfile;
    }

    @Override
    public void onProfileLookupFailed(GameProfile gameProfile, Exception arg1) {
    }

    @Override
    public void onProfileLookupSucceeded(GameProfile profile) {
        gameProfile = WrappedGameProfile.fromHandle(profile);
    }

}
