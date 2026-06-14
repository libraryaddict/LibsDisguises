package me.libraryaddict.disguise.utilities.wrapped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class BundleContext {
    private static class PendingState {
        final Disguise disguise;
        final int entityId;
        final boolean inLineOfSight;

        PendingState(Disguise disguise, int entityId, boolean inLineOfSight) {
            this.disguise = disguise;
            this.entityId = entityId;
            this.inLineOfSight = inLineOfSight;
        }
    }

    @Getter
    private final IWrappedPlayer observer;
    @Getter
    private boolean insideBundle;
    private PendingState pendingSpawnMeta;
    private PendingState pendingNameSpawn;

    /**
     * Triggers when a bundle packet is received, which indicates either the start or end of a bundle.
     */
    public void onBundle() {
        insideBundle = !insideBundle;

        if (!insideBundle) {
            flushPendingSpawnMeta();
            flushPendingNameSpawn();
        }
    }

    public int getPendingSpawnEntityId() {
        return pendingSpawnMeta != null ? pendingSpawnMeta.entityId : -1;
    }

    public void setPendingSpawnMetadata(Disguise disguise, int entityId, boolean inLineOfSight) {
        flushPendingSpawnMeta();

        pendingSpawnMeta = new PendingState(disguise, entityId, inLineOfSight);
    }

    public void clearPendingSpawnMetadata() {
        pendingSpawnMeta = null;
    }

    public void flushPendingSpawnMeta() {
        if (pendingSpawnMeta == null) {
            return;
        }

        PendingState state = pendingSpawnMeta;
        pendingSpawnMeta = null;

        int entityId = state.entityId == getObserver().getEntityId() ? DisguiseAPI.getSelfDisguiseId() : state.entityId;

        List<WatcherValue> watcherValues =
            state.inLineOfSight ? DisguiseUtilities.createSanitizedWatcherValues(getObserver(), state.disguise.getWatcher()) :
                Collections.singletonList(new WatcherValue(MetaIndex.ENTITY_META, (byte) 32, true));

        getObserver().sendPacketSilently(ReflectionManager.getMetadataPacket(entityId, watcherValues));
    }

    public void setPendingNameSpawn(Disguise disguise, int entityId, boolean inLineOfSight) {
        flushPendingNameSpawn();

        pendingNameSpawn = new PendingState(disguise, entityId, inLineOfSight);
    }

    public void onAttributeScale(int entityId) {
        if (pendingNameSpawn == null || pendingNameSpawn.entityId != entityId) {
            return;
        }

        flushPendingNameSpawn();
    }

    private void flushPendingNameSpawn() {
        if (pendingNameSpawn == null) {
            return;
        }

        PendingState state = pendingNameSpawn;
        pendingNameSpawn = null;

        if (!state.disguise.isPlayerDisguise() || state.inLineOfSight) {
            Double stored = state.disguise.getInternals().getLastTransmittedScale(getObserver().getUniqueId());
            double scale = stored != null ? stored : 1.0;
            DisguiseUtilities.getNamePackets(state.disguise, getObserver().getEntity(), new String[0], scale)
                .forEach(getObserver()::sendPacketSilently);
        }
    }
}
