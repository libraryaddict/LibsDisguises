package me.libraryaddict.disguise.utilities;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.utilities.DisguiseParser.DisguisePerm;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionFlagWatchers {
    public static class ParamInfo {
        private Class paramClass;
        private String name;
        private String[] enums;
        private String description;

        public ParamInfo(Class paramClass, String name, String description) {
            this(name, description);
            this.paramClass = paramClass;

            Enum[] enums = (Enum[]) paramClass.getEnumConstants();

            if (enums != null) {
                this.enums = new String[enums.length];

                for (int i = 0; i < enums.length; i++) {
                    this.enums[i] = enums[i].name();
                }
            }

            paramList.add(this);
        }

        private ParamInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public ParamInfo(Class paramClass, Enum[] enums, String name, String description) {
            this(name, description);
            this.enums = new String[enums.length];
            this.paramClass = paramClass;

            for (int i = 0; i < enums.length; i++) {
                this.enums[i] = enums[i].name();
            }

            paramList.add(this);
        }

        public ParamInfo(Class paramClass, String name, String description, String[] enums) {
            this(name, description);
            this.enums = enums;
            this.paramClass = paramClass;

            paramList.add(this);
        }

        public boolean isEnums() {
            return enums != null;
        }

        public Class getParamClass() {
            return paramClass;
        }

        public String getName() {
            return TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(getRawName());
        }

        public String getRawName() {
            return name;
        }

        public String getDescription() {
            return TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(getRawDescription());
        }

        public String getRawDescription() {
            return description;
        }

        public String[] getEnums(String tabComplete) {
            return enums;
        }
    }

    private static ArrayList<ParamInfo> paramList = new ArrayList<>();

    public static ArrayList<ParamInfo> getParamInfos() {
        return paramList;
    }

    public static ParamInfo getParamInfo(Class c) {
        for (ParamInfo info : getParamInfos()) {
            if (info.getParamClass() != c)
                continue;

            return info;
        }

        return null;
    }

    public static ParamInfo getParamInfo(DisguisePerm disguiseType, String methodName) {
        return getParamInfo(disguiseType.getType(), methodName);
    }

    public static ParamInfo getParamInfo(DisguiseType disguiseType, String methodName) {
        for (Method method : getDisguiseWatcherMethods(disguiseType.getWatcherClass())) {
            if (!method.getName().toLowerCase().equals(methodName.toLowerCase()))
                continue;

            if (method.getParameterTypes().length != 1)
                continue;

            if (method.getAnnotation(Deprecated.class) != null)
                continue;

            return getParamInfo(method.getParameterTypes()[0]);
        }

        return null;
    }

    static {
        new ParamInfo(AnimalColor.class, "Animal Color", "View all the colors you can use for an animal color");
        new ParamInfo(Art.class, "Art", "View all the paintings you can use for a painting disguise");
        new ParamInfo(Horse.Color.class, "Horse Color", "View all the colors you can use for a horses color");

        new ParamInfo(Ocelot.Type.class, "Ocelot Type", "View all the ocelot types you can use for ocelots");
        new ParamInfo(Villager.Profession.class, "Villager Profession",
                "View all the professions you can set on a Zombie and Normal Villager");
        new ParamInfo(BlockFace.class, Arrays.copyOf(BlockFace.values(), 6),
                "Direction (North, East, South, West, Up, Down)",
                "View the directions usable on player setSleeping and shulker direction");
        new ParamInfo(RabbitType.class, "Rabbit Type", "View the kinds of rabbits you can turn into");
        new ParamInfo(TreeSpecies.class, "Tree Species", "View the different types of tree species");
        new ParamInfo(EulerAngle.class, "Euler Angle (X,Y,Z)", "Set the X,Y,Z directions on an armorstand");
        new ParamInfo(MainHand.class, "Main Hand", "Set the main hand for an entity");
        new ParamInfo(Llama.Color.class, "Llama Color", "View all the colors you can use for a llama color");
        new ParamInfo(Parrot.Variant.class, "Parrot Variant", "View the different colors a parrot can be");
        new ParamInfo(Particle.class, "Particle", "The different particles of Minecraft");
        new ParamInfo(TropicalFish.Pattern.class, "Pattern", "Patterns of a tropical fish");
        new ParamInfo(DyeColor.class, "DyeColor", "Dye colors of many different colors");

        ArrayList<String> potionEnums = new ArrayList<>();

        for (PotionEffectType effectType : PotionEffectType.values()) {
            if (effectType == null)
                continue;

            potionEnums.add(toReadable(effectType.getName()));
        }

        String[] materials = new String[Material.values().length];

        for (int i = 0; i < Material.values().length; i++) {
            materials[i] = Material.values()[i].name();
        }

        new ParamInfo(ItemStack.class, "Item (Material:Damage:Amount:Glow), only Material required",
                "An ItemStack compromised of Material:Durability", materials);

        new ParamInfo(ItemStack[].class,
                "Four ItemStacks (Material:Damage:Amount:Glow,Material:Damage:Amount:Glow..), only Material required",
                "Four ItemStacks separated by an ,", materials) {
            @Override
            public String[] getEnums(String tabComplete) {
                String beginning = tabComplete
                        .substring(0, tabComplete.contains(",") ? tabComplete.lastIndexOf(",") + 1 : 0);
                String end = tabComplete.substring(tabComplete.contains(",") ? tabComplete.lastIndexOf(",") + 1 : 0);

                ArrayList<String> toReturn = new ArrayList<>();

                for (String material : super.getEnums("")) {
                    if (!material.toLowerCase().startsWith(end.toLowerCase()))
                        continue;

                    toReturn.add(beginning + material);
                }

                return toReturn.toArray(new String[0]);
            }
        };

        new ParamInfo(PotionEffectType.class, "Potion Effect", "View all the potion effects you can add",
                potionEnums.toArray(new String[0]));
        new ParamInfo(String.class, "Text", "A line of text");
        new ParamInfo(boolean.class, "True/False", "True or False", new String[]{"true", "false"});
        new ParamInfo(int.class, "Number", "A whole number, no decimals");
        new ParamInfo(double.class, "Number.0", "A number which can have decimals");
        new ParamInfo(float.class, "Number.0", "A number which can have decimals");

        new ParamInfo(Horse.Style.class, "Horse Style", "Horse style which is the patterns on the horse");
        new ParamInfo(int[].class, "number,number,number..", "Numbers separated by an ,");

        new ParamInfo(BlockPosition.class, "Block Position (num,num,num)", "Three numbers separated by a ,");
        new ParamInfo(WrappedGameProfile.class, "GameProfile", "Get the gameprofile here https://sessionserver.mojang" +
                ".com/session/minecraft/profile/PLAYER_UUID_GOES_HERE?unsigned=false");

        Collections.sort(paramList, new Comparator<ParamInfo>() {
            @Override
            public int compare(ParamInfo o1, ParamInfo o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });
    }

    public static Method[] getDisguiseWatcherMethods(@Nullable Class<? extends FlagWatcher> watcherClass) {
        if (watcherClass == null) {
            return new Method[0];
        }

        ArrayList<Method> methods = new ArrayList<>(Arrays.asList(watcherClass.getMethods()));

        Iterator<Method> itel = methods.iterator();

        while (itel.hasNext()) {
            Method method = itel.next();

            if (method.getParameterTypes().length != 1) {
                itel.remove();
            } else if (method.getName().startsWith("get")) {
                itel.remove();
            } else if (method.getAnnotation(Deprecated.class) != null) {
                itel.remove();
            } else if (getParamInfo(method.getParameterTypes()[0]) == null) {
                itel.remove();
            } else if (!method.getReturnType().equals(Void.TYPE)) {
                itel.remove();
            } else if (method.getName().equals("removePotionEffect")) {
                itel.remove();
            }
        }

        for (String methodName : new String[]{"setViewSelfDisguise", "setHideHeldItemFromSelf", "setHideArmorFromSelf",
                "setHearSelfDisguise", "setHidePlayer"}) {
            try {
                methods.add(Disguise.class.getMethod(methodName, boolean.class));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return methods.toArray(new Method[0]);
    }

    private static String toReadable(String string) {
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        }

        return StringUtils.join(split, "_");
    }
}
