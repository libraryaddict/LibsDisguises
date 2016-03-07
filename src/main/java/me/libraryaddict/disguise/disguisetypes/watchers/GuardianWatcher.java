package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GuardianWatcher extends LivingWatcher {

    public GuardianWatcher(Disguise disguise) {
        super(disguise);
    }

    /**
     * Is this guardian targetting someone?
     * @return
     */
    public boolean isTarget() {
        return ((int)getValue(12, 0)) != 0;
    }

    /**
     * Shoot a beam at the given entityId.
     * @param entityId
     */
    public void setTarget(int entityId) {
        setValue(12, entityId);
        sendData(12);
    }

    /**
     * Shoot a beam at the given player name.
     * @param playername
     */
    public void setTarget(String playername) {
        Player player = Bukkit.getPlayer(playername);
        if (player == null) return;
        setValue(12, player.getEntityId());
        sendData(12);
    }

    public boolean isRetractingSpikes() {
        return isGuardianFlag(2);
    }

    public void setRetractingSpikes(boolean isRetracting) {
        setGuardianFlag(2, isRetracting);
    }

    public boolean isElder() {
        return isGuardianFlag(4);
    }

    public void setElder(boolean isGuardian) {
        setGuardianFlag(4, isGuardian);
    }

    protected boolean isGuardianFlag(int no) {
        return ((byte) getValue(11, (byte) 0) & no) != 0;
    }

    protected void setGuardianFlag(int no, boolean flag) {
        byte b0 = (byte) getValue(11, (byte) 0);
        if (flag) {
            setValue(11, (byte) (b0 | no));
        } else {
            setValue(11, (byte) (b0 & -(no + 1)));
        }
        sendData(11);
    }

}
