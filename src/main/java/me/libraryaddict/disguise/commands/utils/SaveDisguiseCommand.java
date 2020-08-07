package me.libraryaddict.disguise.commands.utils;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

/**
 * Created by libraryaddict on 28/12/2019.
 */
public class SaveDisguiseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the plugin for non-admin usage!");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.savedisguise")) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (strings.length == 0) {
            sendHelp(sender);
            return true;
        }

        strings = DisguiseUtilities.split(StringUtils.join(strings, " "));

        String name = strings[0];
        String[] args = Arrays.copyOfRange(strings, 1, strings.length);

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                LibsMsg.NO_CONSOLE.send(sender);
                return true;
            }

            Disguise disguise = DisguiseAPI.getDisguise((Entity) sender);

            if (disguise == null) {
                LibsMsg.NOT_DISGUISED.send(sender);
                return true;
            }

            String disguiseString = DisguiseAPI.parseToString(disguise);

            try {
                DisguiseAPI.addCustomDisguise(name, disguiseString);

                LibsMsg.CUSTOM_DISGUISE_SAVED.send(sender, name);
            }
            catch (DisguiseParseException e) {
                if (e.getMessage() != null) {
                    DisguiseUtilities.sendMessage(sender, e.getMessage());
                } else {
                    LibsMsg.PARSE_CANT_LOAD.send(sender);
                }
            }

            return true;
        }

        // If going to be doing a player disguise...
        if (args.length >= 2 && args[0].equalsIgnoreCase("player")) {
            int i = 2;

            for (; i < args.length; i++) {
                if (!args[i].equalsIgnoreCase("setskin"))
                    continue;

                break;
            }

            // Make array larger, and some logic incase 'setskin' was the last arg
            // Player Notch = 2 - Add 2
            // player Notch setskin = 2 - Add 1
            // player Notch setskin Notch = 2 - Add 0
            if (args.length < i + 1) {
                args = Arrays.copyOf(args, Math.max(args.length, i + 2));
                i = args.length - 2;

                args[i] = "setSkin";
                args[i + 1] = args[1];
            }

            int skinId = i + 1;

            if (!args[skinId].startsWith("{")) {
                String usable = SkinUtils.getUsableStatus();

                if (usable != null) {
                    DisguiseUtilities.sendMessage(sender, usable);
                    return true;
                }

                String[] finalArgs = args;

                SkinUtils.grabSkin(args[skinId], new SkinUtils.SkinCallback() {
                    private BukkitTask runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            LibsMsg.PLEASE_WAIT.send(sender);
                        }
                    }.runTaskTimer(LibsDisguises.getInstance(), 100, 100);

                    @Override
                    public void onError(LibsMsg msg, Object... args) {
                        runnable.cancel();

                        msg.send(sender, args);
                    }

                    @Override
                    public void onInfo(LibsMsg msg, Object... args) {
                        msg.send(sender, args);
                    }

                    @Override
                    public void onSuccess(WrappedGameProfile profile) {
                        runnable.cancel();

                        finalArgs[skinId] = DisguiseUtilities.getGson().toJson(profile);

                        saveDisguise(sender, name, finalArgs);
                    }
                });
            } else {
                saveDisguise(sender, name, args);
            }
        } else {
            saveDisguise(sender, name, args);
        }

        return true;
    }

    private void saveDisguise(CommandSender sender, String name, String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = DisguiseUtilities.quote(args[i]);
        }

        String disguiseString = StringUtils.join(args, " ");

        try {
            DisguiseAPI.addCustomDisguise(name, disguiseString);
            LibsMsg.CUSTOM_DISGUISE_SAVED.send(sender, name);

            DisguiseUtilities.setSaveDisguiseCommandUsed();
        }
        catch (DisguiseParseException e) {
            if (e.getMessage() != null) {
                DisguiseUtilities.sendMessage(sender, e.getMessage());
            } else {
                LibsMsg.PARSE_CANT_LOAD.send(sender);
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        LibsMsg.SAVE_DISG_HELP_1.send(sender);
        LibsMsg.SAVE_DISG_HELP_2.send(sender);
        LibsMsg.SAVE_DISG_HELP_3.send(sender);
        LibsMsg.SAVE_DISG_HELP_4.send(sender);
        LibsMsg.SAVE_DISG_HELP_5.send(sender);
        LibsMsg.SAVE_DISG_HELP_6.send(sender);
    }
}
