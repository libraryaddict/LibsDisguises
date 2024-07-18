package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LDDebugDisguiseLoop implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Collections.singletonList("disguiseloop");
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp();
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED +
                "Must be a player to use this command, if you're seeing this then you clearly don't know what this command is for and " +
                "wouldn't benefit from using it.");
            return;
        } else if (args.length != 2 || !args[1].equalsIgnoreCase("agree")) {
            sender.sendMessage(ChatColor.RED +
                "This is for debugging and is meant to loop over every disguise and disguise you to verify if there's any obvious issues." +
                " There is no value for you in this command. Type 'agree' into the command to continue.");
            return;
        }

        Iterator<String> iterator = getDisguisesToRun().iterator();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!((Player) sender).isOnline()) {
                    cancel();
                    return;
                }

                String command = iterator.next();
                String message = ChatColor.AQUA + "Now disguising " + sender.getName() + " as " + command;
                sender.sendMessage(message);
                LibsDisguises.getInstance().getLogger().info(message);

                Disguise disguise = DisguiseAPI.getDisguise((Player) sender);

                try {
                    ((Player) sender).performCommand("disguise " + command);

                    if (disguise == DisguiseAPI.getDisguise((Player) sender)) {
                        LibsDisguises.getInstance().getLogger().info("Looks like '" + command + "' failed.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (!iterator.hasNext()) {
                        cancel();
                        sender.sendMessage(ChatColor.AQUA + "Command Complete!");
                    }
                }
            }
        }.runTaskTimer(LibsDisguises.getInstance(), 5, 5);
    }

    @NotNull
    private static List<String> getDisguisesToRun() {
        List<String> commands = new ArrayList<>();

        for (DisguiseType type : DisguiseType.values()) {
            if (type.getEntityType() == null || type.isUnknown() || type.isCustom()) {
                continue;
            }

            String command;

            switch (type) {
                case PLAYER:
                    command = "player libraryaddict";
                    break;
                case DROPPED_ITEM:
                    command = "dropped_item obsidian";
                    break;
                default:
                    command = type.name();
                    break;
            }

            commands.add(command);
        }
        return commands;
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_DEBUG_MINESKIN;
    }
}
