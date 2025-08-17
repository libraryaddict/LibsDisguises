package me.libraryaddict.disguise.commands.interactions;

import lombok.AllArgsConstructor;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class DisguiseModifyInteraction implements LibsEntityInteract {
    private String[] options;

    @Override
    public void onInteract(Player p, Entity entity) {
        Disguise disguise = DisguiseAPI.getDisguise(p, entity);

        if (disguise == null) {
            String entityName;

            if (entity instanceof Player) {
                entityName = entity.getName();
            } else {
                entityName = DisguiseType.getType(entity).toReadable();
            }

            LibsMsg.NOT_DISGUISED_FAIL.send(p, entityName);
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
            DisguiseParser.callMethods(p, disguise, perms, disguisePerm, new ArrayList<>(Arrays.asList(options)), options,
                "DisguiseModifyEntity");
            LibsMsg.LISTENER_MODIFIED_DISG.send(p);
        } catch (DisguiseParseException ex) {
            ex.send(p);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
