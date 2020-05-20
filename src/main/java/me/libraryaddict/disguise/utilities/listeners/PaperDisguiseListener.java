package me.libraryaddict.disguise.utilities.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by libraryaddict on 20/05/2020.
 */
public class PaperDisguiseListener implements Listener {
    @EventHandler
    public void onEntityLoad(EntityAddToWorldEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises())
            return;

        Entity entity = event.getEntity();

        Disguise[] disguises = DisguiseUtilities.getSavedDisguises(entity.getUniqueId(), true);

        if (disguises.length <= 0)
            return;

        DisguiseUtilities.resetPluginTimer();

        for (Disguise disguise : disguises) {
            disguise.setEntity(entity);
            disguise.startDisguise();
        }
    }
}
