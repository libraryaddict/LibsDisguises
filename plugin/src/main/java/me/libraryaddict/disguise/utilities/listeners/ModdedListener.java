package me.libraryaddict.disguise.utilities.listeners;

import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ModdedListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        ModdedManager.doMods(event.getPlayer());
    }
}
