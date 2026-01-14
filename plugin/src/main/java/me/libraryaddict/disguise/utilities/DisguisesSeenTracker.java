package me.libraryaddict.disguise.utilities;

import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.LibsDisguises;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Class meant to track when a entity is still "disguised" on the client, but the server no longer considers them disguised. Yet, there
 * are packets yet to be processed that would crash the client if not held back until the client can be told about the new state of affairs.
 */
public class DisguisesSeenTracker {
    @RequiredArgsConstructor
    private static final class DisguiseKey implements Delayed {
        private final UUID observer;
        private final int entityId;
        private final Long expires;

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = expires - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return Long.compare(this.expires, ((DisguiseKey) o).expires);
        }
    }

    private final Map<UUID, Map<Integer, Long>> disguisesInTransit = new ConcurrentHashMap<>();
    private final DelayQueue<DisguiseKey> expiryQueue = new DelayQueue<>();
    private static final long EXPIRES_AFTER = TimeUnit.SECONDS.toMillis(10);

    public void setDisguiseBeingChangedOver(UUID observer, int entityId) {
        // We use a timer to prevent ghost entries for w/e reason, otherwise we should be avoid to avoid timers
        long expiryTime = System.currentTimeMillis() + EXPIRES_AFTER;

        disguisesInTransit.compute(observer, (k, map) -> {
            if (map == null) {
                map = new ConcurrentHashMap<>();
            }

            map.put(entityId, expiryTime);
            return map;
        });

        expiryQueue.put(new DisguiseKey(observer, entityId, expiryTime));
    }

    public void setDisguiseTransitionFinished(UUID observer, int entityId) {
        // Called when there is a real spawn packet being sent, that we do not handle. Prevent us canceling metadata we would never
        // transform
        disguisesInTransit.computeIfPresent(observer, (k, map) -> {
            map.remove(entityId);

            return map.isEmpty() ? null : map;
        });
    }

    public boolean isDisguiseChangingOver(UUID observer, int entityId) {
        Map<Integer, Long> map = disguisesInTransit.get(observer);

        return map != null && map.containsKey(entityId);
    }

    private void expireTransitQueue() {
        DisguiseKey key;

        while ((key = expiryQueue.poll()) != null) {
            final DisguiseKey finalKey = key;

            disguisesInTransit.computeIfPresent(key.observer, (k, map) -> {
                map.remove(finalKey.entityId, finalKey.expires);

                return map.isEmpty() ? null : map;
            });
        }
    }

    public void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                expireTransitQueue();
            }
        }.runTaskTimer(LibsDisguises.getInstance(), 20, 20);
    }
}