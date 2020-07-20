package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Set;

/**
 * Created by libraryaddict on 4/07/2020.
 */
public class PacketListenerScoreboardTeam extends PacketAdapter {
    public PacketListenerScoreboardTeam() {
        super(LibsDisguises.getInstance(), PacketType.Play.Server.SCOREBOARD_TEAM);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        String name = packet.getStrings().read(0);

        if (!name.startsWith("LD_") || name.equals("LD_NoName") || name.equals("LD_Pushing")) {
            return;
        }

        DisguiseUtilities.DScoreTeam team = null;

        loop:
        for (Set<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
            for (Disguise disguise : disguises) {
                if (!disguise.isPlayerDisguise() || !((PlayerDisguise) disguise).hasScoreboardName()) {
                    continue;
                }

                DisguiseUtilities.DScoreTeam t = ((PlayerDisguise) disguise).getScoreboardName();

                if (!name.equals(t.getTeamName())) {
                    continue;
                }

                team = t;
                break loop;
            }
        }

        if (team == null) {
            return;
        }

        StructureModifier<WrappedChatComponent> chats = packet.getChatComponents();

        BaseComponent[] prefix = DisguiseUtilities.getColoredChat(team.getPrefix());
        BaseComponent[] suffix = DisguiseUtilities.getColoredChat(team.getSuffix());

        chats.write(1, WrappedChatComponent.fromJson(ComponentSerializer.toString(prefix)));
        chats.write(2, WrappedChatComponent.fromJson(ComponentSerializer.toString(suffix)));
    }
}
