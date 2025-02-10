package me.libraryaddict.disguise.utilities.mineskin.models.responses;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.utilities.mineskin.models.structures.MineSkinNotification;

import java.util.Map;

@Getter
public class MineSkinResponse {
    @Setter
    private int responseCode;
    private boolean success;
    private MineSkinNotification[] errors;
    private MineSkinNotification[] warnings;
    private MineSkinNotification[] messages;
    // Known key/values are 'skin' and an url string as value
    private Map<String, Object> links;
}
