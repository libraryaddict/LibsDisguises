package me.libraryaddict.disguise.utilities.reflection;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import lombok.Getter;

@Getter
public class LibsProfileLookupCaller implements ProfileLookupCallback {
    private WrappedGameProfile gameProfile;

    @Override
    public void onProfileLookupFailed(String s, Exception e) {
    }

    @Deprecated
    public void onProfileLookupFailed(GameProfile gameProfile, Exception arg1) {
    }

    @Override
    public void onProfileLookupSucceeded(GameProfile profile) {
        gameProfile = WrappedGameProfile.fromHandle(profile);
    }
}
