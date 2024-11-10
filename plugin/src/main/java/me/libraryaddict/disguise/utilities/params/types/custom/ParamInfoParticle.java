package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.protocol.color.AlphaColor;
import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleBlockStateData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustColorTransitionData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleSculkChargeData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleShriekData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleTrailData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleVibrationData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.positionsource.builtin.BlockPositionSource;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
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

    public static class PacketColorParser {
        private final String[] args;
        private final boolean decimalMode;
        private final boolean alphaColor;

        PacketColorParser(String[] args, boolean alphaColor) {
            this.args = args;

            decimalMode = Arrays.stream(args).anyMatch(s -> s.contains("."));
            this.alphaColor = alphaColor;
        }

        private void validateArgs() {
            int expect = (args.length >= 3 ? 3 : 1);

            if (expect != args.length && alphaColor) {
                expect++;
            }

            if (args.length != expect) {
                throw new IllegalArgumentException("Expected " + expect + " args, but got " + args.length);
            }
        }

        public Color parseFromNameOrInt() throws DisguiseParseException {
            // At least 1 arg, max of 2
            // Alpha can only have "alpha:colorname" or "int"

            String colorStr = args[args.length - 1];

            // Must not have 2 args if its an int
            if (colorStr.matches("-?\\d+")) {
                if (args.length != 1) {
                    throw new IllegalArgumentException("Only expected 1 arg, not 2 when parsing int color");
                }

                if (alphaColor) {
                    return new AlphaColor(Integer.parseInt(colorStr));
                }

                return new Color(Integer.parseInt(colorStr));
            }

            int alpha = args.length > 1 ? getInt(args[0]) : 255;

            String[] toPass = args;

            if (alphaColor && (toPass.length == 2 || toPass.length == 4)) {
                toPass = Arrays.copyOfRange(toPass, 1, toPass.length);
            }

            org.bukkit.Color color =
                ((ParamInfoColor) ParamInfoManager.getParamInfo(org.bukkit.Color.class)).parseToColor(StringUtils.join(toPass, ","));

            if (alphaColor) {
                return new AlphaColor(alpha, color.getRed(), color.getGreen(), color.getBlue());
            }

            return new Color(color.getRed(), color.getGreen(), color.getBlue());
        }

        public Color parseFromRGB() {
            // At least 3 args were given

            int r = getInt(args[args.length - 3]);
            int g = getInt(args[args.length - 2]);
            int b = getInt(args[args.length - 1]);

            if (alphaColor) {
                int alpha = args.length > 3 ? getInt(args[0]) : 255;

                return new AlphaColor(alpha, r, g, b);
            }

            return new Color(r, g, b);
        }

        public Color parse() throws DisguiseParseException {
            validateArgs();

            if (args.length > 2) {
                return parseFromRGB();
            }

            return parseFromNameOrInt();
        }

        private int getInt(String s) {
            if (decimalMode) {
                return (int) (Double.parseDouble(s) * 255);
            }

            return Integer.parseInt(s);
        }

    }

    @RequiredArgsConstructor
    @Accessors(chain = true)
    @Setter
    private static class ColorParser {
        private final String[] split;
        private boolean alphaColor;
        @Getter
        private int argsConsumed;

        public Color getColor() throws DisguiseParseException {
            // Might need to redo this whole class, esp for alpha support
            int need = getArgsNeed();
            int start = split.length - (argsConsumed + need);
            String[] copyOf = Arrays.copyOfRange(split, start, start + need);

            argsConsumed += need;

            return new PacketColorParser(copyOf, alphaColor).parse();
        }

        public int getArgsRemaining() {
            return split.length - getArgsConsumed();
        }

        private int getArgsNeed() {
            if (alphaColor) {
                // We always expect to consume all args at the current state of it, so return everything
                return getArgsRemaining();
            }

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

    private String alphaColorToString(int color) {
        if (color == -1) {
            return String.valueOf(color);
        }

        AlphaColor alphaColor = new AlphaColor(color);

        for (Map.Entry<String, org.bukkit.Color> entry : ParamInfoColor.getStaticColors().entrySet()) {
            org.bukkit.Color c = entry.getValue();

            if (c.getRed() != alphaColor.red() || c.getGreen() != alphaColor.green() || c.getBlue() != alphaColor.blue()) {
                continue;
            }

            // Always include the alpha to show usage
            return alphaColor.alpha() + ":" + entry.getKey();
        }

        // Return in Alpha:Red,Green,Blue format to be more readable
        return alphaColor.alpha() + ":" + alphaColor.red() + "," + alphaColor.green() + "," + alphaColor.blue();
    }

    private String colorToString(int red, int green, int blue) {
        for (Map.Entry<String, org.bukkit.Color> entry : ParamInfoColor.getStaticColors().entrySet()) {
            org.bukkit.Color c = entry.getValue();

            if (red != c.getRed() || green != c.getGreen() || blue != c.getBlue()) {
                continue;
            }

            return entry.getKey();
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

                returns += ":" + colorToString((int) (dust.getRed() * 255), (int) (dust.getGreen() * 255), (int) (dust.getBlue() * 255));
            } else if (data instanceof ParticleColorData) {
                returns += ":" + alphaColorToString(((ParticleColorData) data).getColor());
            } else if (data instanceof ParticleTrailData) {
                ParticleTrailData trail = (ParticleTrailData) data;

                returns += ":" + trail.getTarget().getX() + "," + trail.getTarget().getY() + "," + trail.getTarget().getZ() + ":" +
                    colorToString(trail.getColor().red(), trail.getColor().green(), trail.getColor().blue());
            } else if (data instanceof ParticleDustColorTransitionData) {
                ParticleDustColorTransitionData dust = (ParticleDustColorTransitionData) data;

                returns += ":";

                if (dust.getScale() != 1f) {
                    returns += dust.getScale() + ":";
                }

                returns +=
                    colorToString((int) (dust.getStartRed() * 255), (int) (dust.getStartGreen() * 255), (int) (dust.getStartBlue() * 255));
                returns += ":";
                returns += colorToString((int) (dust.getEndRed() * 255), (int) (dust.getEndGreen() * 255), (int) (dust.getEndBlue() * 255));
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
            Color color = new Color(0, 0, 0);
            float scale = 1;

            if (split.length > 0) {
                ColorParser parser = new ColorParser(split).setAlphaColor(false);

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

            data = new ParticleDustData(scale, color);
        } else if (cl == ParticleDustColorTransitionData.class) {
            // Scale is optional, color is either a name, or three numbers. Same for the second color.
            // So it can be from 1 to 7 args.
            // Without scale, it can be 1, 2, 4, 6 args.
            // With scale, it can be 2, 3, 5, 7 args.

            // We work backwards. Figure out the last color, then if we have enough args, another color, then if we have enough args, the
            // scale, then if we have enough args, throw.
            ColorParser parser = new ColorParser(split).setAlphaColor(false);

            Color color1;
            Color color2;

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

            data = new ParticleDustColorTransitionData(scale, color1, color2);
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
            int color = getColorAsInt(string, split, name, true);

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
        } else if (cl == ParticleTrailData.class) {
            // x,y,z
            // x,y,z,red
            // x,y,z,red,blue,green
            // 3, 4, 6
            if (split.length < 3 || split.length == 5 || split.length > 6) {
                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_TRAIL, name, string);
            }

            // Verify the first 3 args are doubles
            for (int i = 0; i < 3; i++) {
                if (split[i].matches("-?\\d+(\\.\\d+)?")) {
                    continue;
                }

                throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_TRAIL, name, string);
            }

            Vector3d target = new Vector3d(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));

            Color color;

            if (split.length > 3) {
                try {
                    color = new ColorParser(Arrays.copyOfRange(split, 3, split.length)).getColor();
                } catch (Exception ex) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_TRAIL, name, string);
                }
            } else {
                // Creaking has two colors 16545810 : 6250335
                color = new Color(95, 95, 255);
            }

            data = new ParticleTrailData(target, color);
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

    private static int getColorAsInt(String string, String[] split, String name, boolean alphaColor) throws DisguiseParseException {
        int color = alphaColor ? -1 : 0;

        if (split.length == 1 && split[0].matches("-?\\d+")) {
            color = Integer.parseInt(split[0]);
        } else if (split.length > 0) {
            ColorParser parser = new ColorParser(split).setAlphaColor(alphaColor);

            try {
                color = parser.getColor().asRGB();
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
