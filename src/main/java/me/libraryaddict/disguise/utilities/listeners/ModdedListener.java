package me.libraryaddict.disguise.utilities.listeners;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.modded.ModdedEntity;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

/**
 * Created by libraryaddict on 11/06/2020.
 */
public class ModdedListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        ModdedManager.doMods(event.getPlayer());
    }
}
