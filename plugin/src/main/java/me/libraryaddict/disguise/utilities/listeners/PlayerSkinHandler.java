package me.libraryaddict.disguise.utilities.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by libraryaddict on 15/09/2020.
 */
public class PlayerSkinHandler implements Listener {
    @RequiredArgsConstructor
    public static class PlayerSkin {
        private final long firstPacketSent = System.currentTimeMillis();
        @Getter
        private final WeakReference<PlayerDisguise> disguise;
        @Getter
        private final HashMap<Integer, ArrayList<PacketContainer>> sleptPackets = new HashMap<>();
        @Getter
        @Setter
        private boolean doTabList = true;
        @Getter
        @Setter
        private boolean sleepPackets;

        public boolean canRemove(boolean onMoved) {
            return firstPacketSent + (DisguiseConfig.getTablistRemoveDelay() * 50L) + (onMoved ? 0 : DisguiseConfig.getPlayerDisguisesSkinExpiresMove() * 50L) <
                System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PlayerSkin that = (PlayerSkin) o;
            return getDisguise().get() == that.getDisguise().get();
        }
    }

    @Getter
    private final Cache<Player, List<PlayerSkin>> cache =
        CacheBuilder.newBuilder().weakKeys().expireAfterWrite(DisguiseConfig.getPlayerDisguisesSkinExpiresMove() * 50L, TimeUnit.MILLISECONDS)
            .removalListener((event) -> {
                if (event.getCause() != RemovalCause.EXPIRED) {
                    return;
                }

                List<PlayerSkin> skins = (List<PlayerSkin>) event.getValue();

                for (PlayerSkin skin : skins) {
                    doPacketRemoval((Player) event.getKey(), skin);
                }

                skins.clear();
            }).build();

    public PlayerSkinHandler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getCache().asMap().forEach((key, value) -> doTeleport(key, value));
            }
        }.runTaskTimer(LibsDisguises.getInstance(), 1, 1);
    }

    public boolean isSleeping(Player player, PlayerDisguise disguise) {
        List<PlayerSkin> disguises = getCache().getIfPresent(player);

        if (disguises == null) {
            return false;
        }

        return disguises.stream().anyMatch(d -> d.getDisguise().get() == disguise);
    }

    public PlayerSkin addPlayerSkin(Player player, PlayerDisguise disguise) {
        tryProcess(player, false);

        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            skins = new ArrayList<>();
        }

        PlayerSkin toReturn = new PlayerSkin(new WeakReference<>(disguise));

        skins.add(toReturn);
        getCache().put(player, skins);

        return toReturn;
    }

    private void doTeleport(Player player, List<PlayerSkin> value) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Location loc = player.getLocation();
        loc.add(loc.getDirection().normalize().multiply(10));

        PacketContainer packet = new PacketContainer(Server.ENTITY_TELEPORT);
        packet.getModifier().write(1, loc.getX());
        packet.getModifier().write(2, loc.getY());
        packet.getModifier().write(3, loc.getZ());

        for (PlayerSkin skin : new ArrayList<>(value)) {
            if (!value.contains(skin) || !skin.isSleepPackets()) {
                continue;
            }

            PlayerDisguise disguise = skin.getDisguise().get();

            if (disguise == null || !disguise.isDisguiseInUse()) {
                continue;
            }

            packet = packet.shallowClone();

            int id = disguise.getEntity().getEntityId();

            if (id == player.getEntityId()) {
                id = DisguiseAPI.getSelfDisguiseId();
            }

            packet.getModifier().write(0, id);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
        }
    }

    public void handlePackets(Player player, PlayerDisguise disguise, LibsPackets packets) {
        boolean spawn = packets.getPackets().stream().anyMatch(p -> p.getType() == Server.NAMED_ENTITY_SPAWN);

        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            return;
        }

        PlayerSkin skin = skins.stream().filter(s -> s.getDisguise().get() == disguise).findAny().orElse(null);

        if (skin == null || !skin.isSleepPackets()) {
            return;
        }

        if (spawn) {
            packets.getDelayedPacketsMap().entrySet().removeIf(entry -> {
                entry.getValue().removeIf(packet -> packet.getType() == Server.ENTITY_EQUIPMENT && isRemove(skin, packet));

                return entry.getValue().isEmpty();
            });

            return;
        }

        packets.getPackets().removeIf(packet -> isRemove(skin, packet));

        packets.getDelayedPacketsMap().entrySet().removeIf(entry -> {
            entry.getValue().removeIf(packet -> isRemove(skin, packet));

            return entry.getValue().isEmpty();
        });
    }

    private boolean isRemove(PlayerSkin skin, PacketContainer packetContainer) {
        PacketType type = packetContainer.getType();

        if (type != Server.ENTITY_EQUIPMENT && type != Server.ENTITY_METADATA) {
            return false;
        }

        // Only do equip atm
        if (type == Server.ENTITY_EQUIPMENT) {
            skin.getSleptPackets().computeIfAbsent(3, (a) -> new ArrayList<>()).add(packetContainer);
        }

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onUndisguise(UndisguiseEvent event) {
        if (!event.getDisguise().isPlayerDisguise()) {
            return;
        }

        PlayerDisguise disguise = (PlayerDisguise) event.getDisguise();

        ArrayList<Player> players = new ArrayList<>(getCache().asMap().keySet());

        for (Player player : players) {
            List<PlayerSkin> skins = getCache().getIfPresent(player);

            if (skins == null) {
                continue;
            }

            PlayerSkin skin = skins.stream().filter(s -> s.getDisguise().get() == disguise).findAny().orElse(null);

            if (skin == null) {
                continue;
            }

            doPacketRemoval(player, skin);

            if (skins.size() == 1) {
                getCache().invalidate(player);
            } else {
                skins.remove(skin);
            }
        }
    }

    private void addMetadata(Player player, PlayerSkin skin) throws InvocationTargetException {
        PlayerDisguise disguise = skin.getDisguise().get();

        if (!disguise.isDisguiseInUse()) {
            return;
        }

        Entity entity = disguise.getEntity();

        List<WatcherValue> watcherValues =
            DisguiseUtilities.createSanitizedWatcherValues(player, WrappedDataWatcher.getEntityWatcher(entity), disguise.getWatcher());

        PacketContainer metaPacket = ReflectionManager.getMetadataPacket(entity.getEntityId(), watcherValues);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, metaPacket, false);
    }

    private void addTeleport(Player player, PlayerSkin skin) throws InvocationTargetException {
        PlayerDisguise disguise = skin.getDisguise().get();

        PacketContainer teleport = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        StructureModifier<Object> mods = teleport.getModifier();
        Location loc = disguise.getEntity().getLocation().add(0, disguise.getWatcher().getYModifier() + DisguiseUtilities.getYModifier(disguise), 0);

        Float pitchLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getPitchLock() : null;
        Float yawLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getYawLock() : null;

        byte yaw = (byte) (int) ((yawLock == null ? loc.getYaw() : yawLock) * 256.0F / 360.0F);
        byte pitch = (byte) (int) ((pitchLock == null ? loc.getPitch() : pitchLock) * 256.0F / 360.0F);

        if (DisguiseConfig.isMovementPacketsEnabled()) {
            if (yawLock == null) {
                yaw = DisguiseUtilities.getYaw(DisguiseType.getType(disguise.getEntity().getType()), yaw);
            }

            if (pitchLock == null) {
                pitch = DisguiseUtilities.getPitch(DisguiseType.getType(disguise.getEntity().getType()), pitch);
            }

            yaw = DisguiseUtilities.getYaw(disguise.getType(), yaw);
            pitch = DisguiseUtilities.getPitch(disguise.getType(), pitch);
        }

        int id = disguise.getEntity().getEntityId();

        if (id == player.getEntityId()) {
            id = DisguiseAPI.getSelfDisguiseId();
        }

        mods.write(0, id);
        mods.write(1, loc.getX());
        mods.write(2, loc.getY());
        mods.write(3, loc.getZ());
        mods.write(4, yaw);
        mods.write(5, pitch);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, teleport, false);
    }

    private void doPacketRemoval(Player player, PlayerSkin skin) {
        PlayerDisguise disguise = skin.getDisguise().get();

        if (disguise == null) {
            return;
        }

        try {
            if (disguise.isDisguiseInUse()) {
                for (Map.Entry<Integer, ArrayList<PacketContainer>> entry : skin.getSleptPackets().entrySet()) {
                    if (entry.getKey() == 0) {
                        for (PacketContainer packet : entry.getValue()) {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                        }
                    } else {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!disguise.isDisguiseInUse()) {
                                    return;
                                }

                                for (PacketContainer packet : entry.getValue()) {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                                }
                            }
                        }.runTaskLater(LibsDisguises.getInstance(), entry.getKey());
                    }
                }

                if (skin.isSleepPackets()) {
                    addTeleport(player, skin);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                addMetadata(player, skin);
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTask(LibsDisguises.getInstance());
                }

                if (DisguiseConfig.isArmorstandsName() && disguise.isNameVisible() && disguise.getMultiNameLength() > 0) {
                    ArrayList<PacketContainer> packets = DisguiseUtilities.getNamePackets(disguise, new String[0]);

                    for (PacketContainer p : packets) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, p);
                    }
                }
            }

            if (skin.isDoTabList()) {
                PacketContainer packetContainer = ReflectionManager.createTablistPacket(disguise, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void tryProcess(Player player, boolean onMove) {
        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            return;
        }

        ArrayList<PlayerSkin> removed = new ArrayList<>();

        skins.removeIf(skin -> {
            if (!skin.canRemove(onMove)) {
                return false;
            }

            removed.add(skin);
            return true;
        });

        removed.forEach(skin -> doPacketRemoval(player, skin));

        if (!skins.isEmpty()) {
            return;
        }

        getCache().invalidate(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        tryProcess(event.getPlayer(), true);
    }
}
