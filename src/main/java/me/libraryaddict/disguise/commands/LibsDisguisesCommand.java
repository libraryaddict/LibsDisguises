package me.libraryaddict.disguise.commands;

import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LibsDisguisesCommand implements CommandExecutor, TabCompleter {
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

        return list;
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            LibsDisguises disguises = LibsDisguises.getInstance();

            String version = disguises.getDescription().getVersion();

            if (!disguises.isReleaseBuild()) {
                version += "-";

                if (disguises.isNumberedBuild()) {
                    version += "b";
                }

                version += disguises.getBuildNo();
            }

            sender.sendMessage(ChatColor.DARK_GREEN + "This server is running " + "Lib's Disguises v" + version +
                    " by libraryaddict, formerly maintained by Byteflux and NavidK0." +
                    (sender.hasPermission("libsdisguises.reload") ?
                            "\nUse " + ChatColor.GREEN + "/libsdisguises " + "reload" + ChatColor.DARK_GREEN +
                                    " to reload the config. All disguises will be blown by doing this" + "." : ""));

            if (LibsPremium.isPremium()) {
                sender.sendMessage(ChatColor.DARK_GREEN + "This server supports the plugin developer!");
            }
        } else if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("libsdisguises.reload")) {
                    sender.sendMessage(LibsMsg.NO_PERM.get());
                    return true;
                }

                DisguiseConfig.loadConfig();
                sender.sendMessage(LibsMsg.RELOADED_CONFIG.get());
                return true;
            } else if (args[0].equalsIgnoreCase("scoreboard") || args[0].equalsIgnoreCase("board") ||
                    args[0].equalsIgnoreCase("teams")) {
                if (!sender.hasPermission("libsdisguises.scoreboardtest")) {
                    sender.sendMessage(LibsMsg.NO_PERM.get());
                    return true;
                }

                if (DisguiseConfig.getPushingOption() == DisguiseConfig.DisguisePushing.IGNORE_SCOREBOARD) {
                    sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_DISABLED.get());
                }

                Player player;

                if (args.length > 1) {
                    player = Bukkit.getPlayer(args[1]);

                    if (player == null) {
                        sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[1]));
                        return true;
                    }

                    if (!DisguiseAPI.isDisguised(player)) {
                        sender.sendMessage(LibsMsg.DMODPLAYER_NODISGUISE.get(player.getName()));
                        return true;
                    }
                } else if (sender instanceof Player) {
                    player = (Player) sender;

                    if (!DisguiseAPI.isDisguised(player)) {
                        sender.sendMessage(LibsMsg.NOT_DISGUISED.get());
                        return true;
                    }
                } else {
                    sender.sendMessage(LibsMsg.NO_CONSOLE.get());
                    return true;
                }

                Scoreboard board = player.getScoreboard();

                Team team = board.getEntryTeam(sender.getName());

                if (team == null) {
                    sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_NO_TEAM.get());
                    return true;
                }

                if (team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.NEVER &&
                        team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.FOR_OTHER_TEAMS) {
                    sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_NO_TEAM_PUSH.get(team.getName()));
                    return true;
                }

                sender.sendMessage(LibsMsg.LIBS_SCOREBOARD_SUCCESS.get(team.getName()));
                return true;
            } else if (args[0].equalsIgnoreCase("permtest")) {
                if (!sender.hasPermission("libsdisguises.permtest")) {
                    sender.sendMessage(LibsMsg.NO_PERM.get());
                    return true;
                }

                Permissible player;

                if (args.length > 1) {
                    player = Bukkit.getPlayer(args[1]);

                    if (player == null) {
                        sender.sendMessage(LibsMsg.CANNOT_FIND_PLAYER.get(args[1]));
                        return true;
                    }
                } else {
                    player = sender;
                }

                DisguisePermissions permissions = new DisguisePermissions(player, "disguise");
                sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_INFO_1.get());
                sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_INFO_2.get());

                if (player.hasPermission("libsdisguises.disguise.pig")) {
                    sender.sendMessage(LibsMsg.NORMAL_PERM_CHECK_SUCCESS.get());

                    if (permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.PIG))) {
                        sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_SUCCESS.get());
                    } else {
                        sender.sendMessage(LibsMsg.LIBS_PERM_CHECK_FAIL.get());
                    }
                } else {
                    sender.sendMessage(LibsMsg.NORMAL_PERM_CHECK_FAIL.get());
                }
            } else if (args[0].equalsIgnoreCase("json") || args[0].equalsIgnoreCase("gson") ||
                    args[0].equalsIgnoreCase("item") || args[0].equalsIgnoreCase("parse") ||
                    args[0].equalsIgnoreCase("tostring")) {
                if (!sender.hasPermission("libsdisguises.json")) {
                    sender.sendMessage(LibsMsg.NO_PERM.get());
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(LibsMsg.NO_CONSOLE.get());
                    return true;
                }

                ItemStack item = ((Player) sender).getInventory().getItemInMainHand();

                String gson = DisguiseUtilities.getGson().toJson(item);
                String simple = ParamInfoManager.toString(item);

                // item{nbt} amount
                // item amount data {nbt}

                String itemName = ReflectionManager.getItemName(item.getType());
                ArrayList<String> mcArray = new ArrayList<>();

                if (NmsVersion.v1_13.isSupported() && item.hasItemMeta()) {
                    mcArray.add(itemName + DisguiseUtilities.serialize(NbtFactory.fromItemTag(item)));
                } else {
                    mcArray.add(itemName);
                }

                if (item.getAmount() != 1) {
                    mcArray.add(String.valueOf(item.getAmount()));
                }

                if (!NmsVersion.v1_13.isSupported()) {
                    if (item.getDurability() != 0) {
                        mcArray.add(String.valueOf(item.getDurability()));
                    }

                    if (item.hasItemMeta()) {
                        mcArray.add(DisguiseUtilities.serialize(NbtFactory.fromItemTag(item)));
                    }
                }

                String ldItem = StringUtils.join(mcArray, "-");
                String mcItem = StringUtils.join(mcArray, " ");

                sendMessage(sender, LibsMsg.ITEM_SERIALIZED, LibsMsg.ITEM_SERIALIZED_NO_COPY, gson);

                if (!gson.equals(simple) && !ldItem.equals(simple) && !mcItem.equals(simple)) {
                    sendMessage(sender, LibsMsg.ITEM_SIMPLE_STRING, LibsMsg.ITEM_SIMPLE_STRING_NO_COPY, simple);
                }

                sendMessage(sender, LibsMsg.ITEM_SERIALIZED_MC, LibsMsg.ITEM_SERIALIZED_MC_NO_COPY, mcItem);

                if (mcArray.size() > 1) {
                    sendMessage(sender, LibsMsg.ITEM_SERIALIZED_MC, LibsMsg.ITEM_SERIALIZED_MC_NO_COPY, ldItem);
                }
            } else if (args[0].equalsIgnoreCase("metainfo") || args[0].equalsIgnoreCase("meta")) {
                if (!sender.hasPermission("libsdisguises.metainfo")) {
                    sender.sendMessage(LibsMsg.NO_PERM.get());
                    return true;
                }

                if (args.length > 1) {
                    MetaIndex index = MetaIndex.getMetaIndexByName(args[1]);

                    if (index == null) {
                        sender.sendMessage(LibsMsg.META_NOT_FOUND.get());
                        return true;
                    }

                    sender.sendMessage(index.toString());
                } else {
                    ArrayList<String> names = new ArrayList<>();

                    for (MetaIndex index : MetaIndex.values()) {
                        names.add(MetaIndex.getName(index));
                    }

                    names.sort(String::compareToIgnoreCase);

                    if (NmsVersion.v1_13.isSupported()) {
                        ComponentBuilder builder = new ComponentBuilder("").appendLegacy(LibsMsg.META_VALUES.get());

                        Iterator<String> itel = names.iterator();

                        while (itel.hasNext()) {
                            String name = itel.next();

                            builder.appendLegacy(name);
                            builder.event(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd.getName() + " metainfo " + name));
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ComponentBuilder("").appendLegacy(LibsMsg.META_CLICK_SHOW.get(name)).create()));

                            if (itel.hasNext()) {
                                builder.appendLegacy(LibsMsg.META_VALUE_SEPERATOR.get());
                            }
                        }

                        sender.spigot().sendMessage(builder.create());
                    } else {
                        sender.sendMessage(LibsMsg.META_VALUES_NO_CLICK
                                .get(StringUtils.join(names, LibsMsg.META_VALUE_SEPERATOR.get())));
                    }
                }
            } else {
                sender.sendMessage(LibsMsg.LIBS_COMMAND_WRONG_ARG.get());
            }
        }
        return true;
    }

    private void sendMessage(CommandSender sender, LibsMsg prefix, LibsMsg oldVer, String string) {
        if (!NmsVersion.v1_13.isSupported()) {
            sender.sendMessage(oldVer.get(string));
            return;
        }

        int start = 0;
        int msg = 1;

        ComponentBuilder builder = new ComponentBuilder("").appendLegacy(prefix.get());

        while (start < string.length()) {
            int end = Math.min(256, string.length() - start);

            String sub = string.substring(start, start + end);

            builder.append(" ");

            if (string.length() <= 256) {
                builder.appendLegacy(LibsMsg.CLICK_TO_COPY_DATA.get());
            } else {
                builder.reset();
                builder.appendLegacy(LibsMsg.CLICK_COPY.get(msg));
            }

            start += end;

            builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, sub));
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(LibsMsg.CLICK_TO_COPY_HOVER.get() + (string.length() <= 256 ? "" : " " + msg))
                            .create()));
            msg += 1;
        }

        sender.spigot().sendMessage(builder.create());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getArgs(origArgs);

        if (args.length == 0)
            tabs.addAll(Arrays.asList("reload", "scoreboard", "permtest", "json", "metainfo"));

        return filterTabs(tabs, origArgs);
    }
}
