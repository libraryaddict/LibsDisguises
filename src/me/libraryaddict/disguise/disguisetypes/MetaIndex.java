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

public class MetaIndex<Y> {
    private static MetaIndex[] _values = new MetaIndex[0];

    public static MetaIndex<Boolean> AGEABLE_BABY = new MetaIndex<Boolean>(AgeableWatcher.class, 0, false);

    public static MetaIndex<Integer> AREA_EFFECT_CLOUD_COLOR = new MetaIndex<Integer>(AreaEffectCloudWatcher.class, 1,
            Color.BLACK.asRGB());

    public static MetaIndex<Boolean> AREA_EFFECT_IGNORE_RADIUS = new MetaIndex<Boolean>(AreaEffectCloudWatcher.class, 2, false);

    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE = new MetaIndex<Integer>(AreaEffectCloudWatcher.class, 3, 0);

    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_PARAM_1 = new MetaIndex<Integer>(AreaEffectCloudWatcher.class, 4, 0);

    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_PARAM_2 = new MetaIndex<Integer>(AreaEffectCloudWatcher.class, 5, 0);

    public static MetaIndex<Float> AREA_EFFECT_RADIUS = new MetaIndex<Float>(AreaEffectCloudWatcher.class, 0, 0F);

    public static MetaIndex<Vector3F> ARMORSTAND_BODY = new MetaIndex<Vector3F>(ArmorStandWatcher.class, 2,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_HEAD = new MetaIndex<Vector3F>(ArmorStandWatcher.class, 1,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_LEFT_ARM = new MetaIndex<Vector3F>(ArmorStandWatcher.class, 3,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_LEFT_LEG = new MetaIndex<Vector3F>(ArmorStandWatcher.class, 5,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Byte> ARMORSTAND_META = new MetaIndex<Byte>(ArmorStandWatcher.class, 0, (byte) 0);

    public static MetaIndex<Vector3F> ARMORSTAND_RIGHT_ARM = new MetaIndex<Vector3F>(ArmorStandWatcher.class, 4,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_RIGHT_LEG = new MetaIndex<Vector3F>(ArmorStandWatcher.class, 6,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Byte> ARROW_CRITICAL = new MetaIndex<Byte>(ArrowWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> BAT_HANGING = new MetaIndex<Byte>(BatWatcher.class, 0, (byte) 1);

    public static MetaIndex<Byte> BLAZE_BLAZING = new MetaIndex<Byte>(BlazeWatcher.class, 0, (byte) 0);

    public static MetaIndex<Float> BOAT_DAMAGE = new MetaIndex<Float>(BoatWatcher.class, 2, 40F);

    public static MetaIndex<Integer> BOAT_DIRECTION = new MetaIndex<Integer>(BoatWatcher.class, 1, 0);

    public static MetaIndex<Integer> BOAT_LAST_HIT = new MetaIndex<Integer>(BoatWatcher.class, 0, 0);

    public static MetaIndex<Boolean> BOAT_LEFT_PADDLING = new MetaIndex<Boolean>(BoatWatcher.class, 5, false);

    public static MetaIndex<Boolean> BOAT_RIGHT_PADDLING = new MetaIndex<Boolean>(BoatWatcher.class, 4, false);

    public static MetaIndex<Integer> BOAT_TYPE = new MetaIndex<Integer>(BoatWatcher.class, 3, 0);

    public static MetaIndex<Boolean> CREEPER_IGNITED = new MetaIndex<Boolean>(CreeperWatcher.class, 2, false);

    public static MetaIndex<Boolean> CREEPER_POWERED = new MetaIndex<Boolean>(CreeperWatcher.class, 1, false);

    public static MetaIndex<Integer> CREEPER_STATE = new MetaIndex<Integer>(CreeperWatcher.class, 0, -1);

    public static MetaIndex<ItemStack> DROPPED_ITEM = new MetaIndex<ItemStack>(DroppedItemWatcher.class, 0,
            new ItemStack(Material.STONE));

    public static MetaIndex<Optional<BlockPosition>> ENDER_CRYSTAL_BEAM = new MetaIndex<Optional<BlockPosition>>(
            EnderCrystalWatcher.class, 0, Optional.<BlockPosition> absent());

    public static MetaIndex<Boolean> ENDER_CRYSTAL_PLATE = new MetaIndex<Boolean>(EnderCrystalWatcher.class, 1, false);

    public static MetaIndex<Integer> ENDERD_RAGON_PHASE = new MetaIndex<Integer>(EnderDragonWatcher.class, 0, 0);

    public static MetaIndex<Boolean> ENDERMAN_AGRESSIVE = new MetaIndex<Boolean>(EndermanWatcher.class, 1, false);

    public static MetaIndex<Optional<WrappedBlockData>> ENDERMAN_ITEM = new MetaIndex<Optional<WrappedBlockData>>(
            EndermanWatcher.class, 0, Optional.<WrappedBlockData> absent());

    public static MetaIndex<Integer> ENTITY_AIR_TICKS = new MetaIndex<Integer>(FlagWatcher.class, 1, 0);

    public static MetaIndex<String> ENTITY_CUSTOM_NAME = new MetaIndex<String>(FlagWatcher.class, 2, "");

    public static MetaIndex<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new MetaIndex<Boolean>(FlagWatcher.class, 3, false);

    public static MetaIndex<Byte> ENTITY_META = new MetaIndex<Byte>(FlagWatcher.class, 0, (byte) 0);

    public static MetaIndex<Boolean> ENTITY_NO_GRAVITY = new MetaIndex<Boolean>(FlagWatcher.class, 5, false);

    public static MetaIndex<Boolean> ENTITY_SILENT = new MetaIndex<Boolean>(FlagWatcher.class, 4, false);

    public static MetaIndex<Byte> EVOKER_SPELL_TICKS = new MetaIndex<Byte>(EvokerWatcher.class, 0, (byte) 0);

    public static MetaIndex<BlockPosition> FALLING_BLOCK_POSITION = new MetaIndex<BlockPosition>(FallingBlockWatcher.class, 0,
            BlockPosition.ORIGIN);

    public static MetaIndex<ItemStack> FIREWORK_ITEM = new MetaIndex<ItemStack>(FireworkWatcher.class, 0,
            new ItemStack(Material.AIR));

    public static MetaIndex<Integer> FIREWORK_ATTACHED_ENTITY = new MetaIndex<Integer>(FireworkWatcher.class, 1, 0);

    public static MetaIndex<Integer> FISHING_HOOK_HOOKED = new MetaIndex<Integer>(FishingHookWatcher.class, 0, 0);

    public static MetaIndex<Boolean> GHAST_AGRESSIVE = new MetaIndex<Boolean>(GhastWatcher.class, 0, false);

    public static MetaIndex<Boolean> GUARDIAN_RETRACT_SPIKES = new MetaIndex<Boolean>(GuardianWatcher.class, 0, false);

    public static MetaIndex<Integer> GUARDIAN_TARGET = new MetaIndex<Integer>(GuardianWatcher.class, 1, 0);

    public static MetaIndex<Integer> HORSE_ARMOR = new MetaIndex<Integer>(HorseWatcher.class, 1, 0);

    public static MetaIndex<Boolean> HORSE_CHESTED_CARRYING_CHEST = new MetaIndex<Boolean>(ChestedHorseWatcher.class, 0, false);

    public static MetaIndex<Integer> HORSE_COLOR = new MetaIndex<Integer>(HorseWatcher.class, 0, 0);

    public static MetaIndex<Byte> HORSE_META = new MetaIndex<Byte>(AbstractHorseWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<UUID>> HORSE_OWNER = new MetaIndex<Optional<UUID>>(AbstractHorseWatcher.class, 1,
            Optional.<UUID> absent());

    public static MetaIndex<Byte> INSENTIENT_META = new MetaIndex<Byte>(InsentientWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> IRON_GOLEM_PLAYER_CREATED = new MetaIndex<Byte>(IronGolemWatcher.class, 0, (byte) 0);

    public static MetaIndex<ItemStack> ITEMFRAME_ITEM = new MetaIndex<ItemStack>(ItemFrameWatcher.class, 0,
            new ItemStack(Material.AIR));

    public static MetaIndex<Integer> ITEMFRAME_ROTATION = new MetaIndex<Integer>(ItemFrameWatcher.class, 1, 0);

    public static MetaIndex<Integer> LIVING_ARROWS = new MetaIndex<Integer>(LivingWatcher.class, 4, 0);

    public static MetaIndex<Byte> LIVING_HAND = new MetaIndex<Byte>(LivingWatcher.class, 0, (byte) 0);

    public static MetaIndex<Float> LIVING_HEALTH = new MetaIndex<Float>(LivingWatcher.class, 1, 1F);

    public static MetaIndex<Boolean> LIVING_POTION_AMBIENT = new MetaIndex<Boolean>(LivingWatcher.class, 3, false);

    public static MetaIndex<Integer> LIVING_POTIONS = new MetaIndex<Integer>(LivingWatcher.class, 2, 0);

    public static MetaIndex<Integer> LLAMA_CARPET = new MetaIndex<Integer>(LlamaWatcher.class, 1, 0);

    public static MetaIndex<Integer> LLAMA_COLOR = new MetaIndex<Integer>(LlamaWatcher.class, 2, -1);

    public static MetaIndex<Integer> LLAMA_STRENGTH = new MetaIndex<Integer>(LlamaWatcher.class, 0, 0);

    public static MetaIndex<Integer> MINECART_BLOCK = new MetaIndex<Integer>(MinecartWatcher.class, 3, 0);

    public static MetaIndex<Boolean> MINECART_BLOCK_VISIBLE = new MetaIndex<Boolean>(MinecartWatcher.class, 5, false);

    public static MetaIndex<Integer> MINECART_BLOCK_Y = new MetaIndex<Integer>(MinecartWatcher.class, 4, 0);

    public static MetaIndex<Integer> MINECART_SHAKING_DIRECTION = new MetaIndex<Integer>(MinecartWatcher.class, 1, 1);

    public static MetaIndex<Float> MINECART_SHAKING_MULITPLIER = new MetaIndex<Float>(MinecartWatcher.class, 2, 0F);

    public static MetaIndex<Integer> MINECART_SHAKING_POWER = new MetaIndex<Integer>(MinecartWatcher.class, 0, 0);

    public static MetaIndex<Integer> OCELOT_TYPE = new MetaIndex<Integer>(OcelotWatcher.class, 0, 0);

    public static MetaIndex<Boolean> PIG_SADDLED = new MetaIndex<Boolean>(PigWatcher.class, 0, false);

    public static MetaIndex<Integer> PIG_UNKNOWN = new MetaIndex<Integer>(PigWatcher.class, 1, 0);

    public static MetaIndex<Float> PLAYER_ABSORPTION = new MetaIndex<Float>(PlayerWatcher.class, 0, 0F);

    public static MetaIndex<Byte> PLAYER_HAND = new MetaIndex<Byte>(PlayerWatcher.class, 3, (byte) 0);

    public static MetaIndex<Integer> PLAYER_SCORE = new MetaIndex<Integer>(PlayerWatcher.class, 1, 0);

    public static MetaIndex<Byte> PLAYER_SKIN = new MetaIndex<Byte>(PlayerWatcher.class, 2, (byte) 127);

    public static MetaIndex<Boolean> POLAR_BEAR_STANDING = new MetaIndex<Boolean>(PolarBearWatcher.class, 0, false);

    public static MetaIndex<Integer> RABBIT_TYPE = new MetaIndex<Integer>(RabbitWatcher.class, 0, 0);

    public static MetaIndex<Byte> SHEEP_WOOL = new MetaIndex<Byte>(SheepWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<BlockPosition>> SHULKER_ATTACHED = new MetaIndex<Optional<BlockPosition>>(
            ShulkerWatcher.class, 1, Optional.<BlockPosition> absent());

    public static MetaIndex<Byte> SHULKER_COLOR = new MetaIndex<Byte>(ShulkerWatcher.class, 3, (byte) 10);

    public static MetaIndex<Direction> SHULKER_FACING = new MetaIndex<Direction>(ShulkerWatcher.class, 0, Direction.DOWN);

    public static MetaIndex<Byte> SHULKER_PEEKING = new MetaIndex<Byte>(ShulkerWatcher.class, 2, (byte) 0);

    public static MetaIndex<Boolean> SKELETON_SWING_ARMS = new MetaIndex<Boolean>(SkeletonWatcher.class, 0, false);

    public static MetaIndex<Integer> SLIME_SIZE = new MetaIndex<Integer>(SlimeWatcher.class, 0, 0);

    public static MetaIndex<Byte> SNOWMAN_DERP = new MetaIndex<Byte>(SnowmanWatcher.class, 0, (byte) 16);

    public static MetaIndex<Byte> SPIDER_CLIMB = new MetaIndex<Byte>(SpiderWatcher.class, 0, (byte) 0);

    public static MetaIndex<ItemStack> SPLASH_POTION_ITEM = new MetaIndex<ItemStack>(SplashPotionWatcher.class, 1,
            new ItemStack(Material.SPLASH_POTION)); // Yeah, the '1' isn't a bug. No idea why but MC thinks
                                                    // there's a '0' already.

    public static MetaIndex<ItemStack> SPLASH_POTION_ITEM_BAD = new MetaIndex<ItemStack>(SplashPotionWatcher.class, 0,
            new ItemStack(Material.SPLASH_POTION)); // Yeah, the '1' isn't a bug. No
                                                    // idea why but MC thinks there's a
                                                    // '0' already.

    public static MetaIndex<Byte> TAMEABLE_META = new MetaIndex<Byte>(TameableWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<UUID>> TAMEABLE_OWNER = new MetaIndex<Optional<UUID>>(TameableWatcher.class, 1,
            Optional.<UUID> absent());

    public static MetaIndex<Integer> TIPPED_ARROW_COLOR = new MetaIndex<Integer>(ArrowWatcher.class, 1, Color.WHITE.asRGB());

    public static MetaIndex<Integer> TNT_FUSE_TICKS = new MetaIndex<Integer>(TNTWatcher.class, 0, Integer.MAX_VALUE);

    public static MetaIndex<Byte> VEX_ANGRY = new MetaIndex<Byte>(VexWatcher.class, 0, (byte) 0);

    public static MetaIndex<Integer> VILLAGER_PROFESSION = new MetaIndex<Integer>(VillagerWatcher.class, 0, 0);

    public static MetaIndex<Byte> VINDICATOR_JOHNNY = new MetaIndex<Byte>(VindicatorWatcher.class, 0, (byte) 0);

    public static MetaIndex<Boolean> WITCH_AGGRESSIVE = new MetaIndex<Boolean>(WitchWatcher.class, 0, false);

    public static MetaIndex<Integer> WITHER_INVUL = new MetaIndex<Integer>(WitherWatcher.class, 3, 0);

    public static MetaIndex<Integer> WITHER_TARGET_1 = new MetaIndex<Integer>(WitherWatcher.class, 0, 0);

    public static MetaIndex<Integer> WITHER_TARGET_2 = new MetaIndex<Integer>(WitherWatcher.class, 1, 0);

    public static MetaIndex<Integer> WITHER_TARGET_3 = new MetaIndex<Integer>(WitherWatcher.class, 2, 0);

    public static MetaIndex<Boolean> WITHER_SKULL_BLUE = new MetaIndex<Boolean>(WitherSkullWatcher.class, 0, false);

    public static MetaIndex<Boolean> WOLF_BEGGING = new MetaIndex<Boolean>(WolfWatcher.class, 1, false);

    public static MetaIndex<Integer> WOLF_COLLAR = new MetaIndex<Integer>(WolfWatcher.class, 2, 14);

    public static MetaIndex<Float> WOLF_DAMAGE = new MetaIndex<Float>(WolfWatcher.class, 0, 0F);

    public static MetaIndex<Boolean> ZOMBIE_AGGRESSIVE = new MetaIndex<Boolean>(ZombieWatcher.class, 2, false);

    public static MetaIndex<Boolean> ZOMBIE_BABY = new MetaIndex<Boolean>(ZombieWatcher.class, 0, false);

    public static MetaIndex<Integer> ZOMBIE_PLACEHOLDER = new MetaIndex<Integer>(ZombieWatcher.class, 1, 0);

    public static MetaIndex<Integer> ZOMBIE_VILLAGER_PROFESSION = new MetaIndex<Integer>(ZombieVillagerWatcher.class, 1, 0);

    public static MetaIndex<Boolean> ZOMBIE_VILLAGER_SHAKING = new MetaIndex<Boolean>(ZombieVillagerWatcher.class, 0, false);

    static {
        for (MetaIndex flagType : values()) {
            if (flagType.getFlagWatcher() == FlagWatcher.class)
                continue;

            flagType._index += getNoIndexes(flagType.getFlagWatcher().getSuperclass());

        }

        // Simple verification for the dev that he's setting up the FlagType's properly.
        // All flag types should be from 0 to <Max Number> with no empty numbers.
        // All flag types should never occur twice.

        HashMap<Class, Integer> maxValues = new HashMap<Class, Integer>();

        for (MetaIndex type : values()) {
            if (maxValues.containsKey(type.getFlagWatcher()) && maxValues.get(type.getFlagWatcher()) > type.getIndex())
                continue;

            maxValues.put(type.getFlagWatcher(), type.getIndex());
        }

        for (Entry<Class, Integer> entry : maxValues.entrySet()) {
            loop:

            for (int i = 0; i < entry.getValue(); i++) {
                MetaIndex found = null;

                for (MetaIndex type : values()) {
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

    public static MetaIndex getFlag(Class<? extends FlagWatcher> watcherClass, int flagNo) {
        for (MetaIndex type : values()) {
            if (type.getIndex() != flagNo)
                continue;

            if (!type.getFlagWatcher().isAssignableFrom(watcherClass))
                continue;

            return type;
        }

        return null;
    }

    public static ArrayList<MetaIndex> getFlags(Class<? extends FlagWatcher> watcherClass) {
        ArrayList<MetaIndex> list = new ArrayList<MetaIndex>();

        for (MetaIndex type : values()) {
            if (!type.getFlagWatcher().isAssignableFrom(watcherClass))
                continue;

            list.add(type);
        }

        return list;
    }

    private static int getNoIndexes(Class c) {
        int found = 0;

        for (MetaIndex type : values()) {
            if (type.getFlagWatcher() != c)
                continue;

            found++;
        }

        if (c != FlagWatcher.class) {
            found += getNoIndexes(c.getSuperclass());
        }

        return found;
    }

    public static MetaIndex[] values() {
        return _values;
    }

    private Y _defaultValue;
    private int _index;
    private Class<? extends FlagWatcher> _watcher;

    private MetaIndex(Class<? extends FlagWatcher> watcher, int index, Y defaultValue) {
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
