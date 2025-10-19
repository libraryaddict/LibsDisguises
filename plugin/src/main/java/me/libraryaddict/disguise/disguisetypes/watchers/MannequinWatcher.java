package me.libraryaddict.disguise.disguisetypes.watchers;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.SkinResolver;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class MannequinWatcher extends AvatarWatcher {
    private final SkinResolver skinResolver;

    public MannequinWatcher(Disguise disguise) {
        super(disguise);
        skinResolver = new SkinResolver(disguise, this::setSkin);
    }

    @Override
    public MannequinWatcher clone(Disguise disguise) {
        MannequinWatcher watcher = (MannequinWatcher) super.clone(disguise);
        watcher.skinResolver.copyResolver(skinResolver);

        return watcher;
    }

    @Override
    protected void onPreDisguiseStart() {
        super.onPreDisguiseStart();

        skinResolver.lookupSkinIfNeeded();
    }

    @Override
    public @Nullable String getSkinName() {
        return skinResolver.getSkin();
    }

    @Override
    public void setSkin(String playerName) {
        if (playerName != null && playerName.startsWith("{") && playerName.startsWith("}")) {
            // Tries to load it as json
            try {
                // If valid json, will not error
                // If valid json but wasn't meant to be, well, it wasn't a player name to begin with and they did the command wrong.
                setSkin(DisguiseUtilities.getGson().fromJson(playerName, ItemProfile.class));

                return;
            } catch (Exception ignored) {
                // It wasn't actually json! Most likely!
            }
        }

        skinResolver.setSkin(playerName);
    }

    @Override
    public void setSkin(@Nullable UserProfile profile) {
        // We handle as UserProfile for consistency, and because packetevents lets us...
        ItemProfile itemProfile;

        if (profile == null) {
            itemProfile = new ItemProfile(null, null, new ArrayList<>());
        } else {
            itemProfile = DisguiseUtilities.getItemProfile(profile);
        }

        setSkin(itemProfile);
    }

    public @NotNull ItemProfile getSkin() {
        return getData(MetaIndex.MANNEQUIN_PROFILE);
    }

    @RandomDefaultValue
    public void setSkin(@Nullable ItemProfile profile) {
        if (profile == null) {
            profile = new ItemProfile(null, null, new ArrayList<>());
        }

        sendData(MetaIndex.MANNEQUIN_PROFILE, profile);
    }

    public @Nullable Component getDescription() {
        return getData(MetaIndex.MANNEQUIN_DESCRIPTION).orElse(null);
    }

    public void setDescription(@Nullable Component description) {
        setData(MetaIndex.MANNEQUIN_DESCRIPTION, Optional.of(description));
    }
}
