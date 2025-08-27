package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.sounds.SoundManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class LDReload implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("reload");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.reload";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        DisguiseConfig.loadConfig();
        DisguiseUtilities.reload();
        new SoundManager().load();
        DisguisePermissions.onReload();
        LibsMsg.RELOADED_CONFIG.send(sender);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_RELOAD;
    }
}
