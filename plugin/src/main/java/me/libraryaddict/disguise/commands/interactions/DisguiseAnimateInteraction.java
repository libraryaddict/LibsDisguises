package me.libraryaddict.disguise.commands.interactions;

import lombok.AllArgsConstructor;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.commands.animate.BaseDisguiseAnimate;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public class DisguiseAnimateInteraction extends BaseDisguiseAnimate implements LibsEntityInteract {
    private String[] options;

    @Override
    public void onInteract(Player p, Entity entity) {
        Disguise disguise = DisguiseAPI.getDisguise(p, entity);

        if (disguise == null) {
            String entityName;

            if (entity instanceof Player) {
                entityName = entity.getName();
            } else {
                entityName = DisguiseType.getType(entity).toReadable();
            }

            LibsMsg.NOT_DISGUISED_FAIL.send(p, entityName);
            return;
        }

        Disguise[] disguises = DisguiseAPI.getDisguises(entity);

        if (disguises.length == 0) {
            LibsMsg.TARGET_NOT_DISGUISED.send(p);

            return;
        }

        List<DisguiseAnimation>[] validAnimations = new List[disguises.length];

        for (int i = 0; i < disguises.length; i++) {
            validAnimations[i] = DisguiseAnimation.getAnimations(disguises[i].getWatcher().getClass());
        }

        DisguiseAnimation[] animations = getAnimations(p, validAnimations, options);

        if (animations == null) {
            return;
        }

        for (int i = 0; i < disguises.length; i++) {
            for (int animation = 0; animation < validAnimations.length; animation++) {
                if (!validAnimations[i].contains(animations[i])) {
                    continue;
                }

                disguises[i].playAnimation(animations[animation]);
            }
        }
    }
}
