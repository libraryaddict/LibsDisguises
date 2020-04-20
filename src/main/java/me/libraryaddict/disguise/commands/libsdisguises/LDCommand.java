package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public interface LDCommand {
    List<String> getTabComplete();

    String getPermission();

    void onCommand(CommandSender sender, String[] args);

    LibsMsg getHelp();
}
