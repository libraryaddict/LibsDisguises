package me.libraryaddict.disguise.commands.animate;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DisguisePlayerAnimationCommand extends BaseDisguiseAnimate implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String @NotNull [] args) {
        if (commandSender instanceof Player && !commandSender.isOp() && !LibsPremium.isPremium()) {
            commandSender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the " +
                "plugin for non-admin usage!");
            return true;
        }

        Entity entityTarget = null;

        if (args.length > 0) {
            entityTarget = Bukkit.getPlayer(args[0]);

            if (entityTarget == null) {
                if (args[0].contains("-")) {
                    try {
                        entityTarget = Bukkit.getEntity(UUID.fromString(args[0]));
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (args.length < 2) {
            LibsMsg.D_ANIMATE_PLAYER.send(commandSender, s.toLowerCase(Locale.ENGLISH));

            List<String> valid = entityTarget == null ? Collections.emptyList() : getSupported(entityTarget);

            // If no valid animations, or not disguised
            if (valid.isEmpty()) {
                LibsMsg.DISGUISE_ANIMATE_SEE_ALL_ANIMATIONS.send(commandSender,
                    ParamInfoManager.getParamInfo(DisguiseAnimation.class).getName().replace(" ", ""));
            } else {
                LibsMsg.DISGUISE_ANIMATE_HELP_DISGUISED.send(commandSender,
                    String.join(LibsMsg.DISGUISE_ANIMATE_HELP_DISGUISED_SEPERATOR.get(), valid));
            }
            return true;
        }

        if (entityTarget == null) {
            LibsMsg.CANNOT_FIND_PLAYER.send(commandSender);
            return true;
        }

        Disguise[] disguises = DisguiseAPI.getDisguises(entityTarget);

        if (disguises.length == 0) {
            LibsMsg.TARGET_NOT_DISGUISED.send(commandSender);

            return true;
        }

        List<DisguiseAnimation>[] validAnimations = new List[disguises.length];

        for (int i = 0; i < disguises.length; i++) {
            validAnimations[i] = DisguiseAnimation.getAnimations(disguises[i].getWatcher().getClass());
        }

        DisguiseAnimation[] animations = getAnimations(commandSender, validAnimations, Arrays.copyOfRange(args, 1, args.length));

        if (animations == null) {
            return true;
        }

        for (int i = 0; i < disguises.length; i++) {
            for (int animation = 0; animation < validAnimations.length; animation++) {
                if (!validAnimations[i].contains(animations[i])) {
                    continue;
                }

                disguises[i].playAnimation(animations[animation]);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                                                @NotNull String @NotNull [] strings) {
        List<String> tabs = new ArrayList<>();

        String prefix = strings.length == 0 ? "" : strings[strings.length - 1].toLowerCase(Locale.ENGLISH);

        if (strings.length <= 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // If command user cannot see player online, don't tab-complete name
                if (sender instanceof Player && !((Player) sender).canSee(player)) {
                    continue;
                }

                if (!player.getName().toLowerCase(Locale.ENGLISH).startsWith(prefix)) {
                    continue;
                }

                tabs.add(player.getName());
            }

            return tabs;
        }

        Entity player = Bukkit.getPlayer(strings[0]);

        if (player == null && strings[0].contains("-")) {
            try {
                player = Bukkit.getEntity(UUID.fromString(strings[0]));
            } catch (Exception ignored) {
            }
        }

        if (player == null) {
            return tabs;
        }

        List<String> valid = getSupported(player);

        if (strings.length > 0) {
            String lower = strings[strings.length - 1].toLowerCase(Locale.ENGLISH);

            valid.removeIf(arg -> !arg.toLowerCase(Locale.ENGLISH).startsWith(lower));
        }

        return valid;
    }
}
