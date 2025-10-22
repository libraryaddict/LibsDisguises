package me.libraryaddict.disguise.utilities.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerClientCustomPayload;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerClientInteract;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerEntityDestroy;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerInventory;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerMain;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerScoreboardTeam;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerSounds;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerTabList;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerVehicleMovement;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerViewSelfDisguise;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PacketsManager {
    @Getter
    private final static PacketsManager packetsManager = new PacketsManager();
    private final PacketListenerClientInteract clientInteractEntityListener = new PacketListenerClientInteract();
    private final PacketListenerTabList tablistListener = new PacketListenerTabList();
    private final PacketListenerClientCustomPayload customPayload = new PacketListenerClientCustomPayload();
    private PacketListenerInventory inventoryListener;
    @Getter
    private boolean inventoryListenerEnabled;
    private PacketListenerMain mainListener;
    private PacketListenerSounds soundsListener;
    private boolean soundsListenerEnabled;
    private PacketListenerViewSelfDisguise viewDisguisesListener;
    private PacketListenerVehicleMovement vehicleMovement;
    @Getter
    private boolean viewDisguisesListenerEnabled;
    @Getter
    private PacketsHandler packetsHandler;
    private boolean initialListenersRegistered;

    public void addPacketListeners() {
        if (!initialListenersRegistered) {
            // Add a client listener to cancel them interacting with uninteractable disguised entitys.
            // You ain't supposed to be allowed to 'interact' with a item that cannot be clicked.
            // Because it kicks you for hacking.
            PacketEvents.getAPI().getEventManager().registerListener(clientInteractEntityListener);
            PacketEvents.getAPI().getEventManager().registerListener(tablistListener);

            if (DisguiseConfig.isLoginPayloadPackets()) {
                PacketEvents.getAPI().getEventManager().registerListener(customPayload);
            }

            initialListenersRegistered = true;
            PacketEvents.getAPI().getSettings().fullStackTrace(true);
        }

        // Now I call this and the main listener is registered!
        setupMainPacketsListener();
    }

    /**
     * Creates the packet listeners
     */
    public void init() {
        soundsListener = new PacketListenerSounds();

        // Self disguise (/vsd) listener
        viewDisguisesListener = new PacketListenerViewSelfDisguise(createConflicting(true));

        inventoryListener = new PacketListenerInventory();
        packetsHandler = new PacketsHandler();
    }

    public boolean isHearDisguisesEnabled() {
        return soundsListenerEnabled;
    }

    public void setInventoryListenerEnabled(boolean enabled) {
        if (isInventoryListenerEnabled() == enabled || inventoryListener == null) {
            return;
        }

        inventoryListenerEnabled = enabled;

        if (inventoryListenerEnabled) {
            PacketEvents.getAPI().getEventManager().registerListener(inventoryListener);
        } else {
            PacketEvents.getAPI().getEventManager().unregisterListener(inventoryListener);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Disguise disguise = DisguiseAPI.getDisguise(player, player);

            if (disguise == null) {
                continue;
            }

            if (!isViewDisguisesListenerEnabled()) {
                continue;
            }

            if (!disguise.isSelfDisguiseVisible()) {
                continue;
            }

            if (!(disguise.isHideArmorFromSelf() || disguise.isHideHeldItemFromSelf())) {
                continue;
            }

            player.updateInventory();
        }
    }

    public void setHearDisguisesListener(boolean enabled) {
        if (soundsListenerEnabled == enabled) {
            return;
        }

        soundsListenerEnabled = enabled;

        if (soundsListenerEnabled) {
            PacketEvents.getAPI().getEventManager().registerListener(soundsListener);
        } else {
            PacketEvents.getAPI().getEventManager().unregisterListener(soundsListener);
        }
    }

    public void setupMainPacketsListener() {
        if (!initialListenersRegistered) {
            return;
        }

        if (mainListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(mainListener);
        }

        // If armorstand packet listener was registered, but isn't wanted anymore
        if (vehicleMovement != null && !DisguiseConfig.isArmorstandsName()) {
            PacketEvents.getAPI().getEventManager().unregisterListener(vehicleMovement);
            HandlerList.unregisterAll(vehicleMovement);

            vehicleMovement = null;
        }

        getPacketsHandler().registerPacketHandlers();

        boolean[] listenedPackets = new boolean[Server.values().length];
        boolean[] spawnPackets = new boolean[Server.values().length];
        boolean[] conflictingPackets = createConflicting(false);

        if (!NmsVersion.v1_20_R2.isSupported()) {
            spawnPackets[Server.SPAWN_PLAYER.ordinal()] = true;
        }

        spawnPackets[Server.SPAWN_EXPERIENCE_ORB.ordinal()] = true;
        spawnPackets[Server.SPAWN_ENTITY.ordinal()] = true;

        if (!NmsVersion.v1_19_R1.isSupported()) {
            spawnPackets[Server.SPAWN_LIVING_ENTITY.ordinal()] = true;
            spawnPackets[Server.SPAWN_PAINTING.ordinal()] = true;
        }

        // Add movement packets
        if (DisguiseConfig.isMovementPacketsEnabled()) {
            listenedPackets[Server.ENTITY_MOVEMENT.ordinal()] = true;
            listenedPackets[Server.ENTITY_RELATIVE_MOVE_AND_ROTATION.ordinal()] = true;
            listenedPackets[Server.ENTITY_HEAD_LOOK.ordinal()] = true;
            listenedPackets[Server.ENTITY_TELEPORT.ordinal()] = true;
            listenedPackets[Server.ENTITY_RELATIVE_MOVE.ordinal()] = true;
            listenedPackets[Server.ENTITY_VELOCITY.ordinal()] = true;
            listenedPackets[Server.SET_PASSENGERS.ordinal()] = true;
            listenedPackets[Server.ENTITY_POSITION_SYNC.ordinal()] = true;
        }

        // Add equipment packet
        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            listenedPackets[Server.ENTITY_EQUIPMENT.ordinal()] = true;
        }

        for (int i = 0; i < listenedPackets.length; i++) {
            listenedPackets[i] |= conflictingPackets[i] || spawnPackets[i];
        }

        mainListener = new PacketListenerMain(listenedPackets, spawnPackets, createConflicting(true));

        PacketEvents.getAPI().getEventManager().registerListener(mainListener);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerEntityDestroy());

        if (NmsVersion.v1_13.isSupported() && DisguiseConfig.getPlayerNameType().isScoreboardPacketListenerNeeded()) {
            PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerScoreboardTeam());
        }

        if (vehicleMovement == null && DisguiseConfig.isArmorstandsName()) {
            vehicleMovement = new PacketListenerVehicleMovement();

            Bukkit.getPluginManager().registerEvents(vehicleMovement, LibsDisguises.getInstance());
            PacketEvents.getAPI().getEventManager().registerListener(vehicleMovement);
        }
    }

    /**
     * The packets that will potentially kick clients
     *
     * @param isntHighPerformance If true, will be used in a way that is not going to drag performance down
     * @return boolean[] the packets to handle
     */
    public boolean[] createConflicting(boolean isntHighPerformance) {
        boolean[] conflictingPackets = new boolean[Server.values().length];

        // Add packets that always need to be enabled to ensure safety
        conflictingPackets[Server.ENTITY_METADATA.ordinal()] = true;

        if (isntHighPerformance || DisguiseConfig.isCollectPacketsEnabled()) {
            conflictingPackets[Server.COLLECT_ITEM.ordinal()] = true;
        }

        if (isntHighPerformance || DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            conflictingPackets[Server.UPDATE_ATTRIBUTES.ordinal()] = true;
        }

        // Add the packet that ensures if they are sleeping or not
        if (isntHighPerformance || DisguiseConfig.isAnimationPacketsEnabled()) {
            conflictingPackets[Server.ENTITY_ANIMATION.ordinal()] = true;
        }

        // Add the packet that makes sure that entities with armor do not send unpickupable armor on death
        if (isntHighPerformance || DisguiseConfig.isEntityStatusPacketsEnabled()) {
            conflictingPackets[Server.ENTITY_STATUS.ordinal()] = true;
        }

        return conflictingPackets;
    }

    public void setViewDisguisesListener(boolean enabled) {
        if (viewDisguisesListenerEnabled == enabled) {
            return;
        }

        viewDisguisesListenerEnabled = enabled;

        if (viewDisguisesListenerEnabled) {
            PacketEvents.getAPI().getEventManager().registerListener(viewDisguisesListener);
        } else {
            PacketEvents.getAPI().getEventManager().unregisterListener(viewDisguisesListener);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Disguise disguise = DisguiseAPI.getDisguise(player, player);

            if (disguise == null || !disguise.isSelfDisguiseVisible()) {
                continue;
            }

            if (enabled) {
                DisguiseUtilities.setupFakeDisguise(disguise);
            } else {
                DisguiseUtilities.removeSelfDisguise(disguise);
            }

            if (!inventoryListenerEnabled || !(disguise.isHidingArmorFromSelf() || disguise.isHidingHeldItemFromSelf())) {
                continue;
            }

            player.updateInventory();
        }
    }
}
