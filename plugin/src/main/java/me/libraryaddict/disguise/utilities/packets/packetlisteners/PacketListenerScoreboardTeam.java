package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class PacketListenerScoreboardTeam extends SimplePacketListenerAbstract {
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Server.TEAMS) {
            return;
        }

        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(event);
        WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo = packet.getTeamInfo().orElse(null);

        if (teamInfo == null) {
            return;
        }

        String name = packet.getTeamName();

        if (name == null || !name.startsWith("LD_") || name.equals("LD_NoName") || name.startsWith("LD_Color_")) {
            return;
        }

        DisguiseUtilities.DScoreTeam team = DisguiseUtilities.getTeams().get(name);

        if (team == null) {
            return;
        }

        teamInfo.setPrefix(DisguiseUtilities.getAdventureChat(team.getPrefix()));
        teamInfo.setSuffix(DisguiseUtilities.getAdventureChat(team.getSuffix()));
    }
}
