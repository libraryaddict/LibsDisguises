package me.libraryaddict.disguise.commands.interactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.commands.utils.CopyDisguiseCommand;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by libraryaddict on 4/04/2020.
 */
@Getter
@AllArgsConstructor
public class CopyDisguiseInteraction implements LibsEntityInteract {
    private CopyDisguiseCommand copyDisguiseCommand;

    @Override
    public void onInteract(Player player, Entity entity) {
        if (DisguiseAPI.isDisguised(entity)) {
            Disguise disguise = DisguiseAPI.getDisguise(player, entity);
            String disguiseString = DisguiseParser.parseToString(disguise, false);

            getCopyDisguiseCommand()
                    .sendMessage(player, LibsMsg.CLICK_TO_COPY, LibsMsg.COPY_DISGUISE_NO_COPY, disguiseString, false);

            if (disguise instanceof PlayerDisguise) {
                getCopyDisguiseCommand()
                        .sendMessage(player, LibsMsg.CLICK_TO_COPY_WITH_SKIN, LibsMsg.CLICK_TO_COPY_WITH_SKIN_NO_COPY,
                                DisguiseParser.parseToString(disguise), true);
            }
        } else {
            player.sendMessage(LibsMsg.TARGET_NOT_DISGUISED.get());
        }
    }
}
