package me.libraryaddict.disguise.utilities.mineskin.models.responses;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinJob;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinRateLimit;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinSkin;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinUsage;
import org.jetbrains.annotations.Nullable;

@Getter
public class MineSkinQueueResponse extends MineSkinResponse {
    private MineSkinJob job;
    private @Nullable MineSkinSkin skin;
    private MineSkinRateLimit rateLimit;
    private MineSkinUsage usage;

    public GameProfile getGameProfile() {
        if (skin == null) {
            return null;
        }

        return skin.getGameProfile();
    }
}
