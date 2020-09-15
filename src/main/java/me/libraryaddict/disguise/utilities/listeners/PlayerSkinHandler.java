package me.libraryaddict.disguise.utilities.listeners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by libraryaddict on 15/09/2020.
 */
public class PlayerSkinHandler implements Listener {
    @RequiredArgsConstructor
    private static class PlayerSkin {
        private final long firstPacketSent = System.currentTimeMillis();
        @Getter
        private final WeakReference<PlayerDisguise> disguise;

        public boolean canRemove() {
            return firstPacketSent + (DisguiseConfig.getTablistRemoveDelay() * 50) < System.currentTimeMillis();
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
                    PlayerDisguise disguise = skin.disguise.get();

                    if (disguise == null) {
                        return;
                    }

                    doPacketRemoval((Player) event.getKey(), disguise);
                }

                skins.clear();
            }).build();

    public void addPlayerSkin(Player player, PlayerDisguise disguise) {
        tryProcess(player);

        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            skins = new ArrayList<>();
        }

        skins.add(new PlayerSkin(new WeakReference<>(disguise)));
        getCache().put(player, skins);
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

            doPacketRemoval(player, disguise);

            if (skins.size() == 1) {
                getCache().invalidate(player);
            } else {
                skins.remove(skin);
            }
        }
    }

    private void doPacketRemoval(Player player, PlayerDisguise disguise) {
        if (disguise == null) {
            return;
        }

        PacketContainer packetContainer =
                DisguiseUtilities.getTabPacket(disguise, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void tryProcess(Player player) {
        List<PlayerSkin> skins = getCache().getIfPresent(player);

        if (skins == null) {
            return;
        }

        skins.removeIf(skin -> {
            if (!skin.canRemove()) {
                return false;
            }

            doPacketRemoval(player, skin.getDisguise().get());
            return true;
        });

        if (!skins.isEmpty()) {
            return;
        }

        getCache().invalidate(player);

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        tryProcess(event.getPlayer());
    }
}
