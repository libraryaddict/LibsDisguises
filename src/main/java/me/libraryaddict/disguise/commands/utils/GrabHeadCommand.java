package me.libraryaddict.disguise.commands.utils;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;

/**
 * Created by libraryaddict on 20/06/2020.
 */
public class GrabHeadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the plugin for non-admin usage!");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.grabhead")) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            LibsMsg.NO_CONSOLE.send(sender);
            return true;
        }

        if (strings.length == 0) {
            sendHelp(sender);
            return true;
        }

        String[] args = DisguiseUtilities.split(StringUtils.join(strings, " "));
        String skin = args[0];

        String usable = SkinUtils.getUsableStatus();

        if (usable != null) {
            sender.sendMessage(usable);
            return true;
        }

        SkinUtils.SkinCallback callback = new SkinUtils.SkinCallback() {
            private BukkitTask runnable = new BukkitRunnable() {
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
            public void onSuccess(WrappedGameProfile profile) {
                runnable.cancel();

                DisguiseUtilities.setGrabHeadCommandUsed();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) skull.getItemMeta();

                        try {
                            Field field = meta.getClass().getDeclaredField("profile");
                            field.setAccessible(true);
                            field.set(meta, profile.getHandle());
                        }
                        catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        skull.setItemMeta(meta);

                        ((Player) sender).getInventory().addItem(skull);
                        LibsMsg.GRAB_HEAD_SUCCESS.send(sender);
                    }
                }.runTask(LibsDisguises.getInstance());
            }
        };

        SkinUtils.grabSkin(skin, callback);

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
}
