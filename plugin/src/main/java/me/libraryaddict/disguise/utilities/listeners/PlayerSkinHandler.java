package me.libraryaddict.disguise.utilities.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
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

public class PlayerSkinHandler implements Listener {
    @RequiredArgsConstructor
    @Getter
    public static class PlayerSkin {
        private final long firstPacketSent = System.currentTimeMillis();
        private final WeakReference<PlayerDisguise> disguise;
        private final HashMap<Integer, List<PacketWrapper>> sleptPackets = new HashMap<>();
        @Setter
        private boolean doTabList = true;
        @Setter
        private boolean sleepPackets;

        public boolean canRemove(boolean onMoved) {
            return firstPacketSent + (DisguiseConfig.getTablistRemoveDelay() * 50L) +
                (onMoved ? 0 : DisguiseConfig.getPlayerDisguisesSkinExpiresMove() * 50L) < System.currentTimeMillis();
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
    private final Cache<Player, List<PlayerSkin>> cache = CacheBuilder.newBuilder().weakKeys()
        .expireAfterWrite(DisguiseConfig.getPlayerDisguisesSkinExpiresMove() * 50L, TimeUnit.MILLISECONDS).removalListener((event) -> {
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

    public synchronized boolean isSleeping(Player player, PlayerDisguise disguise) {
        List<PlayerSkin> disguises = getCache().getIfPresent(player);

        if (disguises == null) {
            return false;
        }

        return disguises.stream().anyMatch(d -> d.getDisguise().get() == disguise);
    }

    public synchronized PlayerSkin addPlayerSkin(Player player, PlayerDisguise disguise) {
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

    private synchronized void doTeleport(Player player, List<PlayerSkin> value) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Location loc = player.getLocation();
        loc.add(loc.getDirection().normalize().multiply(10));

        for (PlayerSkin skin : new ArrayList<>(value)) {
            if (!value.contains(skin) || !skin.isSleepPackets()) {
                continue;
            }

            PlayerDisguise disguise = skin.getDisguise().get();

            if (disguise == null || !disguise.isDisguiseInUse()) {
                continue;
            }

            int id = disguise.getEntity().getEntityId();

            if (id == player.getEntityId()) {
                id = DisguiseAPI.getSelfDisguiseId();
            }

            WrapperPlayServerEntityTeleport packet =
                new WrapperPlayServerEntityTeleport(id, SpigotConversionUtil.fromBukkitLocation(loc), true);
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }

    public synchronized void handlePackets(Player player, PlayerDisguise disguise, LibsPackets<?> packets) {
        boolean spawn = packets.isSkinHandling();

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
                entry.getValue().removeIf(packet -> packet.getPacketTypeData().getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT &&
                    isProcessedLater(skin, packet));

                return entry.getValue().isEmpty();
            });

            return;
        }

        packets.getPackets().removeIf(packet -> isProcessedLater(skin, packet));

        packets.getDelayedPacketsMap().entrySet().removeIf(entry -> {
            entry.getValue().removeIf(packet -> isProcessedLater(skin, packet));

            return entry.getValue().isEmpty();
        });
    }

    private boolean isProcessedLater(PlayerSkin skin, PacketWrapper packetContainer) {
        PacketTypeCommon type = packetContainer.getPacketTypeData().getPacketType();

        // Only do equip atm
        if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            // We clone the packet, attempts to fix a crash reported via discord for "IllegalReferenceCountException"
            // Working theory is that the equipment packet sent via packetlistener has some leftover data or whatnot
            // And it doesn't handle being sent again very well
            WrapperPlayServerEntityEquipment equipment = (WrapperPlayServerEntityEquipment) packetContainer;

            List<Equipment> list = new ArrayList<>();

            for (Equipment equip : equipment.getEquipment()) {
                list.add(new Equipment(equip.getSlot(), equip.getItem().copy()));
            }

            skin.getSleptPackets().computeIfAbsent(3, (a) -> new ArrayList<>())
                .add(new WrapperPlayServerEntityEquipment(equipment.getEntityId(), list));

            return true;
        }

        return type == PacketType.Play.Server.ENTITY_METADATA;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private synchronized void onUndisguise(UndisguiseEvent event) {
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

    private synchronized void addMetadata(Player player, PlayerSkin skin) throws InvocationTargetException {
        PlayerDisguise disguise = skin.getDisguise().get();

        if (!disguise.isDisguiseInUse()) {
            return;
        }

        Entity entity = disguise.getEntity();

        List<WatcherValue> watcherValues = DisguiseUtilities.createSanitizedWatcherValues(player, entity, disguise.getWatcher());

        WrapperPlayServerEntityMetadata metaPacket = ReflectionManager.getMetadataPacket(entity.getEntityId(), watcherValues);

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, metaPacket);
    }

    private synchronized void addTeleport(Player player, PlayerSkin skin) throws InvocationTargetException {
        PlayerDisguise disguise = skin.getDisguise().get();

        Location loc =
            disguise.getEntity().getLocation().add(0, disguise.getWatcher().getYModifier() + DisguiseUtilities.getYModifier(disguise), 0);

        Float pitchLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getPitchLock() : null;
        Float yawLock = DisguiseConfig.isMovementPacketsEnabled() ? disguise.getWatcher().getYawLock() : null;

        float yaw = (yawLock == null ? loc.getYaw() : yawLock);
        float pitch = (pitchLock == null ? loc.getPitch() : pitchLock);

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

        WrapperPlayServerEntityTeleport teleport =
            new WrapperPlayServerEntityTeleport(id, new Vector3d(loc.getX(), loc.getY(), loc.getZ()), yaw, pitch,
                disguise.getEntity().isOnGround());
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, teleport);
    }

    private synchronized void doPacketRemoval(Player player, PlayerSkin skin) {
        PlayerDisguise disguise = skin.getDisguise().get();

        if (disguise == null) {
            return;
        }

        try {
            if (disguise.isDisguiseInUse()) {
                for (Map.Entry<Integer, List<PacketWrapper>> entry : skin.getSleptPackets().entrySet()) {
                    if (entry.getKey() == 0) {
                        for (PacketWrapper packet : entry.getValue()) {
                            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
                        }
                    } else {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!disguise.isDisguiseInUse()) {
                                    return;
                                }

                                for (PacketWrapper packet : entry.getValue()) {
                                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
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
                    List<PacketWrapper<?>> packets = DisguiseUtilities.getNamePackets(disguise, player, new String[0]);

                    for (PacketWrapper p : packets) {
                        PacketEvents.getAPI().getPlayerManager().sendPacket(player, p);
                    }
                }
            }

            if (skin.isDoTabList()) {
                PacketWrapper packetContainer =
                    DisguiseUtilities.createTablistPacket(disguise, WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER);

                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packetContainer);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private synchronized void tryProcess(Player player, boolean onMove) {
        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            return;
        }

        List<PlayerSkin> removed = new ArrayList<>();

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
