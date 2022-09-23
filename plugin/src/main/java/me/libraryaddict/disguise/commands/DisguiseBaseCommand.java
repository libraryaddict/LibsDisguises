package me.libraryaddict.disguise.commands;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.commands.disguise.DisguiseCommand;
import me.libraryaddict.disguise.commands.disguise.DisguiseEntityCommand;
import me.libraryaddict.disguise.commands.disguise.DisguisePlayerCommand;
import me.libraryaddict.disguise.commands.disguise.DisguiseRadiusCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyEntityCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyPlayerCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyRadiusCommand;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author libraryaddict
 */
public abstract class DisguiseBaseCommand implements CommandExecutor {
    private static final Map<Class<? extends DisguiseBaseCommand>, String> disguiseCommands;
    private final Cache<UUID, Long> rateLimit = CacheBuilder.newBuilder().expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

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

    protected boolean hasHitRateLimit(CommandSender sender) {
        if (sender.isOp() || !(sender instanceof Player) || sender.hasPermission("libsdisguises.ratelimitbypass")) {
            return false;
        }

        if (rateLimit.getIfPresent(((Player) sender).getUniqueId()) != null) {
            LibsMsg.TOO_FAST.send(sender);
            return true;
        }

        rateLimit.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
        return false;
    }

    protected boolean isNotPremium(CommandSender sender) {
        String requiredProtocolLib = StringUtils.join(DisguiseUtilities.getProtocolLibRequiredVersion(), " or build #");
        String version = ProtocolLibrary.getPlugin().getDescription().getVersion();

        if (DisguiseUtilities.isProtocolLibOutdated() && sender.isOp()) {
            DisguiseUtilities.sendProtocolLibUpdateMessage(sender, version, requiredProtocolLib);
        }

        if (sender instanceof Player && !sender.isOp() &&
            (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "This is the free version of Lib's Disguises, player commands are limited to console and " +
                "Operators only! Purchase the plugin for non-admin usage!");
            return true;
        }

        return false;
    }

    protected List<String> getTabDisguiseTypes(CommandSender sender, DisguisePermissions perms, String[] allArgs, int startsAt, String currentArg) {
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
        if (allArgs.length == startsAt + 1 && disguiseType.getType() == DisguiseType.PLAYER && !disguiseType.isCustomDisguise()) {
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

        return getTabDisguiseOptions(sender, perms, disguiseType, allArgs, startsAt + (disguiseType.isPlayer() ? 2 : 1), currentArg);
    }

    /**
     * @param perms        What permissions they can use
     * @param disguisePerm The disguise permission they're using
     * @param allArgs      All the arguments in the command
     * @param startsAt     What index this starts at
     * @return a list of viable disguise options
     */
    protected List<String> getTabDisguiseOptions(CommandSender commandSender, DisguisePermissions perms, DisguisePerm disguisePerm, String[] allArgs,
                                                 int startsAt, String currentArg) {
        ArrayList<String> usedOptions = new ArrayList<>();

        WatcherMethod[] methods = ParamInfoManager.getDisguiseWatcherMethods(disguisePerm.getWatcherClass());

        // Find which methods the disguiser has already used
        for (int i = startsAt; i < allArgs.length; i++) {
            for (WatcherMethod method : methods) {
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

    protected List<String> getTabDisguiseSubOptions(CommandSender commandSender, DisguisePermissions perms, DisguisePerm disguisePerm, String[] allArgs,
                                                    int startsAt, String currentArg) {
        boolean addMethods = true;
        List<String> tabs = new ArrayList<>();

        ParamInfo info = null;
        String methodName = null;

        if (allArgs.length == startsAt) {
            if (disguisePerm.getType() == DisguiseType.FALLING_BLOCK) {
                info = ParamInfoManager.getParamInfoItemBlock();
                methodName = "setBlock";
            } else if (disguisePerm.getType() == DisguiseType.DROPPED_ITEM) {
                info = ParamInfoManager.getParamInfo(ItemStack.class);
                methodName = "setItemstack";
            }
        } else if (allArgs.length > startsAt) {
            // Check what argument was used before the current argument to see what we're displaying

            String prevArg = allArgs[allArgs.length - 1];

            info = ParamInfoManager.getParamInfo(disguisePerm, prevArg);
            methodName = prevArg;

            if (info != null && !info.isParam(boolean.class)) {
                addMethods = false;
            }

            // Enderman can't hold non-blocks
            if (disguisePerm.getType() == DisguiseType.ENDERMAN && prevArg.equalsIgnoreCase("setItemInMainHand")) {
                info = ParamInfoManager.getParamInfoItemBlock();
            }
        }

        // If the previous argument is a method
        if (info != null) {
            Collection<String> wantToUse = null;

            // If there is a list of default values
            if (info.hasValues()) {
                wantToUse = info.getEnums(currentArg);
            } else if (info.isParam(String.class)) {
                wantToUse = new ArrayList<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    // If command user cannot see player online, don't tab-complete name
                    if (commandSender instanceof Player && !((Player) commandSender).canSee(player)) {
                        continue;
                    }

                    wantToUse.add(player.getName());
                }
            }

            if (wantToUse != null) {
                HashMap<String, HashMap<String, Boolean>> allowedOptions = DisguisePermissions.getDisguiseOptions(commandSender, getPermNode(), disguisePerm);
                HashMap<String, Boolean> methodValues = allowedOptions.get(methodName.toLowerCase(Locale.ENGLISH));

                if (methodValues != null) {
                    for (String value : wantToUse) {
                        if (!DisguisePermissions.hasMethodOption(methodValues, value)) {
                            continue;
                        }

                        tabs.add(value);
                    }
                } else {
                    tabs.addAll(wantToUse);
                }
            }
        }

        if (addMethods) {
            // If this is a method, add. Else if it can be a param of the previous argument, add.
            for (WatcherMethod method : ParamInfoManager.getDisguiseWatcherMethods(disguisePerm.getWatcherClass())) {
                if (!perms.isAllowedDisguise(disguisePerm, Collections.singletonList(method.getName())) || !method.isUsable(disguisePerm.getType())) {
                    continue;
                }

                tabs.add(method.getName());
            }
        }

        return tabs;
    }

    protected List<String> filterTabs(List<String> list, String[] origArgs) {
        if (origArgs.length == 0) {
            return list;
        }

        Iterator<String> itel = list.iterator();
        String label = origArgs[origArgs.length - 1].toLowerCase(Locale.ENGLISH);

        while (itel.hasNext()) {
            String name = itel.next();

            if (name.toLowerCase(Locale.ENGLISH).startsWith(label)) {
                continue;
            }

            itel.remove();
        }

        return new ArrayList<>(new HashSet<>(list));
    }

    protected String getDisplayName(CommandSender player) {
        String name = DisguiseConfig.getNameAboveDisguise().replace("%simple%", player.getName());

        if (name.contains("%complex%")) {
            name = name.replace("%complex%", DisguiseUtilities.getDisplayName(player));
        }

        return DisguiseUtilities.translateAlternateColorCodes(name);
    }

    protected ArrayList<String> getAllowedDisguises(DisguisePermissions permissions) {
        ArrayList<String> allowedDisguises = new ArrayList<>();

        for (DisguisePerm type : permissions.getAllowed()) {
            if (type.isUnknown()) {
                continue;
            }

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

            if (s.trim().isEmpty()) {
                continue;
            }

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
        } catch (Exception ex) {
            return false;
        }
    }

    protected abstract void sendCommandUsage(CommandSender sender, DisguisePermissions disguisePermissions);
}
