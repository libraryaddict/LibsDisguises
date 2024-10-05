package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class LDDebugging implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("debugging");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public String getPermission() {
        return "libsdisguises.disguise";
    }

    @Override
    public boolean isEnabled() {
        return !LibsDisguises.getInstance().isReleaseBuild();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        DisguiseUtilities.setDebuggingMode(!DisguiseUtilities.isDebuggingMode());

        sender.sendMessage(ChatColor.DARK_AQUA + "Libs Disguises Debugging: " +
            (DisguiseUtilities.isDebuggingMode() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_DEBUG_MODE;
    }
}
