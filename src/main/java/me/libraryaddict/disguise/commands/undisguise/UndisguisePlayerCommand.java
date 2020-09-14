package me.libraryaddict.disguise.commands.undisguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class UndisguisePlayerCommand implements CommandExecutor, TabCompleter {
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
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the plugin for non-admin usage!");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.undisguiseplayer")) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (args.length == 0) {
            LibsMsg.UNDISG_PLAYER_HELP.send(sender);
            return true;
        }

        Entity entityTarget = Bukkit.getPlayer(args[0]);

        if (entityTarget == null) {
            if (args[0].contains("-")) {
                try {
                    entityTarget = Bukkit.getEntity(UUID.fromString(args[0]));
                }
                catch (Exception ignored) {
                }
            }
        }

        if (entityTarget == null) {
            LibsMsg.CANNOT_FIND_PLAYER.send(sender, args[0]);
            return true;
        }

        if (DisguiseAPI.isDisguised(entityTarget)) {
            DisguiseAPI.undisguiseToAll(entityTarget);
            LibsMsg.UNDISG_PLAYER.send(sender,
                    entityTarget instanceof Player ? entityTarget.getName() :
                            DisguiseType.getType(entityTarget).toReadable());
        } else {
            LibsMsg.UNDISG_PLAYER_FAIL.send(sender,
                    entityTarget instanceof Player ? entityTarget.getName() :
                            DisguiseType.getType(entityTarget).toReadable());
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
            // If command user cannot see player online, don't tab-complete name
            if (sender instanceof Player && !((Player) sender).canSee(player)) {
                continue;
            }

            tabs.add(player.getName());
        }

        return filterTabs(tabs, origArgs);
    }
}
