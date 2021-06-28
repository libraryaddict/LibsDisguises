package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Created by libraryaddict on 4/07/2020.
 */
public class PacketListenerScoreboardTeam extends PacketAdapter {
    public PacketListenerScoreboardTeam() {
        super(new AdapterParameteters().optionAsync().plugin(LibsDisguises.getInstance()).types(PacketType.Play.Server.SCOREBOARD_TEAM));
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        if (NmsVersion.v1_17.isSupported()) {
            int type = packet.getIntegers().read(0);

            if (type != 0 && type != 2) {
                return;
            }
        }

        String name = packet.getStrings().read(0);

        if (name == null || !name.startsWith("LD_") || name.equals("LD_NoName")) {
            return;
        }

        DisguiseUtilities.DScoreTeam team = DisguiseUtilities.getTeams().get(name);

        if (team == null) {
            return;
        }

        StructureModifier<WrappedChatComponent> chats;

        if (NmsVersion.v1_17.isSupported()) {
            // Might need to do sanity checks but eh
            chats = packet.getOptionalStructures().read(0).get().getChatComponents();
        } else {
            chats = packet.getChatComponents();
        }

        BaseComponent[] prefix = DisguiseUtilities.getColoredChat(team.getPrefix());
        BaseComponent[] suffix = DisguiseUtilities.getColoredChat(team.getSuffix());

        chats.write(1, WrappedChatComponent.fromJson(ComponentSerializer.toString(prefix)));
        chats.write(2, WrappedChatComponent.fromJson(ComponentSerializer.toString(suffix)));
    }
}
