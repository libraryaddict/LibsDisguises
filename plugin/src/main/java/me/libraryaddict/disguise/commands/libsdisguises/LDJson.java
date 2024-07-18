package me.libraryaddict.disguise.commands.libsdisguises;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.reflection.ItemStackSerializer;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LDJson implements LDCommand {
    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("json", "gson", "tostring", "item", "parse");
    }

    @Override
    public String getPermission() {
        return "libsdisguises.json";
    }

    private Component createComponent(String text, int section, int max) {
        Component component = LibsMsg.CLICK_COPY.getAdv(section);

        if (NmsVersion.v1_15.isSupported()) {
            component = component.clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(text));
        } else {
            component = component.clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand(text));
        }

        LibsMsg hover = NmsVersion.v1_15.isSupported() ? LibsMsg.CLICK_TO_COPY_HOVER_CLIPBOARD : LibsMsg.CLICK_TO_COPY_HOVER;
        Component hoverText = hover.getAdv(section, max, DisguiseUtilities.getMiniMessage().escapeTags(text));
        component = component.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(hoverText));

        return component;
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
        List<String> mcArray = ItemStackSerializer.serialize(item);

        String ldItem = StringUtils.join(mcArray, "-");
        String mcItem = StringUtils.join(mcArray, " ");

        sendMessage(sender, LibsMsg.ITEM_SERIALIZED, LibsMsg.ITEM_SERIALIZED_NO_COPY, gson, false);

        if (!gson.equals(simple) && !ldItem.equals(simple) && !mcItem.equals(simple)) {
            sendMessage(sender, LibsMsg.ITEM_SIMPLE_STRING, LibsMsg.ITEM_SIMPLE_STRING_NO_COPY, simple, false);
        }

        sendMessage(sender, LibsMsg.ITEM_SERIALIZED_MC, LibsMsg.ITEM_SERIALIZED_MC_NO_COPY, mcItem, false);

        if (mcArray.size() > 1) {
            sendMessage(sender, LibsMsg.ITEM_SERIALIZED_MC, LibsMsg.ITEM_SERIALIZED_MC_NO_COPY, ldItem, false);
        }
    }

    private void sendMessage(CommandSender sender, LibsMsg msg, LibsMsg oldVer, String string, boolean forceAbbrev) {
        TextComponent.Builder builder = Component.text().append(msg.getAdv()).appendSpace();

        if (string.length() > 256 || forceAbbrev) {
            String[] split = DisguiseUtilities.split(string);

            // Because the splitter removes the quotes..
            for (int i = 0; i < split.length; i++) {
                split[i] = DisguiseUtilities.quote(split[i]);
            }

            for (int i = 0; i < split.length; i++) {
                if (split[i].length() <= 256) {
                    continue;
                }

                split = Arrays.copyOf(split, split.length + 1);

                for (int a = split.length - 1; a > i; a--) {
                    split[a] = split[a - 1];
                }

                split[i + 1] = split[i].substring(256);
                split[i] = split[i].substring(0, 256);
            }

            List<String> sections = new ArrayList<>();
            StringBuilder current = new StringBuilder();

            for (int i = 0; i < split.length; i++) {
                if (current.length() > 0) {
                    current.append(" ");
                }

                current.append(split[i]);

                // If the next split would fit
                if (split.length > i + 1 && split[i + 1].length() + current.length() + 1 <= 256) {
                    continue;
                }

                // Have non-final end with a space
                if (i + 1 < split.length) {
                    current.append(" ");
                }

                sections.add(current.toString());
                current = new StringBuilder();
            }

            for (int i = 0; i < sections.size(); i++) {
                if (i > 0) {
                    builder.appendSpace();
                }

                builder.append(createComponent(sections.get(i), i + 1, sections.size()));
            }
        } else {
            builder.append(createComponent(string, 1, 1));
        }

        DisguiseUtilities.sendMessage(sender, builder.build());
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
