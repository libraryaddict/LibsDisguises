package me.libraryaddict.disguise.disguisetypes;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.watchers.BatWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

class DisguiseRunnable extends BukkitRunnable {
    private int blockX, blockY, blockZ, facing;
    private int deadTicks = 0;
    private int actionBarTicks = -1;
    private int refreshRate;
    private long lastRefreshed = System.currentTimeMillis();
    private final Disguise disguise;
    final Double vectorY;
    final boolean alwaysSendVelocity;

    public DisguiseRunnable(Disguise disguise) {
        this.disguise = disguise;

        switch (disguise.getType()) {
            case FIREWORK:
            case WITHER_SKULL:
            case EXPERIENCE_ORB:
                vectorY = 0.000001D;
                alwaysSendVelocity = true;
                break;
            default:
                vectorY = null;
                alwaysSendVelocity = false;
                break;
        }

        // Where refresh rate is in ticks, exp is in here due to a fire exploit + stop it glitching out so much
        switch (disguise.getType()) {
            case FIREWORK:
            case EXPERIENCE_ORB:
                refreshRate = 40; // 2 seconds
                break;
            case EVOKER_FANGS:
                refreshRate = 23;
                break;
            default:
                break;
        }

        refreshRate *= 50;
    }

    @Override
    public void run() {
        if (!disguise.isDisguiseInUse() || disguise.getEntity() == null || !Bukkit.getWorlds().contains(disguise.getEntity().getWorld())) {
            disguise.stopDisguise();

            // If still somehow not cancelled
            if (!isCancelled()) {
                cancel();
            }
            return;
        }

        if (++actionBarTicks % 15 == 0) {
            actionBarTicks = 0;

            disguise.doPeriodicTick();
        }

        // If entity is no longer valid. Remove it.
        if (disguise.getEntity() instanceof Player && !((Player) disguise.getEntity()).isOnline()) {
            disguise.removeDisguise();

            return;
        } else if (disguise.disguiseExpires > 0 && (DisguiseConfig.isDynamicExpiry() ? disguise.disguiseExpires-- == 1 :
            disguise.disguiseExpires < System.currentTimeMillis())) { // If disguise expired
            disguise.removeDisguise();

            if (disguise.getEntity() instanceof Player) {
                LibsMsg.EXPIRED_DISGUISE.send(disguise.getEntity());
            }

            return;
        } else if (!disguise.getEntity().isValid()) {
            // If it has been dead for 30+ ticks
            // This is to ensure that this disguise isn't removed while clients think its the real entity
            // The delay is because if it sends the destroy entity packets straight away, then it means no
            // death animation
            // This is probably still a problem for wither and enderdragon deaths.
            if (deadTicks++ > (disguise.getType() == DisguiseType.ENDER_DRAGON ? 200 : 20)) {
                if (disguise.isRemoveDisguiseOnDeath()) {
                    disguise.removeDisguise();
                }
            }

            return;
        }

        deadTicks = 0;

        // If the disguise type is invisible, we need to resend the entity packet else it will turn invisible
        if (refreshRate > 0 && lastRefreshed + refreshRate < System.currentTimeMillis()) {
            lastRefreshed = System.currentTimeMillis();

            DisguiseUtilities.refreshTrackers((TargetedDisguise) disguise);
        }

        if (disguise.isModifyBoundingBox()) {
            DisguiseUtilities.doBoundingBox((TargetedDisguise) disguise);
        }

        if (disguise.getType() == DisguiseType.BAT && !((BatWatcher) disguise.getWatcher()).isHanging()) {
            return;
        }

        doVelocity(vectorY, alwaysSendVelocity);

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {
            for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                if (disguise.getEntity() != player) {
                    WrapperPlayServerEntityRelativeMove packet =
                        new WrapperPlayServerEntityRelativeMove(disguise.getEntity().getEntityId(), 0, 0, 0, true);

                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
                    continue;
                } else if (!disguise.isSelfDisguiseVisible() || !(disguise.getEntity() instanceof Player)) {
                    continue;
                }

                WrapperPlayServerEntityRelativeMove selfPacket =
                    new WrapperPlayServerEntityRelativeMove(DisguiseAPI.getSelfDisguiseId(), 0, 0, 0, true);

                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, selfPacket);
            }
        }
    }

    private void doVelocity(Double vectorY, boolean alwaysSendVelocity) {
        // If the vectorY isn't 0. Cos if it is. Then it doesn't want to send any vectors.
        if (vectorY == null || !disguise.isVelocitySent()) {
            return;
        }

        Entity entity = disguise.getEntity();

        // If this disguise has velocity sending enabled and the entity is flying.
        if (!alwaysSendVelocity && entity.isOnGround()) {
            return;
        }

        Vector vector = entity.getVelocity();

        // If the entity doesn't have velocity changes already - You know. I really can't wrap my
        // head about the
        // if statement.
        // But it doesn't seem to do anything wrong..
        if (vector.getY() != 0 && !(vector.getY() < 0 && alwaysSendVelocity && entity.isOnGround())) {
            return;
        }

        // If disguise isn't a experience orb, or the entity isn't standing on the ground
        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB && entity.isOnGround()) {
            return;
        }

        WrapperPlayServerEntityRotation lookPacket = null;

        if (disguise.getType() == DisguiseType.WITHER_SKULL && DisguiseConfig.isWitherSkullPacketsEnabled()) {
            Location loc = entity.getLocation();
            float yaw = DisguiseUtilities.getYaw(disguise.getType(), entity.getType(), loc.getYaw());
            float pitch = DisguiseUtilities.getPitch(disguise.getType(), entity.getType(), loc.getPitch());
            lookPacket = new WrapperPlayServerEntityRotation(entity.getEntityId(), yaw, pitch, entity.isOnGround());

            if (disguise.isSelfDisguiseVisible() && entity instanceof Player) {
                WrapperPlayServerEntityRotation selfPacket =
                    new WrapperPlayServerEntityRotation(DisguiseAPI.getSelfDisguiseId(), yaw, pitch, entity.isOnGround());

                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(entity, selfPacket);
            }
        }

        try {

            for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                int entityId = entity.getEntityId();

                // If the viewing player is the disguised player
                if (entity == player) {
                    // If not using self disguise, continue
                    if (!disguise.isSelfDisguiseVisible()) {
                        continue;
                    }

                    // Write self disguise ID
                    entityId = DisguiseAPI.getSelfDisguiseId();
                } else if (lookPacket != null) {
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player,
                        new WrapperPlayServerEntityRotation(lookPacket.getEntityId(), lookPacket.getYaw(), lookPacket.getPitch(),
                            lookPacket.isOnGround()));
                }

                // The number isn't me trying to be funny
                WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(entityId,
                    new Vector3d(vector.getX(), (vectorY * ReflectionManager.getPing(player)) * 0.069D, vector.getZ()));

                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, velocity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // If we need to send a packet to update the exp position as it likes to gravitate client
    // sided to
    // players.
}


