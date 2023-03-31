package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.watchers.BatWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created by libraryaddict on 20/05/2021.
 */
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

            disguise.doActionBar();
        }

        // If entity is no longer valid. Remove it.
        if (disguise.getEntity() instanceof Player && !((Player) disguise.getEntity()).isOnline()) {
            disguise.removeDisguise();
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

        // If the disguise type is invisibable, we need to resend the entity packet else it will turn invisible
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);

            packet.getIntegers().write(0, disguise.getEntity().getEntityId());

            for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                if (disguise.getEntity() != player) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                    continue;
                } else if (!disguise.isSelfDisguiseVisible() || !(disguise.getEntity() instanceof Player)) {
                    continue;
                }

                PacketContainer selfPacket = packet.shallowClone();

                selfPacket.getModifier().write(0, DisguiseAPI.getSelfDisguiseId());

                ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), selfPacket, false);
            }
        }
    }

    private void doVelocity(Double vectorY, boolean alwaysSendVelocity) {
        // If the vectorY isn't 0. Cos if it is. Then it doesn't want to send any vectors.
        // If this disguise has velocity sending enabled and the entity is flying.
        if (disguise.isVelocitySent() && vectorY != null && (alwaysSendVelocity || !disguise.getEntity().isOnGround())) {
            Vector vector = disguise.getEntity().getVelocity();

            // If the entity doesn't have velocity changes already - You know. I really can't wrap my
            // head about the
            // if statement.
            // But it doesn't seem to do anything wrong..
            if (vector.getY() != 0 && !(vector.getY() < 0 && alwaysSendVelocity && disguise.getEntity().isOnGround())) {
                return;
            }

            // If disguise isn't a experience orb, or the entity isn't standing on the ground
            if (disguise.getType() != DisguiseType.EXPERIENCE_ORB || !disguise.getEntity().isOnGround()) {
                PacketContainer lookPacket = null;

                if (disguise.getType() == DisguiseType.WITHER_SKULL && DisguiseConfig.isWitherSkullPacketsEnabled()) {
                    lookPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);

                    StructureModifier<Object> mods = lookPacket.getModifier();
                    lookPacket.getIntegers().write(0, disguise.getEntity().getEntityId());
                    Location loc = disguise.getEntity().getLocation();

                    mods.write(4,
                        DisguiseUtilities.getYaw(disguise.getType(), disguise.getEntity().getType(), (byte) Math.floor(loc.getYaw() * 256.0F / 360.0F)));
                    mods.write(5,
                        DisguiseUtilities.getPitch(disguise.getType(), disguise.getEntity().getType(), (byte) Math.floor(loc.getPitch() * 256.0F / 360.0F)));

                    if (disguise.isSelfDisguiseVisible() && disguise.getEntity() instanceof Player) {
                        PacketContainer selfLookPacket = lookPacket.shallowClone();

                        selfLookPacket.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());

                        ProtocolLibrary.getProtocolManager().sendServerPacket((Player) disguise.getEntity(), selfLookPacket, false);
                    }
                }

                try {
                    PacketContainer velocityPacket = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);

                    StructureModifier<Integer> mods = velocityPacket.getIntegers();

                    // Write entity ID
                    mods.write(0, disguise.getEntity().getEntityId());
                    mods.write(1, (int) (vector.getX() * 8000));
                    mods.write(3, (int) (vector.getZ() * 8000));

                    for (Player player : DisguiseUtilities.getPerverts(disguise)) {
                        PacketContainer tempVelocityPacket = velocityPacket.shallowClone();
                        mods = tempVelocityPacket.getIntegers();

                        // If the viewing player is the disguised player
                        if (disguise.getEntity() == player) {
                            // If not using self disguise, continue
                            if (!disguise.isSelfDisguiseVisible()) {
                                continue;
                            }

                            // Write self disguise ID
                            mods.write(0, DisguiseAPI.getSelfDisguiseId());
                        }

                        mods.write(2, (int) (8000D * (vectorY * ReflectionManager.getPing(player)) * 0.069D));

                        if (lookPacket != null && player != disguise.getEntity()) {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, lookPacket, false);
                        }

                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, tempVelocityPacket, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // If we need to send a packet to update the exp position as it likes to gravitate client
            // sided to
            // players.
        }
    }
}
