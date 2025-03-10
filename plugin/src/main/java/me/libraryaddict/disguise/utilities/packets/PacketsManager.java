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

import java.util.ArrayList;

public class PacketsManager {
    private static final PacketListenerClientInteract clientInteractEntityListener = new PacketListenerClientInteract();
    private static final PacketListenerTabList tablistListener = new PacketListenerTabList();
    private static final PacketListenerClientCustomPayload customPayload = new PacketListenerClientCustomPayload();
    private static PacketListenerInventory inventoryListener;
    @Getter
    private static boolean inventoryListenerEnabled;
    private static PacketListenerMain mainListener;
    private static PacketListenerSounds soundsListener;
    private static boolean soundsListenerEnabled;
    private static PacketListenerViewSelfDisguise viewDisguisesListener;
    private static PacketListenerVehicleMovement vehicleMovement;
    @Getter
    private static boolean viewDisguisesListenerEnabled;
    @Getter
    private static PacketsHandler packetsHandler;
    @Getter
    private static boolean initialListenersRegistered;

    public static void addPacketListeners() {
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
    public static void init() {
        soundsListener = new PacketListenerSounds();

        // Self disguise (/vsd) listener
        viewDisguisesListener = new PacketListenerViewSelfDisguise();

        inventoryListener = new PacketListenerInventory();
        packetsHandler = new PacketsHandler();
    }

    public static boolean isHearDisguisesEnabled() {
        return soundsListenerEnabled;
    }

    public static void setInventoryListenerEnabled(boolean enabled) {
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

    public static void setHearDisguisesListener(boolean enabled) {
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

    public static void setupMainPacketsListener() {
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

        ArrayList<Server> packetsToListen = new ArrayList<>();

        // Add spawn packets
        {
            if (!NmsVersion.v1_20_R2.isSupported()) {
                packetsToListen.add(Server.SPAWN_PLAYER);
            }

            packetsToListen.add(Server.SPAWN_EXPERIENCE_ORB);
            packetsToListen.add(Server.SPAWN_ENTITY);

            if (!NmsVersion.v1_19_R1.isSupported()) {
                packetsToListen.add(Server.SPAWN_LIVING_ENTITY);
                packetsToListen.add(Server.SPAWN_PAINTING);
            }
        }

        // Add packets that always need to be enabled to ensure safety
        {
            packetsToListen.add(Server.ENTITY_METADATA);
        }

        if (DisguiseConfig.isCollectPacketsEnabled()) {
            packetsToListen.add(Server.COLLECT_ITEM);
        }

        if (DisguiseConfig.isMiscDisguisesForLivingEnabled()) {
            packetsToListen.add(Server.UPDATE_ATTRIBUTES);
        }

        // Add movement packets
        if (DisguiseConfig.isMovementPacketsEnabled()) {
            packetsToListen.add(Server.ENTITY_MOVEMENT);
            packetsToListen.add(Server.ENTITY_RELATIVE_MOVE_AND_ROTATION);
            packetsToListen.add(Server.ENTITY_HEAD_LOOK);
            packetsToListen.add(Server.ENTITY_TELEPORT);
            packetsToListen.add(Server.ENTITY_RELATIVE_MOVE);
            packetsToListen.add(Server.ENTITY_VELOCITY);
            packetsToListen.add(Server.SET_PASSENGERS);
            packetsToListen.add(Server.ENTITY_POSITION_SYNC);
        }

        // Add equipment packet
        if (DisguiseConfig.isEquipmentPacketsEnabled()) {
            packetsToListen.add(Server.ENTITY_EQUIPMENT);
        }

        // Add the packet that ensures if they are sleeping or not
        if (DisguiseConfig.isAnimationPacketsEnabled()) {
            packetsToListen.add(Server.ENTITY_ANIMATION);
        }

        // Add the packet that makes sure that entities with armor do not send unpickupable armor on death
        if (DisguiseConfig.isEntityStatusPacketsEnabled()) {
            packetsToListen.add(Server.ENTITY_STATUS);
        }

        mainListener = new PacketListenerMain(packetsToListen);

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

    public static void setViewDisguisesListener(boolean enabled) {
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
