package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.interactions.CopyDisguiseInteraction;
import me.libraryaddict.disguise.commands.interactions.DisguiseModifyInteraction;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by libraryaddict on 1/01/2020.
 */
public class CopyDisguiseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "Please purchase Lib's Disguises to enable player commands");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.copydisguise")) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        Entity target = sender instanceof Player ? (Entity) sender : null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                if (args[0].contains("-")) {
                    try {
                        target = Bukkit.getEntity(UUID.fromString(args[0]));
                    }
                    catch (Exception ignored) {
                    }
                }
            }

            if (target == null) {
                sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[0]));
                return true;
            }
        }

        Disguise disguise = DisguiseAPI.getDisguise(target);

        if (disguise == null) {
            LibsDisguises.getInstance().getListener()
                    .addInteraction(sender.getName(), new CopyDisguiseInteraction(this),
                            DisguiseConfig.getDisguiseEntityExpire());

            sender.sendMessage(LibsMsg.DISGUISECOPY_INTERACT.get(DisguiseConfig.getDisguiseEntityExpire()));
            return true;
        }

        String disguiseString = DisguiseParser.parseToString(disguise, false);

        sendMessage(sender, LibsMsg.CLICK_TO_COPY, LibsMsg.COPY_DISGUISE_NO_COPY, disguiseString, false);

        if (disguise instanceof PlayerDisguise) {
            sendMessage(sender, LibsMsg.CLICK_TO_COPY_WITH_SKIN, LibsMsg.CLICK_TO_COPY_WITH_SKIN_NO_COPY,
                    DisguiseParser.parseToString(disguise), true);
        }

        DisguiseUtilities.setCopyDisguiseCommandUsed();

        return true;
    }

    public void sendMessage(CommandSender sender, LibsMsg msg, LibsMsg oldVer, String string, boolean forceAbbrev) {
        if (!NmsVersion.v1_13.isSupported()) {
            sender.sendMessage(oldVer.get(string));
            return;
        }

        ComponentBuilder builder = new ComponentBuilder("").appendLegacy(msg.get()).append(" ");

        if (string.length() > 256 || forceAbbrev) {
            String[] split = DisguiseUtilities.split(string);

            for (int i = 0; i < split.length; i++) {
                if (split[i].length() <= 256) {
                    continue;
                }

                split = Arrays.copyOf(split, split.length + 1);

                for (int a = split.length - 1; a > i; a--) {
                    split[a] = split[a - 1];
                }

                split[i + 1] = split[i].substring(256);
                split[i] = split[i].substring(0, 256);
            }

            int sections = 0;
            StringBuilder current = new StringBuilder();

            for (int i = 0; i < split.length; i++) {
                if (current.length() > 0) {
                    current.append(" ");
                }

                current.append(split[i]);

                // If the next split would fit
                if (split.length > i + 1 && split[i + 1].length() + current.length() + 1 <= 256) {
                    continue;
                }

                if (sections != 0) {
                    builder.append(" ");
                    builder.reset();
                }

                sections++;

                builder.appendLegacy(LibsMsg.CLICK_COPY.get(sections));
                builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, current.toString()));
                builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(LibsMsg.CLICK_TO_COPY_HOVER.get() + " " + sections).create()));

                current = new StringBuilder();
            }
        } else {
            builder.appendLegacy(LibsMsg.CLICK_COPY.get(string));
            builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, string));
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(LibsMsg.CLICK_TO_COPY_HOVER.get()).create()));
        }

        sender.spigot().sendMessage(builder.create());
    }
}
