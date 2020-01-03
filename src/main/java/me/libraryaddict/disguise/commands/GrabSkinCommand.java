package me.libraryaddict.disguise.commands;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Created by libraryaddict on 28/12/2019.
 */
public class GrabSkinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "Please purchase Lib's Disguises to enable player commands");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.grabskin")) {
            sender.sendMessage(LibsMsg.NO_PERM.get());
            return true;
        }

        if (strings.length == 0) {
            sendHelp(sender);
            return true;
        }

        String[] args = DisguiseUtilities.split(StringUtils.join(strings, " "));
        String tName = args.length > 1 ? args[1] : null;

        String usable = SkinUtils.getUsableStatus();

        if (usable != null) {
            sender.sendMessage(usable);
            return true;
        }

        if (tName == null && args[0].matches("(.*\\/)?[a-zA-Z0-9_-]{3,20}\\.png")) {
            tName = tName.substring(args[0].lastIndexOf("/") + 1, args[0].lastIndexOf("."));

            if (DisguiseUtilities.hasGameProfile(tName)) {
                tName = null;
            }
        }

        String name = tName;

        SkinUtils.SkinCallback callback = new SkinUtils.SkinCallback() {
            private BukkitTask runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    sender.sendMessage(LibsMsg.PLEASE_WAIT.get());
                }
            }.runTaskTimer(LibsDisguises.getInstance(), 100, 100);

            @Override
            public void onError(LibsMsg msg, Object... args) {
                sender.sendMessage(msg.get(args));

                runnable.cancel();
            }

            @Override
            public void onInfo(LibsMsg msg, Object... args) {
                sender.sendMessage(msg.get(args));
            }

            @Override
            public void onSuccess(WrappedGameProfile profile) {
                runnable.cancel();

                String nName = name;

                if (nName == null) {
                    if (profile.getName() != null && profile.getName().length() > 0 &&
                            !DisguiseUtilities.hasGameProfile(profile.getName())) {
                        nName = profile.getName();
                    } else {
                        int i = 1;

                        while (DisguiseUtilities.hasGameProfile("skin" + i)) {
                            i++;
                        }

                        nName = "skin" + i;
                    }
                }

                if (profile.getName() == null || !profile.getName().equals(nName)) {
                    profile = ReflectionManager
                            .getGameProfileWithThisSkin(profile.getUUID(), profile.getName(), profile);
                }

                DisguiseAPI.addGameProfile(nName, profile);
                sender.sendMessage(LibsMsg.GRABBED_SKIN.get(nName));

                String string = DisguiseUtilities.getGson().toJson(profile);
                int start = 0;
                int msg = 1;

                ComponentBuilder builder = new ComponentBuilder("").appendLegacy(LibsMsg.CLICK_TO_COPY.get());

                while (start < string.length()) {
                    int end = Math.min(256, string.length() - start);

                    String sub = string.substring(start, start + end);

                    builder.append(" ");

                    if (string.length() <= 256) {
                        builder.appendLegacy(LibsMsg.CLICK_TO_COPY_DATA.get());
                    } else {
                        builder.reset();
                        builder.appendLegacy(LibsMsg.CLICK_COPY.get(msg));
                    }

                    start += end;

                    builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, sub));
                    builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(LibsMsg.CLICK_TO_COPY_HOVER.get() + " " + msg).create()));
                    msg += 1;
                }

                sender.spigot().sendMessage(builder.create());

                DisguiseUtilities.setGrabSkinCommandUsed();
            }
        };

        SkinUtils.grabSkin(args[0], callback);

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(LibsMsg.GRAB_DISG_HELP_1.get());
        sender.sendMessage(LibsMsg.GRAB_DISG_HELP_2.get());
        sender.sendMessage(LibsMsg.GRAB_DISG_HELP_3.get());
        sender.sendMessage(LibsMsg.GRAB_DISG_HELP_4.get());
        sender.sendMessage(LibsMsg.GRAB_DISG_HELP_5.get());
    }
}
