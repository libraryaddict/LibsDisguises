package me.libraryaddict.disguise.utilities.params.types.custom;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedParticle;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by libraryaddict on 19/09/2018.
 */
public class ParamInfoParticle extends ParamInfoEnum {
    private final Material[] materials;

    public ParamInfoParticle(Class paramClass, String name, String description, Enum[] possibleValues, Material[] materials) {
        super(paramClass, name, description, possibleValues);

        this.materials = materials;
    }

    public Set<String> getEnums(String tabComplete) {
        Set<String> enums = getValues().keySet();

        if (tabComplete.isEmpty()) {
            return enums;
        }

        enums = new HashSet<>(enums);

        tabComplete = tabComplete.toUpperCase(Locale.ENGLISH);

        for (Particle particle : new Particle[]{Particle.BLOCK_CRACK, Particle.BLOCK_DUST, Particle.ITEM_CRACK}) {
            for (Material mat : materials) {
                if (particle != Particle.ITEM_CRACK && !mat.isBlock()) {
                    continue;
                }

                String name = particle.name() + ":" + mat.name();

                if (!name.startsWith(tabComplete)) {
                    continue;
                }

                enums.add(name);
            }
        }

        return enums;
    }

    @Override
    public String toString(Object object) {
        WrappedParticle particle = (WrappedParticle) object;

        Object data = particle.getData();
        String returns = particle.getParticle().name();

        if (data != null) {
            if (data instanceof ItemStack) {
                returns += "," + ((ItemStack) data).getType().name();
            } else if (data instanceof WrappedBlockData) {

                returns += "," + ((WrappedBlockData) data).getType().name();
            } else if (data instanceof Particle.DustOptions) {
                returns += "," + ParamInfoManager.getParamInfo(Color.class).toString(((Particle.DustOptions) data).getColor());

                if (((Particle.DustOptions) data).getSize() != 1f) {
                    returns += "," + ((Particle.DustOptions) data).getSize();
                }
            }
        }

        return returns;
    }

    @Override
    public Object fromString(String string) throws DisguiseParseException {
        String[] split = string.split("[:,]"); // Split on comma or colon
        Particle particle = (Particle) super.fromString(split[0]);

        if (particle == null) {
            return null;
        }

        Object data = null;

        switch (particle) {
            case BLOCK_CRACK:
            case BLOCK_DUST:
            case FALLING_DUST:
                Material material;

                if (split.length != 2 || (material = Material.getMaterial(split[1])) == null || !material.isBlock()) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_BLOCK, particle.name(), string);
                }

                data = WrappedBlockData.createData(material);
                break;
            case ITEM_CRACK:
                if (split.length != 1) {
                    data = ParamInfoItemStack.parseToItemstack(Arrays.copyOfRange(split, 1, split.length));
                }

                if (data == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_ITEM, particle.name(), string);
                }
                break;
            case REDSTONE:
                // If it can't be a RGB color or color name
                // REDSTONE:BLUE - 2 args
                // REDSTONE:BLUE,4 - 3 args
                // REDSTONE:3,5,2 - 4 args
                // REDSTONE:3,5,6,2 - 5 args
                if (split.length < 2 || split.length > 5) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_REDSTONE, particle.name(), string);
                }

                Color color = ((ParamInfoColor) ParamInfoManager.getParamInfo(Color.class)).parseToColor(
                    StringUtils.join(Arrays.copyOfRange(split, 1, split.length - (split.length % 2)), ","));

                if (color == null) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_REDSTONE, particle.name(), string);
                }

                float size;

                if (split.length % 2 == 0) {
                    size = 1;
                } else if (!split[split.length - 1].matches("[0-9.]+")) {
                    throw new DisguiseParseException(LibsMsg.PARSE_PARTICLE_REDSTONE, particle.name(), string);
                } else {
                    size = Math.max(0.2f, Float.parseFloat(split[split.length - 1]));

                    // Stupid high cap
                    if (size > 100) {
                        size = 100;
                    }
                }

                data = new Particle.DustOptions(color, size);
                break;
        }

        if (data == null && split.length > 1) {
            return null;
        }

        return WrappedParticle.create(particle, data);
    }

    /**
     * Is the values it returns all it can do?
     */
    @Override
    public boolean isCustomValues() {
        return true;
    }
}
