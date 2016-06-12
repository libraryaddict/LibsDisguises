package me.libraryaddict.disguise.disguisetypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.watchers.*;

public class FlagType<Y>
{
    private static FlagType[] _values = new FlagType[0];

    public static FlagType<Boolean> AGEABLE_BABY = new FlagType<Boolean>(AgeableWatcher.class, 11, false);

    public static FlagType<Integer> AREA_EFFECT_COLOR = new FlagType<Integer>(AreaEffectCloudWatcher.class, 6,
            Color.BLACK.asRGB());

    public static FlagType<Boolean> AREA_EFFECT_IGNORE_RADIUS = new FlagType<Boolean>(AreaEffectCloudWatcher.class, 7, false);

    public static FlagType<Integer> AREA_EFFECT_PARTICLE = new FlagType<Integer>(AreaEffectCloudWatcher.class, 8, 0);

    public static FlagType<Float> AREA_EFFECT_RADIUS = new FlagType<Float>(AreaEffectCloudWatcher.class, 6, 0F);

    public static FlagType<Byte> ARMORSTAND_META = new FlagType<Byte>(ArmorStandWatcher.class, 10, (byte) 0);

    public static FlagType<Byte> ARROW_CRITICAL = new FlagType<Byte>(ArrowWatcher.class, 6, (byte) 0);

    public static FlagType<Byte> BAT_HANGING = new FlagType<Byte>(BatWatcher.class, 11, (byte) 1);

    public static FlagType<Boolean> BLAZE_BLAZING = new FlagType<Boolean>(BlazeWatcher.class, 11, false);

    public static FlagType<Float> BOAT_DAMAGE = new FlagType<Float>(BoatWatcher.class, 7, 40F);

    public static FlagType<Boolean> CREEPER_IGNITED = new FlagType<Boolean>(CreeperWatcher.class, 13, false);

    public static FlagType<Boolean> CREEPER_POWERED = new FlagType<Boolean>(CreeperWatcher.class, 12, false);

    public static FlagType<ItemStack> DROPPED_ITEM = new FlagType<ItemStack>(DroppedItemWatcher.class, 6,
            new ItemStack(Material.AIR));

    public static FlagType<Optional> ENDER_CRYSTAL_BEAM = new FlagType<Optional>(EnderCrystalWatcher.class, 6, Optional.absent());

    public static FlagType<Boolean> ENDER_CRYSTAL_PLATE = new FlagType<Boolean>(EnderCrystalWatcher.class, 6, false);

    public static FlagType<Integer> ENDERDRAGON_PHASE = new FlagType<Integer>(EnderDragonWatcher.class, 6, 0);

    public static FlagType<Boolean> ENDERMAN_AGRESSIVE = new FlagType<Boolean>(EndermanWatcher.class, 12, false);

    public static FlagType<Optional<Integer>> ENDERMAN_ITEM = new FlagType<Optional<Integer>>(EndermanWatcher.class, 11,
            Optional.of(1));

    public static FlagType<Integer> ENTITY_AIR_TICKS = new FlagType<Integer>(FlagWatcher.class, 1, 0);

    public static FlagType<String> ENTITY_CUSTOM_NAME = new FlagType<String>(FlagWatcher.class, 2, null);

    public static FlagType<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new FlagType<Boolean>(FlagWatcher.class, 3, false);

    public static FlagType<Byte> ENTITY_META = new FlagType<Byte>(FlagWatcher.class, 0, (byte) 0);

    public static FlagType<Boolean> ENTITY_NO_GRAVITY = new FlagType<Boolean>(FlagWatcher.class, 5, false);

    public static FlagType<Integer> ENTITY_SILENT = new FlagType<Integer>(FlagWatcher.class, 4, 0);

    public static FlagType<Boolean> GHAST_AGRESSIVE = new FlagType<Boolean>(GhastWatcher.class, 11, false);

    public static FlagType<Byte> GUARDIAN_FLAG = new FlagType<Byte>(GuardianWatcher.class, 11, (byte) 0);

    public static FlagType<Integer> GUARDIAN_TARGET = new FlagType<Integer>(GuardianWatcher.class, 12, 0);

    public static FlagType<Integer> HORSE_ARMOR = new FlagType<Integer>(HorseWatcher.class, 16, 0);

    public static FlagType<Integer> HORSE_COLOR = new FlagType<Integer>(HorseWatcher.class, 14, 0);

    public static FlagType<Byte> HORSE_META = new FlagType<Byte>(HorseWatcher.class, 12, (byte) 0);

    public static FlagType<Optional<UUID>> HORSE_OWNER = new FlagType<Optional<UUID>>(HorseWatcher.class, 15,
            Optional.<UUID> absent());

    public static FlagType<Integer> HORSE_STYLE = new FlagType<Integer>(HorseWatcher.class, 14, 0);

    public static FlagType<Integer> HORSE_VARIANT = new FlagType<Integer>(HorseWatcher.class, 13, 0);

    public static FlagType<Byte> INSENTIENT_META = new FlagType<Byte>(LivingWatcher.class, 10, (byte) 0);

    public static FlagType<ItemStack> ITEMFRAME_ITEM = new FlagType<ItemStack>(ItemFrameWatcher.class, 6, null);

    public static FlagType<Byte> ITEMFRAME_ROTATION = new FlagType<Byte>(ItemFrameWatcher.class, 6, (byte) 0);

    public static FlagType<Integer> LIVING_ARROWS = new FlagType<Integer>(LivingWatcher.class, 9, 0);

    public static FlagType<Float> LIVING_HEALTH = new FlagType<Float>(LivingWatcher.class, 6, 0F);

    public static FlagType<Boolean> LIVING_POTION_AMBIENT = new FlagType<Boolean>(LivingWatcher.class, 8, false);

    public static FlagType<Integer> LIVING_POTIONS = new FlagType<Integer>(LivingWatcher.class, 7, 0);

    public static FlagType<Integer> MINECART_BLOCK = new FlagType<Integer>(MinecartWatcher.class, 8, 0);

    public static FlagType<Boolean> MINECART_BLOCK_VISIBLE = new FlagType<Boolean>(MinecartWatcher.class, 10, false);

    public static FlagType<Integer> MINECART_BLOCK_Y = new FlagType<Integer>(MinecartWatcher.class, 9, 0);

    public static FlagType<Integer> OCELOT_TYPE = new FlagType<Integer>(OcelotWatcher.class, 14, 0);

    public static FlagType<Boolean> PIG_SADDLED = new FlagType<Boolean>(PigWatcher.class, 12, false);

    public static FlagType<Byte> PLAYER_SKIN = new FlagType<Byte>(PlayerWatcher.class, 12, (byte) 0);

    public static FlagType<Integer> RABBIT_TYPE = new FlagType<Integer>(RabbitWatcher.class, 12, 0);

    public static FlagType<Byte> SHEEP_WOOL = new FlagType<Byte>(SheepWatcher.class, 12, (byte) 0);

    public static FlagType<Integer> SKELETON_TYPE = new FlagType<Integer>(SkeletonWatcher.class, 11, 0);

    public static FlagType<Integer> SLIME_SIZE = new FlagType<Integer>(SlimeWatcher.class, 11, 0);

    public static FlagType<Byte> TAMEABLE_META = new FlagType<Byte>(TameableWatcher.class, 12, (byte) 0);

    public static FlagType<Optional<UUID>> TAMEABLE_OWNER = new FlagType<Optional<UUID>>(TameableWatcher.class, 13,
            Optional.<UUID> absent());

    public static FlagType<Integer> TIPPED_ARROW_COLOR = new FlagType<Integer>(TippedArrowWatcher.class, 6, Color.WHITE.asRGB());

    public static FlagType<Integer> VILLAGER_PROFESSION = new FlagType<Integer>(VillagerWatcher.class, 12, 0);

    public static FlagType<Boolean> WITCH_AGGRESSIVE = new FlagType<Boolean>(WitchWatcher.class, 11, false);

    public static FlagType<Integer> WITHER_INVUL = new FlagType<Integer>(WitchWatcher.class, 14, 0);

    public static FlagType<Integer> WITHER_TARGET_1 = new FlagType<Integer>(WitherWatcher.class, 11, 0);

    public static FlagType<Integer> WITHER_TARGET_2 = new FlagType<Integer>(WitherWatcher.class, 12, 0);

    public static FlagType<Integer> WITHER_TARGET_3 = new FlagType<Integer>(WitherWatcher.class, 13, 0);

    public static FlagType<Boolean> WITHERSKULL_BLUE = new FlagType<Boolean>(WitherSkullWatcher.class, 6, false);

    public static FlagType<Boolean> WOLF_BEGGING = new FlagType<Boolean>(WolfWatcher.class, 15, false);

    public static FlagType<Integer> WOLF_COLLAR = new FlagType<Integer>(WolfWatcher.class, 16, 14);

    public static FlagType<Float> WOLF_DAMAGE = new FlagType<Float>(WolfWatcher.class, 14, 0F);

    public static FlagType<Boolean> ZOMBIE_AGGRESSIVE = new FlagType<Boolean>(ZombieWatcher.class, 14, false);

    public static FlagType<Boolean> ZOMBIE_BABY = new FlagType<Boolean>(ZombieWatcher.class, 11, false);

    public static FlagType<Integer> ZOMBIE_PROFESSION = new FlagType<Integer>(ZombieWatcher.class, 12, 0);

    public static FlagType<Boolean> ZOMBIE_SHAKING = new FlagType<Boolean>(ZombieWatcher.class, 13, false);

    static
    {
        // Simple verification for the dev that he's setting up the FlagType's properly.
        // All flag types should be from 0 to <Max Number> with no empty numbers.
        // All flag types should never occur twice.

        HashMap<Class, Integer> maxValues = new HashMap<Class, Integer>();

        for (FlagType type : values())
        {
            if (maxValues.containsKey(type.getFlagWatcher()) && maxValues.get(type.getFlagWatcher()) > type.getIndex())
                continue;

            maxValues.put(type.getFlagWatcher(), type.getIndex());
        }

        for (Entry<Class, Integer> entry : maxValues.entrySet())
        {
            loop:

            for (int i = 0; i < entry.getValue(); i++)
            {
                FlagType found = null;

                for (FlagType type : values())
                {
                    if (type.getIndex() != i)
                        continue;

                    if (!type.getFlagWatcher().isAssignableFrom(entry.getKey()))
                        continue;

                    if (found != null)
                    {
                        System.err.println(entry.getKey().getSimpleName() + " has multiple FlagType's registered for the index "
                                + i + " (" + type.getFlagWatcher().getSimpleName() + ", " + found.getFlagWatcher().getSimpleName()
                                + ")");
                        continue loop;
                    }

                    found = type;
                }

                if (found != null)
                    continue;

                System.err.println(entry.getKey().getSimpleName() + " has no FlagType registered for the index " + i);
            }
        }
    }

    public static FlagType getFlag(Class<? extends FlagWatcher> watcherClass, int flagNo)
    {
        for (FlagType type : values())
        {
            if (type.getIndex() != flagNo)
                continue;

            if (!type.getFlagWatcher().isAssignableFrom(watcherClass))
                continue;

            return type;
        }

        return null;
    }

    public static FlagType[] values()
    {
        return _values;
    }

    private Y _defaultValue;

    private int _index;

    private Class<? extends FlagWatcher> _watcher;

    private FlagType(Class<? extends FlagWatcher> watcher, int index, Y defaultValue)
    {
        _index = index;
        _watcher = watcher;

        _values = Arrays.copyOf(_values, _values.length + 1);
        _values[_values.length - 1] = this;
    }

    public Y getDefault()
    {
        return _defaultValue;
    }

    public Class<? extends FlagWatcher> getFlagWatcher()
    {
        return _watcher;
    }

    public int getIndex()
    {
        return _index;
    }
}
