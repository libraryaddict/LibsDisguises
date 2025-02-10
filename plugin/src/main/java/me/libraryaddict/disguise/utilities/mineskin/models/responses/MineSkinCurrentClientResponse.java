package me.libraryaddict.disguise.utilities.mineskin.models.responses;

import lombok.Getter;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinClient;

@Getter
public class MineSkinCurrentClientResponse extends MineSkinResponse {
    private MineSkinClient client;
}
