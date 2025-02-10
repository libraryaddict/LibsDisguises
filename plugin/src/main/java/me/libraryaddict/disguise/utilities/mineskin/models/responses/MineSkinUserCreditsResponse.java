package me.libraryaddict.disguise.utilities.mineskin.models.responses;

import lombok.Getter;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinCredits;

@Getter
public class MineSkinUserCreditsResponse extends MineSkinResponse {
    private MineSkinCredits credits;
}
