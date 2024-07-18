package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LDMissingDescription implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("missingdescriptions");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.disguise";
    }

    @Override
    public boolean isEnabled() {
        return !LibsDisguises.getInstance().isJenkins();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        List<WatcherMethod> methods = new ArrayList<>(ParamInfoManager.getDisguiseMethods().getMethods());
        methods.removeIf(m -> m.isNoVisibleDifference() || m.getDescription() != null && m.getDescription().length() > 5);

        for (int i = 0; i < methods.size(); i++) {
            if (i > 10) {
                sender.sendMessage(ChatColor.RED + "Skipping the remaining " + (methods.size() - (i - 1)) + " methods...");
                break;
            }

            WatcherMethod m = methods.get(i);
            sender.sendMessage(
                ChatColor.DARK_AQUA + m.getWatcherClass().getSimpleName() + ChatColor.AQUA + " -> " + ChatColor.DARK_AQUA + m.getName() +
                    ChatColor.AQUA + " = " + ChatColor.DARK_AQUA + "Missing");
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_RELOAD;
    }
}
