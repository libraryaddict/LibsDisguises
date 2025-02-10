package me.libraryaddict.disguise.utilities.mineskin.models.responses;

import lombok.Getter;

import java.util.Map;

@Getter
public class MineSkinUserResponse extends MineSkinResponse {
    // User UUID
    private String user;
    // Grants for this user
    // Example: {"delay":3,"per_minute":20,"concurrency":1,"priority":0,"ad_free":true,"private_skins":true}
    private Map<String, Object> grants;
}
