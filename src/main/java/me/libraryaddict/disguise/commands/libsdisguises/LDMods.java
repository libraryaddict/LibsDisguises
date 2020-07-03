package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDMods implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("mods");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.mods";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (ModdedManager.getEntities().isEmpty()) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_MODS_LISTENING);
            return;
        }

        Player player;

        if (args.length > 1) {
            player = Bukkit.getPlayer(args[1]);

            if (player == null) {
                DisguiseUtilities.sendMessage(sender, LibsMsg.CANNOT_FIND_PLAYER, args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_CONSOLE);
            return;
        }

        if (!player.hasMetadata("forge_mods")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_MODS, player.getName());
            return;
        }

        DisguiseUtilities.sendMessage(sender, LibsMsg.MODS_LIST, player.getName(),
                StringUtils.join((List<String>) player.getMetadata("forge_mods").get(0).value(), ", "));
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_MODS;
    }
}
