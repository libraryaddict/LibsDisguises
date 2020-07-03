package me.libraryaddict.disguise.commands.interactions;

import lombok.AllArgsConstructor;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by libraryaddict on 4/04/2020.
 */

@AllArgsConstructor
public class DisguiseModifyInteraction implements LibsEntityInteract {
    private String[] options;

    @Override
    public void onInteract(Player p, Entity entity) {
        String entityName;

        if (entity instanceof Player) {
            entityName = entity.getName();
        } else {
            entityName = DisguiseType.getType(entity).toReadable();
        }

        Disguise disguise = DisguiseAPI.getDisguise(p, entity);

        if (disguise == null) {
            LibsMsg.UNDISG_PLAYER_FAIL.send(p, entityName);
            return;
        }

        options = DisguiseParser.parsePlaceholders(options, p, entity);

        DisguisePermissions perms = DisguiseParser.getPermissions(p, "disguiseentitymodify");
        DisguisePerm disguisePerm = new DisguisePerm(disguise.getType());

        if (!perms.isAllowedDisguise(disguisePerm, Arrays.asList(options))) {
            LibsMsg.DMODPLAYER_NOPERM.send(p);
            return;
        }

        try {
            DisguiseParser
                    .callMethods(p, disguise, perms, disguisePerm, new ArrayList<>(Arrays.asList(options)), options,
                            "DisguiseModifyEntity");
            LibsMsg.LISTENER_MODIFIED_DISG.send(p);
        }
        catch (DisguiseParseException ex) {
            if (ex.getMessage() != null) {
                DisguiseUtilities.sendMessage(p, ex.getMessage());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
