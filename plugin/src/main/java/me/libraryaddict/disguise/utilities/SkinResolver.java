package me.libraryaddict.disguise.utilities;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.reflection.LibsProfileLookup;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class SkinResolver {
    @Getter
    private final Disguise disguise;
    private final Consumer<UserProfile> skinUpdated;
    /**
     * The current lookup, null if not in progress
     */
    @Getter
    private volatile @Nullable LibsProfileLookup currentLookup;
    /**
     * This is set to false everytime a skin is assigned, true when the skin already failed (or worked!) to look up
     */
    private boolean skinNeedsToBeResolved = false;
    /**
     * In the case the skin is null, then it is assumed that there is no skin to be looked up
     */
    @Getter
    private @Nullable String skin;

    /**
     * Copies the values from another resolver to use in this resolver, if the resolver is not yet done
     *
     * @param resolver
     */
    public void copyResolver(SkinResolver resolver) {
        if (isSkinFullyResolved()) {
            skin = resolver.getSkin();
        } else {
            setSkin(resolver.getSkin());
        }
    }

    /**
     * Updates the UserProfile that's used
     */
    protected void onProfileUpdate(@Nullable UserProfile profile) {
        skinUpdated.accept(profile);
    }

    protected boolean isSkinFullyResolved() {
        return !skinNeedsToBeResolved && getCurrentLookup() == null;
    }

    private synchronized void setResolver(LibsProfileLookup resolver) {
        this.currentLookup = resolver;
        this.skinNeedsToBeResolved = false;
    }

    private synchronized LibsProfileLookup createResolver() {
        currentLookup = new LibsProfileLookup() {
            @Override
            public void onLookup(UserProfile userProfile) {
                synchronized (SkinResolver.this) {
                    if (currentLookup != this) {
                        return;
                    }

                    setResolver(null);
                }

                setSkinInternal(userProfile);
            }
        };

        return currentLookup;
    }

    protected String getSkinToLookup() {
        return getSkin();
    }

    public void lookupSkinIfNeeded() {
        if (!skinNeedsToBeResolved) {
            return;
        }

        lookupSkinByName();
    }

    public void lookupSkinByName() {
        skinNeedsToBeResolved = false;

        String skin = getSkinToLookup();

        if (skin == null) {
            return;
        }

        UserProfile userProfile = DisguiseUtilities.getProfileFromMojang(skin, createResolver(), DisguiseConfig.isContactMojangServers());

        if (userProfile == null) {
            return;
        }

        setSkinInternal(userProfile);
    }

    private final Pattern skinAndProfilePattern = Pattern.compile("^(?:(.*?):)?(\\{\"(?:uuid|id)\":.*?,\"name\":.*?})$");
    private final Pattern profilePattern = Pattern.compile("^(\\{\"(?:uuid|id)\":.*?,\"name\":.*?})$");

    public void setSkin(@Nullable String newSkin) {
        // Attempt to load via json first
        UserProfile profile = getProfileFromJson(newSkin);

        if (profile != null) {
            setSkin(profile);
            return;
        }

        // If multiline name, only use the first line as the skin name
        if (newSkin != null) {
            String[] split = DisguiseUtilities.splitNewLine(newSkin);

            if (split.length > 0) {
                newSkin = split[0];
            }
        }

        String oldSkin = this.skin;
        this.skin = newSkin;

        if (newSkin == null) {
            setSkinInternal(null);
            return;
        } else if (newSkin.equals(oldSkin)) {
            return;
        } else if (!getDisguise().isDisguiseInUse()) {
            // If the disguise isn't active yet, don't resolve the skin just yet! It might change!
            // ... though developers may hate me for this. But they should be caching the name already!
            skinNeedsToBeResolved = true;
            return;
        }

        lookupSkinByName();
    }

    public UserProfile getProfileFromJson(String string) {
        if (string == null || string.length() <= 70) {
            return null;
        }

        Matcher match = profilePattern.matcher(string);

        if (!match.find()) {
            return null;
        }

        try {
            return DisguiseUtilities.getGson().fromJson(match.group(1), UserProfile.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Tried to parse " + string + " to a GameProfile, but it has been formatted incorrectly!");
        }
    }

    private void setSkinInternal(UserProfile profile) {
        setResolver(null);
        onProfileUpdate(profile);
    }

    public void setSkinAndProfile(String skin, UserProfile profile) {
        this.skin = skin;
        setSkinInternal(profile);
    }

    /**
     * This clears the String skin, and sets the skin data to this
     *
     * @param userProfile
     */
    public void setSkin(UserProfile userProfile) {
        this.skin = null;

        setSkinInternal(userProfile);
    }
}
