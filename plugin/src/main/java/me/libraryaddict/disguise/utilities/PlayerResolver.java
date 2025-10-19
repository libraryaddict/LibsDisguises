package me.libraryaddict.disguise.utilities;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class PlayerResolver extends SkinResolver {
    @Getter
    private final PlayerDisguise disguise;
    // This is never null when retrieved
    private UserProfile userProfile;

    public PlayerResolver(PlayerDisguise disguise, Consumer<UserProfile> skinUpdated) {
        super(disguise, skinUpdated);

        this.disguise = disguise;
    }

    @Override
    protected void onProfileUpdate(@Nullable UserProfile profile) {
        sanitizeSkin(profile);

        super.onProfileUpdate(profile);
    }

    @Override
    public void copyResolver(SkinResolver resolver) {
        super.copyResolver(resolver);

        if (!resolver.isSkinFullyResolved()) {
            return;
        }

        sanitizeSkin(((PlayerResolver) resolver).getUserProfile());
    }

    private void sanitizeSkin(UserProfile userProfile) {
        this.userProfile =
            ReflectionManager.getUserProfileWithThisSkin(getDisguise().getUUID(), getDisguise().getProfileName(), userProfile);
    }

    @Override
    public synchronized boolean isSkinFullyResolved() {
        return getCurrentLookup() == null && userProfile != null;
    }

    public void ensureUniqueProfile() {
        if (!hasIncorrectProfileUUIDOrName()) {
            return;
        }

        sanitizeSkin(userProfile);
    }

    @Override
    protected String getSkinToLookup() {
        String skinToLookup = super.getSkinToLookup();

        if (skinToLookup == null) {
            skinToLookup = getDisguise().getName();
        }

        return skinToLookup;
    }

    public UserProfile getUserProfile() {
        if (userProfile == null) {
            // Attempts to fetch the skin
            lookupSkinByName();

            // Sanitizes the skin if it exists, or null
            sanitizeSkin(userProfile);
        }

        return userProfile;
    }

    /**
     * If the user profile internally exists, and the uuid or name are not correct
     */
    public boolean hasIncorrectProfileUUIDOrName() {
        if (userProfile == null) {
            return false;
        }

        // The UUID check isn't needed as we should never be using a profile given to us externally
        return !Objects.equals(getUserProfile().getName(), getDisguise().getProfileName());
    }
}
