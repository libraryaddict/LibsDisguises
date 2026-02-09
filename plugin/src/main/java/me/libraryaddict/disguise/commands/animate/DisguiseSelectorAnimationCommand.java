package me.libraryaddict.disguise.commands.animate;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class DisguiseSelectorAnimationCommand extends BaseDisguiseAnimate implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!NmsVersion.v1_13.isSupported()) {
            sender.sendMessage(ChatColor.RED + "Entity selectors require 1.13+, this server is running an older version of Minecraft.");
            return true;
        }

        if (!LibsPremium.isPremium()) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, entity selector commands are limited to premium versions only!");
            return true;
        }

        if (args.length == 0) {
            LibsMsg.D_ANIM_SELECTOR_HELP_1.send(sender);
            LibsMsg.D_ANIM_SELECTOR_HELP_2.send(sender, s.toLowerCase(Locale.ENGLISH));
            LibsMsg.DISGUISE_ANIMATE_SEE_ALL_ANIMATIONS.send(sender,
                ParamInfoManager.getParamInfo(DisguiseAnimation.class).getName().replace(" ", ""));
            return true;
        }

        String[] disguiseArgs = DisguiseUtilities.split(StringUtils.join(args, " "));

        if (disguiseArgs.length == 1) {
            LibsMsg.D_ANIM_SELECTOR_NEED_ANIMS_ENTITY.send(sender);
            return true;
        }

        DisguiseAnimation[] animations = getAnimations(sender, null, Arrays.copyOfRange(disguiseArgs, 1, disguiseArgs.length));

        if (animations == null) {
            return true;
        }

        int modifiedDisguises = 0;
        List<Entity> entities;

        try {
            entities = Bukkit.selectEntities(sender, disguiseArgs[0]);
        } catch (IllegalArgumentException ex) {
            LibsMsg.DISGUISE_ENTITY_SELECTOR_INVALID.send(sender, disguiseArgs[0]);
            return true;
        }

        for (Entity entity : entities) {
            if (sender instanceof Player && entity instanceof Player && !((Player) sender).canSee((Player) entity)) {
                continue;
            }

            Disguise[] disguises = DisguiseAPI.getDisguises(entity);

            for (Disguise disguise : disguises) {
                List<DisguiseAnimation> validAnimations = DisguiseAnimation.getAnimations(disguise.getType());

                for (DisguiseAnimation disguiseAnimation : animations) {
                    if (!validAnimations.contains(disguiseAnimation)) {
                        continue;
                    }

                    disguise.playAnimation(disguiseAnimation);
                    modifiedDisguises++;
                }
            }
        }

        if (modifiedDisguises > 0) {
            LibsMsg.D_ANIM_SELECTOR_SUCCESS.send(sender, modifiedDisguises);
        } else {
            LibsMsg.D_ANIM_SELECTOR_FAIL.send(sender);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                                @NotNull String @NotNull [] args) {
        List<String> tabs = new ArrayList<>();

        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ENGLISH) : "";

        if (args.length <= 1) {
            return tabs;
        }

        List<String> list = getAnimations(null);

        for (String animation : list) {
            if (!animation.toLowerCase(Locale.ENGLISH).startsWith(lastArg)) {
                continue;
            }

            tabs.add(animation);
        }

        return tabs;
    }
}
