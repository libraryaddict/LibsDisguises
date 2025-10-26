package me.libraryaddict.disguise.utilities.params;

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import com.github.retrooper.packetevents.protocol.entity.data.struct.CopperGolemState;
import com.github.retrooper.packetevents.protocol.entity.data.struct.WeatheringCopperState;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariant;
import com.github.retrooper.packetevents.protocol.entity.wolfvariant.WolfSoundVariants;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.GolemCrack;
import me.libraryaddict.disguise.utilities.animations.DisguiseAnimation;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoBoolean;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoByte;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoDouble;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoFloat;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoFloatNullable;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoInteger;
import me.libraryaddict.disguise.utilities.params.types.base.ParamInfoString;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoBlockPosition;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoBoatType;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoChatColor;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoColor;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoComponent;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoDisplayBrightness;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoEulerAngle;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoItemProfile;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoItemStack;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoItemStackArray;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoPacketEvents;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoParticle;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoPotionEffect;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoQuaternionf;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoSoundGroup;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoTime;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoTransformation;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoUserProfile;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoVector3f;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoWrappedBlockData;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.sounds.DisguiseSoundCategory;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Display;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ParamInfoTypes {
    /**
     * Constructor values are listed here for continuity
     */
    public List<ParamInfo> getParamInfos() {
        // If I remember correctly, some of the things in here may matter in the order they're constructed. Annoying.
        List<ParamInfo> paramInfos = new ArrayList<>();

        // Register enum types
        paramInfos.add(new ParamInfoEnum(Art.class, "Art", "View all the paintings you can use for a painting disguise"));
        paramInfos.add(new ParamInfoEnum(Horse.Color.class, "Horse Color", "View all the colors you can use for a horses color"));

        paramInfos.add(new ParamInfoEnum(Villager.Profession.class, "Villager Profession",
            "View all the professions you can set on a Villager and Zombie Villager"));
        paramInfos.add(new ParamInfoEnum(DisguiseConfig.TallSelfDisguise.class, "Tall Disguise",
            "Controls how the player will view oversized self disguises"));

        if (NmsVersion.v1_14.isSupported()) {
            paramInfos.add(new ParamInfoEnum(Villager.Type.class, "Villager Biome",
                "View all the biomes you can set on a Villager and Zombie Villager"));
        }

        paramInfos.add(new ParamInfoEnum(BlockFace.class, "Direction", "Direction (North, East, South, West, Up, Down)",
            "View the directions usable on player setSleeping and shulker direction", Arrays.copyOf(BlockFace.values(), 6)));
        paramInfos.add(new ParamInfoEnum(Rabbit.Type.class, "Rabbit Type", "View the kinds of rabbits you can turn into"));

        paramInfos.add(new ParamInfoEnum(MainHand.class, "Main Hand", "Set the main hand for an entity"));
        paramInfos.add(new ParamInfoEnum(Llama.Color.class, "Llama Color", "View all the colors you can use for a llama color"));
        paramInfos.add(new ParamInfoEnum(Parrot.Variant.class, "Parrot Variant", "View the different colors a parrot can be"));
        paramInfos.add(new ParamInfoComponent(Component.class, "Component", "A kyori adventure text component"));
        paramInfos.add(new ParamInfoEnum(DisguiseAnimation.class, "Disguise Animation",
            "Every animation that Mojang has added to entities, not all of them will work"));
        paramInfos.add(new ParamInfoEnum(DisguiseSoundCategory.class, "Sound Category",
            "The sound category the sounds from this disguise will play under, not set when global default",
            Arrays.stream(DisguiseSoundCategory.values()).filter(DisguiseSoundCategory::isAvailable)
                .toArray(DisguiseSoundCategory[]::new)));

        Material[] materials = getMaterials();

        paramInfos.add(new ParamInfoWrappedBlockData(WrappedBlockState.class, "BlockData",
            "The block data states, barrel[facing=north,open=false] as example"));

        if (NmsVersion.v1_13.isSupported()) {
            paramInfos.add(new ParamInfoParticle("Particle", "The different particles of Minecraft", materials));
            paramInfos.add(new ParamInfoEnum(Particle.class, "ParticleType", "The different particles of Minecraft"));
            paramInfos.add(new ParamInfoEnum(TropicalFish.Pattern.class, "Pattern", "Patterns of a tropical fish"));
            //   paramInfos.add(
            //      new ParamInfoBlockData(BlockData.class, "BlockData", "The block data states, barrel[facing=north,open=false] as
            //      example",
            //         getMaterials()));
        } else {
            paramInfos.add(new ParamInfoEnum(Particle.class, "Particle", "The different particles of Minecraft"));
        }

        paramInfos.add(new ParamInfoEnum(DyeColor.class, "DyeColor", "Dye colors of many different colors"));
        paramInfos.add(new ParamInfoEnum(Horse.Style.class, "Horse Style", "Horse style which is the patterns on the horse"));

        if (NmsVersion.v1_14.isSupported()) {
            paramInfos.add(new ParamInfoEnum(EntityPose.class, "EntityPose", "The pose the entity should strike",
                Arrays.stream(Pose.values()).map(pose -> {
                    try {
                        return EntityPose.valueOf(pose == Pose.SNEAKING ? "CROUCHING" : pose.name());
                    } catch (Exception ignored) {
                    }
                    return null;
                }).filter(Objects::nonNull).toArray(EntityPose[]::new)));
            paramInfos.add(new ParamInfoEnum(Cat.Type.class, "Cat Type", "The type of cat"));
            paramInfos.add(new ParamInfoEnum(Fox.Type.class, "Fox Type", "The type of fox"));
            paramInfos.add(new ParamInfoEnum(Panda.Gene.class, "Panda Gene", "The panda gene type"));
            paramInfos.add(
                new ParamInfoEnum(MushroomCow.Variant.class, "Mushroom Cow Variant", "The different variants for mushroom cows"));
        } else {
            paramInfos.add(new ParamInfoEnum(Ocelot.Type.class, "Ocelot Type", "The type of ocelot"));
        }

        if (NmsVersion.v1_17.isSupported()) {
            paramInfos.add(new ParamInfoEnum(Axolotl.Variant.class, "Axolotl Variant", "The variant of Axolotl"));
        }

        if (NmsVersion.v1_19_R1.isSupported()) {
            paramInfos.add(new ParamInfoEnum(Frog.Variant.class, "Frog Variant", "The variant of Frog"));
            paramInfos.add(new ParamInfoBoatType(Boat.Type.class, "Boat Type", "The different types of boats"));
        } else {
            paramInfos.add(new ParamInfoEnum(TreeSpecies.class, "Tree Species", "View the different types of tree species"));
        }

        if (NmsVersion.v1_19_R3.isSupported()) {
            paramInfos.add(new ParamInfoTransformation(Transformation.class, "Transformation",
                "Translation (Transform, Left Rotation, Scale, Right Rotation). 3, then 4, then 3, then 4 numbers. All seperated by a " +
                    "comma", "14 comma seperated numbers for a position translation"));
            paramInfos.add(new ParamInfoVector3f(Vector3f.class, "Vector3f", "Vector3f (X, Y, Z)",
                "Used as part of a Transformation for the Transform and Scale"));
            paramInfos.add(new ParamInfoQuaternionf(Quaternionf.class, "Quaternion", "Quaternion (X, Y, Z, W)",
                "Four values used to define part of a Transformation for the rotations"));
            paramInfos.add(
                new ParamInfoEnum(ItemDisplay.ItemDisplayTransform.class, "Item Display Transform", "How the Item Display is transformed"));
            paramInfos.add(new ParamInfoEnum(Display.Billboard.class, "Display Billboard", "How the billboard is aligned"));
            paramInfos.add(new ParamInfoDisplayBrightness(Display.Brightness.class, "Display Brightness",
                "The block and sky light brightness of the display"));

            try {
                paramInfos.add(
                    new ParamInfoEnum(TextDisplay.TextAlignment.class, "Text Display Alignment", "How the text is aligned in the display"));
            } catch (Throwable ex) {
                LibsDisguises.getInstance().getLogger().severe(
                    "You are using 1.19.4, but you're using an outdated build of 1.19.4, you need to update the spigot (or paper) jar");
                ex.printStackTrace();
            }
        }

        if (NmsVersion.v1_20_R1.isSupported()) {
            paramInfos.add(new ParamInfoEnum(Sniffer.State.class, "Sniffer State", "The current mindset of a Sniffer"));
        }

        if (NmsVersion.v1_20_R4.isSupported()) {
            paramInfos.add(new ParamInfoEnum(Wolf.Variant.class, "Wolf Variant", "The variant of a wolf"));
        }

        if (NmsVersion.v1_21_R1.isSupported()) {
            paramInfos.add(new ParamInfoEnum(ArmadilloState.class, "Armadillo State", "The current state of an Armadillo"));
        }

        if (NmsVersion.v1_21_R2.isSupported()) {
            paramInfos.add(new ParamInfoEnum(Salmon.Variant.class, "Salmon Variant", "The size of a salmon"));
        }

        if (NmsVersion.v1_21_R4.isSupported()) {
            // Spigot does not have Wolf.SoundVariant
            // Although Lib's Disguises could support Wolf.SoundVariant without breaking on Spigot servers, we do not implement it as
            // this could indirectly break third party plugins
            paramInfos.add(new ParamInfoPacketEvents(WolfSoundVariant.class, WolfSoundVariants.getRegistry(), "Wolf Sound Variant",
                "The variant of wolf sounds"));
            paramInfos.add(new ParamInfoEnum(Chicken.Variant.class, "Chicken Variant", "The variant of a chicken"));
            paramInfos.add(new ParamInfoEnum(Pig.Variant.class, "Pig Variant", "The variant of a pig"));
            paramInfos.add(new ParamInfoEnum(Cow.Variant.class, "Cow Variant", "The variant of a cow"));
        }

        if (NmsVersion.v1_21_R6.isSupported()) {
            paramInfos.add(new ParamInfoEnum(CopperGolemState.class, "Copper Golem State", "The state of a Copper Golem"));
            paramInfos.add(new ParamInfoEnum(WeatheringCopperState.class, "Weathering Copper State", "The state of oxidizing copper"));
        }

        paramInfos.add(new ParamInfoEnum(DisguiseConfig.NotifyBar.class, "NotifyBar", "Where the disguised indicator should appear"));
        paramInfos.add(new ParamInfoEnum(BarColor.class, "BarColor", "The color of the boss bar"));
        paramInfos.add(new ParamInfoEnum(BarStyle.class, "BarStyle", "The style of the boss bar"));

        // Register custom types
        paramInfos.add(
            new ParamInfoEulerAngle(EulerAngle.class, "Euler Angle", "Euler Angle (X,Y,Z)", "Set the X,Y,Z directions on an armorstand"));
        paramInfos.add(new ParamInfoColor(Color.class, "Color", "Colors that can also be defined through RGB", getColors()));
        paramInfos.add(new ParamInfoEnum(Material.class, "Material", "A material used for blocks and items", materials));
        paramInfos.add(new ParamInfoItemStack(ItemStack.class, "ItemStack",
            "ItemStack (Material,Amount?,Glow?) or ItemStack[data=data] (valid via /give)",
            "An ItemStack compromised of Material,Amount,Glow. Only requires Material", materials));
        paramInfos.add(new ParamInfoItemStackArray(ItemStack[].class, "ItemStack[]",
            "Four ItemStacks (Material:Amount?:Glow?,Material:Amount?:Glow?..)", "Four ItemStacks separated by a comma", materials));
        paramInfos.add(
            new ParamInfoPotionEffect(PotionEffectType.class, "Potion Effect", "View all the potion effects you can add", getPotions()));

        paramInfos.add(
            new ParamInfoBlockPosition(Vector3i.class, "Block Position", "Block Position (num,num,num)", "Three numbers separated by a ,"));
        paramInfos.add(new ParamInfoUserProfile(UserProfile.class, "UserProfile", "Get the userprofile here https://sessionserver.mojang" +
            ".com/session/minecraft/profile/PLAYER_UUID_GOES_HERE?unsigned=false"));
        paramInfos.add(new ParamInfoItemProfile(ItemProfile.class, "ItemProfile", "ItemProfile as used for mannequins"));
        paramInfos.add(new ParamInfoTime(long.class, "Expiry Time",
            "Set how long the disguise lasts, <Num><Time><Num>... where <Time> is (s/sec)(m/min)(h/hour)(d/day) " +
                "etc. 30m20secs = 30 minutes, 20 seconds"));

        paramInfos.add(new ParamInfoChatColor(ChatColor.class, "ChatColor", "A chat color"));
        paramInfos.add(new ParamInfoEnum(GolemCrack.class, "Golem Cracked", "The stage a golem has been cracked"));

        // Register base types
        Map<String, Boolean> booleanMap = new HashMap<>();
        booleanMap.put("true", true);
        booleanMap.put("false", false);

        paramInfos.add(new ParamInfoBoolean("Boolean", "True/False", "True or False", booleanMap));
        paramInfos.add(new ParamInfoString(String.class, "Text", "A line of text"));
        paramInfos.add(new ParamInfoInteger("Number", "A whole number without decimals"));
        paramInfos.add(new ParamInfoFloat("Number.0", "A number which can have decimal places"));
        paramInfos.add(new ParamInfoFloatNullable("Number.0", "A number which can have decimal places or be null"));
        paramInfos.add(new ParamInfoDouble("Number.0", "A number which can have decimal places"));
        paramInfos.add(new ParamInfoByte("Number", "A whole number from -127 to 128"));
        paramInfos.add(new ParamInfoSoundGroup());

        return paramInfos;
    }

    private Map<String, Color> getColors() {
        try {
            Map<String, Color> map = new HashMap<>();
            Class cl = Class.forName("org.bukkit.Color");

            for (Field field : cl.getFields()) {
                if (field.getType() != cl) {
                    continue;
                }

                map.put(field.getName(), (Color) field.get(null));
            }

            return map;
        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Material[] getMaterials() {
        List<Material> list = new ArrayList<>();

        for (Material material : ReflectionManager.enumValues(Material.class)) {
            if (material.name().matches("([A-Z]+_)?AIR")) {
                continue;
            }

            try {
                Field field = Material.class.getField(material.name());

                // Ignore all legacies materials
                if (field.isAnnotationPresent(Deprecated.class)) {
                    continue;
                }

                list.add(material);
            } catch (NoSuchFieldException ignored) {
            }
        }

        return list.toArray(new Material[0]);
    }

    private Map<String, Object> getPotions() {
        Map<String, Object> map = new HashMap<>();

        if (Bukkit.getServer() == null) {
            return map;
        }

        for (PotionEffectType effectType : PotionEffectType.values()) {
            if (effectType == null) {
                continue;
            }

            map.put(toReadable(effectType.getName()), effectType);
        }

        return map;
    }

    private String toReadable(String string) {
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].charAt(0) + split[i].substring(1).toLowerCase(Locale.ENGLISH);
        }

        return StringUtils.join(split, "_");
    }
}
