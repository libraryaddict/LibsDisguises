package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LDConfig implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("config", "configuration");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public String getPermission() {
        return "libsdisguises.config";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ArrayList<String> returns = DisguiseConfig.doOutput(true, true);

        if (returns.isEmpty()) {
            LibsMsg.USING_DEFAULT_CONFIG.send(sender);
            return;
        }

        for (String s : returns) {
            sender.sendMessage(ChatColor.AQUA + "[LibsDisguises] " + s);
        }
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_CONFIG;
    }
}
