package me.libraryaddict.disguise.utilities.listeners;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

public class DisguiseListener1_18 implements Listener {
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises()) {
            return;
        }

        for (Entity entity : event.getEntities()) {
            Disguise[] disguises = DisguiseUtilities.getSavedDisguises(entity, true);

            if (disguises.length <= 0) {
                continue;
            }

            DisguiseUtilities.resetPluginTimer();

            for (Disguise disguise : disguises) {
                disguise.setEntity(entity);
                disguise.startDisguise();
            }
        }
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        if (!DisguiseConfig.isSaveEntityDisguises()) {
            return;
        }

        for (Entity entity : event.getEntities()) {
            Disguise[] disguises = DisguiseAPI.getDisguises(entity);

            if (disguises.length <= 0) {
                continue;
            }

            DisguiseUtilities.saveDisguises(entity, disguises);
        }
    }
}
