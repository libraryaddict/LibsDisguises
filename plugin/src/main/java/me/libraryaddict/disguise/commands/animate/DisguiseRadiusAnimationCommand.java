package me.libraryaddict.disguise.commands.animate;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
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

public class DisguiseRadiusAnimationCommand extends BaseDisguiseAnimate implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (sender instanceof Player && !sender.isOp() && !LibsPremium.isPremium()) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the " +
                "plugin for non-admin usage!");
            return true;
        }

        if (args.length < 2) {
            LibsMsg.D_ANIM_RADIUS_HELP_1.send(sender, DisguiseConfig.getDisguiseRadiusMax());
            LibsMsg.D_ANIM_RADIUS_HELP_2.send(sender, s.toLowerCase(Locale.ENGLISH));
            LibsMsg.DISGUISE_ANIMATE_SEE_ALL_ANIMATIONS.send(sender,
                ParamInfoManager.getParamInfo(DisguiseAnimation.class).getName().replace(" ", ""));
            return true;
        }

        DisguiseType baseType = null;
        int starting = 0;

        if (isNotInteger(args[0])) {
            for (DisguiseType t : DisguiseType.values()) {
                if (t.getEntityType() == null) {
                    continue;
                }

                if (t.toReadable().replaceAll(" ", "").equalsIgnoreCase(args[0].replaceAll("_", ""))) {
                    baseType = t;
                    starting = 1;
                    break;
                }
            }

            if (baseType == null) {
                LibsMsg.UNRECOGNIZED_DISGUISE_TYPE.send(sender, args[0]);
                return true;
            }
        }

        if (args.length == starting + 1) {
            if (starting == 0) {
                LibsMsg.D_ANIM_RADIUS_NEED_ANIMS.send(sender);
            } else {
                LibsMsg.D_ANIM_RADIUS_NEED_ANIMS_ENTITY.send(sender);
            }

            return true;
        } else if (args.length < 2) {
            LibsMsg.D_ANIM_RADIUS_NEED_ANIMS.send(sender);
            return true;
        }

        if (isNotInteger(args[starting])) {
            LibsMsg.NOT_NUMBER.send(sender, args[starting]);
            return true;
        }

        int radius = Integer.parseInt(args[starting]);

        if (radius > DisguiseConfig.getDisguiseRadiusMax()) {
            LibsMsg.LIMITED_RADIUS.send(sender, DisguiseConfig.getDisguiseRadiusMax());
            radius = DisguiseConfig.getDisguiseRadiusMax();
        }

        DisguiseAnimation[] animations = getAnimations(sender, null, Arrays.copyOfRange(args, starting + 1, args.length));

        if (animations == null) {
            return true;
        }

        int modifiedDisguises = 0;

        for (Entity entity : getNearbyEntities(sender, radius)) {
            if (entity == sender) {
                continue;
            }

            if (baseType != null && !baseType.name().equalsIgnoreCase(entity.getType().name())) {
                continue;
            }

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
            LibsMsg.D_ANIM_RADIUS_SUCCESS.send(sender, modifiedDisguises);
        } else {
            LibsMsg.D_ANIM_RADIUS_FAIL.send(sender);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                                @NotNull String @NotNull [] args) {
        List<String> tabs = new ArrayList<>();

        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ENGLISH) : "";

        // If the argument starts with a non-letter, then it cannot be an entity type/animation
        if (lastArg.matches("^[^a-z].*$")) {
            return tabs;
        }

        // If this is the second param, and the first arg was an entity name, then this arg is the radius.
        if (args.length == 2 && args[0].matches("^[a-zA-Z].*$")) {
            return tabs;
        }

        if (args.length <= 1) {
            for (DisguiseType type : DisguiseType.values()) {
                if (type.getEntityType() == null) {
                    continue;
                }

                String name = type.toReadable().replace(" ", "_");

                if (!name.toLowerCase(Locale.ENGLISH).startsWith(lastArg)) {
                    continue;
                }

                tabs.add(name);
            }

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

    private boolean isNotInteger(String string) {
        try {
            Integer.parseInt(string);
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    private Collection<Entity> getNearbyEntities(CommandSender sender, int radius) {
        Location center;

        if (sender instanceof Player) {
            center = ((Player) sender).getLocation();
        } else {
            center = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5, 0, 0.5);
        }

        return center.getWorld().getNearbyEntities(center, radius, radius, radius);
    }
}
