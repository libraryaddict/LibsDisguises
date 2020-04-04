package me.libraryaddict.disguise.commands.interactions;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 4/04/2020.
 */
public class UndisguiseEntityInteraction implements LibsEntityInteract {
    @Override
    public void onInteract(Player p, Entity entity) {
        String entityName;

        if (entity instanceof Player) {
            entityName = entity.getName();
        } else {
            entityName = DisguiseType.getType(entity).toReadable();
        }

        if (DisguiseAPI.isDisguised(entity)) {
            DisguiseAPI.undisguiseToAll(entity);

            if (entity instanceof Player)
                p.sendMessage(LibsMsg.LISTEN_UNDISG_PLAYER.get(entityName));
            else
                p.sendMessage(LibsMsg.LISTEN_UNDISG_ENT.get(entityName));
        } else {
            if (entity instanceof Player)
                p.sendMessage(LibsMsg.LISTEN_UNDISG_PLAYER_FAIL.get(entityName));
            else
                p.sendMessage(LibsMsg.LISTEN_UNDISG_ENT_FAIL.get(entityName));
        }
    }
}
