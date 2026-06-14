package me.libraryaddict.disguise.utilities.scoreboard.packetevents;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketEventsScoreboardTracker {
    private final Map<UUID, TrackedScoreboard> scoreboards = new ConcurrentHashMap<>();

    public List<WrapperPlayServerTeams> packetsToSend(UUID playerId, WrapperPlayServerTeams packet) {
        return trackedScoreboard(playerId).packetsToSend(packet);
    }

    public void record(UUID playerId, WrapperPlayServerTeams packet) {
        trackedScoreboard(playerId).record(packet);
    }

    public void clear(UUID playerId) {
        scoreboards.remove(playerId);
    }

    public String getEntryTeam(UUID playerId, String entry) {
        return trackedScoreboard(playerId).getEntryTeam(entry);
    }

    private TrackedScoreboard trackedScoreboard(UUID playerId) {
        return scoreboards.computeIfAbsent(playerId, uuid -> new TrackedScoreboard());
    }

    private static class TrackedScoreboard {
        private final Map<String, Set<String>> teams = new HashMap<>();
        private final Map<String, String> entryTeams = new HashMap<>();

        public synchronized List<WrapperPlayServerTeams> packetsToSend(WrapperPlayServerTeams packet) {
            String team = packet.getTeamName();

            if (team == null) {
                return Collections.emptyList();
            }

            switch (packet.getTeamMode()) {
                case CREATE:
                    return packetsForCreate(team, packet.getTeamInfo().orElse(null), packet.getPlayers());
                case REMOVE:
                case UPDATE:
                    return teams.containsKey(team) ? Collections.singletonList(packet) : Collections.emptyList();
                case ADD_ENTITIES:
                    return packetsForAddEntries(team, packet.getPlayers());
                case REMOVE_ENTITIES:
                    return packetsForRemoveEntries(team, packet.getPlayers());
                default:
                    return Collections.emptyList();
            }
        }

        public synchronized void record(WrapperPlayServerTeams packet) {
            String team = packet.getTeamName();

            if (team == null) {
                return;
            }

            switch (packet.getTeamMode()) {
                case CREATE:
                    for (String entry : teams.getOrDefault(team, Collections.emptySet())) {
                        entryTeams.remove(entry);
                    }

                    teams.remove(team);
                    for (String entry : uniqueEntries(packet.getPlayers())) {
                        removeEntry(entry);
                        entryTeams.put(entry, team);
                    }
                    teams.put(team, new HashSet<>(uniqueEntries(packet.getPlayers())));
                    break;
                case REMOVE:
                    Set<String> removed = teams.remove(team);

                    for (String entry : removed == null ? Collections.<String>emptySet() : removed) {
                        entryTeams.remove(entry);
                    }
                    break;
                case UPDATE:
                    teams.putIfAbsent(team, new HashSet<>());
                    break;
                case ADD_ENTITIES:
                    Set<String> teamEntries = teams.computeIfAbsent(team, name -> new HashSet<>());
                    for (String entry : uniqueEntries(packet.getPlayers())) {
                        removeEntry(entry);
                        teamEntries.add(entry);
                        entryTeams.put(entry, team);
                    }
                    break;
                case REMOVE_ENTITIES:
                    Set<String> removingFromTeam = teams.computeIfAbsent(team, name -> new HashSet<>());
                    for (String entry : uniqueEntries(packet.getPlayers())) {
                        removingFromTeam.remove(entry);

                        if (team.equals(entryTeams.get(entry))) {
                            entryTeams.remove(entry);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        private List<WrapperPlayServerTeams> packetsForCreate(String team, WrapperPlayServerTeams.ScoreBoardTeamInfo info,
                                                              Collection<String> entries) {
            if (!teams.containsKey(team)) {
                List<WrapperPlayServerTeams> packets = packetsRemovingEntriesFromOtherTeams(uniqueEntries(entries));
                packets.add(new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.CREATE, info, uniqueEntries(entries)));
                return packets;
            }

            List<WrapperPlayServerTeams> packets = new ArrayList<>();

            if (info != null) {
                packets.add(new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.UPDATE, info));
            }

            packets.addAll(packetsForAddEntries(team, entries));
            return packets;
        }

        private List<WrapperPlayServerTeams> packetsForAddEntries(String team, Collection<String> entries) {
            if (!teams.containsKey(team)) {
                return Collections.emptyList();
            }

            List<String> adding = new ArrayList<>();
            List<WrapperPlayServerTeams> packets = packetsRemovingEntriesFromOtherTeams(team, uniqueEntries(entries));

            for (String entry : uniqueEntries(entries)) {
                if (team.equals(entryTeams.get(entry))) {
                    continue;
                }

                adding.add(entry);
            }

            if (!adding.isEmpty()) {
                packets.add(new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, adding));
            }

            return packets;
        }

        private List<WrapperPlayServerTeams> packetsForRemoveEntries(String team, Collection<String> entries) {
            Set<String> teamEntries = teams.get(team);

            if (teamEntries == null) {
                return Collections.emptyList();
            }

            List<String> removing = new ArrayList<>();

            for (String entry : uniqueEntries(entries)) {
                if (!team.equals(entryTeams.get(entry))) {
                    continue;
                }

                removing.add(entry);
            }

            return removing.isEmpty() ? Collections.emptyList() : Collections.singletonList(
                new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, removing));
        }

        private synchronized String getEntryTeam(String entry) {
            return entryTeams.get(entry);
        }

        private List<WrapperPlayServerTeams> packetsRemovingEntriesFromOtherTeams(Collection<String> entries) {
            return packetsRemovingEntriesFromOtherTeams(null, entries);
        }

        private List<WrapperPlayServerTeams> packetsRemovingEntriesFromOtherTeams(String except, Collection<String> entries) {
            Map<String, List<String>> removals = new LinkedHashMap<>();

            for (String entry : entries) {
                String team = entryTeams.get(entry);

                if (team == null || team.equals(except)) {
                    continue;
                }

                removals.computeIfAbsent(team, ignored -> new ArrayList<>()).add(entry);
            }

            List<WrapperPlayServerTeams> packets = new ArrayList<>(removals.size());

            for (Map.Entry<String, List<String>> entry : removals.entrySet()) {
                packets.add(new WrapperPlayServerTeams(entry.getKey(), WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, entry.getValue()));
            }

            return packets;
        }

        private void removeEntry(String entry) {
            String team = entryTeams.remove(entry);
            Set<String> entries = team == null ? null : teams.get(team);

            if (entries != null) {
                entries.remove(entry);
            }
        }

        private Collection<String> uniqueEntries(Collection<String> entries) {
            return entries == null ? Collections.emptyList() : new LinkedHashSet<>(entries);
        }
    }
}
