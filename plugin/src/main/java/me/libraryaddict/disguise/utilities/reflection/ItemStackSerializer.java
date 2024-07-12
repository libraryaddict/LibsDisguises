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
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemStackSerializer {

    public static List<String> serialize(ItemStack item) {
        // If its not a CraftItemStack
        if (!ReflectionManager.isCraftItem(item) && item.hasItemMeta()) {
            item = ReflectionManager.getCraftItem(item);
        }

        List<String> mcArray = new ArrayList<>();
        String type = ReflectionManager.getItemName(item.getType());

        if (item.hasItemMeta() && NmsVersion.v1_13.isSupported()) {
            if (NmsVersion.v1_20_R4.isSupported()) {
                Object asJava = ReflectionManager.getNmsReflection().serializeComponents(item);

                if (asJava != null) {
                    String asString = serializeObj(asJava);

                    if (asString.length() > 2) {
                        type += "[" + asString.substring(1, asString.length() - 1) + "]";
                    }
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

    public static String serialize(NBT base) {
        return serialize(0, base);
    }

    private static String serializeObj(Object object) {
        return serializeObj(0, object);
    }

    private static String serializeObj(int depth, Object object) {
        if (object instanceof Map) {
            StringBuilder builder = new StringBuilder();

            builder.append("{");

            for (Map.Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) {
                String val = serializeObj(depth + 1, entry.getValue());

                // Skip root empty values
                if (depth == 0 && val.matches("0(\\.0)?")) {
                    continue;
                }

                if (builder.length() > 1) {
                    builder.append(",");
                }

                builder.append(entry.getKey()).append("=").append(val);
            }

            builder.append("}");

            return builder.toString();
        } else if (object instanceof ByteList) {
            ByteList byteArray = (ByteList) object;
            List<String> bytes = new ArrayList<>();

            for (byte b : byteArray) {
                bytes.add(String.valueOf(b));
            }

            return "[B;" + String.join(",", bytes) + "]";
        } else if (object instanceof IntList) {
            IntList byteArray = (IntList) object;
            List<String> bytes = new ArrayList<>();

            for (int b : byteArray) {
                bytes.add(String.valueOf(b));
            }

            return "[I;" + String.join(",", bytes) + "]";
        } else if (object instanceof LongList) {
            LongList byteArray = (LongList) object;
            List<String> bytes = new ArrayList<>();

            for (long b : byteArray) {
                bytes.add(String.valueOf(b));
            }

            return "[L;" + String.join(",", bytes) + "]";
        } else if (object instanceof List) {
            List<String> serialized = new ArrayList<>();

            for (Object obj : ((List) object)) {
                serialized.add(serializeObj(depth + 1, obj));
            }

            return "[" + StringUtils.join(serialized, ",") + "]";
        } else if (object instanceof Number) {
            return object.toString();
        } else if (object instanceof String) {
            if (((String) object).contains("\"") && !((String) object).contains("'")) {
                return "'" + object + "'";
            }

            return "\"" + ((String) object).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else {
            throw new IllegalArgumentException();
        }
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
