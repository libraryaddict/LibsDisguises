package me.libraryaddict.disguise.utilities.mineskin.models.responses;

import lombok.Getter;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinApiKey;

@Getter
public class MineSkinApiResponse extends MineSkinResponse {
    private MineSkinApiKey key;
}
