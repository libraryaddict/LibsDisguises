package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Login.Client;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientPluginResponse;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerPluginRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import me.libraryaddict.disguise.utilities.modded.ModdedManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PacketListenerModdedClient extends SimplePacketListenerAbstract {
    private final Cache<String, WrapperLoginClientLoginStart> loginAttempts =
        CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();
    private static final int packetId1 = 5555554, packetId2 = 5555555;

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

    private void handleModlist(String name, byte[] data) {
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
            throw new DecoderException(
                "The received encoded string buffer length is longer than maximum allowed (" + j + " > " + size * 4 + ")");
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

    @Override
    public void onPacketLoginReceive(PacketLoginReceiveEvent event) {
        if (event.getPacketType() != Client.LOGIN_PLUGIN_RESPONSE && event.getPacketType() != Client.LOGIN_START) {
            return;
        }

        event.setCancelled(true);

        if (event.getPacketType() == Client.LOGIN_PLUGIN_RESPONSE) {
            String address = event.getSocketAddress().toString();

            WrapperLoginClientLoginStart startAttempt = loginAttempts.getIfPresent(address);

            if (startAttempt == null) {
                return;
            }

            WrapperLoginClientPluginResponse wrapper = new WrapperLoginClientPluginResponse(event);

            if (wrapper.getMessageId() == packetId1 && wrapper.isSuccessful()) {
                handleModlist(startAttempt.getUsername(), wrapper.getData());
            } else if (wrapper.getMessageId() == packetId2) {
                loginAttempts.invalidate(address);
                PacketEvents.getAPI().getPlayerManager().receivePacketSilently(event.getPlayer(), startAttempt);
            }

            return;
        }

        loginAttempts.put(event.getSocketAddress().toString(), new WrapperLoginClientLoginStart(event.clone()));

        WrapperLoginServerPluginRequest packet1 =
            new WrapperLoginServerPluginRequest(packetId1, "fml:handshake", ModdedManager.getFmlHandshake());
        WrapperLoginServerPluginRequest packet2 =
            new WrapperLoginServerPluginRequest(packetId2, "fml:handshake", ModdedManager.getFmlRegistries());

        PacketEvents.getAPI().getPlayerManager().sendPacket(event.getPlayer(), packet1);
        PacketEvents.getAPI().getPlayerManager().sendPacket(event.getPlayer(), packet2);
    }
}
