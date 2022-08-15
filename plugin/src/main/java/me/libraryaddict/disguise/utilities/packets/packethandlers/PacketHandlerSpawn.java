package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.datafixers.util.Pair;
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
import me.libraryaddict.disguise.utilities.packets.PacketsHandler;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class PacketHandlerSpawn implements IPacketHandler {
    private PacketsHandler packetsHandler;

    public PacketHandlerSpawn(PacketsHandler packetsHandler) {
        this.packetsHandler = packetsHandler;
    }

    @Override
    public PacketType[] getHandledPackets() {
        PacketType[] packets = new PacketType[]{PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB,
            PacketType.Play.Server.SPAWN_ENTITY};

        if (!NmsVersion.v1_19.isSupported()) {
            packets = Arrays.copyOf(packets, packets.length + 2);
            packets[packets.length - 2] = PacketType.Play.Server.SPAWN_ENTITY_LIVING;
            packets[packets.length - 1] = PacketType.Play.Server.SPAWN_ENTITY_PAINTING;
        }

        return packets;
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer, Entity entity) {
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
        boolean sendArmor = true;

        Location loc = disguisedEntity.getLocation().clone().add(0, DisguiseUtilities.getYModifier(disguise) + disguise.getWatcher().getYModifier(), 0);

        Float pitchLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getPitchLock() : null;
        Float yawLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getYawLock() : null;

        byte yaw = (byte) (int) ((yawLock == null ? loc.getYaw() : yawLock) * 256.0F / 360.0F);
        byte pitch = (byte) (int) ((pitchLock == null ? loc.getPitch() : pitchLock) * 256.0F / 360.0F);

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

        boolean normalPlayerDisguise = true;

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {
            PacketContainer spawnOrb = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB);
            packets.addPacket(spawnOrb);

            StructureModifier<Object> mods = spawnOrb.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY() + 0.06);
            mods.write(3, loc.getZ());
            mods.write(4, 1);
        } else if (!NmsVersion.v1_19.isSupported() && disguise.getType() == DisguiseType.PAINTING) {
            PacketContainer spawnPainting = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_PAINTING);
            packets.addPacket(spawnPainting);

            StructureModifier<Object> mods = spawnPainting.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguise.getUUID());
            mods.write(2, ReflectionManager.getBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            mods.write(3, ReflectionManager.getEnumDirection(((int) loc.getYaw()) % 4));

            int id = ((MiscDisguise) disguise).getData();

            mods.write(4, NmsVersion.v1_13.isSupported() ? id : ReflectionManager.getEnumArt(Art.values()[id]));

            // Make the teleport packet to make it visible..
            PacketContainer teleportPainting = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
            packets.addPacket(teleportPainting);

            mods = teleportPainting.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY());
            mods.write(3, loc.getZ());
            mods.write(4, yaw);
            mods.write(5, pitch);
        } else if (disguise.getType().isPlayer()) {
            PlayerDisguise playerDisguise = (PlayerDisguise) disguise;
            boolean visibleOrNewCompat = playerDisguise.isNameVisible() || DisguiseConfig.isScoreboardNames();

            int entityId = disguisedEntity.getEntityId();
            PlayerSkinHandler.PlayerSkin skin;

            if (!playerDisguise.isDisplayedInTab() || !playerDisguise.isNameVisible()) {
                // Send player info along with the disguise
                PacketContainer sendTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

                // Add player to the list, necessary to spawn them
                sendTab.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(0));

                List playerList = Collections.singletonList(ReflectionManager.getPlayerInfoData(sendTab.getHandle(),
                    ReflectionManager.getGameProfileWithThisSkin(playerDisguise.getUUID(), playerDisguise.getProfileName(), playerDisguise.getGameProfile())));
                sendTab.getModifier().write(1, playerList);

                packets.addPacket(sendTab);

                // Remove player from the list
                PacketContainer deleteTab = sendTab.shallowClone();
                deleteTab.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(4));

                skin = LibsDisguises.getInstance().getSkinHandler().addPlayerSkin(observer, playerDisguise);

                if (LibsPremium.getPaidInformation() != null && !LibsPremium.getPaidInformation().getBuildNumber().matches("#[0-9]+")) {
                    skin.getSleptPackets().computeIfAbsent(0, (a) -> new ArrayList<>()).add(new PacketContainer(PacketType.Play.Server.HELD_ITEM_SLOT));
                }
            } else {
                skin = LibsDisguises.getInstance().getSkinHandler().addPlayerSkin(observer, playerDisguise);
                skin.setDoTabList(false);
            }

            // Spawn the player
            PacketContainer spawnPlayer = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

            spawnPlayer.getIntegers().write(0, entityId); // Id
            spawnPlayer.getModifier().write(1, playerDisguise.getUUID());

            double dist = observer.getLocation().toVector().distanceSquared(disguisedEntity.getLocation().toVector());

            // If self disguise, or further than 50 blocks, or not in front of entity
            normalPlayerDisguise = observer == disguisedEntity || disguisedEntity.getPassengers().contains(observer) || dist > (50 * 50) ||
                (observer.getLocation().add(observer.getLocation().getDirection().normalize()).toVector()
                    .distanceSquared(disguisedEntity.getLocation().toVector()) - dist) < 0.3;
            sendArmor = normalPlayerDisguise;

            skin.setSleepPackets(!normalPlayerDisguise);

            Location spawnAt = normalPlayerDisguise ? loc : observer.getLocation().add(observer.getLocation().getDirection().normalize().multiply(10));

            // Spawn him in front of the observer
            StructureModifier<Double> doubles = spawnPlayer.getDoubles();
            doubles.write(0, spawnAt.getX());
            doubles.write(1, spawnAt.getY());
            doubles.write(2, spawnAt.getZ());

            StructureModifier<Byte> bytes = spawnPlayer.getBytes();
            bytes.write(0, yaw);
            bytes.write(1, pitch);

            packets.addPacket(spawnPlayer);

            WrappedDataWatcher toSend;

            if (!normalPlayerDisguise) {
                toSend = new WrappedDataWatcher();
                WrappedDataWatcher.WrappedDataWatcherObject obj = ReflectionManager.createDataWatcherObject(MetaIndex.ENTITY_META, (byte) 32);

                // Set invis
                toSend.setObject(obj, (byte) 32);
            } else {
                toSend = DisguiseUtilities.createSanitizedDataWatcher(observer, WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher());
            }

            if (NmsVersion.v1_15.isSupported()) {
                PacketContainer metaPacket =
                    ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, entityId, toSend, true)
                        .createPacket(entityId, toSend, true);

                packets.addPacket(metaPacket);
            } else {
                spawnPlayer.getDataWatcherModifier().write(0, toSend);
            }
        } else if (disguise.isMobDisguise() || disguise.getType() == DisguiseType.ARMOR_STAND) {
            Vector vec = disguisedEntity.getVelocity();

            if (disguise.getType() == DisguiseType.SQUID && disguisedEntity.getType() != EntityType.SQUID) {
                vec = new Vector();
            }

            PacketContainer spawnEntity =
                new PacketContainer(NmsVersion.v1_19.isSupported() ? PacketType.Play.Server.SPAWN_ENTITY : PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packets.addPacket(spawnEntity);

            StructureModifier<Object> mods = spawnEntity.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguise.getUUID());

            if (NmsVersion.v1_19.isSupported()) {
                if (!disguise.getType().isCustom()) {
                    mods.write(2, disguise.getType().getNmsEntityType());
                } else {
                    mods.write(2, ((ModdedDisguise) disguise).getModdedEntity().getEntityType());
                }
            } else {
                if (!disguise.getType().isCustom()) {
                    mods.write(2, disguise.getType().getTypeId());
                } else {
                    mods.write(2, ((ModdedDisguise) disguise).getModdedEntity().getTypeId());
                }
            }

            // region Vector calculations
            double d1 = 3.9D;
            double d2 = vec.getX();
            double d3 = vec.getY();
            double d4 = vec.getZ();
            if (d2 < -d1) {
                d2 = -d1;
            }
            if (d3 < -d1) {
                d3 = -d1;
            }
            if (d4 < -d1) {
                d4 = -d1;
            }
            if (d2 > d1) {
                d2 = d1;
            }
            if (d3 > d1) {
                d3 = d1;
            }
            if (d4 > d1) {
                d4 = d1;
            }
            // endregion

            mods.write(3, loc.getX());
            mods.write(4, loc.getY());
            mods.write(5, loc.getZ());
            mods.write(6, (int) (d2 * 8000.0D));
            mods.write(7, (int) (d3 * 8000.0D));
            mods.write(8, (int) (d4 * 8000.0D));

            // Prior to 1.19, it's Y, X, Y
            if (!NmsVersion.v1_19.isSupported()) {
                mods.write(9, yaw);
                mods.write(10, pitch);
            } else {
                mods.write(9, pitch);
                mods.write(10, yaw);
            }

            mods.write(11, yaw);

            WrappedDataWatcher newWatcher =
                DisguiseUtilities.createSanitizedDataWatcher(observer, WrappedDataWatcher.getEntityWatcher(disguisedEntity), disguise.getWatcher());

            if (NmsVersion.v1_15.isSupported()) {
                PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                    .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, disguisedEntity.getEntityId(), newWatcher, true)
                    .createPacket(disguisedEntity.getEntityId(), newWatcher, true);

                packets.addPacket(metaPacket);
            } else {
                spawnEntity.getDataWatcherModifier().write(0, newWatcher);
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
            } else if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                data = ((((int) loc.getYaw() % 360) + 720 + 45) / 90) % 4;
            }

            PacketContainer spawnEntity;

            if (NmsVersion.v1_14.isSupported()) {
                Object entityType;

                if (disguise.isCustomDisguise()) {
                    entityType = ((ModdedDisguise) disguise).getModdedEntity().getEntityType();
                } else {
                    entityType = ReflectionManager.getEntityType(disguise.getType().getEntityType());
                }

                Object[] params = new Object[]{disguisedEntity.getEntityId(), disguise.getUUID(), x, y, z, loc.getPitch(), loc.getYaw(), entityType, data,
                    ReflectionManager.getVec3D(disguisedEntity.getVelocity())};

                if (NmsVersion.v1_19.isSupported()) {
                    params = Arrays.copyOf(params, params.length + 1);

                    params[params.length - 1] = (double) loc.getYaw();
                }

                spawnEntity = ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, params).createPacket(params);
            } else {
                int objectId = disguise.getType().getObjectId();

                if (disguise.isCustomDisguise()) {
                    objectId = ((ModdedDisguise) disguise).getModdedEntity().getTypeId();
                }

                Object nmsEntity = ReflectionManager.getNmsEntity(disguisedEntity);

                spawnEntity = ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, nmsEntity, objectId, data)
                    .createPacket(nmsEntity, objectId, data);

                StructureModifier<Double> doubles = spawnEntity.getDoubles();

                doubles.write(0, x);
                doubles.write(1, y);
                doubles.write(2, z);
            }

            spawnEntity.getModifier().write(8, pitch);
            spawnEntity.getModifier().write(9, yaw);

            if (NmsVersion.v1_19.isSupported()) {
                spawnEntity.getModifier().write(10, yaw);
            }

            packets.addPacket(spawnEntity);

            // If it's not the same type, then highly likely they have different velocity settings which we'd want to
            // cancel
            if (DisguiseType.getType(disguisedEntity) != disguise.getType()) {
                StructureModifier<Integer> ints = spawnEntity.getIntegers();

                ints.write(1, 0);
                ints.write(2, 0);
                ints.write(3, 0);

                if (disguise.getType() == DisguiseType.DROPPED_ITEM) {
                    PacketContainer velocity = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);
                    velocity.getIntegers().write(0, disguisedEntity.getEntityId());

                    packets.addPacket(velocity);
                }
            }

            if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                if (data % 2 == 0) {
                    spawnEntity.getDoubles().write(2, loc.getZ() + (data == 0 ? -1 : 1));
                } else {
                    spawnEntity.getDoubles().write(0, loc.getX() + (data == 3 ? -1 : 1));
                }
            }
        }

        if (packets.getPackets().size() <= 1 || disguise.isPlayerDisguise()) {
            PacketContainer rotateHead = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

            StructureModifier<Object> mods = rotateHead.getModifier();

            if (!DisguiseUtilities.isRunningPaper()) {
                packets.addPacket(rotateHead);
            } else {
                packets.addDelayedPacket(rotateHead, 10);
            }

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, yaw);
        }

        if (disguise.getType() == DisguiseType.EVOKER_FANGS) {
            PacketContainer newPacket = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);

            StructureModifier<Object> mods = newPacket.getModifier();
            mods.write(0, disguise.getEntity().getEntityId());
            mods.write(1, (byte) 4);

            packets.addPacket(newPacket);
        }

        if (DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            if (disguise.getWatcher() instanceof LivingWatcher) {
                ArrayList<WrappedAttribute> attributes = new ArrayList<>();

                WrappedAttribute.Builder builder =
                    WrappedAttribute.newBuilder().attributeKey(NmsVersion.v1_16.isSupported() ? "generic.max_health" : "generic.maxHealth");

                if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                    builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity() && disguisedEntity instanceof Damageable) {
                    builder.baseValue(((Damageable) disguisedEntity).getMaxHealth());
                } else {
                    builder.baseValue(DisguiseValues.getDisguiseValues(disguise.getType()).getMaxHealth());
                }

                PacketContainer packet = new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES);

                builder.packet(packet);

                attributes.add(builder.build());

                packet.getIntegers().write(0, disguisedEntity.getEntityId());
                packet.getAttributeCollectionModifier().write(0, attributes);

                packets.addPacket(packet);
            }
        }

        if (!disguise.isPlayerDisguise() || normalPlayerDisguise) {
            DisguiseUtilities.getNamePackets(disguise, new String[0]).forEach(packets::addPacket);
        }

        // If armor must be sent because its currently not displayed and would've been sent normally

        // This sends the armor packets so that the player isn't naked.
        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                // Get what the disguise wants to show for its armor
                ItemStack itemToSend = disguise.getWatcher().getItemStack(slot);

                // If the disguise armor isn't visible
                if (itemToSend == null) {
                    itemToSend = ReflectionManager.getEquipment(slot, disguisedEntity);

                    // If natural armor isn't sent either
                    if (itemToSend == null || itemToSend.getType() == Material.AIR) {
                        continue;
                    }
                } else if (itemToSend.getType() == Material.AIR) {
                    // Its air which shouldn't be sent
                    continue;
                }

                PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

                StructureModifier<Object> mods = packet.getModifier();

                mods.write(0, disguisedEntity.getEntityId());

                if (NmsVersion.v1_16.isSupported()) {
                    List<Pair<Object, Object>> list = new ArrayList<>();
                    list.add(Pair.of(ReflectionManager.createEnumItemSlot(slot), ReflectionManager.getNmsItem(itemToSend)));

                    mods.write(1, list);
                } else {
                    mods.write(1, ReflectionManager.createEnumItemSlot(slot));
                    mods.write(2, ReflectionManager.getNmsItem(itemToSend));
                }

                packets.addDelayedPacket(packet);
            }
        }
    }
}
