package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Navid
 */
public class DisguiseViewSelfCommand implements CommandExecutor {
    private List<Map.Entry<UUID, Long>> warnedTall = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        Player player = (Player) sender;

        if (DisguiseAPI.isViewSelfToggled(player)) {
            DisguiseAPI.setViewDisguiseToggled(player, false);
            LibsMsg.VIEW_SELF_OFF.send(sender);
        } else {
            DisguiseAPI.setViewDisguiseToggled(player, true);
            LibsMsg.VIEW_SELF_ON.send(sender);

            TargetedDisguise disguise = (TargetedDisguise) DisguiseAPI.getDisguise(player, player);

            // If they're disguised, tall disguises are hidden, it's a tall disguise
            // Then tell the player, it's not a bug! The disguise is too tall
            if (disguise != null && !disguise.isTallDisguisesVisible() &&
                (!NmsVersion.v1_21_R1.isSupported() || !DisguiseConfig.isTallSelfDisguisesScaling() ||
                    (disguise.isMiscDisguise() || disguise.getType() == DisguiseType.ENDER_DRAGON)) && disguise.canSee(player) &&
                DisguiseUtilities.isTallDisguise(disguise)) {
                LibsMsg.VIEW_SELF_TALL_NOTE.send(player);
            }
        }

        return true;
    }
}
