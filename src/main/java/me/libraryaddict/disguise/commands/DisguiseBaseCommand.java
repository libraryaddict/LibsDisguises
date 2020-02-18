package me.libraryaddict.disguise.commands;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
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

    protected boolean isNotPremium(CommandSender sender) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "Please purchase Lib's Disguises to enable player commands");
            return true;
        }

        return false;
    }

    protected List<String> getTabDisguiseTypes(CommandSender sender, DisguisePermissions perms, String[] allArgs,
            int startsAt, String currentArg) {
        // If not enough arguments to get current disguise type
        if (allArgs.length <= startsAt) {
            return getAllowedDisguises(perms);
        }

        // Get current disguise type
        DisguisePerm disguiseType = DisguiseParser.getDisguisePerm(allArgs[startsAt]);

        // If disguise type isn't found, return nothing
        if (disguiseType == null) {
            return new ArrayList<>();
        }

        // If current argument is just after the disguise type, and disguise type is a player which is not a custom
        // disguise
        if (allArgs.length == startsAt + 1 && disguiseType.getType() == DisguiseType.PLAYER &&
                !disguiseType.isCustomDisguise()) {
            ArrayList<String> tabs = new ArrayList<>();

            // Add all player names to tab list
            for (Player player : Bukkit.getOnlinePlayers()) {
                // If command user cannot see player online, don't tab-complete name
                if (sender instanceof Player && !((Player) sender).canSee(player)) {
                    continue;
                }

                tabs.add(player.getName());
            }

            // Return tablist
            return tabs;
        }

        return getTabDisguiseOptions(sender, perms, disguiseType, allArgs, startsAt + (disguiseType.isPlayer() ? 2 : 1),
                currentArg);
    }

    /**
     * @param perms        What permissions they can use
     * @param disguisePerm The disguise permission they're using
     * @param allArgs      All the arguments in the command
     * @param startsAt     What index this starts at
     * @return a list of viable disguise options
     */
    protected List<String> getTabDisguiseOptions(CommandSender commandSender, DisguisePermissions perms,
            DisguisePerm disguisePerm, String[] allArgs, int startsAt, String currentArg) {
        ArrayList<String> usedOptions = new ArrayList<>();

        Method[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguisePerm.getWatcherClass());

        // Find which methods the disguiser has already used
        for (int i = startsAt; i < allArgs.length; i++) {
            for (Method method : methods) {
                String arg = allArgs[i];

                if (!method.getName().equalsIgnoreCase(arg)) {
                    continue;
                }

                usedOptions.add(arg);
                break;
            }
        }

        // If the disguiser has used options that they have not been granted to use, ignore them
        if (!perms.isAllowedDisguise(disguisePerm, usedOptions)) {
            return new ArrayList<>();
        }

        return getTabDisguiseSubOptions(commandSender, perms, disguisePerm, allArgs, startsAt, currentArg);
    }

    protected List<String> getTabDisguiseSubOptions(CommandSender commandSender, DisguisePermissions perms,
            DisguisePerm disguisePerm, String[] allArgs, int startsAt, String currentArg) {
        boolean addMethods = true;
        List<String> tabs = new ArrayList<>();

        // Check what argument was used before the current argument to see what we're displaying
        if (allArgs.length > startsAt) {
            String prevArg = allArgs[allArgs.length - 1];

            ParamInfo info = ParamInfoManager.getParamInfo(disguisePerm, prevArg);

            // If the previous argument is a method
            if (info != null) {
                if (!info.isParam(boolean.class)) {
                    addMethods = false;
                }

                // If there is a list of default values
                if (info.hasValues()) {
                    tabs.addAll(info.getEnums(currentArg));
                } else if (info.isParam(String.class)) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        // If command user cannot see player online, don't tab-complete name
                        if (commandSender instanceof Player && !((Player) commandSender).canSee(player)) {
                            continue;
                        }

                        tabs.add(player.getName());
                    }
                }
            }
        }

        if (addMethods) {
            // If this is a method, add. Else if it can be a param of the previous argument, add.
            for (Method method : ParamInfoManager.getDisguiseWatcherMethods(disguisePerm.getWatcherClass())) {
                if (!perms.isAllowedDisguise(disguisePerm, Collections.singletonList(method.getName()))) {
                    continue;
                }

                tabs.add(method.getName());
            }
        }

        return tabs;
    }

    protected List<String> filterTabs(List<String> list, String[] origArgs) {
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
            team = ((Player) player).getScoreboard().getEntryTeam(((Player) player).getUniqueId().toString());

            if (team == null) {
                return player.getName();
            }
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

    /**
     * @param args
     * @return Array of strings excluding current argument
     */
    protected String[] getPreviousArgs(String[] args) {
        ArrayList<String> newArgs = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {
            String s = args[i];

            if (s.trim().isEmpty())
                continue;

            newArgs.add(s);
        }

        return newArgs.toArray(new String[0]);
    }

    protected String getCurrentArg(String[] args) {
        if (args.length == 0) {
            return "";
        }

        return args[args.length - 1].trim();
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
