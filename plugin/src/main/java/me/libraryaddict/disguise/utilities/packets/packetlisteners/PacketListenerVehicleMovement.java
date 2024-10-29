package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.ENTITY_RELATIVE_MOVE;
import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION;
import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.ENTITY_TELEPORT;
import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.RESPAWN;

public class PacketListenerVehicleMovement extends SimplePacketListenerAbstract implements Listener {
    static class PlayerTracker {
        /**
         * A vehicle can have multiple passengers
         */
        private final Map<Integer, int[]> vehicleAndPassengersId = new HashMap<>();
    }

    private final Map<UUID, PlayerTracker> trackerMap = new HashMap<>();

    public PacketListenerVehicleMovement() {
        super(PacketListenerPriority.LOW);
        // This handling does not handle packets that were only sent to specific players
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        trackerMap.remove(event.getPlayer().getUniqueId());
    }

    private void refreshPosition(Player observer, int entityId) {
        refreshPosition(observer, entityId, null);
    }

    private void refreshPosition(Player observer, int entityId, PacketWrapper sentPacket) {
        Disguise disguise = DisguiseUtilities.getDisguise(observer, entityId);

        if (disguise == null || disguise.getArmorstandIds().length == 0) {
            return;
        }

        Entity entity = disguise.getEntity();

        if (entity == null || !entity.isValid()) {
            return;
        }

        Location loc = entity.getLocation();

        if (sentPacket == null) {
            sentPacket = new WrapperPlayServerEntityTeleport(entityId,
                new com.github.retrooper.packetevents.protocol.world.Location(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),
                    loc.getPitch()), true);
        }

        List<PacketWrapper> wrappers = DisguiseUtilities.adjustNamePositions(disguise, Collections.singletonList(sentPacket));

        if (wrappers == null) {
            return;
        }

        for (PacketWrapper wrapper : wrappers) {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(observer, wrapper);
        }
    }

    private void refreshPassengersRecursive(Map<Integer, int[]> vehicleAndPassengersId, int[] passengers, Map<Integer, Boolean> map) {
        for (int entityId : passengers) {
            // If this entity is already handled
            if (map.containsKey(entityId)) {
                continue;
            }

            // Refresh the passenger
            map.put(entityId, true);

            // Get the passengers on the passenger
            int[] mounted = vehicleAndPassengersId.get(entityId);

            // If not carrying anyone
            if (mounted == null) {
                continue;
            }

            // Refresh the passengers on the passenger
            refreshPassengersRecursive(vehicleAndPassengersId, mounted, map);
        }
    }

    private void updatePassengersRecursive(int depth, Player player, PlayerTracker tracker, int[] passengers, PacketWrapper wrapper) {
        // Basic sanity check
        if (depth > 15) {
            return;
        }

        for (int entityId : passengers) {
            refreshPosition(player, entityId, wrapper);

            int[] newPassengers = tracker.vehicleAndPassengersId.get(entityId);

            if (newPassengers == null) {
                continue;
            }

            updatePassengersRecursive(depth++, player, tracker, newPassengers, wrapper);
        }
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        Player player = event.getPlayer();

        if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
            WrapperPlayServerSetPassengers packet;

            if (event.getLastUsedWrapper() != null) {
                packet = (WrapperPlayServerSetPassengers) event.getLastUsedWrapper();
            } else {
                packet = new WrapperPlayServerSetPassengers(event);
            }

            PlayerTracker tracker = trackerMap.get(player.getUniqueId());
            int[] mountedNow = packet.getPassengers();

            if (tracker == null) {
                if (mountedNow.length == 0) {
                    return;
                }

                trackerMap.put(player.getUniqueId(), tracker = new PlayerTracker());
            }

            int[] mountedPrevious = tracker.vehicleAndPassengersId.get(packet.getEntityId());

            if (packet.getPassengers().length == 0) {
                // Dismounted
                tracker.vehicleAndPassengersId.remove(packet.getEntityId());
            } else {
                tracker.vehicleAndPassengersId.put(packet.getEntityId(), mountedNow);
            }

            // We could do some fancy logic to avoid sending packets when it is not required, but that's making this complicated

            if (mountedPrevious != null) {
                loop:
                for (int entityId : mountedPrevious) {
                    boolean stillRiding = false;

                    // If the entity is still riding the same vehicle, skip loop
                    for (int ridingId : mountedNow) {
                        if (entityId != ridingId) {
                            continue;
                        }

                        continue loop;
                    }

                    // This entity has dismounted
                    refreshPosition(player, entityId);
                }

                loop:
                for (int ridingId : mountedNow) {
                    // If the entity is still riding the same vehicle, skip loop
                    for (int entityId : mountedNow) {
                        if (entityId != ridingId) {
                            continue;
                        }

                        continue loop;
                    }

                    // This entity has mounted
                    refreshPosition(player, ridingId);
                }
            } else {
                for (int entityId : mountedNow) {
                    refreshPosition(player, entityId);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
            PlayerTracker tracker = trackerMap.get(player.getUniqueId());

            if (tracker == null) {
                return;
            }

            int[] ids;

            if (event.getLastUsedWrapper() != null) {
                ids = ((WrapperPlayServerDestroyEntities) event.getLastUsedWrapper()).getEntityIds();
            } else {
                ids = new WrapperPlayServerDestroyEntities(event).getEntityIds();
            }

            Map<Integer, Boolean> toRefresh = new HashMap<>();

            for (int entityId : ids) {
                // Don't refresh this entity, it is destroyed
                toRefresh.put(entityId, false);
            }

            // Seperate block so the map is prefilled
            for (int entityId : ids) {
                int[] passengers = tracker.vehicleAndPassengersId.remove(entityId);

                // If nothing was riding this entity, continue
                if (passengers == null) {
                    continue;
                }

                // Refresh the passengers, recursive if there happens to be a pillar of mounted entities
                refreshPassengersRecursive(tracker.vehicleAndPassengersId, passengers, toRefresh);
            }

            for (Map.Entry<Integer, Boolean> entry : toRefresh.entrySet()) {
                // If this entity is not one to be refreshed
                if (!entry.getValue()) {
                    continue;
                }

                // Refresh the position
                refreshPosition(player, entry.getKey());
            }
        } else if (event.getPacketType() == ENTITY_TELEPORT || event.getPacketType() == ENTITY_RELATIVE_MOVE ||
            event.getPacketType() == ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            PlayerTracker tracker = trackerMap.get(player.getUniqueId());

            if (tracker == null || tracker.vehicleAndPassengersId.isEmpty()) {
                return;
            }

            PacketWrapper wrapper = event.getLastUsedWrapper();
            int entityId;

            if (wrapper == null) {
                if (event.getPacketType() == ENTITY_TELEPORT) {
                    wrapper = new WrapperPlayServerEntityTeleport(event);
                    entityId = ((WrapperPlayServerEntityTeleport) wrapper).getEntityId();
                } else if (event.getPacketType() == ENTITY_RELATIVE_MOVE) {
                    wrapper = new WrapperPlayServerEntityRelativeMove(event);
                    entityId = ((WrapperPlayServerEntityRelativeMove) wrapper).getEntityId();
                } else {
                    wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
                    entityId = ((WrapperPlayServerEntityRelativeMoveAndRotation) wrapper).getEntityId();
                }
            } else {
                entityId = DisguiseUtilities.getEntityId(wrapper);
            }

            int[] passengers = tracker.vehicleAndPassengersId.get(entityId);

            if (passengers == null) {
                return;
            }

            updatePassengersRecursive(0, player, tracker, passengers, wrapper);
        } else if (event.getPacketType() == RESPAWN) {
            // Respawn will remove entities, not sure if entities are removed in other ways
            // This might need a perioditic check to ensure that we're not creating a memory leak if a player does not die or switch worlds
            // Yet the entities just keep being added
            // Even if it is the case, should be minor as this only tracks ints, and only mounted entities
            trackerMap.remove(player.getUniqueId());
        }
    }
}
