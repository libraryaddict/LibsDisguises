package me.libraryaddict.disguise.commands.animate;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DisguiseAnimationCommand extends BaseDisguiseAnimate implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String @NotNull [] strings) {
        if (sender instanceof Player && !sender.isOp() && !LibsPremium.isPremium()) {
            sender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the " +
                "plugin for non-admin usage!");
            return true;
        }

        if (strings.length == 0) {
            LibsMsg.DISGUISE_ANIMATE_HELP_COMMAND.send(sender, s.toLowerCase(Locale.ENGLISH));

            List<String> valid = sender instanceof Entity ? getSupported((Entity) sender) : Collections.emptyList();

            // If no valid animations, or not disguised
            if (valid.isEmpty()) {
                LibsMsg.DISGUISE_ANIMATE_SEE_ALL_ANIMATIONS.send(sender,
                    ParamInfoManager.getParamInfo(DisguiseAnimation.class).getName().replace(" ", ""));
            } else {
                LibsMsg.DISGUISE_ANIMATE_HELP_DISGUISED.send(sender,
                    String.join(LibsMsg.DISGUISE_ANIMATE_HELP_DISGUISED_SEPERATOR.get(), valid));
            }
            return true;
        }

        Disguise[] disguises = DisguiseAPI.getDisguises((Entity) sender);

        if (disguises.length == 0) {
            LibsMsg.NOT_DISGUISED.send(sender);

            return true;
        }

        List<DisguiseAnimation>[] validAnimations = new List[disguises.length];

        for (int i = 0; i < disguises.length; i++) {
            validAnimations[i] = DisguiseAnimation.getAnimations(disguises[i].getWatcher().getClass());
        }

        DisguiseAnimation[] animations = getAnimations(sender, validAnimations, strings);

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

        // No command message feedback needed on a success
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                                @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player)) {
            return Collections.emptyList();
        }

        Disguise[] disguises = DisguiseAPI.getDisguises((Player) commandSender);

        if (disguises.length == 0) {
            return Collections.emptyList();
        }

        List<String> valid = getSupported((Entity) commandSender);

        if (strings.length > 0) {
            String lower = strings[strings.length - 1].toLowerCase(Locale.ENGLISH);

            valid.removeIf(arg -> !arg.toLowerCase(Locale.ENGLISH).startsWith(lower));
        }

        return valid;
    }
}
