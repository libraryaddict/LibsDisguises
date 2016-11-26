package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

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
        return ((int) getData(FlagType.GUARDIAN_TARGET)) != 0;
    }

    /**
     * Shoot a beam at the given entityId.
     * 
     * @param entityId
     */
    public void setTarget(int entityId) {
        setData(FlagType.GUARDIAN_TARGET, entityId);
        sendData(FlagType.GUARDIAN_TARGET);
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

        if (player == null)
            return;

        setData(FlagType.GUARDIAN_TARGET, player.getEntityId());
        sendData(FlagType.GUARDIAN_TARGET);
    }

    public boolean isRetractingSpikes() {
        return getData(FlagType.GUARDIAN_RETRACT_SPIKES);
    }

    public void setRetractingSpikes(boolean isRetracting) {
        setData(FlagType.GUARDIAN_RETRACT_SPIKES, isRetracting);
        sendData(FlagType.GUARDIAN_RETRACT_SPIKES);
    }

}
