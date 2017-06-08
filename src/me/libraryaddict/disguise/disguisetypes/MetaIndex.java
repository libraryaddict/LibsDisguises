package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Optional;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class MetaIndex<Y> {
    private static MetaIndex[] _values = new MetaIndex[0];

    public static MetaIndex<Boolean> AGEABLE_BABY = new MetaIndex<>(AgeableWatcher.class, 0, false);

    public static MetaIndex<Integer> AREA_EFFECT_CLOUD_COLOR = new MetaIndex<>(AreaEffectCloudWatcher.class, 1,
            Color.BLACK.asRGB());

    public static MetaIndex<Boolean> AREA_EFFECT_IGNORE_RADIUS = new MetaIndex<>(AreaEffectCloudWatcher.class, 2,
            false);

    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE = new MetaIndex<>(AreaEffectCloudWatcher.class, 3, 0);

    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_PARAM_1 = new MetaIndex<>(AreaEffectCloudWatcher.class, 4, 0);

    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_PARAM_2 = new MetaIndex<>(AreaEffectCloudWatcher.class, 5, 0);

    public static MetaIndex<Float> AREA_EFFECT_RADIUS = new MetaIndex<>(AreaEffectCloudWatcher.class, 0, 0F);

    public static MetaIndex<Vector3F> ARMORSTAND_BODY = new MetaIndex<>(ArmorStandWatcher.class, 2,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_HEAD = new MetaIndex<>(ArmorStandWatcher.class, 1,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_LEFT_ARM = new MetaIndex<>(ArmorStandWatcher.class, 3,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_LEFT_LEG = new MetaIndex<>(ArmorStandWatcher.class, 5,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Byte> ARMORSTAND_META = new MetaIndex<>(ArmorStandWatcher.class, 0, (byte) 0);

    public static MetaIndex<Vector3F> ARMORSTAND_RIGHT_ARM = new MetaIndex<>(ArmorStandWatcher.class, 4,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Vector3F> ARMORSTAND_RIGHT_LEG = new MetaIndex<>(ArmorStandWatcher.class, 6,
            new Vector3F(0, 0, 0));

    public static MetaIndex<Byte> ARROW_CRITICAL = new MetaIndex<>(ArrowWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> BAT_HANGING = new MetaIndex<>(BatWatcher.class, 0, (byte) 1);

    public static MetaIndex<Byte> BLAZE_BLAZING = new MetaIndex<>(BlazeWatcher.class, 0, (byte) 0);

    public static MetaIndex<Float> BOAT_DAMAGE = new MetaIndex<>(BoatWatcher.class, 2, 40F);

    public static MetaIndex<Integer> BOAT_DIRECTION = new MetaIndex<>(BoatWatcher.class, 1, 0);

    public static MetaIndex<Integer> BOAT_LAST_HIT = new MetaIndex<>(BoatWatcher.class, 0, 0);

    public static MetaIndex<Boolean> BOAT_LEFT_PADDLING = new MetaIndex<>(BoatWatcher.class, 5, false);

    public static MetaIndex<Boolean> BOAT_RIGHT_PADDLING = new MetaIndex<>(BoatWatcher.class, 4, false);

    public static MetaIndex<Integer> BOAT_TYPE = new MetaIndex<>(BoatWatcher.class, 3, 0);

    public static MetaIndex<Boolean> CREEPER_IGNITED = new MetaIndex<>(CreeperWatcher.class, 2, false);

    public static MetaIndex<Boolean> CREEPER_POWERED = new MetaIndex<>(CreeperWatcher.class, 1, false);

    public static MetaIndex<Integer> CREEPER_STATE = new MetaIndex<>(CreeperWatcher.class, 0, -1);

    public static MetaIndex<ItemStack> DROPPED_ITEM = new MetaIndex<>(DroppedItemWatcher.class, 0,
            new ItemStack(Material.STONE));

    public static MetaIndex<Optional<BlockPosition>> ENDER_CRYSTAL_BEAM = new MetaIndex<>(EnderCrystalWatcher.class, 0,
            Optional.<BlockPosition>absent());

    public static MetaIndex<Boolean> ENDER_CRYSTAL_PLATE = new MetaIndex<>(EnderCrystalWatcher.class, 1, false);

    public static MetaIndex<Integer> ENDERD_RAGON_PHASE = new MetaIndex<>(EnderDragonWatcher.class, 0, 0);

    public static MetaIndex<Boolean> ENDERMAN_AGRESSIVE = new MetaIndex<>(EndermanWatcher.class, 1, false);

    public static MetaIndex<Optional<WrappedBlockData>> ENDERMAN_ITEM = new MetaIndex<>(EndermanWatcher.class, 0,
            Optional.<WrappedBlockData>absent());

    public static MetaIndex<Integer> ENTITY_AIR_TICKS = new MetaIndex<>(FlagWatcher.class, 1, 0);

    public static MetaIndex<String> ENTITY_CUSTOM_NAME = new MetaIndex<>(FlagWatcher.class, 2, "");

    public static MetaIndex<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new MetaIndex<>(FlagWatcher.class, 3, false);

    public static MetaIndex<Byte> ENTITY_META = new MetaIndex<>(FlagWatcher.class, 0, (byte) 0);

    public static MetaIndex<Boolean> ENTITY_NO_GRAVITY = new MetaIndex<>(FlagWatcher.class, 5, false);

    public static MetaIndex<Boolean> ENTITY_SILENT = new MetaIndex<>(FlagWatcher.class, 4, false);

    public static MetaIndex<Byte> ILLAGER_SPELL_TICKS = new MetaIndex<>(IllagerWizardWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> ILLAGER_META = new MetaIndex<>(IllagerWatcher.class, 0, (byte) 0);

    public static MetaIndex<BlockPosition> FALLING_BLOCK_POSITION = new MetaIndex<>(FallingBlockWatcher.class, 0,
            BlockPosition.ORIGIN);

    public static MetaIndex<ItemStack> FIREWORK_ITEM = new MetaIndex<>(FireworkWatcher.class, 0,
            new ItemStack(Material.AIR));

    public static MetaIndex<Integer> FIREWORK_ATTACHED_ENTITY = new MetaIndex<>(FireworkWatcher.class, 1, 0);

    public static MetaIndex<Integer> FISHING_HOOK_HOOKED = new MetaIndex<>(FishingHookWatcher.class, 0, 0);

    public static MetaIndex<Boolean> GHAST_AGRESSIVE = new MetaIndex<>(GhastWatcher.class, 0, false);

    public static MetaIndex<Boolean> GUARDIAN_RETRACT_SPIKES = new MetaIndex<>(GuardianWatcher.class, 0, false);

    public static MetaIndex<Integer> GUARDIAN_TARGET = new MetaIndex<>(GuardianWatcher.class, 1, 0);

    public static MetaIndex<Integer> HORSE_ARMOR = new MetaIndex<>(HorseWatcher.class, 1, 0);

    public static MetaIndex<Boolean> HORSE_CHESTED_CARRYING_CHEST = new MetaIndex<>(ChestedHorseWatcher.class, 0,
            false);

    public static MetaIndex<Integer> HORSE_COLOR = new MetaIndex<>(HorseWatcher.class, 0, 0);

    public static MetaIndex<Byte> HORSE_META = new MetaIndex<>(AbstractHorseWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<UUID>> HORSE_OWNER = new MetaIndex<>(AbstractHorseWatcher.class, 1,
            Optional.<UUID>absent());

    public static MetaIndex<Byte> INSENTIENT_META = new MetaIndex<>(InsentientWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> IRON_GOLEM_PLAYER_CREATED = new MetaIndex<>(IronGolemWatcher.class, 0, (byte) 0);

    public static MetaIndex<ItemStack> ITEMFRAME_ITEM = new MetaIndex<>(ItemFrameWatcher.class, 0,
            new ItemStack(Material.AIR));

    public static MetaIndex<Integer> ITEMFRAME_ROTATION = new MetaIndex<>(ItemFrameWatcher.class, 1, 0);

    public static MetaIndex<Integer> LIVING_ARROWS = new MetaIndex<>(LivingWatcher.class, 4, 0);

    public static MetaIndex<Byte> LIVING_HAND = new MetaIndex<>(LivingWatcher.class, 0, (byte) 0);

    public static MetaIndex<Float> LIVING_HEALTH = new MetaIndex<>(LivingWatcher.class, 1, 1F);

    public static MetaIndex<Boolean> LIVING_POTION_AMBIENT = new MetaIndex<>(LivingWatcher.class, 3, false);

    public static MetaIndex<Integer> LIVING_POTIONS = new MetaIndex<>(LivingWatcher.class, 2, 0);

    public static MetaIndex<Integer> LLAMA_CARPET = new MetaIndex<>(LlamaWatcher.class, 1, 0);

    public static MetaIndex<Integer> LLAMA_COLOR = new MetaIndex<>(LlamaWatcher.class, 2, -1);

    public static MetaIndex<Integer> LLAMA_STRENGTH = new MetaIndex<>(LlamaWatcher.class, 0, 0);

    public static MetaIndex<Integer> MINECART_BLOCK = new MetaIndex<>(MinecartWatcher.class, 3, 0);

    public static MetaIndex<Boolean> MINECART_BLOCK_VISIBLE = new MetaIndex<>(MinecartWatcher.class, 5, false);

    public static MetaIndex<Integer> MINECART_BLOCK_Y = new MetaIndex<>(MinecartWatcher.class, 4, 0);

    public static MetaIndex<Integer> MINECART_SHAKING_DIRECTION = new MetaIndex<>(MinecartWatcher.class, 1, 1);

    public static MetaIndex<Float> MINECART_SHAKING_MULITPLIER = new MetaIndex<>(MinecartWatcher.class, 2, 0F);

    public static MetaIndex<Integer> MINECART_SHAKING_POWER = new MetaIndex<>(MinecartWatcher.class, 0, 0);

    public static MetaIndex<Integer> OCELOT_TYPE = new MetaIndex<>(OcelotWatcher.class, 0, 0);

    public static MetaIndex<Integer> PARROT_VARIANT = new MetaIndex<>(ParrotWatcher.class, 0, 0);

    public static MetaIndex<Boolean> PIG_SADDLED = new MetaIndex<>(PigWatcher.class, 0, false);

    public static MetaIndex<Integer> PIG_UNKNOWN = new MetaIndex<>(PigWatcher.class, 1, 0);

    public static MetaIndex<Float> PLAYER_ABSORPTION = new MetaIndex<>(PlayerWatcher.class, 0, 0F);

    public static MetaIndex<Byte> PLAYER_HAND = new MetaIndex<>(PlayerWatcher.class, 3, (byte) 0);

    public static MetaIndex<Integer> PLAYER_SCORE = new MetaIndex<>(PlayerWatcher.class, 1, 0);

    public static MetaIndex<Byte> PLAYER_SKIN = new MetaIndex<>(PlayerWatcher.class, 2, (byte) 127);

    public static MetaIndex<NbtCompound> PLAYER_LEFT_SHOULDER_ENTITY = new MetaIndex<>(PlayerWatcher.class, 4,
            NbtFactory.ofCompound("None"));

    public static MetaIndex<NbtCompound> PLAYER_RIGHT_SHOULDER_ENTITY = new MetaIndex<>(PlayerWatcher.class, 5,
            NbtFactory.ofCompound("None"));

    public static MetaIndex<Boolean> POLAR_BEAR_STANDING = new MetaIndex<>(PolarBearWatcher.class, 0, false);

    public static MetaIndex<Integer> RABBIT_TYPE = new MetaIndex<>(RabbitWatcher.class, 0, 0);

    public static MetaIndex<Byte> SHEEP_WOOL = new MetaIndex<>(SheepWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<BlockPosition>> SHULKER_ATTACHED = new MetaIndex<>(ShulkerWatcher.class, 1,
            Optional.<BlockPosition>absent());

    public static MetaIndex<Byte> SHULKER_COLOR = new MetaIndex<>(ShulkerWatcher.class, 3, (byte) 10);

    public static MetaIndex<Direction> SHULKER_FACING = new MetaIndex<>(ShulkerWatcher.class, 0, Direction.DOWN);

    public static MetaIndex<Byte> SHULKER_PEEKING = new MetaIndex<>(ShulkerWatcher.class, 2, (byte) 0);

    public static MetaIndex<Boolean> SKELETON_SWING_ARMS = new MetaIndex<>(SkeletonWatcher.class, 0, false);

    public static MetaIndex<Integer> SLIME_SIZE = new MetaIndex<>(SlimeWatcher.class, 0, 0);

    public static MetaIndex<Byte> SNOWMAN_DERP = new MetaIndex<>(SnowmanWatcher.class, 0, (byte) 16);

    public static MetaIndex<Byte> SPIDER_CLIMB = new MetaIndex<>(SpiderWatcher.class, 0, (byte) 0);

    public static MetaIndex<ItemStack> SPLASH_POTION_ITEM = new MetaIndex<>(SplashPotionWatcher.class, 0,
            new ItemStack(Material.SPLASH_POTION));

    public static MetaIndex<Byte> TAMEABLE_META = new MetaIndex<>(TameableWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<UUID>> TAMEABLE_OWNER = new MetaIndex<>(TameableWatcher.class, 1,
            Optional.<UUID>absent());

    public static MetaIndex<Integer> TIPPED_ARROW_COLOR = new MetaIndex<>(ArrowWatcher.class, 1, Color.WHITE.asRGB());

    public static MetaIndex<Integer> TNT_FUSE_TICKS = new MetaIndex<>(TNTWatcher.class, 0, Integer.MAX_VALUE);

    public static MetaIndex<Byte> VEX_ANGRY = new MetaIndex<>(VexWatcher.class, 0, (byte) 0);

    public static MetaIndex<Integer> VILLAGER_PROFESSION = new MetaIndex<>(VillagerWatcher.class, 0, 0);

    public static MetaIndex<Byte> VINDICATOR_JOHNNY = new MetaIndex<>(VindicatorWatcher.class, 0, (byte) 0);

    public static MetaIndex<Boolean> WITCH_AGGRESSIVE = new MetaIndex<>(WitchWatcher.class, 0, false);

    public static MetaIndex<Integer> WITHER_INVUL = new MetaIndex<>(WitherWatcher.class, 3, 0);

    public static MetaIndex<Integer> WITHER_TARGET_1 = new MetaIndex<>(WitherWatcher.class, 0, 0);

    public static MetaIndex<Integer> WITHER_TARGET_2 = new MetaIndex<>(WitherWatcher.class, 1, 0);

    public static MetaIndex<Integer> WITHER_TARGET_3 = new MetaIndex<>(WitherWatcher.class, 2, 0);

    public static MetaIndex<Boolean> WITHER_SKULL_BLUE = new MetaIndex<>(WitherSkullWatcher.class, 0, false);

    public static MetaIndex<Boolean> WOLF_BEGGING = new MetaIndex<>(WolfWatcher.class, 1, false);

    public static MetaIndex<Integer> WOLF_COLLAR = new MetaIndex<>(WolfWatcher.class, 2, 14);

    public static MetaIndex<Float> WOLF_DAMAGE = new MetaIndex<>(WolfWatcher.class, 0, 0F);

    public static MetaIndex<Boolean> ZOMBIE_AGGRESSIVE = new MetaIndex<>(ZombieWatcher.class, 2, false);

    public static MetaIndex<Boolean> ZOMBIE_BABY = new MetaIndex<>(ZombieWatcher.class, 0, false);

    public static MetaIndex<Integer> ZOMBIE_PLACEHOLDER = new MetaIndex<>(ZombieWatcher.class, 1, 0);

    public static MetaIndex<Integer> ZOMBIE_VILLAGER_PROFESSION = new MetaIndex<>(ZombieVillagerWatcher.class, 1, 0);

    public static MetaIndex<Boolean> ZOMBIE_VILLAGER_SHAKING = new MetaIndex<>(ZombieVillagerWatcher.class, 0, false);

    static {
        setValues();
    }

    public static void fillInBlankIndexes() {
        ArrayList<Entry<Class, ArrayList<MetaIndex>>> list = new ArrayList<>();

        for (MetaIndex index : values()) {
            Entry<Class, ArrayList<MetaIndex>> entry = null;

            for (Entry e : list) {
                if (e.getKey() != index.getFlagWatcher())
                    continue;

                entry = e;
                break;
            }

            if (entry == null) {
                entry = new AbstractMap.SimpleEntry(index.getFlagWatcher(), new ArrayList<MetaIndex>());
                list.add(entry);
            }

            entry.getValue().add(index);
        }

        for (Entry<Class, ArrayList<MetaIndex>> entry : list) {
            Collections.sort(entry.getValue(), new Comparator<MetaIndex>() {
                @Override
                public int compare(MetaIndex o1, MetaIndex o2) {
                    return o1.getIndex() - o2.getIndex();
                }
            });

            for (MetaIndex ind : entry.getValue()) {
                ind._index = entry.getValue().indexOf(ind);
            }
        }
    }

    public static void orderMetaIndexes() {
        for (MetaIndex flagType : values()) {
            if (flagType.getFlagWatcher() == FlagWatcher.class)
                continue;

            flagType._index += getNoIndexes(flagType.getFlagWatcher().getSuperclass());
        }
    }

    public static void validateMetadata() {
        // Simple verification for the dev that he's setting up the FlagType's properly.
        // All flag types should be from 0 to <Max Number> with no empty numbers.
        // All flag types should never occur twice.

        HashMap<Class, Integer> maxValues = new HashMap<>();

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
                        System.err.println(
                                entry.getKey().getSimpleName() + " has multiple FlagType's registered for the index " + i + " (" + type.getFlagWatcher().getSimpleName() + ", " + found.getFlagWatcher().getSimpleName() + ")");
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

    public static void printMetadata() {
        ArrayList<String> toPrint = new ArrayList<>();

        try {
            for (Field field : MetaIndex.class.getFields()) {
                if (field.getType() != MetaIndex.class)
                    continue;

                MetaIndex index = (MetaIndex) field.get(null);

                toPrint.add(
                        index.getFlagWatcher().getSimpleName() + " " + field.getName() + " " + index.getIndex() + " " + index.getDefault().getClass().getSimpleName());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Collections.sort(toPrint, String.CASE_INSENSITIVE_ORDER);

        for (String s : toPrint) {
            System.out.println(s);
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
        ArrayList<MetaIndex> list = new ArrayList<>();

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

    public static void addMetaIndexes(MetaIndex... metaIndexes) {
        _values = Arrays.copyOf(values(), values().length + metaIndexes.length);

        for (int i = values().length - metaIndexes.length, a = 0; i < values().length; i++, a++) {
            MetaIndex index = metaIndexes[a];

            for (MetaIndex metaIndex : values()) {
                if (metaIndex.getFlagWatcher() != index.getFlagWatcher() || metaIndex.getIndex() != index.getIndex()) {
                    continue;
                }

                System.err.println(
                        "[LibsDisguises] MetaIndex " + metaIndex.getFlagWatcher().getSimpleName() + " at index " + metaIndex.getIndex() + " has already registered this! (" + metaIndex.getDefault() + "," + index.getDefault() + ")");
            }

            values()[i] = metaIndexes[a];
        }
    }

    public static void setValues() {
        try {
            _values = new MetaIndex[0];

            for (Field field : MetaIndex.class.getFields()) {
                if (field.getType() != MetaIndex.class)
                    continue;

                MetaIndex index = (MetaIndex) field.get(null);

                if (index == null)
                    continue;

                _values = Arrays.copyOf(_values, _values.length + 1);
                _values[_values.length - 1] = index;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if success, false if the field doesn't exist
     */
    public static boolean setMetaIndex(String name, MetaIndex metaIndex) {
        try {
            Field field = MetaIndex.class.getField(name);
            MetaIndex index = (MetaIndex) field.get(null);

            field.set(null, metaIndex);
        }
        catch (NoSuchFieldException ex) {
            return false;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private Y _defaultValue;
    private int _index;
    private Class<? extends FlagWatcher> _watcher;

    private MetaIndex(Class<? extends FlagWatcher> watcher, int index, Y defaultValue) {
        _index = index;
        _watcher = watcher;
        _defaultValue = defaultValue;
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
