package me.libraryaddict.disguise.utilities;

import java.lang.reflect.Field;
import java.util.UUID;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.ProfileLookupCallback;

public class LibsProfileLookupCaller implements ProfileLookupCallback {
    private GameProfile gameProfile;

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    @Override
    public void onProfileLookupFailed(GameProfile gameProfile, Exception arg1) {
        try {
            Field field = GameProfile.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(gameProfile, UUID.randomUUID());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.onProfileLookupSucceeded(gameProfile);
    }

    @Override
    public void onProfileLookupSucceeded(GameProfile profile) {
        gameProfile = profile;
    }

}
