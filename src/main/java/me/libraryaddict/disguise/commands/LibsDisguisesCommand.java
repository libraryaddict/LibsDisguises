package me.libraryaddict.disguise.commands;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.libsdisguises.*;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class LibsDisguisesCommand implements CommandExecutor, TabCompleter {
    @Getter
    private final ArrayList<LDCommand> commands = new ArrayList<>();

    public LibsDisguisesCommand() {
        getCommands().add(new LDHelp(this));
        getCommands().add(new LDReload());
        getCommands().add(new LDUpdate());
        getCommands().add(new LDChangelog());
        getCommands().add(new LDCount());
        getCommands().add(new LDConfig());
        getCommands().add(new LDPermTest());
        getCommands().add(new LDScoreboard());
        getCommands().add(new LDJson());
        getCommands().add(new LDMods());
        getCommands().add(new LDMetaInfo());
        getCommands().add(new LDDebugPlayer());
        getCommands().add(new LDUploadLogs());
        getCommands().add(new LDUpdateProtocolLib());
    }

    protected ArrayList<String> filterTabs(ArrayList<String> list, String[] origArgs) {
        if (origArgs.length == 0)
            return list;

        Iterator<String> itel = list.iterator();
        String label = origArgs[origArgs.length - 1].toLowerCase(Locale.ENGLISH);

        while (itel.hasNext()) {
            String name = itel.next();

            if (name.toLowerCase(Locale.ENGLISH).startsWith(label))
                continue;

            itel.remove();
        }

        return list;
    }

    protected String[] getArgs(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {
            String s = args[i];

            if (s.trim().isEmpty())
                continue;

            newArgs.add(s);
        }

        return newArgs.toArray(new String[0]);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            LibsDisguises disguises = LibsDisguises.getInstance();

            String version = disguises.getDescription().getVersion();

            if (!disguises.isReleaseBuild()) {
                version += "-";

                if (disguises.isNumberedBuild()) {
                    version += "b";
                }

                version += disguises.getBuildNo();
            }

            sender.sendMessage(ChatColor.DARK_GREEN + "This server is running Lib's Disguises " + "v" + version +
                    " by libraryaddict, formerly maintained by Byteflux and NavidK0.");

            if (sender.hasPermission("libsdisguises.reload")) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Use " + ChatColor.GREEN + "/libsdisguises " + "reload" +
                        ChatColor.DARK_GREEN + " to reload the config. All disguises will be blown by doing this" +
                        ".");
                sender.sendMessage(ChatColor.DARK_GREEN + "Use /libsdisguises help to see more help");
            }

            if (LibsPremium.isPremium()) {
                sender.sendMessage(ChatColor.DARK_GREEN + "This server supports the plugin developer!");
            }
        } else if (args.length > 0) {
            LDCommand command = null;

            for (LDCommand c : getCommands()) {
                if (!c.getTabComplete().contains(args[0].toLowerCase(Locale.ENGLISH))) {
                    continue;
                }

                command = c;
                break;
            }

            if (command != null) {
                if (!command.hasPermission(sender)) {
                    LibsMsg.NO_PERM.send(sender);
                    return true;
                }

                command.onCommand(sender, args);
            } else {
                LibsMsg.LIBS_COMMAND_WRONG_ARG.send(sender);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getArgs(origArgs);

        for (LDCommand command : getCommands()) {
            if (!command.hasPermission(sender)) {
                continue;
            }

            if (origArgs.length <= 1) {
                tabs.addAll(command.getTabComplete());
            } else {
                for (String s : command.getTabComplete()) {
                    if (!s.contains(" ")) {
                        continue;
                    }

                    String[] split = s.split(" ");

                    if (!args[0].equalsIgnoreCase(split[0])) {
                        continue;
                    }

                    tabs.add(split[1]);
                }
            }
        }

        return filterTabs(tabs, origArgs);
    }
}
