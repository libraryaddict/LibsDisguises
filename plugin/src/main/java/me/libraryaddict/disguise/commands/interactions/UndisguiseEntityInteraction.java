package me.libraryaddict.disguise.commands.interactions;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
            DisguiseAPI.undisguiseToAll(p, entity);

            if (entity instanceof Player) {
                LibsMsg.LISTEN_UNDISG_PLAYER.send(p, entityName);
            } else {
                LibsMsg.LISTEN_UNDISG_ENT.send(p, entityName);
            }
        } else {
            if (entity instanceof Player) {
                LibsMsg.LISTEN_UNDISG_PLAYER_FAIL.send(p, entityName);
            } else {
                LibsMsg.LISTEN_UNDISG_ENT_FAIL.send(p, entityName);
            }
        }
    }
}
