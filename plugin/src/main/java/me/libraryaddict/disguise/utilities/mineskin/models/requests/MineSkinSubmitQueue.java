package me.libraryaddict.disguise.utilities.mineskin.models.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVariant;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.SkinVisibility;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Accessors(chain = true)
public abstract class MineSkinSubmitQueue {
    private @Nullable String name;
    private @Nullable SkinVariant variant = SkinVariant.UNKNOWN;
    private @Nullable SkinVisibility visibility = SkinVisibility.PUBLIC;
}
