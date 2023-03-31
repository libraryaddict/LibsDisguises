package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by libraryaddict on 11/06/2020.
 */
public class PacketListenerModdedClient extends PacketAdapter {
    private final Cache<String, String> loginAttempts = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();
    private final int packetId1 = 5555554, packetId2 = 5555555;

    public PacketListenerModdedClient() {
        super(LibsDisguises.getInstance(), PacketType.Login.Client.START, PacketType.Login.Client.CUSTOM_PAYLOAD);
    }

    private int getInt(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    private void handleModlist(Player player, String name, byte[] data) {
        ByteBuf buf = Unpooled.copiedBuffer(data);

        int packetId = getInt(buf);

        if (packetId != 2) {
            return;
        }

        int count = getInt(buf);
        ArrayList<String> mods = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            mods.add(getString(buf));
        }

        ModdedManager.getForgeMods().put(name, mods);
    }

    public String getString(ByteBuf buf) {
        int j = getInt(buf);
        int size = 256;

        if (j > size * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + size * 4 + ")");
        } else if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = buf.toString(buf.readerIndex(), j, StandardCharsets.UTF_8);
            buf.readerIndex(buf.readerIndex() + j);
            if (s.length() > size) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + size + ")");
            } else {
                return s;
            }
        }
    }

    private void handleDataReceived(Player player, String name) {
        // Continue
        PacketContainer packet = new PacketContainer(PacketType.Login.Client.START);
        packet.getModifier().write(0, new GameProfile(null, name));

        ProtocolLibrary.getProtocolManager().receiveClientPacket(player, packet, false);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        event.setCancelled(true);

        if (event.getPacketType() != PacketType.Login.Client.START) {
            String address = event.getPlayer().getAddress().toString();

            String name = loginAttempts.getIfPresent(address);

            if (name == null) {
                return;
            }

            if (event.getPacket().getIntegers().read(0) == packetId2) {
                loginAttempts.invalidate(address);
                handleDataReceived(event.getPlayer(), name);
                return;
            } else if (event.getPacket().getIntegers().read(0) == packetId1) {
                ByteBuf buf = (ByteBuf) event.getPacket().getModifier().read(1);

                if (buf != null) {
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);

                    handleModlist(event.getPlayer(), name, bytes);
                }
            }

            return;
        }

        loginAttempts.put(event.getPlayer().getAddress().toString(), event.getPacket().getGameProfiles().read(0).getName());

        PacketContainer packet1 = new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD);
        packet1.getIntegers().write(0, packetId1);
        packet1.getMinecraftKeys().write(0, new com.comphenix.protocol.wrappers.MinecraftKey("fml", "handshake"));

        try {
            Object obj1 =
                ReflectionManager.getNmsConstructor("PacketDataSerializer", ByteBuf.class).newInstance(Unpooled.wrappedBuffer(ModdedManager.getFmlHandshake()));

            packet1.getModifier().write(2, obj1);

            PacketContainer packet2 = new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD);
            packet2.getIntegers().write(0, packetId2);
            packet2.getMinecraftKeys().write(0, new MinecraftKey("fml", "handshake"));
            Object obj2 = ReflectionManager.getNmsConstructor("PacketDataSerializer", ByteBuf.class)
                .newInstance(Unpooled.wrappedBuffer(ModdedManager.getFmlRegistries()));

            packet2.getModifier().write(2, obj2);

            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet1);
            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
