package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;

public class GuardianWatcher extends InsentientWatcher
{

    public GuardianWatcher(Disguise disguise)
    {
        super(disguise);
    }

    /**
     * Is this guardian targetting someone?
     * 
     * @return
     */
    public boolean isTarget()
    {
        return ((int) getData(FlagType.GUARDIAN_TARGET)) != 0;
    }

    /**
     * Shoot a beam at the given entityId.
     * 
     * @param entityId
     */
    public void setTarget(int entityId)
    {
        setData(FlagType.GUARDIAN_TARGET, entityId);
        sendData(FlagType.GUARDIAN_TARGET);
    }

    public void setTarget(Entity entity)
    {
        setTarget(entity == null ? 0 : entity.getEntityId());
    }

    /**
     * Shoot a beam at the given player name.
     * 
     * @param playername
     */
    public void setTarget(String playername)
    {
        Player player = Bukkit.getPlayer(playername);

        if (player == null)
            return;

        setData(FlagType.GUARDIAN_TARGET, player.getEntityId());
        sendData(FlagType.GUARDIAN_TARGET);
    }

    public boolean isRetractingSpikes()
    {
        return isGuardianFlag(2);
    }

    public void setRetractingSpikes(boolean isRetracting)
    {
        setGuardianFlag(2, isRetracting);
    }

    public boolean isElder()
    {
        return isGuardianFlag(4);
    }

    public void setElder(boolean isGuardian)
    {
        setGuardianFlag(4, isGuardian);
    }

    protected boolean isGuardianFlag(int no)
    {
        return (getData(FlagType.GUARDIAN_FLAG) & no) != 0;
    }

    protected void setGuardianFlag(int no, boolean flag)
    {
        byte b0 = getData(FlagType.GUARDIAN_FLAG);

        if (flag)
        {
            setData(FlagType.GUARDIAN_FLAG, (byte) (b0 | no));
        }
        else
        {
            setData(FlagType.GUARDIAN_FLAG, (byte) (b0 & -(no + 1)));
        }

        sendData(FlagType.GUARDIAN_FLAG);
    }

}
