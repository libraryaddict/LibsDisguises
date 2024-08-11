package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public interface LDCommand {
    List<String> getTabComplete();

    default List<String> onTabComplete(String[] args) {
        return new ArrayList<>();
    }

    boolean hasPermission(CommandSender sender);

    String getPermission();

    void onCommand(CommandSender sender, String[] args);

    LibsMsg getHelp();

    default boolean isEnabled() {
        return true;
    }
}
