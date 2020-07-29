package me.libraryaddict.disguise.commands.libsdisguises;

import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by libraryaddict on 20/04/2020.
 */
public class LDJson implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("json", "gson", "tostring", "item", "parse");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.json";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            LibsMsg.NO_CONSOLE.send(sender);
            return;
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
    }

    private void sendMessage(CommandSender sender, LibsMsg prefix, LibsMsg oldVer, String string) {
       /* if (!NmsVersion.v1_13.isSupported()) {
            oldVer.send(sender, string);
            return;
        }*/

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
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    @Override
    public LibsMsg getHelp() {
        return LibsMsg.LD_COMMAND_JSON;
    }
}
