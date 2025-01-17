package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByteArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTIntArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTLongArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackSerializer {

    public static List<String> serialize(ItemStack item) {
        // If its not a CraftItemStack
        if (!ReflectionManager.isCraftItem(item) && item.hasItemMeta()) {
            item = ReflectionManager.getCraftItem(item);
        }

        List<String> mcArray = new ArrayList<>();
        String type = ReflectionManager.getItemName(item.getType());

        if (item.hasItemMeta() && NmsVersion.v1_13.isSupported()) {
            if (ReflectionManager.getNmsReflection() != null) {
                String asString = ReflectionManager.getNmsReflection().getDataAsString(item);

                if (asString != null && asString.length() > 2) {
                    // Vanilla seems to turn this into a string that contains for UUID
                    // : [I; 772059800,
                    // And there should be no space, so we must strip all spaces that are unneeded.
                    asString = stripSpacesFromString(asString);

                    type += "[" + asString.substring(1, asString.length() - 1) + "]";
                }
            } else {
                NBT nbt = DisguiseUtilities.fromBukkitItemStack(item).getNBT();

                if (nbt != null) {
                    String asString = serialize(nbt);

                    if (asString.length() > 2) {
                        type += asString;
                    }
                }
            }
        }

        mcArray.add(type);

        if (item.getAmount() != 1) {
            mcArray.add(String.valueOf(item.getAmount()));
        }

        if (!NmsVersion.v1_13.isSupported()) {
            if (item.getDurability() != 0) {
                mcArray.add(String.valueOf(item.getDurability()));
            }

            if (item.hasItemMeta()) {
                NBT nbt = DisguiseUtilities.fromBukkitItemStack(item).getNBT();

                if (nbt != null) {
                    String asString = serialize(nbt);

                    if (asString.length() > 2) {
                        mcArray.add(serialize(nbt));
                    }
                }
            }
        }

        return mcArray;
    }

    private static String stripSpacesFromString(String string) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        boolean escaped = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inQuote = !inQuote;
            } else if (!inQuote && c == ' ') {
                continue; // Skip spaces outside quotes
            }

            result.append(c);
        }

        return result.toString();
    }

    public static String serialize(NBT base) {
        return serialize(0, base);
    }

    private static String serialize(int depth, NBT base) {
        if (base.getType() == NBTType.COMPOUND) {
            StringBuilder builder = new StringBuilder();

            builder.append("{");

            for (String key : ((NBTCompound) base).getTagNames()) {
                NBT nbt = ((NBTCompound) base).getTagOrThrow(key);
                String val = serialize(depth + 1, nbt);

                // Skip root empty values
                if (depth == 0 && val.matches("0(\\.0)?")) {
                    continue;
                }

                if (builder.length() > 1) {
                    builder.append(",");
                }

                builder.append(key).append(":").append(val);
            }

            builder.append("}");

            return builder.toString();
        } else if (base.getType() == NBTType.LIST) {
            List<String> serialized = new ArrayList<>();

            for (NBT something : ((NBTList<NBT>) base).getTags()) {
                serialized.add(serialize(depth + 1, something));
            }

            return "[" + StringUtils.join(serialized, ",") + "]";
        } else if (base.getType() == NBTType.BYTE_ARRAY) {
            NBTByteArray byteArray = (NBTByteArray) base;
            List<String> bytes = new ArrayList<>();

            for (byte b : byteArray.getValue()) {
                bytes.add(String.valueOf(b));
            }

            return "[B;" + String.join(",", bytes) + "]";
        }
        if (base.getType() == NBTType.INT_ARRAY) {
            NBTIntArray byteArray = (NBTIntArray) base;
            List<String> bytes = new ArrayList<>();

            for (int b : byteArray.getValue()) {
                bytes.add(String.valueOf(b));
            }

            return "[I;" + String.join(",", bytes) + "]";
        }
        if (base.getType() == NBTType.LONG_ARRAY) {
            NBTLongArray byteArray = (NBTLongArray) base;
            List<String> bytes = new ArrayList<>();

            for (long b : byteArray.getValue()) {
                bytes.add(String.valueOf(b));
            }

            return "[L;" + String.join(",", bytes) + "]";
        } else if (base.getType() == NBTType.BYTE || base.getType() == NBTType.INT || base.getType() == NBTType.LONG ||
            base.getType() == NBTType.FLOAT || base.getType() == NBTType.SHORT || base.getType() == NBTType.DOUBLE) {
            NBTNumber number = (NBTNumber) base;
            return number.getAsNumber().toString();
        } else if (base.getType() == NBTType.STRING) {
            String val = ((NBTString) base).getValue();

            return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else if (base.getType() == NBTType.END) {
            return "";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
