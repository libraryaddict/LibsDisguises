package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisguiseCloneCommand extends DisguiseBaseCommand implements TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage(
                    TranslateType.MESSAGE.get(ChatColor.RED + "You may not use this command from the console!"));
            return true;
        }

        if (sender.hasPermission("libsdisguises.disguise.disguiseclone")) {
            boolean doEquipment = true;
            boolean doSneak = false;
            boolean doSprint = false;
            Player player = null;

            if (args.length > 0) {
                player = Bukkit.getPlayerExact(args[0]);
            }

            for (int i = player == null ? 0 : 1; i < args.length; i++) {
                String option = args[i];
                if (StringUtils.startsWithIgnoreCase(option, "ignoreEquip") || StringUtils.startsWithIgnoreCase(option,
                        "ignoreEnquip")) {
                    doEquipment = false;
                } else if (option.equalsIgnoreCase("doSneakSprint")) {
                    doSneak = true;
                    doSprint = true;
                } else if (option.equalsIgnoreCase("doSneak")) {
                    doSneak = true;
                } else if (option.equalsIgnoreCase("doSprint")) {
                    doSprint = true;
                } else {
                    sender.sendMessage(String.format(TranslateType.MESSAGE.get(
                            ChatColor.DARK_RED + "Unknown " + "option '%s" + "' - Valid options are 'IgnoreEquipment' 'DoSneakSprint' 'DoSneak' 'DoSprint'"),
                            option));
                    return true;
                }
            }

            Boolean[] options = new Boolean[]{doEquipment, doSneak, doSprint};

            if (player != null) {
                DisguiseUtilities.createClonedDisguise((Player) sender, player, options);
            } else {
                LibsDisguises.getInstance().getListener().setDisguiseClone(sender.getName(), options);

                sender.sendMessage(String.format(TranslateType.MESSAGE.get(
                        ChatColor.RED + "Right click a entity in the next %s" + " seconds to grab the disguise reference!"),
                        DisguiseConfig.getDisguiseCloneExpire()));
            }
        } else {
            sender.sendMessage(TranslateType.MESSAGE.get(ChatColor.RED + "You are forbidden to use this command."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();

        String[] args = getArgs(origArgs);

        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tabs.add(player.getName());
            }
        }

        tabs.add("ignoreEquip");
        tabs.add("doSneakSprint");
        tabs.add("doSneak");
        tabs.add("doSprint");

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender,
            HashMap<DisguisePerm, HashMap<ArrayList<String>, Boolean>> map) {
        sender.sendMessage(TranslateType.MESSAGE.get(
                ChatColor.DARK_GREEN + "Right click a entity to get a disguise reference you can pass to other " + "disguise commands!"));
        sender.sendMessage(TranslateType.MESSAGE.get(
                ChatColor.DARK_GREEN + "Security note: Any references you create will be available to all players " + "able to use disguise references."));
        sender.sendMessage(TranslateType.MESSAGE.get(
                ChatColor.DARK_GREEN + "/disguiseclone IgnoreEquipment" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")"));
    }
}
