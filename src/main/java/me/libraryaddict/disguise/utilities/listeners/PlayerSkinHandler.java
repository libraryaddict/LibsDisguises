package me.libraryaddict.disguise.utilities.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.LibsPackets;
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
            return firstPacketSent + (DisguiseConfig.getTablistRemoveDelay() * 50) +
                    (onMoved ? 0 : TimeUnit.SECONDS.toMillis(5)) < System.currentTimeMillis();
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
            CacheBuilder.newBuilder().weakKeys().expireAfterWrite(30, TimeUnit.SECONDS).removalListener((event) -> {
                if (event.getCause() != RemovalCause.EXPIRED) {
                    return;
                }

                List<PlayerSkin> skins = (List<PlayerSkin>) event.getValue();

                for (PlayerSkin skin : skins) {
                    doPacketRemoval((Player) event.getKey(), skin);
                }

                skins.clear();
            }).build();

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

    public void handlePackets(Player player, PlayerDisguise disguise, LibsPackets packets) {
        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            return;
        }

        PlayerSkin skin = skins.stream().filter(s -> s.getDisguise().get() == disguise).findAny().orElse(null);

        if (skin == null || !skin.isSleepPackets()) {
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

    @EventHandler(priority = EventPriority.MONITOR,
            ignoreCancelled = true)
    private void onUndisguise(UndisguiseEvent event) {
        if (!event.getDisguise().isPlayerDisguise()) {
            return;
        }

        PlayerDisguise disguise = (PlayerDisguise) event.getDisguise();

        for (Player player : DisguiseUtilities.getPerverts(disguise)) {
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

    private void doPacketRemoval(Player player, PlayerSkin skin) {
        PlayerDisguise disguise = skin.getDisguise().get();

        if (disguise == null) {
            return;
        }

        try {
            for (Map.Entry<Integer, ArrayList<PacketContainer>> entry : skin.getSleptPackets().entrySet()) {
                if (entry.getKey() == 0) {
                    for (PacketContainer packet : entry.getValue()) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                    }
                } else {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                if (!disguise.isDisguiseInUse()) {
                                    return;
                                }

                                for (PacketContainer packet : entry.getValue()) {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
                                }
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }.runTaskLater(LibsDisguises.getInstance(), entry.getKey());
                }
            }

            if (skin.isDoTabList()) {
                PacketContainer packetContainer =
                        DisguiseUtilities.getTabPacket(disguise, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

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
