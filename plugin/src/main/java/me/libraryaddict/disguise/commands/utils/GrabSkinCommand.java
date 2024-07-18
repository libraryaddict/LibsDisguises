package me.libraryaddict.disguise.commands.utils;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GrabSkinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player && !sender.isOp() &&
            (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the " +
                "plugin for " + "non-admin usage!");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.grabskin")) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (strings.length == 0) {
            sendHelp(sender);
            return true;
        }

        String[] args = DisguiseUtilities.split(StringUtils.join(strings, " "));
        String tName = args.length > 1 ? args[0] : null;
        String skin = args.length > 1 ? args[1] : args[0];

        String usable = SkinUtils.getUsableStatus();

        if (usable != null) {
            DisguiseUtilities.sendMessage(sender, usable);
            return true;
        }

        if (tName == null && skin.matches("(.*\\/)?[a-zA-Z0-9_-]{3,20}(\\.png)?")) {
            int start = skin.lastIndexOf("/") + 1;
            int end = skin.length();

            if (skin.lastIndexOf(".", start) > start) {
                end = skin.lastIndexOf(".", start);
            }

            tName = skin.substring(start, end);

            if (DisguiseUtilities.hasUserProfile(tName)) {
                tName = null;
            }
        }

        String name =
            tName != null && tName.toLowerCase(Locale.ENGLISH).endsWith(":slim") ? tName.substring(0, tName.lastIndexOf(":")) : tName;

        if (name != null && name.replaceAll("[_a-zA-Z \\d-@#]", "").length() > 0) {
            LibsMsg.SKIN_API_INVALID_NAME.send(sender);
            return true;
        }

        SkinUtils.SkinCallback callback = new SkinUtils.SkinCallback() {
            private final BukkitTask runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    LibsMsg.PLEASE_WAIT.send(sender);
                }
            }.runTaskTimer(LibsDisguises.getInstance(), 100, 100);

            @Override
            public void onError(LibsMsg msg, Object... args) {
                msg.send(sender, args);

                runnable.cancel();
            }

            @Override
            public void onInfo(LibsMsg msg, Object... args) {
                msg.send(sender, args);
            }

            @Override
            public void onSuccess(UserProfile profile) {
                runnable.cancel();
                DisguiseUtilities.doSkinUUIDWarning(sender);

                String nName = name;

                if (nName == null) {
                    int i = 1;

                    while (DisguiseUtilities.hasUserProfile("skin" + i)) {
                        i++;
                    }

                    nName = "skin" + i;
                }

                if (profile.getName() == null || !profile.getName().equals(nName)) {
                    profile = ReflectionManager.getUserProfileWithThisSkin(profile.getUUID(), profile.getName(), profile);
                }

                DisguiseAPI.addGameProfile(nName, profile);
                LibsMsg.GRABBED_SKIN.send(sender, nName);

                String string = DisguiseUtilities.getGson().toJson(profile);

                sendMessage(sender, LibsMsg.CLICK_TO_COPY, LibsMsg.CLICK_TO_COPY, string, false);

                DisguiseUtilities.setGrabSkinCommandUsed();
            }
        };

        SkinUtils.grabSkin(sender, skin, callback);

        return true;
    }

    private void sendHelp(CommandSender sender) {
        LibsMsg.GRAB_DISG_HELP_1.send(sender);
        LibsMsg.GRAB_DISG_HELP_2.send(sender);
        LibsMsg.GRAB_DISG_HELP_3.send(sender);
        LibsMsg.GRAB_DISG_HELP_4.send(sender);
        LibsMsg.GRAB_DISG_HELP_5.send(sender);
        LibsMsg.GRAB_DISG_HELP_6.send(sender);
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

    private void sendMessage(CommandSender sender, LibsMsg msg, LibsMsg oldVer, String string, boolean forceAbbrev) {
        TextComponent.Builder builder = Component.text().append(msg.getAdv()).appendSpace();

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
