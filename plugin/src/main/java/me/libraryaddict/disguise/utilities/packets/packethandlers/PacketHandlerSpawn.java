package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.world.Direction;
import com.github.retrooper.packetevents.protocol.world.PaintingType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCamera;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnExperienceOrb;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPainting;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.ModdedDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.listeners.PlayerSkinHandler;
import me.libraryaddict.disguise.utilities.packets.IPacketHandler;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PacketHandlerSpawn implements IPacketHandler {
    @Override
    public PacketTypeCommon[] getHandledPackets() {
        List<PacketTypeCommon> packets = new ArrayList<>();

        if (!NmsVersion.v1_20_R2.isSupported()) {
            packets.add(Server.SPAWN_PLAYER);
        }

        packets.add(Server.SPAWN_EXPERIENCE_ORB);
        packets.add(Server.SPAWN_ENTITY);

        if (!NmsVersion.v1_19_R1.isSupported()) {
            packets.add(Server.SPAWN_LIVING_ENTITY);
            packets.add(Server.SPAWN_PAINTING);
        }

        return packets.toArray(new PacketTypeCommon[0]);
    }

    @Override
    public void handle(Disguise disguise, LibsPackets packets, Player observer, Entity entity) {
        packets.clear();

        if (disguise.getType() == DisguiseType.UNKNOWN) {
            return;
        }

        constructSpawnPackets(observer, packets, entity);
    }

    /**
     * Construct the packets I need to spawn in the disguise
     */
    private void constructSpawnPackets(final Player observer, LibsPackets packets, Entity disguisedEntity) {
        Disguise disguise = packets.getDisguise();

        Location loc = disguisedEntity.getLocation().clone()
            .add(0, DisguiseUtilities.getYModifier(disguise) + disguise.getWatcher().getYModifier(), 0);

        Float pitchLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getPitchLock() : null;
        Float yawLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getYawLock() : null;
        int entityId = observer == disguisedEntity ? DisguiseAPI.getSelfDisguiseId() : disguisedEntity.getEntityId();

        float yaw = (yawLock == null ? loc.getYaw() : yawLock);
        float pitch = (pitchLock == null ? loc.getPitch() : pitchLock);

        if (DisguiseConfig.isMovementPacketsEnabled()) {
            if (yawLock == null) {
                yaw = DisguiseUtilities.getYaw(DisguiseType.getType(disguisedEntity.getType()), yaw);
            }

            if (pitchLock == null) {
                pitch = DisguiseUtilities.getPitch(DisguiseType.getType(disguisedEntity.getType()), pitch);
            }

            yaw = DisguiseUtilities.getYaw(disguise.getType(), yaw);
            pitch = DisguiseUtilities.getPitch(disguise.getType(), pitch);
        }

        com.github.retrooper.packetevents.protocol.world.Location pLoc =
            new com.github.retrooper.packetevents.protocol.world.Location(loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);

        boolean inLineOfSight = true;

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {
            WrapperPlayServerSpawnExperienceOrb spawnOrb =
                new WrapperPlayServerSpawnExperienceOrb(entityId, loc.getX(), loc.getY() + 0.06, loc.getZ(), (short) 1);
            packets.addPacket(spawnOrb);
        } else {
            if (!NmsVersion.v1_19_R1.isSupported() && disguise.getType() == DisguiseType.PAINTING) {
                int id = ((MiscDisguise) disguise).getData();
                PaintingType paintingType = PaintingType.getById(id);
                Direction direction = DisguiseUtilities.getHangingDirection(yaw);
                WrapperPlayServerSpawnPainting spawnPainting =
                    new WrapperPlayServerSpawnPainting(entityId, disguise.getUUID(), paintingType,
                        new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), direction);

                packets.addPacket(spawnPainting);

                // Make the teleport packet to make it visible..
                WrapperPlayServerEntityTeleport teleportPainting =
                    new WrapperPlayServerEntityTeleport(entityId, pLoc, disguisedEntity.isOnGround());
                packets.addPacket(teleportPainting);
            } else if (disguise.getType().isPlayer()) {
                PlayerDisguise playerDisguise = (PlayerDisguise) disguise;
                boolean visibleOrNewCompat = playerDisguise.isNameVisible() || DisguiseConfig.isScoreboardNames();
                double dist = observer.getLocation().toVector().distanceSquared(disguisedEntity.getLocation().toVector());

                // If self disguise, or further than 50 blocks, or not in front of entity
                inLineOfSight = DisguiseUtilities.isFancyHiddenTabs() || observer == disguisedEntity ||
                    disguisedEntity.getPassengers().contains(observer) || dist > (50 * 50) ||
                    (observer.getLocation().add(observer.getLocation().getDirection().normalize()).toVector()
                        .distanceSquared(disguisedEntity.getLocation().toVector()) - dist) < 0.3;

                PlayerSkinHandler.PlayerSkin skin;

                if (DisguiseUtilities.isFancyHiddenTabs() || !playerDisguise.isDisplayedInTab() || !playerDisguise.isNameVisible()) {
                    // Send player info along with the disguise

                    packets.addPacket(DisguiseUtilities.createTablistAddPackets(playerDisguise));

                    skin = LibsDisguises.getInstance().getSkinHandler().addPlayerSkin(observer, playerDisguise);
                    skin.setDoTabList(!DisguiseUtilities.isFancyHiddenTabs());

                    if (LibsPremium.getPaidInformation() != null && !LibsPremium.getPaidInformation().getBuildNumber().matches("#?\\d+")) {
                        skin.getSleptPackets().computeIfAbsent(0, (a) -> new ArrayList<>()).add(new WrapperPlayServerHeldItemChange(0));
                    }
                } else {
                    skin = LibsDisguises.getInstance().getSkinHandler().addPlayerSkin(observer, playerDisguise);
                    skin.setDoTabList(false);
                }

                skin.setSleepPackets(!inLineOfSight);
                packets.setSkinHandling(true);
                PacketWrapper spawnPlayer;

                if (NmsVersion.v1_20_R2.isSupported()) {
                    spawnPlayer = constructLivingPacket(observer, packets, disguisedEntity, loc, pitch, yaw);
                } else {
                    // Spawn them in front of the observer
                    Location spawnAt =
                        inLineOfSight ? loc : observer.getLocation().add(observer.getLocation().getDirection().normalize().multiply(10));

                    // Spawn the player
                    spawnPlayer = new WrapperPlayServerSpawnPlayer(entityId, playerDisguise.getUUID(), pLoc, new ArrayList<>());

                    packets.addPacket(spawnPlayer);
                }

                List<WatcherValue> watcherValues;

                if (!inLineOfSight) {
                    watcherValues = Collections.singletonList(new WatcherValue(MetaIndex.ENTITY_META, (byte) 32, true));
                } else {
                    watcherValues = DisguiseUtilities.createSanitizedWatcherValues(observer, disguisedEntity, disguise.getWatcher());
                }

                if (NmsVersion.v1_15.isSupported()) {
                    WrapperPlayServerEntityMetadata metaPacket = ReflectionManager.getMetadataPacket(entityId, watcherValues);

                    packets.addPacket(metaPacket);
                } else if (spawnPlayer instanceof WrapperPlayServerSpawnLivingEntity) {
                    ((WrapperPlayServerSpawnLivingEntity) spawnPlayer).setEntityMetadata(
                        DisguiseUtilities.createDatawatcher(watcherValues));
                } else if (spawnPlayer instanceof WrapperPlayServerSpawnPlayer) {
                    ((WrapperPlayServerSpawnPlayer) spawnPlayer).setEntityMetadata(DisguiseUtilities.createDatawatcher(watcherValues));
                }
            } else if (disguise.isMobDisguise() || disguise.getType() == DisguiseType.ARMOR_STAND) {
                PacketWrapper spawnEntity = constructLivingPacket(observer, packets, disguisedEntity, loc, pitch, yaw);

                List<WatcherValue> watcherValues =
                    DisguiseUtilities.createSanitizedWatcherValues(observer, disguisedEntity, disguise.getWatcher());

                if (NmsVersion.v1_15.isSupported()) {
                    WrapperPlayServerEntityMetadata metaPacket = ReflectionManager.getMetadataPacket(entityId, watcherValues);

                    packets.addPacket(metaPacket);
                } else {
                    ((WrapperPlayServerSpawnLivingEntity) spawnEntity).setEntityMetadata(
                        DisguiseUtilities.createDatawatcher(watcherValues));
                }
            } else if (disguise.getType().isMisc()) {
                int data = ((MiscDisguise) disguise).getData();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();

                if (disguise.getType() == DisguiseType.FALLING_BLOCK) {
                    data = ((FallingBlockWatcher) disguise.getWatcher()).getBlockCombinedId();

                    if (((FallingBlockWatcher) disguise.getWatcher()).isGridLocked()) {
                        double yMod = disguise.getWatcher().getYModifier();
                        y -= yMod;

                        // Center the block
                        x = loc.getBlockX() + 0.5;
                        y = Math.floor(y) + yMod + (y % 1 >= 0.85 ? 1 : y % 1 >= 0.35 ? .5 : 0);
                        z = loc.getBlockZ() + 0.5;
                    }
                } else if (disguise.getType() == DisguiseType.FISHING_HOOK && data == -1) {
                    // If the MiscDisguise data isn't set. Then no entity id was provided, so default to the owners
                    // entity id
                    data = observer.getEntityId();
                } else if (disguise.getType().isArtDisplay()) {
                    data = DisguiseUtilities.getHangingDirection(yaw).ordinal();
                }

                WrapperPlayServerSpawnEntity spawnEntity;

                com.github.retrooper.packetevents.protocol.entity.type.EntityType entityType = getEntityType(disguise);

                Vector vec = disguisedEntity.getVelocity();
                spawnEntity =
                    new WrapperPlayServerSpawnEntity(entityId, Optional.of(disguise.getUUID()), entityType, new Vector3d(x, y, z), pitch,
                        yaw, yaw, data, Optional.of(new Vector3d(vec.getX(), vec.getY(), vec.getZ())));

                packets.addPacket(spawnEntity);

                // Since 1.19.3 we apparently no longer send all metadata but only the non-default
                if (NmsVersion.v1_19_R2.isSupported()) {
                    List<WatcherValue> watcherValues =
                        DisguiseUtilities.createSanitizedWatcherValues(observer, disguisedEntity, disguise.getWatcher());
                    WrapperPlayServerEntityMetadata metaPacket = ReflectionManager.getMetadataPacket(entityId, watcherValues);

                    packets.addPacket(metaPacket);
                }

                // If it's not the same type, then highly likely they have different velocity settings which we'd want to
                // cancel
                if (DisguiseType.getType(disguisedEntity) != disguise.getType()) {
                    spawnEntity.setVelocity(Optional.of(new Vector3d(0, 0, 0)));

                    if (disguise.getType() == DisguiseType.DROPPED_ITEM) {
                        WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(entityId, new Vector3d(0, 0, 0));

                        packets.addPacket(velocity);
                    }
                }

                if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                    if (data % 2 == 0) {
                        spawnEntity.setPosition(new Vector3d(loc.getX(), 0, loc.getZ() + data == 0 ? -1 : 1));
                    } else {
                        spawnEntity.setPosition(new Vector3d(loc.getX() + data == 3 ? -1 : 1, loc.getY(), loc.getZ()));
                    }
                }
            }
        }

        if (packets.getPackets().size() <= 1 || disguise.isPlayerDisguise()) {
            WrapperPlayServerEntityRotation rotateHead =
                new WrapperPlayServerEntityRotation(entityId, yaw, pitch, disguisedEntity.isOnGround());

            if (!DisguiseUtilities.isRunningPaper()) {
                packets.addPacket(rotateHead);
            } else {
                packets.addDelayedPacket(rotateHead, 10);
            }
        }

        if (disguise.getType() == DisguiseType.EVOKER_FANGS) {
            packets.addPacket(new WrapperPlayServerEntityStatus(entityId, 4));
        }

        if (disguise.getWatcher() instanceof LivingWatcher) {
            List<WrapperPlayServerUpdateAttributes.Property> attributes = new ArrayList<>();

            if (NmsVersion.v1_20_R4.isSupported()) {
                Double scale = ((LivingWatcher) disguise.getWatcher()).getScale();

                if (observer == disguisedEntity && DisguiseConfig.isTallSelfDisguisesScaling()) {
                    if (scale == null) {
                        scale = DisguiseUtilities.getEntityScaleWithoutLibsDisguises(observer);
                    }

                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE,
                        Math.min(disguise.getInternals().getSelfDisguiseTallScaleMax(), scale), new ArrayList<>()));
                } else if (scale != null) {
                    attributes.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, scale, new ArrayList<>()));
                }
            }

            if (DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
                double health;

                if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                    health = ((LivingWatcher) disguise.getWatcher()).getMaxHealth();
                } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity() && disguisedEntity instanceof Damageable) {
                    health = ((Damageable) disguisedEntity).getMaxHealth();
                } else {
                    health = DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth();
                }

                attributes.add(new WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_MAX_HEALTH, health, new ArrayList<>()));
            }

            if (!attributes.isEmpty()) {
                packets.addPacket(new WrapperPlayServerUpdateAttributes(entityId, attributes));
            }
        }

        if (!disguise.isPlayerDisguise() || inLineOfSight) {
            DisguiseUtilities.getNamePackets(disguise, observer, new String[0]).forEach(packets::addPacket);
        }

        // If armor must be sent because its currently not displayed and would've been sent normally

        // This sends the armor packets so that the player isn't naked.
        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot == EquipmentSlot.BODY && !NmsVersion.v1_20_R4.isSupported()) {
                    continue;
                }

                org.bukkit.inventory.EquipmentSlot bSlot = DisguiseUtilities.getSlot(slot);
                // Get what the disguise wants to show for its armor
                ItemStack itemToSend = disguise.getWatcher().getItemStack(bSlot);

                // If the disguise armor isn't visible
                if (itemToSend == null) {
                    itemToSend = DisguiseUtilities.getEquipment(bSlot, disguisedEntity);

                    // If natural armor isn't sent either
                    if (itemToSend == null || itemToSend.getType() == Material.AIR) {
                        continue;
                    }
                } else if (itemToSend.getType() == Material.AIR) {
                    // Its air which shouldn't be sent
                    continue;
                }

                WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(entityId,
                    Collections.singletonList(new Equipment(slot, DisguiseUtilities.fromBukkitItemStack(itemToSend))));

                packets.addDelayedPacket(packet);
            }
        }

        if (disguisedEntity != observer && observer.getSpectatorTarget() == disguisedEntity) {
            WrapperPlayServerCamera camera = new WrapperPlayServerCamera(entityId);
            packets.addPacket(camera);
        }
    }

    private PacketWrapper constructLivingPacket(Player observer, LibsPackets packets, Entity disguisedEntity, Location loc, float pitch,
                                                float yaw) {
        Disguise disguise = packets.getDisguise();
        Vector vec = disguisedEntity.getVelocity();

        if (disguise.getType() == DisguiseType.SQUID && disguisedEntity.getType() != EntityType.SQUID) {
            vec = new Vector();
        }

        com.github.retrooper.packetevents.protocol.entity.type.EntityType entityType = getEntityType(disguise);
        PacketWrapper spawnEntity;
        int entityId = observer == disguisedEntity ? DisguiseAPI.getSelfDisguiseId() : disguisedEntity.getEntityId();

        if (NmsVersion.v1_19_R1.isSupported()) {
            spawnEntity =
                new WrapperPlayServerSpawnEntity(entityId, disguise.getUUID(), entityType, SpigotConversionUtil.fromBukkitLocation(loc),
                    loc.getYaw(), 0, new Vector3d(vec.getX(), vec.getY(), vec.getZ()));
        } else {
            spawnEntity = new WrapperPlayServerSpawnLivingEntity(entityId, disguise.getUUID(), entityType,
                SpigotConversionUtil.fromBukkitLocation(loc), loc.getPitch(), new Vector3d(vec.getX(), vec.getY(), vec.getZ()),
                new ArrayList<>());
        }

        packets.addPacket(spawnEntity);

        return spawnEntity;
    }

    private com.github.retrooper.packetevents.protocol.entity.type.EntityType getEntityType(Disguise disguise) {
        if (disguise.getType().isCustom()) {
            return ((ModdedDisguise) disguise).getModdedEntity().getPacketEntityType();
        } else {
            return disguise.getType().getPacketEntityType();
        }
    }
}
