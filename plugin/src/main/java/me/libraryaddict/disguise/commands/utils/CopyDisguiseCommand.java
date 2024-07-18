package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.interactions.CopyDisguiseInteraction;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CopyDisguiseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player && !sender.isOp() &&
            (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the " +
                "plugin for " + "non-admin usage!");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.copydisguise")) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        Entity target = sender instanceof Player ? (Entity) sender : null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                if (args[0].contains("-")) {
                    try {
                        target = Bukkit.getEntity(UUID.fromString(args[0]));
                    } catch (Exception ignored) {
                    }
                }
            }

            if (target == null) {
                LibsMsg.CANNOT_FIND_PLAYER.send(sender, args[0]);
                return true;
            }
        }

        Disguise disguise = DisguiseAPI.getDisguise(target);

        if (disguise == null) {
            LibsDisguises.getInstance().getListener()
                .addInteraction(sender.getName(), new CopyDisguiseInteraction(this), DisguiseConfig.getDisguiseEntityExpire());

            LibsMsg.DISGUISECOPY_INTERACT.send(sender, DisguiseConfig.getDisguiseEntityExpire());
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

    private Component createComponent(String text, int section, int max) {
        Component component = LibsMsg.CLICK_COPY.getAdv(section);

        if (NmsVersion.v1_15.isSupported()) {
            component = component.clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(text));
        } else {
            component = component.clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand(text));
        }

        LibsMsg hover = NmsVersion.v1_15.isSupported() ? LibsMsg.CLICK_TO_COPY_HOVER_CLIPBOARD : LibsMsg.CLICK_TO_COPY_HOVER;
        Component hoverText = hover.getAdv(section, max, DisguiseUtilities.getMiniMessage().escapeTags(text));
        component = component.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hoverText));

        return component;
    }

    public void sendMessage(CommandSender sender, LibsMsg msg, LibsMsg oldVer, String string, boolean forceAbbrev) {
        Builder builder = Component.text().append(msg.getAdv()).appendSpace();

        if (string.length() > 256 || forceAbbrev) {
            String[] split = DisguiseUtilities.split(string);

            // Because the splitter removes the quotes..
            for (int i = 0; i < split.length; i++) {
                split[i] = DisguiseUtilities.quote(split[i]);
            }

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

            List<String> sections = new ArrayList<>();
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

                // Have non-final end with a space
                if (i + 1 < split.length) {
                    current.append(" ");
                }

                sections.add(current.toString());
                current = new StringBuilder();
            }

            for (int i = 0; i < sections.size(); i++) {
                if (i > 0) {
                    builder.appendSpace();
                }

                builder.append(createComponent(sections.get(i), i + 1, sections.size()));
            }
        } else {
            builder.append(createComponent(string, 1, 1));
        }

        DisguiseUtilities.sendMessage(sender, builder.build());
    }
}
