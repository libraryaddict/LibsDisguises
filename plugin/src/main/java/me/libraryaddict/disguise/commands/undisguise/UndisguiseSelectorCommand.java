package me.libraryaddict.disguise.commands.undisguise;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class UndisguiseSelectorCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!NmsVersion.v1_13.isSupported()) {
            sender.sendMessage(ChatColor.RED + "Entity selectors require 1.13+, this server is running an older version of Minecraft.");
            return true;
        }

        if (!LibsPremium.isPremium()) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, entity selector commands are limited to premium versions only!");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.undisguiseselector")) {
            LibsMsg.NO_PERM.send(sender);
            return true;
        }

        if (args.length == 0) {
            LibsMsg.UNDISG_SELECTOR_HELP.send(sender);
            return true;
        }

        if (sender instanceof Player) {
            DisguiseUtilities.setCommandsUsed();
        } else {
            DisguiseUtilities.resetPluginTimer();
        }

        List<Entity> entities;

        String arg = String.join(" ", args);

        try {
            entities = Bukkit.selectEntities(sender, arg);
        } catch (IllegalArgumentException ex) {
            LibsMsg.DISGUISE_ENTITY_SELECTOR_INVALID.send(sender, arg);
            return true;
        }

        int disguisedEntitys = 0;

        for (Entity entity : entities) {
            if (DisguiseAPI.isDisguised(entity)) {
                DisguiseAPI.undisguiseToAll(sender, entity);
                disguisedEntitys++;
            }
        }

        LibsMsg.UNDISG_SELECTOR_SUCCESS.send(sender, disguisedEntitys);

        return true;
    }
}
