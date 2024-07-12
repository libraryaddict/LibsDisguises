package me.libraryaddict.disguise.commands.utils;

import me.libraryaddict.disguise.commands.DisguiseBaseCommand;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.parser.DisguisePerm;
import me.libraryaddict.disguise.utilities.parser.DisguisePermissions;
import me.libraryaddict.disguise.utilities.parser.WatcherMethod;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import me.libraryaddict.disguise.utilities.translations.TranslateType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
                    if (help.hasTabCompletion() && help.canTranslateValues()) {
                        LibsMsg.DHELP_HELP4.send(sender, help.getName(),
                            StringUtils.join(help.getEnums(""), LibsMsg.DHELP_HELP4_SEPERATOR.get()));
                    } else {
                        if (!help.getName().equals(help.getDescriptiveName())) {
                            LibsMsg.DHELP_HELP6.send(sender, help.getName(), help.getDescriptiveName(), help.getDescription());
                        } else {
                            LibsMsg.DHELP_HELP5.send(sender, help.getName(), help.getDescription());
                        }
                    }

                    return true;
                }

                DisguisePerm type = DisguiseParser.getDisguisePerm(args[0]);

                if (type == null) {
                    LibsMsg.DHELP_CANTFIND.send(sender, args[0]);
                    return true;
                }

                if (!perms.isAllowedDisguise(type)) {
                    LibsMsg.NO_PERM_DISGUISE.send(sender);
                    return true;
                }

                ArrayList<String> methods = new ArrayList<>();
                Class watcher = type.getWatcherClass();
                int ignored = 0;

                try {
                    for (WatcherMethod method : ParamInfoManager.getDisguiseWatcherMethods(watcher)) {
                        if (method.isHidden(type.getType())) {
                            continue;
                        }

                        if (args.length < 2 || !args[1].equalsIgnoreCase(LibsMsg.DHELP_SHOW.get())) {
                            if (!perms.isAllowedDisguise(type, Collections.singleton(method.getMappedName().toLowerCase(Locale.ENGLISH)))) {
                                ignored++;
                                continue;
                            }
                        }

                        ParamInfo info = ParamInfoManager.getParamInfo(method);

                        int value = ParamInfoManager.getValue(method);
                        LibsMsg methodC = LibsMsg.DHELP_OPTIONS_METHOD_VERY_SPECIFIC;

                        if (value == 1) {
                            methodC = LibsMsg.DHELP_OPTIONS_METHOD_SOMEWHAT_SPECIFIC;
                        } else if (value == 2) {
                            methodC = LibsMsg.DHELP_OPTIONS_METHOD_GENERIC;
                        }

                        String str =
                            LibsMsg.DHELP_OPTIONS_METHOD.get(methodC.get(TranslateType.DISGUISE_OPTIONS.get(method.getMappedName())),
                                info.getName());

                        methods.add(str);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (methods.isEmpty()) {
                    methods.add(LibsMsg.DHELP_NO_OPTIONS.get());
                }

                LibsMsg.DHELP_OPTIONS.send(sender, type.toReadable(), StringUtils.join(methods, LibsMsg.DHELP_OPTIONS_JOINER.getRaw()));

                if (ignored > 0) {
                    LibsMsg.NO_PERMS_USE_OPTIONS.send(sender, ignored);
                }

                return true;
            }
        }

        LibsMsg.NO_PERM.send(sender);
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
                    if (type.isUnknown()) {
                        continue;
                    }

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
        LibsMsg.DHELP_HELP1.send(sender);
        LibsMsg.DHELP_HELP2.send(sender);

        for (ParamInfo s : ParamInfoManager.getParamInfos()) {
            LibsMsg.DHELP_HELP3.send(sender,
                s.getName().replaceAll(" ", "") + (!s.getName().equals(s.getDescriptiveName()) ? " ~ " + s.getDescriptiveName() : ""),
                s.getDescription());
        }
    }
}
