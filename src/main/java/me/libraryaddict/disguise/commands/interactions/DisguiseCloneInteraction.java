package me.libraryaddict.disguise.commands.interactions;

import lombok.AllArgsConstructor;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 4/04/2020.
 */
@AllArgsConstructor
public class DisguiseCloneInteraction implements LibsEntityInteract {
    private Boolean[] options;

    @Override
    public void onInteract(Player player, Entity entity) {
        DisguiseUtilities.createClonedDisguise(player, entity, options);
    }
}
