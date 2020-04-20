package me.libraryaddict.disguise.commands;

import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.libsdisguises.*;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.UpdateChecker;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class LibsDisguisesCommand implements CommandExecutor, TabCompleter {
    private ArrayList<LDCommand> commands = new ArrayList<>();

    public LibsDisguisesCommand() {
        commands.add(new LDConfig());
        commands.add(new LDCount());
        commands.add(new LDJson());
        commands.add(new LDMetaInfo());
        commands.add(new LDMods());
        commands.add(new LDPermTest());
        commands.add(new LDReload());
        commands.add(new LDScoreboard());
        commands.add(new LDUpdate());
    }

    protected ArrayList<String> filterTabs(ArrayList<String> list, String[] origArgs) {
        if (origArgs.length == 0)
            return list;

        Iterator<String> itel = list.iterator();
        String label = origArgs[origArgs.length - 1].toLowerCase();

        while (itel.hasNext()) {
            String name = itel.next();

            if (name.toLowerCase().startsWith(label))
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

            sender.sendMessage(ChatColor.DARK_GREEN + "This server is running " + "Lib's Disguises v" + version +
                    " by libraryaddict, formerly maintained by Byteflux and NavidK0.");

            if (sender.hasPermission("libsdisguises.reload")) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Use " + ChatColor.GREEN + "/libsdisguises " + "reload" +
                        ChatColor.DARK_GREEN + " to reload the config. All disguises will be blown by doing this" +
                        ".");
            }

            if (sender.hasPermission("libsdisguises.update")) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Use " + ChatColor.GREEN + "/libsdisguises update" +
                        ChatColor.DARK_GREEN +
                        " to update Lib's Disguises to latest jenkins build. This will be updated on server restart. " +
                        "To force an update, use /libsdisguises update! with an ! on the end");
            }

            // TODO Other options

            if (LibsPremium.isPremium()) {
                sender.sendMessage(ChatColor.DARK_GREEN + "This server supports the plugin developer!");
            }
        } else if (args.length > 0) {
            LDCommand command = null;

            for (LDCommand c : commands) {
                if (!c.getTabComplete().contains(args[0].toLowerCase())) {
                    continue;
                }

                command = c;
                break;
            }

            if (command != null) {
                if (!sender.hasPermission(command.getPermission())) {
                    sender.sendMessage(LibsMsg.NO_PERM.get());
                    return true;
                }

                command.onCommand(sender, args);
            } else {
                sender.sendMessage(LibsMsg.LIBS_COMMAND_WRONG_ARG.get());
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getArgs(origArgs);

        for (LDCommand command : commands) {
            if (!sender.hasPermission(command.getPermission())) {
                continue;
            }

            tabs.addAll(command.getTabComplete());
        }

        return filterTabs(tabs, origArgs);
    }
}
