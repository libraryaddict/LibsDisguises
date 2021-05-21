package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.mineskin.MineSkinAPI;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Created by libraryaddict on 22/05/2021.
 */
public class LDDebugMineSkin implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("mineskin");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public String getPermission() {
        return "libsdisguises.mineskin";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        MineSkinAPI api = DisguiseUtilities.getMineSkinAPI();
        api.setDebugging(!api.isDebugging());

        LibsMsg.LD_DEBUG_MINESKIN_TOGGLE.send(sender, api.isDebugging());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_DEBUG_MINESKIN;
    }
}
