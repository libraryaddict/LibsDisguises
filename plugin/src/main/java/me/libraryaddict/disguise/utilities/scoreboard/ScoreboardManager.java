package me.libraryaddict.disguise.utilities.scoreboard;

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.scoreboard.Scoreboard;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ScoreboardManager {
    Map<String, DisguiseScoreboardTeam> getTeams();

    Collection<Scoreboard> getAllScoreboards();

    void updateExtendedName(PlayerDisguise disguise);

    void registerExtendedName(PlayerDisguise disguise);

    void unregisterExtendedName(PlayerDisguise removed);

    void registerNoName(Player player);

    void registerAllExtendedNames(Player player);

    void setGlowColor(UUID uuid, ChatColor color);

    void registerColors(Player player);

    default void registerTeams(Player player) {
        registerNoName(player);
        registerAllExtendedNames(player);
        registerColors(player);
    }

    DisguiseScoreboardTeam createScoreTeam(PlayerDisguise disguise, String[] split);

    void onEnable();

    void onDisable();
}
