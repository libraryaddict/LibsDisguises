package me.libraryaddict.disguise.commands;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.libsdisguises.LDChangelog;
import me.libraryaddict.disguise.commands.libsdisguises.LDCommand;
import me.libraryaddict.disguise.commands.libsdisguises.LDConfig;
import me.libraryaddict.disguise.commands.libsdisguises.LDCount;
import me.libraryaddict.disguise.commands.libsdisguises.LDDebugDisguiseLoop;
import me.libraryaddict.disguise.commands.libsdisguises.LDDebugMineSkin;
import me.libraryaddict.disguise.commands.libsdisguises.LDDebugPlayer;
import me.libraryaddict.disguise.commands.libsdisguises.LDDebugging;
import me.libraryaddict.disguise.commands.libsdisguises.LDHelp;
import me.libraryaddict.disguise.commands.libsdisguises.LDJson;
import me.libraryaddict.disguise.commands.libsdisguises.LDMetaInfo;
import me.libraryaddict.disguise.commands.libsdisguises.LDMissingDescription;
import me.libraryaddict.disguise.commands.libsdisguises.LDMods;
import me.libraryaddict.disguise.commands.libsdisguises.LDPermTest;
import me.libraryaddict.disguise.commands.libsdisguises.LDReload;
import me.libraryaddict.disguise.commands.libsdisguises.LDScoreboard;
import me.libraryaddict.disguise.commands.libsdisguises.LDUpdate;
import me.libraryaddict.disguise.commands.libsdisguises.LDUpdatePacketEvents;
import me.libraryaddict.disguise.commands.libsdisguises.LDUploadLogs;
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
        getCommands().add(new LDUpdatePacketEvents());
        getCommands().add(new LDDebugMineSkin());
        getCommands().add(new LDDebugDisguiseLoop());
        getCommands().add(new LDMissingDescription());
        getCommands().add(new LDDebugging());
    }

    protected ArrayList<String> filterTabs(ArrayList<String> list, String[] origArgs) {
        if (origArgs.length == 0) {
            return list;
        }

        Iterator<String> itel = list.iterator();
        String label = origArgs[origArgs.length - 1].toLowerCase(Locale.ENGLISH);

        while (itel.hasNext()) {
            String name = itel.next();

            if (name.toLowerCase(Locale.ENGLISH).startsWith(label)) {
                continue;
            }

            itel.remove();
        }

        return list;
    }

    protected String[] getArgs(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {
            String s = args[i];

            if (s.trim().isEmpty()) {
                continue;
            }

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

                if (disguises.isJenkins()) {
                    version += "b";
                }

                version += disguises.getBuildNo();
            }

            sender.sendMessage(ChatColor.DARK_GREEN + "This server is running Lib's Disguises " + "v" + version +
                " by libraryaddict, formerly maintained by Byteflux and NavidK0.");

            if (sender.hasPermission("libsdisguises.reload")) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Use " + ChatColor.GREEN + "/libsdisguises " + "reload" + ChatColor.DARK_GREEN +
                    " to reload the config. All disguises will be blown by doing this" + ".");
                sender.sendMessage(
                    ChatColor.DARK_GREEN + "Use " + ChatColor.GREEN + "/libsdisguises help" + ChatColor.DARK_GREEN + " to see more help");
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
                continue;
            }

            List<String> tabComplete = command.onTabComplete(origArgs);

            if (tabComplete != null) {
                tabs.addAll(tabComplete);
            }
        }

        return filterTabs(tabs, origArgs);
    }
}
