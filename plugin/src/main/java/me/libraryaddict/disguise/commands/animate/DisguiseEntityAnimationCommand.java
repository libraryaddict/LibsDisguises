package me.libraryaddict.disguise.commands.animate;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.commands.interactions.DisguiseAnimateInteraction;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class DisguiseEntityAnimationCommand extends BaseDisguiseAnimate implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player && !commandSender.isOp() && !LibsPremium.isPremium()) {
            commandSender.sendMessage(ChatColor.RED +
                "This is the free version of Lib's Disguises, player commands are limited to console and Operators only! Purchase the " +
                "plugin for non-admin usage!");
            return true;
        }

        if (strings.length == 0) {
            LibsMsg.D_ANIMATE_ENTITY.send(commandSender, s.toLowerCase(Locale.ENGLISH));
            LibsMsg.DISGUISE_ANIMATE_SEE_ALL_ANIMATIONS.send(commandSender,
                ParamInfoManager.getParamInfo(DisguiseAnimation.class).getName().replace(" ", ""));
            return true;
        }

        LibsDisguises.getInstance().getListener()
            .addInteraction(commandSender.getName(), new DisguiseAnimateInteraction(strings), DisguiseConfig.getDisguiseEntityExpire());

        LibsMsg.ANIMATE_ENTITY_CLICK.send(commandSender, DisguiseConfig.getDisguiseEntityExpire());
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                                                @NotNull String @NotNull [] strings) {
        String lastArg = strings.length == 0 ? "" : strings[strings.length - 1].toLowerCase(Locale.ENGLISH);

        List<String> list = getAnimations(null);
        list.removeIf(str -> !str.toLowerCase(Locale.ENGLISH).startsWith(lastArg));

        return list;
    }
}
