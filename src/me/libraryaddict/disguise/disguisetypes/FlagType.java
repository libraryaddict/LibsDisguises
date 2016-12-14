package me.libraryaddict.disguise.disguisetypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.base.Optional;

import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AreaEffectCloudWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BlazeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ChestedHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EnderCrystalWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EnderDragonWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EndermanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EvokerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FireworkWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FishingHookWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GhastWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.InsentientWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IronGolemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemFrameWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LlamaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.OcelotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PolarBearWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RabbitWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ShulkerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SkeletonWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnowmanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SpiderWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SplashPotionWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TNTWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VexWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VindicatorWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitchWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitherSkullWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitherWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieVillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;

public class FlagType<Y> {
    private static FlagType[] _values = new FlagType[0];

    public static FlagType<Boolean> AGEABLE_BABY = new FlagType<Boolean>(AgeableWatcher.class, 0, false);

    public static FlagType<Integer> AREA_EFFECT_CLOUD_COLOR = new FlagType<Integer>(AreaEffectCloudWatcher.class, 1,
            Color.BLACK.asRGB());

    public static FlagType<Boolean> AREA_EFFECT_IGNORE_RADIUS = new FlagType<Boolean>(AreaEffectCloudWatcher.class, 2, false);

    public static FlagType<Integer> AREA_EFFECT_PARTICLE = new FlagType<Integer>(AreaEffectCloudWatcher.class, 3, 0);

    public static FlagType<Integer> AREA_EFFECT_PARTICLE_PARAM_1 = new FlagType<Integer>(AreaEffectCloudWatcher.class, 4, 0);

    public static FlagType<Integer> AREA_EFFECT_PARTICLE_PARAM_2 = new FlagType<Integer>(AreaEffectCloudWatcher.class, 5, 0);

    public static FlagType<Float> AREA_EFFECT_RADIUS = new FlagType<Float>(AreaEffectCloudWatcher.class, 0, 0F);

    public static FlagType<Vector3F> ARMORSTAND_BODY = new FlagType<Vector3F>(ArmorStandWatcher.class, 2, new Vector3F(0, 0, 0));

    public static FlagType<Vector3F> ARMORSTAND_HEAD = new FlagType<Vector3F>(ArmorStandWatcher.class, 1, new Vector3F(0, 0, 0));

    public static FlagType<Vector3F> ARMORSTAND_LEFT_ARM = new FlagType<Vector3F>(ArmorStandWatcher.class, 3,
            new Vector3F(0, 0, 0));

    public static FlagType<Vector3F> ARMORSTAND_LEFT_LEG = new FlagType<Vector3F>(ArmorStandWatcher.class, 5,
            new Vector3F(0, 0, 0));

    public static FlagType<Byte> ARMORSTAND_META = new FlagType<Byte>(ArmorStandWatcher.class, 0, (byte) 0);

    public static FlagType<Vector3F> ARMORSTAND_RIGHT_ARM = new FlagType<Vector3F>(ArmorStandWatcher.class, 4,
            new Vector3F(0, 0, 0));

    public static FlagType<Vector3F> ARMORSTAND_RIGHT_LEG = new FlagType<Vector3F>(ArmorStandWatcher.class, 6,
            new Vector3F(0, 0, 0));

    public static FlagType<Byte> ARROW_CRITICAL = new FlagType<Byte>(ArrowWatcher.class, 0, (byte) 0);

    public static FlagType<Byte> BAT_HANGING = new FlagType<Byte>(BatWatcher.class, 0, (byte) 1);

    public static FlagType<Byte> BLAZE_BLAZING = new FlagType<Byte>(BlazeWatcher.class, 0, (byte) 0);

    public static FlagType<Float> BOAT_DAMAGE = new FlagType<Float>(BoatWatcher.class, 2, 40F);

    public static FlagType<Integer> BOAT_DIRECTION = new FlagType<Integer>(BoatWatcher.class, 1, 0);

    public static FlagType<Integer> BOAT_LAST_HIT = new FlagType<Integer>(BoatWatcher.class, 0, 0);

    public static FlagType<Boolean> BOAT_LEFT_PADDLING = new FlagType<Boolean>(BoatWatcher.class, 5, false);

    public static FlagType<Boolean> BOAT_RIGHT_PADDLING = new FlagType<Boolean>(BoatWatcher.class, 4, false);

    public static FlagType<Integer> BOAT_TYPE = new FlagType<Integer>(BoatWatcher.class, 3, 0);

    public static FlagType<Boolean> CREEPER_IGNITED = new FlagType<Boolean>(CreeperWatcher.class, 2, false);

    public static FlagType<Boolean> CREEPER_POWERED = new FlagType<Boolean>(CreeperWatcher.class, 1, false);

    public static FlagType<Integer> CREEPER_STATE = new FlagType<Integer>(CreeperWatcher.class, 0, -1);

    public static FlagType<ItemStack> DROPPED_ITEM = new FlagType<ItemStack>(DroppedItemWatcher.class, 0,
            new ItemStack(Material.STONE));

    public static FlagType<Optional<BlockPosition>> ENDER_CRYSTAL_BEAM = new FlagType<Optional<BlockPosition>>(
            EnderCrystalWatcher.class, 0, Optional.<BlockPosition> absent());

    public static FlagType<Boolean> ENDER_CRYSTAL_PLATE = new FlagType<Boolean>(EnderCrystalWatcher.class, 1, false);

    public static FlagType<Integer> ENDERD_RAGON_PHASE = new FlagType<Integer>(EnderDragonWatcher.class, 0, 0);

    public static FlagType<Boolean> ENDERMAN_AGRESSIVE = new FlagType<Boolean>(EndermanWatcher.class, 1, false);

    public static FlagType<Optional<WrappedBlockData>> ENDERMAN_ITEM = new FlagType<Optional<WrappedBlockData>>(
            EndermanWatcher.class, 0, Optional.<WrappedBlockData> absent());

    public static FlagType<Integer> ENTITY_AIR_TICKS = new FlagType<Integer>(FlagWatcher.class, 1, 0);

    public static FlagType<String> ENTITY_CUSTOM_NAME = new FlagType<String>(FlagWatcher.class, 2, "");

    public static FlagType<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new FlagType<Boolean>(FlagWatcher.class, 3, false);

    public static FlagType<Byte> ENTITY_META = new FlagType<Byte>(FlagWatcher.class, 0, (byte) 0);

    public static FlagType<Boolean> ENTITY_NO_GRAVITY = new FlagType<Boolean>(FlagWatcher.class, 5, false);

    public static FlagType<Boolean> ENTITY_SILENT = new FlagType<Boolean>(FlagWatcher.class, 4, false);

    public static FlagType<Byte> EVOKER_SPELL_TICKS = new FlagType<Byte>(EvokerWatcher.class, 0, (byte) 0);

    public static FlagType<BlockPosition> FALLING_BLOCK_POSITION = new FlagType<BlockPosition>(FallingBlockWatcher.class, 0,
            BlockPosition.ORIGIN);

    public static FlagType<ItemStack> FIREWORK_ITEM = new FlagType<ItemStack>(FireworkWatcher.class, 0,
            new ItemStack(Material.AIR));

    public static FlagType<Integer> FISHING_HOOK_HOOKED = new FlagType<Integer>(FishingHookWatcher.class, 0, 0);

    public static FlagType<Boolean> GHAST_AGRESSIVE = new FlagType<Boolean>(GhastWatcher.class, 0, false);

    public static FlagType<Boolean> GUARDIAN_RETRACT_SPIKES = new FlagType<Boolean>(GuardianWatcher.class, 0, false);

    public static FlagType<Integer> GUARDIAN_TARGET = new FlagType<Integer>(GuardianWatcher.class, 1, 0);

    public static FlagType<Integer> HORSE_ARMOR = new FlagType<Integer>(HorseWatcher.class, 1, 0);

    public static FlagType<Boolean> HORSE_CHESTED_CARRYING_CHEST = new FlagType<Boolean>(ChestedHorseWatcher.class, 0, false);

    public static FlagType<Integer> HORSE_COLOR = new FlagType<Integer>(HorseWatcher.class, 0, 0);

    public static FlagType<Byte> HORSE_META = new FlagType<Byte>(AbstractHorseWatcher.class, 0, (byte) 0);

    public static FlagType<Optional<UUID>> HORSE_OWNER = new FlagType<Optional<UUID>>(AbstractHorseWatcher.class, 1,
            Optional.<UUID> absent());

    // public static FlagType<Integer> HORSE_VARIANT = new FlagType<Integer>(HorseWatcher.class, 0, 0);

    public static FlagType<Byte> INSENTIENT_META = new FlagType<Byte>(InsentientWatcher.class, 0, (byte) 0);

    public static FlagType<Byte> IRON_GOLEM_PLAYER_CREATED = new FlagType<Byte>(IronGolemWatcher.class, 0, (byte) 0);

    public static FlagType<ItemStack> ITEMFRAME_ITEM = new FlagType<ItemStack>(ItemFrameWatcher.class, 0,
            new ItemStack(Material.AIR));

    public static FlagType<Integer> ITEMFRAME_ROTATION = new FlagType<Integer>(ItemFrameWatcher.class, 1, 0);

    public static FlagType<Integer> LIVING_ARROWS = new FlagType<Integer>(LivingWatcher.class, 4, 0);

    public static FlagType<Byte> LIVING_HAND = new FlagType<Byte>(LivingWatcher.class, 0, (byte) 0);

    public static FlagType<Float> LIVING_HEALTH = new FlagType<Float>(LivingWatcher.class, 1, 1F);

    public static FlagType<Boolean> LIVING_POTION_AMBIENT = new FlagType<Boolean>(LivingWatcher.class, 3, false);

    public static FlagType<Integer> LIVING_POTIONS = new FlagType<Integer>(LivingWatcher.class, 2, 0);

    public static FlagType<Integer> LLAMA_CARPET = new FlagType<Integer>(LlamaWatcher.class, 1, 0);

    public static FlagType<Integer> LLAMA_COLOR = new FlagType<Integer>(LlamaWatcher.class, 2, -1);

    public static FlagType<Integer> LLAMA_STRENGTH = new FlagType<Integer>(LlamaWatcher.class, 0, 0);

    public static FlagType<Integer> MINECART_BLOCK = new FlagType<Integer>(MinecartWatcher.class, 3, 0);

    public static FlagType<Boolean> MINECART_BLOCK_VISIBLE = new FlagType<Boolean>(MinecartWatcher.class, 5, false);

    public static FlagType<Integer> MINECART_BLOCK_Y = new FlagType<Integer>(MinecartWatcher.class, 4, 0);

    public static FlagType<Integer> MINECART_SHAKING_DIRECTION = new FlagType<Integer>(MinecartWatcher.class, 1, 1);

    public static FlagType<Float> MINECART_SHAKING_MULITPLIER = new FlagType<Float>(MinecartWatcher.class, 2, 0F);

    public static FlagType<Integer> MINECART_SHAKING_POWER = new FlagType<Integer>(MinecartWatcher.class, 0, 0);

    public static FlagType<Integer> OCELOT_TYPE = new FlagType<Integer>(OcelotWatcher.class, 0, 0);

    public static FlagType<Boolean> PIG_SADDLED = new FlagType<Boolean>(PigWatcher.class, 0, false);

    public static FlagType<Float> PLAYER_ABSORPTION = new FlagType<Float>(PlayerWatcher.class, 0, 0F);

    public static FlagType<Byte> PLAYER_HAND = new FlagType<Byte>(PlayerWatcher.class, 3, (byte) 0);

    public static FlagType<Integer> PLAYER_SCORE = new FlagType<Integer>(PlayerWatcher.class, 1, 0);

    public static FlagType<Byte> PLAYER_SKIN = new FlagType<Byte>(PlayerWatcher.class, 2, (byte) 127);

    public static FlagType<Boolean> POLAR_BEAR_STANDING = new FlagType<Boolean>(PolarBearWatcher.class, 0, false);

    public static FlagType<Integer> RABBIT_TYPE = new FlagType<Integer>(RabbitWatcher.class, 0, 0);

    public static FlagType<Byte> SHEEP_WOOL = new FlagType<Byte>(SheepWatcher.class, 0, (byte) 0);

    public static FlagType<Optional<BlockPosition>> SHULKER_ATTACHED = new FlagType<Optional<BlockPosition>>(ShulkerWatcher.class,
            1, Optional.<BlockPosition> absent());

    public static FlagType<Byte> SHULKER_COLOR = new FlagType<Byte>(ShulkerWatcher.class, 3, (byte) 10);

    public static FlagType<Direction> SHULKER_FACING = new FlagType<Direction>(ShulkerWatcher.class, 0, Direction.DOWN);

    public static FlagType<Byte> SHULKER_PEEKING = new FlagType<Byte>(ShulkerWatcher.class, 2, (byte) 0);

    public static FlagType<Boolean> SKELETON_SWING_ARMS = new FlagType<Boolean>(SkeletonWatcher.class, 0, false);

    public static FlagType<Integer> SLIME_SIZE = new FlagType<Integer>(SlimeWatcher.class, 0, 0);

    public static FlagType<Byte> SNOWMAN_HAT = new FlagType<Byte>(SnowmanWatcher.class, 0, (byte) 0);

    public static FlagType<Byte> SPIDER_CLIMB = new FlagType<Byte>(SpiderWatcher.class, 0, (byte) 0);

    public static FlagType<ItemStack> SPLASH_POTION_ITEM = new FlagType<ItemStack>(SplashPotionWatcher.class, 1,
            new ItemStack(Material.SPLASH_POTION)); // Yeah, the '1' isn't a bug. No idea why but MC thinks
                                                    // there's a '0' already.

    public static FlagType<ItemStack> SPLASH_POTION_ITEM_BAD = new FlagType<ItemStack>(SplashPotionWatcher.class, 0,
            new ItemStack(Material.SPLASH_POTION)); // Yeah, the '1' isn't a bug. No
                                                    // idea why but MC thinks there's a
                                                    // '0' already.

    public static FlagType<Byte> TAMEABLE_META = new FlagType<Byte>(TameableWatcher.class, 0, (byte) 0);

    public static FlagType<Optional<UUID>> TAMEABLE_OWNER = new FlagType<Optional<UUID>>(TameableWatcher.class, 1,
            Optional.<UUID> absent());

    public static FlagType<Integer> TIPPED_ARROW_COLOR = new FlagType<Integer>(ArrowWatcher.class, 1, Color.WHITE.asRGB());

    public static FlagType<Integer> TNT_FUSE_TICKS = new FlagType<Integer>(TNTWatcher.class, 0, Integer.MAX_VALUE);

    public static FlagType<Byte> VEX_ANGRY = new FlagType<Byte>(VexWatcher.class, 0, (byte) 0);

    public static FlagType<Integer> VILLAGER_PROFESSION = new FlagType<Integer>(VillagerWatcher.class, 0, 0);

    public static FlagType<Byte> VINDICATOR_JOHNNY = new FlagType<Byte>(VindicatorWatcher.class, 0, (byte) 0);

    public static FlagType<Boolean> WITCH_AGGRESSIVE = new FlagType<Boolean>(WitchWatcher.class, 0, false);

    public static FlagType<Integer> WITHER_INVUL = new FlagType<Integer>(WitherWatcher.class, 3, 0);

    public static FlagType<Integer> WITHER_TARGET_1 = new FlagType<Integer>(WitherWatcher.class, 0, 0);

    public static FlagType<Integer> WITHER_TARGET_2 = new FlagType<Integer>(WitherWatcher.class, 1, 0);

    public static FlagType<Integer> WITHER_TARGET_3 = new FlagType<Integer>(WitherWatcher.class, 2, 0);

    public static FlagType<Boolean> WITHER_SKULL_BLUE = new FlagType<Boolean>(WitherSkullWatcher.class, 0, false);

    public static FlagType<Boolean> WOLF_BEGGING = new FlagType<Boolean>(WolfWatcher.class, 1, false);

    public static FlagType<Integer> WOLF_COLLAR = new FlagType<Integer>(WolfWatcher.class, 2, 14);

    public static FlagType<Float> WOLF_DAMAGE = new FlagType<Float>(WolfWatcher.class, 0, 0F);

    public static FlagType<Boolean> ZOMBIE_AGGRESSIVE = new FlagType<Boolean>(ZombieWatcher.class, 2, false);

    public static FlagType<Boolean> ZOMBIE_BABY = new FlagType<Boolean>(ZombieWatcher.class, 0, false);

    public static FlagType<Integer> ZOMBIE_PLACEHOLDER = new FlagType<Integer>(ZombieWatcher.class, 1, 0);

    public static FlagType<Integer> ZOMBIE_VILLAGER_PROFESSION = new FlagType<Integer>(ZombieVillagerWatcher.class, 1, 0);

    public static FlagType<Boolean> ZOMBIE_VILLAGER_SHAKING = new FlagType<Boolean>(ZombieVillagerWatcher.class, 0, false);

    static {
        for (FlagType flagType : values()) {
            if (flagType.getFlagWatcher() == FlagWatcher.class)
                continue;

            flagType._index += getNoIndexes(flagType.getFlagWatcher().getSuperclass());

        }

        // Simple verification for the dev that he's setting up the FlagType's properly.
        // All flag types should be from 0 to <Max Number> with no empty numbers.
        // All flag types should never occur twice.

        HashMap<Class, Integer> maxValues = new HashMap<Class, Integer>();

        for (FlagType type : values()) {
            if (maxValues.containsKey(type.getFlagWatcher()) && maxValues.get(type.getFlagWatcher()) > type.getIndex())
                continue;

            maxValues.put(type.getFlagWatcher(), type.getIndex());
        }

        for (Entry<Class, Integer> entry : maxValues.entrySet()) {
            loop:

            for (int i = 0; i < entry.getValue(); i++) {
                FlagType found = null;

                for (FlagType type : values()) {
                    if (type.getIndex() != i)
                        continue;

                    if (!type.getFlagWatcher().isAssignableFrom(entry.getKey()))
                        continue;

                    if (found != null) {
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

    public static FlagType getFlag(Class<? extends FlagWatcher> watcherClass, int flagNo) {
        for (FlagType type : values()) {
            if (type.getIndex() != flagNo)
                continue;

            if (!type.getFlagWatcher().isAssignableFrom(watcherClass))
                continue;

            return type;
        }

        return null;
    }

    public static ArrayList<FlagType> getFlags(Class<? extends FlagWatcher> watcherClass) {
        ArrayList<FlagType> list = new ArrayList<FlagType>();

        for (FlagType type : values()) {
            if (!type.getFlagWatcher().isAssignableFrom(watcherClass))
                continue;

            list.add(type);
        }

        return list;
    }

    private static int getNoIndexes(Class c) {
        int found = 0;

        for (FlagType type : values()) {
            if (type.getFlagWatcher() != c)
                continue;

            found++;
        }

        if (c != FlagWatcher.class) {
            found += getNoIndexes(c.getSuperclass());
        }

        return found;
    }

    public static FlagType[] values() {
        return _values;
    }

    private Y _defaultValue;
    private int _index;
    private Class<? extends FlagWatcher> _watcher;

    private FlagType(Class<? extends FlagWatcher> watcher, int index, Y defaultValue) {
        _index = index;
        _watcher = watcher;
        _defaultValue = defaultValue;

        _values = Arrays.copyOf(_values, _values.length + 1);
        _values[_values.length - 1] = this;
    }

    public Y getDefault() {
        return _defaultValue;
    }

    public Class<? extends FlagWatcher> getFlagWatcher() {
        return _watcher;
    }

    public int getIndex() {
        return _index;
    }
}
