package me.libraryaddict.disguise.utilities.listeners;

import me.frep.vulcan.api.check.Check;
import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VulcanCompatibilityListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVulcanFlag(VulcanFlagEvent event) {
        // Vulcan uses packetevents to check for violations
        Check check = event.getCheck();

        // Check for nullity, because why not, and check if the flag is for velocity
        if (check.getName() == null || !check.getName().equalsIgnoreCase("velocity")) {
            return;
        }

        // Retrieve the self disguise if it exists
        Disguise disguise = DisguiseAPI.getDisguise(event.getPlayer(), event.getPlayer());

        // If no self disguise, then do nothing
        if (disguise == null) {
            return;
        }

        // Never flag them for cheating with velocity when self disguised
        event.setCancelled(true);
    }
}
