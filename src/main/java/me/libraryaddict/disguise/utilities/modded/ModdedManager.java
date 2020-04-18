package me.libraryaddict.disguise.utilities.modded;

import com.comphenix.protocol.utility.StreamSerializer;
import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by libraryaddict on 14/04/2020.
 */
public class ModdedManager implements PluginMessageListener {
    @Getter
    private static final HashMap<NamespacedKey, CustomEntity> entities = new HashMap<>();
    @Getter
    private static byte[] fmlHandshake;

    public ModdedManager(ArrayList<String> channels) {
        if (getEntities().isEmpty()) {
            return;
        }

        if (getEntities().values().stream().noneMatch(e -> e.getMod() != null)) {
            return;
        }

        Bukkit.getMessenger().registerOutgoingPluginChannel(LibsDisguises.getInstance(), "fml:handshake");
        Bukkit.getMessenger().registerIncomingPluginChannel(LibsDisguises.getInstance(), "fml:handshake", this);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(stream);

        StreamSerializer s = StreamSerializer.getDefault();

        try {
            // Packet id 1
            s.serializeVarInt(output, 1);
            // We have no mods to declare
            s.serializeVarInt(output, 0);

            // We want to declare some channels
            s.serializeVarInt(output, channels.size());

            for (String channel : channels) {
                s.serializeString(output, channel.substring(0, channel.indexOf("|")));
                s.serializeString(output, channel.substring(channel.indexOf("|") + 1));
            }

            // We have no resources to declare
            s.serializeVarInt(output, 0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        fmlHandshake = stream.toByteArray();
    }

    public static void registerCustomEntity(NamespacedKey name, CustomEntity entity, boolean register) {
        if (entities.keySet().stream().anyMatch(n -> n.toString().equalsIgnoreCase(name.toString()))) {
            throw new IllegalArgumentException(name + " has already been registered");
        }

        if (entities.values().stream().anyMatch(n -> n.getName().equalsIgnoreCase(entity.getName()))) {
            throw new IllegalArgumentException("Modded entity " + entity.getName() + " has already been registered");
        }

        if (register) {
            Object entityType = ReflectionManager.registerEntityType(name);
            int entityId = ReflectionManager.getEntityTypeId(entityType);

            entity.setTypeId(entityId);
            entity.setEntityType(entityType);
        } else {
            Object entityType = ReflectionManager.getEntityType(name);
            int entityId = ReflectionManager.getEntityTypeId(entityType);

            entity.setTypeId(entityId);
            entity.setEntityType(entityType);
        }

        entities.put(name, entity);
    }

    public static CustomEntity getCustomEntity(NamespacedKey name) {
        return entities.get(name);
    }

    public static CustomEntity getCustomEntity(String name) {
        for (CustomEntity entity : entities.values()) {
            if (!name.contains(":") ? entity.getName().split(":")[1].equalsIgnoreCase(name) :
                    !entity.getName().equalsIgnoreCase(name)) {
                continue;
            }

            return entity;
        }

        return null;
    }

    public static ArrayList<DisguisePerm> getDisguiseTypes() {
        ArrayList<DisguisePerm> perms = new ArrayList<>();

        for (Map.Entry<NamespacedKey, CustomEntity> entry : entities.entrySet()) {
            perms.add(new DisguisePerm(
                    entry.getValue().isLiving() ? DisguiseType.CUSTOM_LIVING : DisguiseType.CUSTOM_MISC,
                    entry.getValue().getName()));
        }

        return perms;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (player.hasMetadata("forge_mods")) {
            return;
        }

        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            StreamSerializer s = StreamSerializer.getDefault();
            int packetId = s.deserializeVarInt(stream);

            if (packetId != 2) {
                return;
            }

            int count = s.deserializeVarInt(stream);

            ArrayList<String> mods = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                mods.add(s.deserializeString(stream, 256));
            }

            player.setMetadata("forge_mods", new FixedMetadataValue(LibsDisguises.getInstance(), mods));

            for (CustomEntity e : getEntities().values()) {
                if (e.getMod() == null) {
                    continue;
                }

                if (mods.contains(e.getMod().toLowerCase())) {
                    continue;
                }

                // TODO Idk, something because they don't have a mod?

                if (e.getRequired() == null) {
                    continue;
                }

                player.kickPlayer(e.getRequired());
                break;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
