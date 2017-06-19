package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UndisguisePlayerCommand implements CommandExecutor, TabCompleter {
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
        if (sender.hasPermission("libsdisguises.undisguiseplayer")) {
            if (args.length > 0) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    if (DisguiseAPI.isDisguised(p)) {
                        DisguiseAPI.undisguiseToAll(p);
                        sender.sendMessage(LibsMsg.UNDISG_PLAYER.get(p.getName()));
                    } else {
                        sender.sendMessage(LibsMsg.UNDISG_PLAYER_FAIL.get(p.getName()));
                    }
                } else {
                    sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[0]));
                }
            } else {
                sender.sendMessage(LibsMsg.UNDISG_PLAYER_HELP.get());
            }
        } else {
            sender.sendMessage(LibsMsg.NO_PERM.get());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getArgs(origArgs);

        if (args.length != 0)
            return filterTabs(tabs, origArgs);

        for (Player player : Bukkit.getOnlinePlayers()) {
            tabs.add(player.getName());
        }

        return filterTabs(tabs, origArgs);
    }
}
