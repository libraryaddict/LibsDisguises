package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
public class MineSkinJob {
    private String id;
    private MineSkinStatus status;
    private @Nullable String result;

    public boolean isJobRunning() {
        return status == MineSkinStatus.ACTIVE || status == MineSkinStatus.WAITING;
    }
}
