package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDMetaInfo implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("metainfo", "metadata", "metadatainfo", "metaindex");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.metainfo";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length > 1) {
            MetaIndex index = MetaIndex.getMetaIndexByName(args[1]);

            if (index == null) {
                LibsMsg.META_NOT_FOUND.send(sender);
                return;
            }

            sender.sendMessage(index.toString());
        } else {
            ArrayList<String> names = new ArrayList<>();

            for (MetaIndex index : MetaIndex.values()) {
                names.add(MetaIndex.getName(index));
            }

            names.sort(String::compareToIgnoreCase);

           // if (NmsVersion.v1_13.isSupported()) {
                ComponentBuilder builder = new ComponentBuilder("").appendLegacy(LibsMsg.META_VALUES.get());

                Iterator<String> itel = names.iterator();

                while (itel.hasNext()) {
                    String name = itel.next();

                    builder.appendLegacy(name);
                    builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/libsdisguises metainfo " + name));
                    builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("").appendLegacy(LibsMsg.META_CLICK_SHOW.get(name)).create()));

                    if (itel.hasNext()) {
                        builder.appendLegacy(LibsMsg.META_VALUE_SEPERATOR.get());
                    }
                }

                sender.spigot().sendMessage(builder.create());
            /*} else {
                LibsMsg.META_VALUES_NO_CLICK.send(sender,
                        StringUtils.join(names, LibsMsg.META_VALUE_SEPERATOR.get()));
            }*/
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_METAINFO;
    }
}
