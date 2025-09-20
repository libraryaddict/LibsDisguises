package me.libraryaddict.disguise.commands.interactions;

import lombok.AllArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsEntityInteract;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class DisguiseEntityInteraction implements LibsEntityInteract {
    private String[] disguiseArgs;

    @Override
    public void onInteract(Player p, Entity entity) {
        String entityName;

        if (entity instanceof Player) {
            entityName = entity.getName();
        } else {
            entityName = DisguiseType.getType(entity).toReadable();
        }

        Disguise disguise;

        try {
            disguise =
                DisguiseParser.parseDisguise(p, entity, "disguiseentity", disguiseArgs, DisguiseParser.getPermissions(p, "disguiseentity"));
        } catch (DisguiseParseException e) {
            e.send(p);
            return;
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        if (disguise.isMiscDisguise() && !DisguiseConfig.isMiscDisguisesForLivingEnabled() && entity instanceof LivingEntity) {
            LibsMsg.DISABLED_LIVING_TO_MISC.send(p);
        } else {
            if (entity instanceof Player && DisguiseConfig.isNameOfPlayerShownAboveDisguise() &&
                !entity.hasPermission("libsdisguises.hidename")) {
                if (disguise.getWatcher() instanceof LivingWatcher) {
                    disguise.getWatcher().setCustomName(getDisplayName((Player) entity));

                    if (DisguiseConfig.isNameAboveHeadAlwaysVisible()) {
                        disguise.getWatcher().setCustomNameVisible(true);
                    }
                }
            }

            DisguiseUtilities.resetPluginTimer();

            DisguiseAPI.disguiseEntity(p, entity, disguise);

            String disguiseName = disguise.getDisguiseName();

            // Jeez, maybe I should redo my messages here
            if (disguise.isDisguiseInUse()) {
                if (disguise.isPlayerDisguise()) {
                    if (entity instanceof Player) {
                        LibsMsg.LISTEN_ENTITY_PLAYER_DISG_PLAYER.send(p, entityName, disguiseName);
                    } else {
                        LibsMsg.LISTEN_ENTITY_ENTITY_DISG_PLAYER.send(p, entityName, disguiseName);
                    }
                } else {
                    if (entity instanceof Player) {
                        LibsMsg.LISTEN_ENTITY_PLAYER_DISG_ENTITY.send(p, entityName, disguiseName);
                    } else {
                        LibsMsg.LISTEN_ENTITY_ENTITY_DISG_ENTITY.send(p, entityName, disguiseName);
                    }
                }
            } else {
                if (disguise.isPlayerDisguise()) {
                    if (entity instanceof Player) {
                        LibsMsg.LISTEN_ENTITY_PLAYER_DISG_PLAYER_FAIL.send(p, entityName, disguiseName);
                    } else {
                        LibsMsg.LISTEN_ENTITY_ENTITY_DISG_PLAYER_FAIL.send(p, entityName, disguiseName);
                    }
                } else {
                    if (entity instanceof Player) {
                        LibsMsg.LISTEN_ENTITY_PLAYER_DISG_ENTITY_FAIL.send(p, entityName, disguiseName);
                    } else {
                        LibsMsg.LISTEN_ENTITY_ENTITY_DISG_ENTITY_FAIL.send(p, entityName, disguiseName);
                    }
                }
            }
        }
    }

    protected String getDisplayName(Player player) {
        String name = DisguiseConfig.getNameAboveDisguise().replace("%simple%", player.getName());

        if (name.contains("%complex%")) {
            name = name.replace("%complex%", DisguiseUtilities.getDisplayName(player));
        }

        // Replace placeholders in the name, but only if the name contains a %
        if (DisguiseUtilities.isPlaceholderApi() && name != null && name.contains("%")) {
            name = PlaceholderAPI.setPlaceholders(player, name);
        }

        return DisguiseUtilities.translateAlternateColorCodes(name);
    }
}