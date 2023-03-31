package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GuardianWatcher extends InsentientWatcher {
    public GuardianWatcher(Disguise disguise) {
        super(disguise);
    }

    /**
     * Is this guardian targetting someone?
     *
     * @return
     */
    public boolean isTarget() {
        return getData(MetaIndex.GUARDIAN_TARGET) != 0;
    }

    /**
     * @return Entity id of target
     */
    public int getTarget() {
        return getData(MetaIndex.GUARDIAN_TARGET);
    }

    /**
     * Shoot a beam at the given entityId.
     *
     * @param entityId
     */
    public void setTarget(int entityId) {
        setData(MetaIndex.GUARDIAN_TARGET, entityId);
        sendData(MetaIndex.GUARDIAN_TARGET);
    }

    public void setTarget(Entity entity) {
        setTarget(entity == null ? 0 : entity.getEntityId());
    }

    /**
     * Shoot a beam at the given player name.
     *
     * @param playername
     */
    public void setTarget(String playername) {
        Player player = Bukkit.getPlayer(playername);

        if (player == null) {
            return;
        }

        setData(MetaIndex.GUARDIAN_TARGET, player.getEntityId());
        sendData(MetaIndex.GUARDIAN_TARGET);
    }

    public boolean isRetractingSpikes() {
        return getData(MetaIndex.GUARDIAN_RETRACT_SPIKES);
    }

    public void setRetractingSpikes(boolean isRetracting) {
        setData(MetaIndex.GUARDIAN_RETRACT_SPIKES, isRetracting);
        sendData(MetaIndex.GUARDIAN_RETRACT_SPIKES);
    }
}
