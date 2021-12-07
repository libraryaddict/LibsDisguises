package me.libraryaddict.disguise.utilities.modded;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.StreamSerializer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.listeners.ModdedListener;
import me.libraryaddict.disguise.utilities.packets.packetlisteners.PacketListenerModdedClient;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by libraryaddict on 14/04/2020.
 */
public class ModdedManager {
    @Getter
    private static final HashMap<NamespacedKey, ModdedEntity> entities = new HashMap<>();
    @Getter
    private static byte[] fmlHandshake;
    @Getter
    private static byte[] fmlRegistries;
    @Getter
    private static final Cache<String, ArrayList<String>> forgeMods = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).build();

    public ModdedManager(ArrayList<String> channels) {
        if (getEntities().isEmpty()) {
            return;
        }

        if (fmlRegistries == null && DisguiseConfig.isLoginPayloadPackets()) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListenerModdedClient());
            Bukkit.getPluginManager().registerEvents(new ModdedListener(), LibsDisguises.getInstance());
        }

        createPayloads(channels);
    }

    private void createPayloads(ArrayList<String> channels) {
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
                // Here's the channel name
                s.serializeString(output, channel.substring(0, channel.indexOf("|")));
                // Here's the channel version, yes <Client Mod> we're using your version!
                s.serializeString(output, channel.substring(channel.indexOf("|") + 1));
            }

            // We want to declare some entities. Wait. No we don't?
            // Weird, am I missing something..
            s.serializeVarInt(output, 0);

            // Only this one thx
            // s.serializeString(output, "minecraft:entity_type");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        fmlHandshake = stream.toByteArray();

        stream = new ByteArrayOutputStream();
        output = new DataOutputStream(stream);

        s = StreamSerializer.getDefault();

        try {
            // Packet id 3
            s.serializeVarInt(output, 3);

            // What registry we're modifying
            s.serializeString(output, "minecraft:entity_type");
            // Yes... We're doing custom data
            s.serializeVarInt(output, 1);

            // We have this many entities
            s.serializeVarInt(output, entities.size());

            // Write the entity names and ids
            for (Map.Entry<NamespacedKey, ModdedEntity> entry : entities.entrySet()) {
                // We are registering librarymod:librarian
                s.serializeString(output, entry.getKey().toString());
                // It's got the type ID of 85
                s.serializeVarInt(output, entry.getValue().getTypeId());
            }

            // Sir, we do not want to declare aliases
            s.serializeVarInt(output, 0);

            // Or overrides
            s.serializeVarInt(output, 0);

            // No.. Not even blocked
            s.serializeVarInt(output, 0);

            // Or dummied. What is dummied anyways. What are these others?
            s.serializeVarInt(output, 0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        fmlRegistries = stream.toByteArray();
    }

    public static void registerModdedEntity(NamespacedKey name, ModdedEntity entity, boolean register) {
        if (entities.keySet().stream().anyMatch(n -> n.toString().equalsIgnoreCase(name.toString()))) {
            throw new IllegalArgumentException(name + " has already been registered");
        }

        if (entities.values().stream().anyMatch(n -> n.getName().equalsIgnoreCase(entity.getName()))) {
            throw new IllegalArgumentException("Modded entity " + entity.getName() + " has already been registered");
        }

        Object entityType;

        if (register) {
            entityType = ReflectionManager.registerEntityType(name);
            int entityId = ReflectionManager.getEntityTypeId(entityType);

            entity.setTypeId(entityId);
        } else {
            entityType = ReflectionManager.getEntityType(name);
            int entityId = ReflectionManager.getEntityTypeId(entityType);

            entity.setTypeId(entityId);
        }

        entity.setEntityType(entityType);

        entities.put(name, entity);
    }

    public static ModdedEntity getModdedEntity(NamespacedKey name) {
        return entities.get(name);
    }

    public static ModdedEntity getModdedEntity(String name) {
        for (ModdedEntity entity : entities.values()) {
            if (!entity.getName().equalsIgnoreCase(name)) {
                continue;
            }

            return entity;
        }

        return null;
    }

    public static void doMods(Player player) {
        ArrayList<String> mods = getForgeMods().getIfPresent(player.getName());

        player.setMetadata("forge_mods", new FixedMetadataValue(LibsDisguises.getInstance(), mods));

        for (ModdedEntity e : ModdedManager.getEntities().values()) {
            if (e.getMod() == null) {
                continue;
            }

            if (mods.contains(e.getMod().toLowerCase(Locale.ENGLISH))) {
                continue;
            }

            if (e.getRequired() == null) {
                continue;
            }

            player.kickPlayer(e.getRequired());
            break;
        }
    }

    public static ArrayList<DisguisePerm> getDisguiseTypes() {
        ArrayList<DisguisePerm> perms = new ArrayList<>();

        for (Map.Entry<NamespacedKey, ModdedEntity> entry : entities.entrySet()) {
            perms.add(new DisguisePerm(
                    entry.getValue().isLiving() ? DisguiseType.MODDED_LIVING : DisguiseType.MODDED_MISC,
                    entry.getValue().getName()));
        }

        return perms;
    }
}
