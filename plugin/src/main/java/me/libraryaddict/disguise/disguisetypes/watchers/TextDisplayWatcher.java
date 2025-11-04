package me.libraryaddict.disguise.disguisetypes.watchers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

public class TextDisplayWatcher extends DisplayWatcher {
    public TextDisplayWatcher(Disguise disguise) {
        super(disguise);

        setBillboard(Display.Billboard.CENTER);
        setText("Text Display");
    }

    public String getText() {
        return DisguiseUtilities.getSimpleString(getAdvText());
    }

    public void setText(String string) {
        if (DisguiseUtilities.isPlaceholderApi() && string != null && string.contains("%")) {
            string = PlaceholderAPI.setPlaceholders(getDisguise().getEntity() instanceof Player ? (Player) getDisguise().getEntity() : null,
                string);
        }

        setAdvText(DisguiseUtilities.getAdventureChat(string));
    }

    public Component getAdvText() {
        return getData(MetaIndex.TEXT_DISPLAY_TEXT);
    }

    public void setAdvText(Component component) {
        sendData(MetaIndex.TEXT_DISPLAY_TEXT, component);
    }

    public int getLineWidth() {
        return getData(MetaIndex.TEXT_DISPLAY_LINE_WIDTH);
    }

    public void setLineWidth(int width) {
        sendData(MetaIndex.TEXT_DISPLAY_LINE_WIDTH, width);
    }

    public Color getBackgroundColor() {
        int color = getData(MetaIndex.TEXT_DISPLAY_BACKGROUND_COLOR);

        if (color == -1) {
            return null;
        }

        return Color.fromARGB(color);
    }

    public void setBackgroundColor(Color color) {
        sendData(MetaIndex.TEXT_DISPLAY_BACKGROUND_COLOR, color != null ? color.asARGB() : null);
    }

    public byte getTextOpacity() {
        return getData(MetaIndex.TEXT_DISPLAY_TEXT_OPACITY);
    }

    public void setTextOpacity(byte opacity) {
        if (opacity < -1 || opacity > 255) {
            return;
        }

        sendData(MetaIndex.TEXT_DISPLAY_TEXT_OPACITY, opacity);
    }

    public boolean isShadowed() {
        return this.getFlag(1);
    }

    public void setShadowed(boolean shadow) {
        this.setFlag(1, shadow);
    }

    public boolean isSeeThrough() {
        return this.getFlag(2);
    }

    public void setSeeThrough(boolean seeThrough) {
        this.setFlag(2, seeThrough);
    }

    public boolean isDefaultBackground() {
        return this.getFlag(4);
    }

    public void setDefaultBackground(boolean defaultBackground) {
        this.setFlag(4, defaultBackground);
    }

    public TextDisplay.TextAlignment getAlignment() {
        int flags = getData(MetaIndex.TEXT_DISPLAY_FLAGS);

        if ((flags & 8) != 0) {
            return TextDisplay.TextAlignment.LEFT;
        } else {
            return (flags & 16) != 0 ? TextDisplay.TextAlignment.RIGHT : TextDisplay.TextAlignment.CENTER;
        }
    }

    public void setAlignment(TextDisplay.TextAlignment alignment) {
        if (alignment == null) {
            sendData(MetaIndex.TEXT_DISPLAY_FLAGS, null);
            return;
        }

        switch (alignment.ordinal()) {
            case 0:
                this.setFlag(8, false);
                this.setFlag(16, false);
                break;
            case 1:
                this.setFlag(8, true);
                this.setFlag(16, false);
                break;
            case 2:
                this.setFlag(8, false);
                this.setFlag(16, true);
                break;
            default:
                throw new IllegalArgumentException("Unknown alignment " + alignment);
        }
    }

    private boolean getFlag(int flag) {
        return (getData(MetaIndex.TEXT_DISPLAY_FLAGS) & flag) != 0;
    }

    private void setFlag(int flag, boolean set) {
        byte flagBits = getData(MetaIndex.TEXT_DISPLAY_FLAGS);
        if (set) {
            flagBits = (byte) (flagBits | flag);
        } else {
            flagBits = (byte) (flagBits & ~flag);
        }

        sendData(MetaIndex.TEXT_DISPLAY_FLAGS, flagBits);
    }
}
