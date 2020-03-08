package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisguiseHelpCommand extends DisguiseBaseCommand implements TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (String node : getCommandNames().values()) {
            DisguisePermissions perms = DisguiseParser.getPermissions(sender, node);

            if (!perms.hasPermissions()) {
                continue;
            }

            if (args.length == 0) {
                sendCommandUsage(sender, null);
                return true;
            } else {
                ParamInfo help = null;

                for (ParamInfo s : ParamInfoManager.getParamInfos()) {
                    String name = s.getName().replaceAll(" ", "");

                    if (args[0].equalsIgnoreCase(name) || args[0].equalsIgnoreCase(name + "s")) {
                        help = s;
                        break;
                    }
                }

                if (help != null) {
                    if (help.hasValues() && help.canTranslateValues()) {
                        sender.sendMessage(LibsMsg.DHELP_HELP4.get(help.getName(),
                                StringUtils.join(help.getEnums(""), LibsMsg.DHELP_HELP4_SEPERATOR.get())));
                    } else {
                        if (!help.getName().equals(help.getDescriptiveName())) {
                            sender.sendMessage(LibsMsg.DHELP_HELP6
                                    .get(help.getName(), help.getDescriptiveName(), help.getDescription()));
                        } else {
                            sender.sendMessage(LibsMsg.DHELP_HELP5.get(help.getName(), help.getDescription()));
                        }
                    }

                    return true;
                }

                DisguisePerm type = DisguiseParser.getDisguisePerm(args[0]);

                if (type == null) {
                    sender.sendMessage(LibsMsg.DHELP_CANTFIND.get(args[0]));
                    return true;
                }

                if (!perms.isAllowedDisguise(type)) {
                    sender.sendMessage(LibsMsg.NO_PERM_DISGUISE.get());
                    return true;
                }

                ArrayList<String> methods = new ArrayList<>();
                Class watcher = type.getWatcherClass();
                int ignored = 0;

                try {
                    for (Method method : ParamInfoManager.getDisguiseWatcherMethods(watcher)) {
                        if (args.length < 2 || !args[1].equalsIgnoreCase(LibsMsg.DHELP_SHOW.get())) {
                            if (!perms.isAllowedDisguise(type, Collections.singleton(method.getName().toLowerCase()))) {
                                ignored++;
                                continue;
                            }
                        }

                        ParamInfo info = ParamInfoManager.getParamInfo(method);

                        int value = ParamInfoManager.getValue(method);
                        ChatColor methodColor = ChatColor.YELLOW;

                        if (value == 1) {
                            methodColor = ChatColor.AQUA;
                        } else if (value == 2) {
                            methodColor = ChatColor.GRAY;
                        }

                        String str = TranslateType.DISGUISE_OPTIONS.get(method.getName()) + ChatColor.DARK_RED + "(" +
                                ChatColor.GREEN + info.getName() + ChatColor.DARK_RED + ")";

                        methods.add(methodColor + str);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (methods.isEmpty()) {
                    methods.add(LibsMsg.DHELP_NO_OPTIONS.get());
                }

                sender.sendMessage(LibsMsg.DHELP_OPTIONS.get(ChatColor.DARK_RED + type.toReadable(),
                        StringUtils.join(methods, ChatColor.DARK_RED + ", ")));

                if (ignored > 0) {
                    sender.sendMessage(LibsMsg.NO_PERMS_USE_OPTIONS.get(ignored));
                }

                return true;
            }
        }

        sender.sendMessage(LibsMsg.NO_PERM.get());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] origArgs) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] args = getPreviousArgs(origArgs);

        for (String node : getCommandNames().values()) {
            DisguisePermissions perms = DisguiseParser.getPermissions(sender, node);

            if (args.length == 0) {
                for (DisguisePerm type : perms.getAllowed()) {
                    if (type.isUnknown())
                        continue;

                    tabs.add(type.toReadable().replaceAll(" ", "_"));
                }

                for (ParamInfo s : ParamInfoManager.getParamInfos()) {
                    tabs.add(s.getName().replaceAll(" ", ""));
                }
            } else if (DisguiseParser.getDisguisePerm(args[0]) == null) {
                tabs.add(LibsMsg.DHELP_SHOW.get());
            }
        }

        return filterTabs(tabs, origArgs);
    }

    /**
     * Send the player the information
     */
    @Override
    protected void sendCommandUsage(CommandSender sender, DisguisePermissions permissions) {
        sender.sendMessage(LibsMsg.DHELP_HELP1.get());
        sender.sendMessage(LibsMsg.DHELP_HELP2.get());

        for (ParamInfo s : ParamInfoManager.getParamInfos()) {
            sender.sendMessage(LibsMsg.DHELP_HELP3.get(s.getName().replaceAll(" ", "") +
                            (!s.getName().equals(s.getDescriptiveName()) ? " ~ " + s.getDescriptiveName() : ""),
                    s.getDescription()));
        }
    }
}
