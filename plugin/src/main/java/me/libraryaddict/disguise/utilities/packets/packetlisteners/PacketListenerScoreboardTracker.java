package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import me.libraryaddict.disguise.utilities.scoreboard.packetevents.PacketEventsScoreboardManager;

public class PacketListenerScoreboardTracker extends SimplePacketListenerAbstract {
    private final PacketEventsScoreboardManager scoreboardManager;

    public PacketListenerScoreboardTracker(PacketEventsScoreboardManager scoreboardManager) {
        super(PacketListenerPriority.MONITOR);

        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Server.TEAMS) {
            return;
        }

        scoreboardManager.getTracker().record(event.getUser().getUUID(), new WrapperPlayServerTeams(event));
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        if (event.getUser().getUUID() == null) {
            return;
        }

        scoreboardManager.getTracker().clear(event.getUser().getUUID());
    }
}
