package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * @author libraryaddict
 */
public abstract class DisguiseBaseCommand implements CommandExecutor {
    private static final Map<Class<? extends DisguiseBaseCommand>, String> disguiseCommands;

    static {
        HashMap<Class<? extends DisguiseBaseCommand>, String> map = new HashMap<>();

        map.put(DisguiseCommand.class, "Disguise");
        map.put(DisguiseEntityCommand.class, "DisguiseEntity");
        map.put(DisguisePlayerCommand.class, "DisguisePlayer");
        map.put(DisguiseRadiusCommand.class, "DisguiseRadius");
        map.put(DisguiseModifyCommand.class, "DisguiseModify");
        map.put(DisguiseModifyEntityCommand.class, "DisguiseModifyEntity");
        map.put(DisguiseModifyPlayerCommand.class, "DisguiseModifyPlayer");
        map.put(DisguiseModifyRadiusCommand.class, "DisguiseModifyRadius");

        disguiseCommands = map;
    }

    protected ArrayList<String> filterTabs(ArrayList<String> list, String[] origArgs) {
        if (origArgs.length == 0)
            return list;

        Iterator<String> itel = list.iterator();
        String label = origArgs[origArgs.length - 1].toLowerCase();

        while (itel.hasNext()) {
            String name = itel.next();

            if (name.toLowerCase().startsWith(label))
                continue;

            itel.remove();
        }

        return new ArrayList<>(new HashSet<>(list));
    }

    protected String getDisplayName(CommandSender player) {
        Team team = ((Player) player).getScoreboard().getEntryTeam(player.getName());

        if (team == null) {
            return player.getName();
        }

        return team.getPrefix() + team.getColor() + player.getName() + team.getSuffix();
    }

    protected ArrayList<String> getAllowedDisguises(DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = new ArrayList<>();

        for (DisguisePerm type : permissions.getAllowed()) {
            if (type.isUnknown())
                continue;

            allowedDisguises.add(type.toReadable().replaceAll(" ", "_"));
        }

        return allowedDisguises;
    }

    protected String[] getArgs(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {
            String s = args[i];

            if (s.trim().isEmpty())
                continue;

            newArgs.add(s);
        }

        return newArgs.toArray(new String[0]);
    }

    protected static final Map<Class<? extends DisguiseBaseCommand>, String> getCommandNames() {
        return disguiseCommands;
    }

    public final String getPermNode() {
        String name = getCommandNames().get(this.getClass());

        if (name == null) {
            throw new UnsupportedOperationException("Unknown disguise command, perm node not found");
        }

        return name;
    }

    protected DisguisePermissions getPermissions(CommandSender sender) {
        return DisguiseParser.getPermissions(sender, getPermNode());
    }

    protected boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    protected abstract void sendCommandUsage(CommandSender sender, DisguisePermissions disguisePermissions);
}
