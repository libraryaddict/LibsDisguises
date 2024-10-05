package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleBlockStateData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustColorTransitionData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleSculkChargeData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleShriekData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleVibrationData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.positionsource.builtin.BlockPositionSource;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ParamInfoParticle extends ParamInfoEnum {
    @Getter
    @AllArgsConstructor
    private static class ParticleInfo {
        private final ParticleType<?> type;
        private final Class<? extends ParticleData> data;
    }

    @RequiredArgsConstructor
    private static class ColorParser {
        private final String[] split;
        private final boolean decimalPointColor;
        @Getter
        private int argsConsumed;

        public float[] getColor() throws DisguiseParseException {
            int need = getArgsNeed();
            int start = split.length - (argsConsumed + need);
            String[] copyOf = Arrays.copyOfRange(split, start, start + need);

            argsConsumed += need;

            if (copyOf.length == 3) {
                return new float[]{Float.parseFloat(copyOf[0]), Float.parseFloat(copyOf[1]), Float.parseFloat(copyOf[2])};
            } else if (copyOf[0].equals("-1")) {
                return new float[]{-1, -1, -1};
            }

            Color color = ((ParamInfoColor) ParamInfoManager.getParamInfo(Color.class)).parseToColor(StringUtils.join(copyOf, ","));

            float r = color.getRed();
            float g = color.getGreen();
            float b = color.getBlue();

            if (decimalPointColor) {
                r /= 255f;
                g /= 255f;
                b /= 255f;
            }

            return new float[]{r, g, b};
        }

        public int getArgsRemaining() {
            return split.length - getArgsConsumed();
        }

        private int getArgsNeed() {
            return split[split.length - (1 + argsConsumed)].matches("-?\\d+(\\.\\d+)?") ? 3 : 1;
        }

        public boolean canConsume() {
            return getArgsRemaining() > 0 && split.length >= getArgsNeed() + getArgsConsumed();
        }
    }

    private final Material[] materials;
    private static final Map<String, ParticleInfo> particleMap = new HashMap<>();

    public ParamInfoParticle(String name, String description, Material[] materials) {
        super(com.github.retrooper.packetevents.protocol.particle.Particle.class, name, description, particleMap);

        this.materials = materials;
    }

    @SneakyThrows
    private static void fillMap() {
        for (Field field : ParticleTypes.class.getFields()) {
            int mods = field.getModifiers();

            if (!Modifier.isFinal(mods) || !Modifier.isPublic(mods) || !Modifier.isStatic(mods) ||
                field.isAnnotationPresent(Deprecated.class)) {
                continue;
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            ParameterizedType pType = (ParameterizedType) type;

            if (pType.getActualTypeArguments().length != 1 || pType.getRawType() != ParticleType.class) {
                continue;
            }

            ParticleType particleType = (ParticleType) field.get(null);
            Class<? extends ParticleData> dataClass = (Class<? extends ParticleData>) pType.getActualTypeArguments()[0];

            particleMap.put(particleType.getName().getKey(), new ParticleInfo(particleType, dataClass));
        }
    }

    @Override
    public boolean hasTabCompletion() {
        return true;
    }

    private String convertName(String oldName) {
        if (oldName.equalsIgnoreCase("ITEM_CRACK")) {
            return "ITEM";
        } else if (oldName.equalsIgnoreCase("BLOCK_CRACK")) {
            return "BLOCK";
        } else if (oldName.equalsIgnoreCase("BLOCK_DUST")) {
            return "FALLING_DUST";
        }

        return oldName;
    }

    public Set<String> getEnums(String tabComplete) {
        Set<String> enums = new HashSet<>(getValues().keySet());

        if (tabComplete.isEmpty()) {
            return enums;
        }

        String tabCompleteUpperCase = tabComplete.toUpperCase(Locale.ENGLISH);
        String[] blockDatas = new String[]{"BLOCK", "BLOCK_MARKER", "FALLING_DUST", "DUST_PILLAR"};

        for (int i = -1; i < blockDatas.length; i++) {
            String particleName = i < 0 ? "ITEM" : blockDatas[i];
            enums.remove(particleName);

            for (Material mat : materials) {
                if (!mat.isBlock() && i == -1) {
                    continue;
                }

                String name = particleName + ":" + mat.name();

                if (!name.startsWith(tabCompleteUpperCase)) {
                    continue;
                }

                enums.add(name);
            }
        }

        String startsWith = tabComplete.split(":")[0];

        if (Stream.of(blockDatas).noneMatch(s -> s.equalsIgnoreCase(startsWith))) {
            return enums;
        }

        Set<String> newEnums =
            ParamInfoManager.getParamInfo(WrappedBlockState.class).getEnums(tabComplete.replaceAll("^" + startsWith + ":?", ""));

        if (newEnums == null) {
            return enums;
        }

        newEnums.forEach(s -> {
            enums.add(startsWith + ":" + s);
        });

        return enums;
    }

    private String colorToString(int color) {
        for (Map.Entry<String, Color> entry : ParamInfoColor.getStaticColors().entrySet()) {
            Color c = entry.getValue();

            if (c.asRGB() != color) {
                continue;
            }

            return entry.getKey();
        }

        return String.valueOf(color);
    }

    private String colorToString(float red, float green, float blue) {
        int r = (int) red * 255;
        int g = (int) green * 255;
        int b = (int) blue * 255;

        for (Map.Entry<String, Color> entry : ParamInfoColor.getStaticColors().entrySet()) {
            Color c = entry.getValue();

            if (r != c.getRed() || g != c.getGreen() || b != c.getBlue()) {
                continue;
            }

            return entry.getKey();
        }

        if (red % 1 == 0 && green % 1 == 0 && blue % 1 == 0) {
            return (int) red + "," + (int) green + "," + (int) blue;
        }

        return red + "," + green + "," + blue;
    }

    @Override
    public String toString(Object object) {
        com.github.retrooper.packetevents.protocol.particle.Particle<?> particle =
            (com.github.retrooper.packetevents.protocol.particle.Particle<?>) object;

        Object data = particle.getData();
        String returns = particle.getType().getName().getKey();

        if (data != null) {
            if (data instanceof ParticleItemStackData) {
                returns += ":" + ((ParticleItemStackData) data).getItemStack().getType().getName().getKey();
            } else if (data instanceof ParticleBlockStateData) {
                returns +=
                    ":" + ParamInfoManager.getParamInfo(WrappedBlockState.class).toString(((ParticleBlockStateData) data).getBlockState());
            } else if (data instanceof ParticleDustData) {
                ParticleDustData dust = (ParticleDustData) data;

                if (dust.getScale() != 1f) {
                    returns += ":" + dust.getScale();
                }

                returns += ":" + colorToString(dust.getRed(), dust.getGreen(), dust.getBlue());
            } else if (data instanceof ParticleColorData) {
                returns += ":" + colorToString(((ParticleColorData) data).getColor());
            } else if (data instanceof ParticleDustColorTransitionData) {
                ParticleDustColorTransitionData dust = (ParticleDustColorTransitionData) data;

                returns += ":";

                if (dust.getScale() != 1f) {
                    returns += dust.getScale() + ":";
                }

                returns += colorToString(dust.getStartRed(), dust.getStartGreen(), dust.getStartBlue());
                returns += ":";
                returns += colorToString(dust.getEndRed(), dust.getEndGreen(), dust.getEndBlue());
            } else if (data instanceof ParticleSculkChargeData) {
                returns += ":" + ((ParticleSculkChargeData) data).getRoll();
            } else if (data instanceof ParticleVibrationData) {
                ParticleVibrationData vib = (ParticleVibrationData) data;
                @Nullable Vector3i start = vib.getStartingPosition();
                BlockPositionSource source = vib.getSource() instanceof BlockPositionSource ? (BlockPositionSource) vib.getSource() :
                    new BlockPositionSource(new Vector3i(0, 0, 0));

                returns += ":" + source.getPos().getX() + "," + source.getPos().getY() + "," + source.getPos().getZ();

                if (start != null) {
                    returns += ":" + start.getX() + "," + start.getY() + "," + start.getZ();
                }

                returns += ":" + vib.getTicks();
            } else if (data instanceof ParticleShriekData) {
                returns += ":" + ((ParticleShriekData) data).getDelay();
            }
        }

        return returns;
    }

    @Override
    public Object fromString(String string) throws DisguiseParseException {
        String[] split = string.split("[:,]", -1); // Split on comma or colon

        ParticleInfo info = (ParticleInfo) super.fromString(convertName(split[0]));

        if (info == null) {
            return null;
        }

        split = Arrays.copyOfRange(split, 1, split.length);

        String name = info.type.getName().getKey();
        ParticleType pType = info.getType();
        Class<? extends ParticleData> cl = info.getData();
        ParticleData data = null;

        if (cl == ParticleBlockStateData.class) {
            if (split.length == 0) {
                data = new ParticleBlockStateData(WrappedBlockState.getDefaultState(StateTypes.STONE));
            } else {
                try {
                    String unSplit = string.substring(string.split("[:,]", -1)[0].length() + 1);

                    if (unSplit.isEmpty()) {
                        throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_BLOCK, name, string);
                    }

                    WrappedBlockState state =
                        (WrappedBlockState) ((ParamInfoWrappedBlockData) ParamInfoManager.getParamInfo(WrappedBlockState.class)).fromString(
                            unSplit);

                    if (state == null) {
                        throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_BLOCK, name, string);
                    }

                    data = new ParticleBlockStateData(state);
                } catch (DisguiseParseException ex) {
                    throw ex;
                } catch (Exception exception) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_BLOCK, name, string);
                }
            }
        } else if (cl == ParticleItemStackData.class) {
            if (split.length > 0) {
                ItemStack item = ParamInfoItemStack.parseToItemstack(split);

                data = new ParticleItemStackData(DisguiseUtilities.fromBukkitItemStack(item));
            } else {
                data = new ParticleItemStackData(
                    com.github.retrooper.packetevents.protocol.item.ItemStack.builder().type(ItemTypes.STONE).build());
            }

        } else if (cl == ParticleDustData.class) {
            float[] color = new float[3];
            float scale = 1;

            if (split.length > 0) {
                ColorParser parser = new ColorParser(split, true);

                if (!parser.canConsume()) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_DUST, name, string);
                }

                try {
                    color = parser.getColor();
                } catch (DisguiseParseException ex) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_DUST, name, string);
                }

                int remain = parser.getArgsRemaining();

                if (remain != 0) {
                    // If we have more than 1 arg, or the 1 arg is not a number
                    if (!split[0].matches("\\d+(\\.\\d+)?") || remain > 1) {
                        throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_DUST, name, string);
                    }

                    scale = Float.parseFloat(split[0]);
                    scale = Math.min(100, Math.max(0.2f, scale));
                }
            }

            data = new ParticleDustData(scale, color[0], color[1], color[2]);
        } else if (cl == ParticleDustColorTransitionData.class) {
            // Scale is optional, color is either a name, or three numbers. Same for the second color.
            // So it can be from 1 to 7 args.
            // Without scale, it can be 1, 2, 4, 6 args.
            // With scale, it can be 2, 3, 5, 7 args.

            // We work backwards. Figure out the last color, then if we have enough args, another color, then if we have enough args, the
            // scale, then if we have enough args, throw.
            ColorParser parser = new ColorParser(split, true);

            float[] color1;
            float[] color2;

            try {
                color2 = parser.getColor();
                color1 = parser.getColor();
            } catch (Exception ex) {
                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, name, string);
            }

            float scale = 1;
            int remain = parser.getArgsRemaining();

            if (remain != 0) {
                // If we have more than 1 arg, or the 1 arg is not a number
                if (!split[0].matches("\\d+(\\.\\d+)?") || remain > 1) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, name, string);
                }

                scale = Float.parseFloat(split[0]);
                scale = Math.min(100, Math.max(0.2f, scale));
            }

            data = new ParticleDustColorTransitionData(scale, color1[0], color1[1], color1[2], color2[0], color2[1], color2[2]);
        } else if (cl == ParticleShriekData.class) {
            int delay = 60;

            if (split.length > 0) {
                if (split.length > 1 || !split[0].matches("\\d+")) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_SHRIEK, name, string);
                }

                delay = Integer.parseInt(split[0]);
            }

            data = new ParticleShriekData(delay);
        } else if (cl == ParticleSculkChargeData.class) {

            float roll = 60f;

            if (split.length > 0) {
                if (split.length > 1 || !split[0].matches("\\d+(\\.\\d+)")) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_SHULK_CHARGE, name, string);
                }

                roll = Float.parseFloat(split[0]);
            }

            data = new ParticleSculkChargeData(roll);
        } else if (cl == ParticleColorData.class) {
            int color = getColorAsInt(string, split, name);

            data = new ParticleColorData(color);
        } else if (cl == ParticleVibrationData.class) {
            if (split.length != 4 && split.length != 7) {
                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_VIBRATION, name, string);
            }

            for (String s : split) {
                if (s.matches("-?\\d+")) {
                    continue;
                }

                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_VIBRATION, name, string);
            }

            Vector3i sourceBlock = new Vector3i(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));

            Vector3i startBlock = null;
            int ticks = Integer.parseInt(split[split.length - 1]);

            if (split.length == 7) {
                startBlock = new Vector3i(Integer.parseInt(split[3]), Integer.parseInt(split[4]), Integer.parseInt(split[5]));
            }

            data = new ParticleVibrationData(startBlock, sourceBlock, ticks);
        }

        if (data == null) {
            // If we were supposed to have data
            if (split.length > 0) {
                return null;
            }

            data = ParticleData.emptyData();
        }

        return new com.github.retrooper.packetevents.protocol.particle.Particle<>(pType, data);
    }

    private static int getColorAsInt(String string, String[] split, String name) throws DisguiseParseException {
        int color = 0;

        if (split.length == 1 && split[0].matches("-?\\d+")) {
            color = Integer.parseInt(split[0]);
        } else if (split.length > 0) {
            ColorParser parser = new ColorParser(split, false);

            try {
                float[] colors = parser.getColor();

                color =
                    new com.github.retrooper.packetevents.protocol.color.Color((int) colors[0], (int) colors[1], (int) colors[2]).asRGB();
            } catch (Exception ex) {
                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_COLOR, name, string);
            }

            if (parser.getArgsRemaining() > 0) {
                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_COLOR, name, string);
            }
        }
        return color;
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }

    static {
        fillMap();
    }
}
