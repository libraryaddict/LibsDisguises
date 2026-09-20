package me.libraryaddict.disguise.utilities.sounds;

import com.github.retrooper.packetevents.util.Vector3i;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to cache disguises by their coords, to minimize the processing sound packets requires
 */
public class DisguiseChunkTracker {
    private static final Map<Long, Set<IWrappedEntity<?>>> disguisedInChunk = new ConcurrentHashMap<>();
    private static final Map<IWrappedEntity<?>, Long> trackedChunk = new ConcurrentHashMap<>();

    private static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static long getChunkKey(Location location) {
        return getChunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static void startTrackingChunk(IWrappedEntity<?> entity) {
        if (entity == null) {
            return;
        }

        long chunk = getChunkKey(entity.getLocation());

        startTrackingChunk(entity, chunk);
    }

    public static void startTrackingChunk(IWrappedEntity<?> entity, Long chunk) {
        trackedChunk.put(entity, chunk);
        disguisedInChunk.computeIfAbsent(chunk, k -> ConcurrentHashMap.newKeySet(1)).add(entity);
    }

    public static void stopTrackingChunk(IWrappedEntity<?> entity) {
        Long chunk = trackedChunk.remove(entity);

        if (chunk == null) {
            return;
        }

        disguisedInChunk.computeIfPresent(chunk, (k, inChunk) -> {
            inChunk.remove(entity);

            return inChunk.isEmpty() ? null : inChunk;
        });
    }

    public static void updateTrackedChunk(IWrappedEntity<?> entity) {
        Long chunk = trackedChunk.get(entity);

        if (chunk == null) {
            return;
        }

        long newChunkKey = getChunkKey(entity.getLocation());

        if (chunk == newChunkKey) {
            return;
        }

        stopTrackingChunk(entity);
        startTrackingChunk(entity, newChunkKey);
    }

    /**
     * The disguised entities near this packet location, they may be in another world
     */
    public static List<IWrappedEntity<?>> getDisguisedNearby(Vector3i packetLocation) {
        int blockX = Math.floorDiv(packetLocation.getX(), 8);
        int blockZ = Math.floorDiv(packetLocation.getZ(), 8);
        List<IWrappedEntity<?>> nearby = new ArrayList<>();

        // the entity might have left the chunk since we last tracked it, so allow a block of leeway
        for (int chunkX = (blockX - 1) >> 4; chunkX <= (blockX + 1) >> 4; chunkX++) {
            for (int chunkZ = (blockZ - 1) >> 4; chunkZ <= (blockZ + 1) >> 4; chunkZ++) {
                Set<IWrappedEntity<?>> inChunk = disguisedInChunk.get(getChunkKey(chunkX, chunkZ));

                if (inChunk != null) {
                    nearby.addAll(inChunk);
                }
            }
        }

        return nearby;
    }
}
