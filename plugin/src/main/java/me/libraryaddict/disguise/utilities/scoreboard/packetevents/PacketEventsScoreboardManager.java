package me.libraryaddict.disguise.utilities.scoreboard.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.scoreboard.AbstractScoreboardManager;
import me.libraryaddict.disguise.utilities.scoreboard.DisguiseScoreboardTeam;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class PacketEventsScoreboardManager extends AbstractScoreboardManager {
    @Getter
    private final PacketEventsScoreboardTracker tracker = new PacketEventsScoreboardTracker();

    public PacketEventsScoreboardManager() {
        PacketsManager.getPacketsManager().registerScoreboardTeamListener(this);
    }

    @Override
    public Collection<Scoreboard> getAllScoreboards() {
        return Collections.emptyList();
    }

    private NamedTextColor getNamedTextColor(ChatColor color) {
        if (color == null || !color.isColor()) {
            return NamedTextColor.WHITE;
        }

        return NamedTextColor.NAMES.value(color.name().toLowerCase(Locale.ENGLISH));
    }

    @Override
    protected void updateRegisteredTeam(PlayerDisguise disguise, DisguiseScoreboardTeam team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTeamCreate(player, team, disguise.isNameVisible(), disguise.getWatcher().getGlowColor());
        }
    }

    @Override
    protected void removeRegisteredTeam(String teamName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTeamRemove(player, teamName);
        }
    }

    @Override
    public void registerNoName(Player player) {
        sendNoNameTeam(player);
    }

    @Override
    public void registerAllExtendedNames(Player player) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        for (Set<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
            for (Disguise disguise : disguises) {
                if (!disguise.isPlayerDisguise() || !disguise.isDisguiseInUse()) {
                    continue;
                }

                DisguiseScoreboardTeam name = ((PlayerDisguise) disguise).getScoreboardName();

                if (name == null || name.getTeamName() == null) {
                    continue;
                }

                sendTeamCreate(player, name, ((PlayerDisguise) disguise).isNameVisible(), disguise.getWatcher().getGlowColor());
            }
        }
    }

    @Override
    public void setGlowColor(UUID uuid, ChatColor color) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        String entry = uuid.toString();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String oldTeam = tracker.getEntryTeam(player.getUniqueId(), entry);
            String teamName = color == null ? null : getTeamName(color);

            if (oldTeam != null) {
                if (!oldTeam.startsWith(COLOR_TEAM_PREFIX) || oldTeam.equals(teamName)) {
                    continue;
                }

                WrapperPlayServerTeams removePacket = new WrapperPlayServerTeams(oldTeam, WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, entry);
                sendPacket(player, removePacket);
            }

            if (color != null) {
                sendPacket(player, getColorPacket(color, entry));
            }
        }
    }

    @Override
    public void registerColors(Player player) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        for (ChatColor color : ChatColor.values()) {
            if (!color.isColor()) {
                continue;
            }

            sendPacket(player, getColorPacket(color));
        }
    }

    @Override
    public void onEnable() {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            LibsDisguises.getInstance().getLogger()
                .severe("Lib's Disguises is being enabled while players are online, while using packet scoreboards. This is not safe.");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            registerTeams(player);
        }
    }

    @Override
    public void onDisable() {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }
        for (String team : new ArrayList<>(getTeams().keySet())) {
            removeRegisteredTeam(team);
        }
    }

    private void sendTeamCreate(Player player, DisguiseScoreboardTeam team, boolean nameVisible, ChatColor glowColor) {
        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(team.getTeamName(), WrapperPlayServerTeams.TeamMode.CREATE,
            getTeamInfo(team, nameVisible, glowColor), team.getEntry());
        sendPacket(player, packet);
    }

    private void sendTeamRemove(Player player, String teamName) {
        WrapperPlayServerTeams packet =
            new WrapperPlayServerTeams(teamName, WrapperPlayServerTeams.TeamMode.REMOVE, (WrapperPlayServerTeams.ScoreBoardTeamInfo) null);
        sendPacket(player, packet);
    }

    private void sendNoNameTeam(Player player) {
        if (!DisguiseConfig.isModifyScoreboards()) {
            return;
        }

        sendPacket(player,
            new WrapperPlayServerTeams(NO_NAME_TEAM, WrapperPlayServerTeams.TeamMode.CREATE, getTeamInfo("", "", false, ChatColor.WHITE),
                "§r"));
    }

    private void sendPacket(Player player, WrapperPlayServerTeams packet) {
        for (WrapperPlayServerTeams wrapper : tracker.packetsToSend(player.getUniqueId(), packet)) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
        }
    }

    private WrapperPlayServerTeams getColorPacket(ChatColor color, String... entries) {
        return new WrapperPlayServerTeams(getTeamName(color), WrapperPlayServerTeams.TeamMode.CREATE, getTeamInfo("", "", true, color),
            entries);
    }

    private WrapperPlayServerTeams.ScoreBoardTeamInfo getTeamInfo(DisguiseScoreboardTeam team, boolean nameVisible, ChatColor glowColor) {
        return getTeamInfo(team.getPrefix(), team.getSuffix(), nameVisible, glowColor);
    }

    private WrapperPlayServerTeams.ScoreBoardTeamInfo getTeamInfo(String prefix, String suffix, boolean nameVisible, ChatColor color) {
        WrapperPlayServerTeams.NameTagVisibility name =
            nameVisible ? WrapperPlayServerTeams.NameTagVisibility.ALWAYS : WrapperPlayServerTeams.NameTagVisibility.NEVER;
        WrapperPlayServerTeams.CollisionRule collide =
            DisguiseConfig.isModifyCollisions() ? WrapperPlayServerTeams.CollisionRule.NEVER : WrapperPlayServerTeams.CollisionRule.ALWAYS;

        return new WrapperPlayServerTeams.ScoreBoardTeamInfo(DisguiseUtilities.getAdventureChat(""),
            DisguiseUtilities.getAdventureChat(prefix), DisguiseUtilities.getAdventureChat(suffix), name, collide, getNamedTextColor(color),
            WrapperPlayServerTeams.OptionData.FRIENDLY_FIRE);
    }
}
