package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import lombok.Getter;

@Getter
public class MineSkinApiKey {
    // API Key ID
    // Example: 21702ac21ab744d32b004fe0
    private String id;
    // API Key name
    // Example: Test Key
    private String name;
    // User ID
    // Example: ef4c52b01fea4715982d8e67fd93ef2d
    private String user;
    // Creation date
    // Example: 2022-11-01T18:06:58.634Z
    private String createdAt;
    // Allowed origins for this API key
    private String[] allowedOrigins;
    // Allowed IPs for this API key
    private String[] allowedIps;
    // Allowed User-Agents for this API key
    private String[] allowedAgents;
    // Whether this API key uses paid credits
    private boolean useCredits;
}
