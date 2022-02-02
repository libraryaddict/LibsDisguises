package me.libraryaddict.disguise.commands.libsdisguises;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.libraryaddict.disguise.commands.LibsDisguisesCommand;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Created by libraryaddict on 22/04/2020.
 */
@AllArgsConstructor
@Getter
public class LDHelp implements LDCommand {
    private LibsDisguisesCommand command;

    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("help");
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return getCommand().getCommands().stream().anyMatch(c -> c.getPermission() != null && c.hasPermission(sender));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        for (LDCommand cmd : command.getCommands()) {
            if (!cmd.hasPermission(sender)) {
                continue;
            }

            cmd.getHelp().send(sender);
        }
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_HELP;
    }
}
