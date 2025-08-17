package me.libraryaddict.disguise.commands.animate;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class BaseDisguiseAnimate {
    public List<String> getSupported(Entity target) {
        List<String> valid = new ArrayList<>();

        for (Disguise disguise : DisguiseAPI.getDisguises(target)) {
            Class<? extends FlagWatcher> cl = disguise.getWatcher().getClass();

            getAnimations(cl).stream().filter(anim -> !valid.contains(anim)).forEach(valid::add);
        }

        return valid;
    }

    /**
     * Helper method to get the animations to send to the player as a chat message or tab completed
     */
    public List<String> getAnimations(@Nullable Class<? extends FlagWatcher> cl) {
        List<DisguiseAnimation> animations = cl == null ? Arrays.asList(DisguiseAnimation.values()) : DisguiseAnimation.getAnimations(cl);

        return animations.stream().filter(anim -> !anim.isHidden()).map(anim -> TranslateType.DISGUISE_ANIMATIONS.get(anim.name()))
            .collect(Collectors.toList());
    }

    /**
     * @return animations supported, or null if there was an issue
     */
    public DisguiseAnimation[] getAnimations(CommandSender sender, @Nullable List<DisguiseAnimation>[] validAnimations,
                                             String[] commandArgs) {
        DisguiseAnimation[] animations = new DisguiseAnimation[commandArgs.length];

        for (int i = 0; i < commandArgs.length; i++) {
            try {
                animations[i] =
                    DisguiseAnimation.valueOf(TranslateType.DISGUISE_ANIMATIONS.reverseGet(commandArgs[i].toUpperCase(Locale.ENGLISH)));
            } catch (Exception ex) {
                LibsMsg.PARSE_INVALID_ANIMATION.send(sender, commandArgs[i]);
                return null;
            }

            DisguiseAnimation animation = animations[i];

            if (validAnimations == null || Arrays.stream(validAnimations).anyMatch(l -> l.contains(animation))) {
                continue;
            }

            LibsMsg.PARSE_MISMATCHED_ANIMATION.send(sender, TranslateType.DISGUISE_ANIMATIONS.get(animation.name()));
            return null;
        }

        return animations;
    }
}
