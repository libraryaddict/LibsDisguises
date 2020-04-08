package me.libraryaddict.disguise.utilities.packets.packethandlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.DisguiseValues;
import me.libraryaddict.disguise.utilities.LibsPremium;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        return new PacketType[]{PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.SPAWN_ENTITY_LIVING,
                PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, PacketType.Play.Server.SPAWN_ENTITY,
                PacketType.Play.Server.SPAWN_ENTITY_PAINTING};
    }

    @Override
    public void handle(Disguise disguise, PacketContainer sentPacket, LibsPackets packets, Player observer,
            Entity entity) {

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

        if (disguise.getEntity() == null) {
            disguise.setEntity(disguisedEntity);
        }

        if (DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            if (disguise.getWatcher() instanceof LivingWatcher) {

                ArrayList<WrappedAttribute> attributes = new ArrayList<>();

                WrappedAttribute.Builder builder = WrappedAttribute.newBuilder().attributeKey("generic.maxHealth");

                if (((LivingWatcher) disguise.getWatcher()).isMaxHealthSet()) {
                    builder.baseValue(((LivingWatcher) disguise.getWatcher()).getMaxHealth());
                } else if (DisguiseConfig.isMaxHealthDeterminedByDisguisedEntity() &&
                        disguisedEntity instanceof Damageable) {
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

        Location loc = disguisedEntity.getLocation().clone()
                .add(0, DisguiseUtilities.getYModifier(disguisedEntity, disguise), 0);

        byte yaw = (byte) (int) (loc.getYaw() * 256.0F / 360.0F);
        byte pitch = (byte) (int) (loc.getPitch() * 256.0F / 360.0F);

        if (DisguiseConfig.isMovementPacketsEnabled()) {
            yaw = DisguiseUtilities.getYaw(disguise.getType(), disguisedEntity.getType(), yaw);
            pitch = DisguiseUtilities.getPitch(disguise.getType(), disguisedEntity.getType(), pitch);
        }

        if (disguise.getType() == DisguiseType.EXPERIENCE_ORB) {
            PacketContainer spawnOrb = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB);
            packets.addPacket(spawnOrb);

            StructureModifier<Object> mods = spawnOrb.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, loc.getX());
            mods.write(2, loc.getY() + 0.06);
            mods.write(3, loc.getZ());
            mods.write(4, 1);
        } else if (disguise.getType() == DisguiseType.PAINTING) {
            PacketContainer spawnPainting = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_PAINTING);
            packets.addPacket(spawnPainting);

            StructureModifier<Object> mods = spawnPainting.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguisedEntity.getUniqueId());
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
            boolean visibleOrNewCompat = playerDisguise.isNameVisible() || DisguiseConfig.isScoreboardDisguiseNames();

            WrappedGameProfile spawnProfile = visibleOrNewCompat ? playerDisguise.getGameProfile() : ReflectionManager
                    .getGameProfileWithThisSkin(UUID.randomUUID(), visibleOrNewCompat ? playerDisguise.getName() : "",
                            playerDisguise.getGameProfile());

            int entityId = disguisedEntity.getEntityId();

            if (!playerDisguise.isDisplayedInTab() || !playerDisguise.isNameVisible()) {
                // Send player info along with the disguise
                PacketContainer sendTab = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

                // Add player to the list, necessary to spawn them
                sendTab.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(0));

                List playerList = Collections
                        .singletonList(ReflectionManager.getPlayerInfoData(sendTab.getHandle(), spawnProfile));
                sendTab.getModifier().write(1, playerList);

                packets.addPacket(sendTab);

                // Remove player from the list
                PacketContainer deleteTab = sendTab.shallowClone();
                deleteTab.getModifier().write(0, ReflectionManager.getEnumPlayerInfoAction(4));

                if (LibsPremium.getPaidInformation() == null ||
                        LibsPremium.getPaidInformation().getBuildNumber().matches("#[0-9]+")) {
                    packets.addDelayedPacket(deleteTab, 2);
                }
            }

            // Spawn the player
            PacketContainer spawnPlayer = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

            spawnPlayer.getIntegers().write(0, entityId); // Id
            spawnPlayer.getModifier().write(1, spawnProfile.getUUID());

            Location spawnAt = disguisedEntity.getLocation();

            boolean selfDisguise = observer == disguisedEntity;

            // Spawn him in front of the observer
            StructureModifier<Double> doubles = spawnPlayer.getDoubles();
            doubles.write(0, spawnAt.getX());
            doubles.write(1, spawnAt.getY());
            doubles.write(2, spawnAt.getZ());

            StructureModifier<Byte> bytes = spawnPlayer.getBytes();
            bytes.write(0, yaw);
            bytes.write(1, pitch);

            packets.addPacket(spawnPlayer);

            WrappedDataWatcher newWatcher = DisguiseUtilities
                    .createSanitizedDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity),
                            disguise.getWatcher());

            if (NmsVersion.v1_15.isSupported()) {
                PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, entityId, newWatcher, true)
                        .createPacket(entityId, newWatcher, true);

                packets.addPacket(metaPacket);
            } else {
                spawnPlayer.getDataWatcherModifier().write(0, newWatcher);
            }
        } else if (disguise.getType().isMob() || disguise.getType() == DisguiseType.ARMOR_STAND) {
            Vector vec = disguisedEntity.getVelocity();

            PacketContainer spawnEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packets.addPacket(spawnEntity);

            StructureModifier<Object> mods = spawnEntity.getModifier();

            mods.write(0, disguisedEntity.getEntityId());
            mods.write(1, disguisedEntity.getUniqueId());
            mods.write(2, disguise.getType().getTypeId());

            // region Vector calculations
            double d1 = 3.9D;
            double d2 = vec.getX();
            double d3 = vec.getY();
            double d4 = vec.getZ();
            if (d2 < -d1)
                d2 = -d1;
            if (d3 < -d1)
                d3 = -d1;
            if (d4 < -d1)
                d4 = -d1;
            if (d2 > d1)
                d2 = d1;
            if (d3 > d1)
                d3 = d1;
            if (d4 > d1)
                d4 = d1;
            // endregion

            mods.write(3, loc.getX());
            mods.write(4, loc.getY());
            mods.write(5, loc.getZ());
            mods.write(6, (int) (d2 * 8000.0D));
            mods.write(7, (int) (d3 * 8000.0D));
            mods.write(8, (int) (d4 * 8000.0D));
            mods.write(9, yaw);
            mods.write(10, pitch);
            mods.write(11, yaw);

            WrappedDataWatcher newWatcher = DisguiseUtilities
                    .createSanitizedDataWatcher(WrappedDataWatcher.getEntityWatcher(disguisedEntity),
                            disguise.getWatcher());

            if (NmsVersion.v1_15.isSupported()) {
                PacketContainer metaPacket = ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(PacketType.Play.Server.ENTITY_METADATA, disguisedEntity.getEntityId(),
                                newWatcher, true).createPacket(disguisedEntity.getEntityId(), newWatcher, true);

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
                ItemStack block = ((FallingBlockWatcher) disguise.getWatcher()).getBlock();

                data = ReflectionManager.getCombinedIdByItemStack(block);

                if (((FallingBlockWatcher) disguise.getWatcher()).isGridLocked()) {
                    // Center the block
                    x = loc.getBlockX() + 0.5;
                    y = loc.getBlockY();
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
                Object entityType = ReflectionManager.getEntityType(disguise.getType().getEntityType());

                Object[] params = new Object[]{disguisedEntity.getEntityId(), disguisedEntity.getUniqueId(), x, y, z,
                        loc.getPitch(), loc.getYaw(), entityType, data,
                        ReflectionManager.getVec3D(disguisedEntity.getVelocity())};

                spawnEntity = ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, params).createPacket(params);
            } else {
                int objectId = disguise.getType().getObjectId();
                Object nmsEntity = ReflectionManager.getNmsEntity(disguisedEntity);

                spawnEntity = ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, nmsEntity, objectId, data)
                        .createPacket(nmsEntity, objectId, data);
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

            spawnEntity.getModifier().write(8, pitch);
            spawnEntity.getModifier().write(9, yaw);

            if (disguise.getType() == DisguiseType.ITEM_FRAME) {
                if (data % 2 == 0) {
                    spawnEntity.getModifier().write(4, loc.getZ() + (data == 0 ? -1 : 1));
                } else {
                    spawnEntity.getModifier().write(2, loc.getX() + (data == 3 ? -1 : 1));
                }
            }
        }

        if (packets.getPackets().size() <= 1 || disguise.isPlayerDisguise()) {
            PacketContainer rotateHead = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            packets.addPacket(rotateHead);

            StructureModifier<Object> mods = rotateHead.getModifier();

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
                mods.write(1, ReflectionManager.createEnumItemSlot(slot));
                mods.write(2, ReflectionManager.getNmsItem(itemToSend));

                packets.addDelayedPacket(packet);
            }
        }
    }
}
