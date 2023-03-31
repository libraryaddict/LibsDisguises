package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractSkeletonWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractVillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AllayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AreaEffectCloudWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AxolotlWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BeeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BlazeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BlockDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.BoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CamelWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ChestedHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DolphinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EnderCrystalWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EnderDragonWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EnderSignalWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EndermanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FallingBlockWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FireballWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FireworkWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FishingHookWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FoxWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FrogWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GhastWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GlowSquidWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HoglinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IllagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IllagerWizardWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.InsentientWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.InteractionWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IronGolemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemFrameWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LlamaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartCommandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartFurnaceWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MushroomCowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.OcelotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PaintingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PandaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ParrotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PhantomWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PigWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PiglinAbstractWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PiglinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PolarBearWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PufferFishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RabbitWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RaiderWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ShulkerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SkeletonWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnifferWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnowmanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SpiderWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SplashPotionWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.StriderWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TNTWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TextDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ThrowableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TippedArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TridentWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TropicalFishWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TurtleWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VexWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WardenWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitchWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitherSkullWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitherWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZoglinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieVillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class MetaIndex<Y> {
    private static MetaIndex[] _values = new MetaIndex[0];

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Boolean> ALLAY_DANCING = new MetaIndex<>(AllayWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Boolean> ALLAY_CAN_DUPLICATE = new MetaIndex<>(AllayWatcher.class, 1, true);
    /**
     * True if entity is a baby
     */
    public static MetaIndex<Boolean> AGEABLE_BABY = new MetaIndex<>(AgeableWatcher.class, 0, false);

    /**
     * The color of the Area Effect Cloud as RGB integer
     */
    public static MetaIndex<Integer> AREA_EFFECT_CLOUD_COLOR = new MetaIndex<>(AreaEffectCloudWatcher.class, 1, Color.BLACK.asRGB());

    /**
     * Ignore radius and show effect as single point, not area
     */
    public static MetaIndex<Boolean> AREA_EFFECT_IGNORE_RADIUS = new MetaIndex<>(AreaEffectCloudWatcher.class, 2, false);

    /**
     * The type of particle to display
     */
    @NmsAddedIn(NmsVersion.v1_13)
    public static MetaIndex<WrappedParticle> AREA_EFFECT_PARTICLE =
        new MetaIndex<>(AreaEffectCloudWatcher.class, 3, NmsVersion.v1_13.isSupported() ? WrappedParticle.create(Particle.SPELL_MOB, null) : null);

    @NmsRemovedIn(NmsVersion.v1_13)
    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_OLD = new MetaIndex<>(AreaEffectCloudWatcher.class, 3, 0);

    @NmsRemovedIn(NmsVersion.v1_13)
    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_PARAM_1_OLD = new MetaIndex<>(AreaEffectCloudWatcher.class, 4, 0);

    @NmsRemovedIn(NmsVersion.v1_13)
    public static MetaIndex<Integer> AREA_EFFECT_PARTICLE_PARAM_2_OLD = new MetaIndex<>(AreaEffectCloudWatcher.class, 5, 0);

    /**
     * The size of the area
     */
    public static MetaIndex<Float> AREA_EFFECT_RADIUS = new MetaIndex<>(AreaEffectCloudWatcher.class, 0, 3F);

    /**
     * Armorstand body eular vector
     */
    public static MetaIndex<Vector3F> ARMORSTAND_BODY = new MetaIndex<>(ArmorStandWatcher.class, 2, new Vector3F(0, 0, 0));

    /**
     * Armorstand head eular vector
     */
    public static MetaIndex<Vector3F> ARMORSTAND_HEAD = new MetaIndex<>(ArmorStandWatcher.class, 1, new Vector3F(0, 0, 0));

    /**
     * Armorstand left arm eular vector
     */
    public static MetaIndex<Vector3F> ARMORSTAND_LEFT_ARM = new MetaIndex<>(ArmorStandWatcher.class, 3, new Vector3F(-10, 0, -10));

    /**
     * Armorstand left leg eular vector
     */
    public static MetaIndex<Vector3F> ARMORSTAND_LEFT_LEG = new MetaIndex<>(ArmorStandWatcher.class, 5, new Vector3F(-1, 0, -1));

    /**
     * Armorstand metadata
     */
    public static MetaIndex<Byte> ARMORSTAND_META = new MetaIndex<>(ArmorStandWatcher.class, 0, (byte) 0);

    /**
     * Armorstand right arm eular vector
     */
    public static MetaIndex<Vector3F> ARMORSTAND_RIGHT_ARM = new MetaIndex<>(ArmorStandWatcher.class, 4, new Vector3F(-15, 0, 10));

    /**
     * Armorstand right leg eular vector
     */
    public static MetaIndex<Vector3F> ARMORSTAND_RIGHT_LEG = new MetaIndex<>(ArmorStandWatcher.class, 6, new Vector3F(1, 0, 1));

    /**
     * If the arrow is a critical strike
     */
    public static MetaIndex<Byte> ARROW_CRITICAL = new MetaIndex<>(ArrowWatcher.class, 0, (byte) 0);

    /**
     * The shooter of the arrow, no visible effect if set
     */
    @NmsAddedIn(NmsVersion.v1_13)
    @NmsRemovedIn(NmsVersion.v1_16)
    public static MetaIndex<Optional<UUID>> ARROW_UUID = new MetaIndex<>(ArrowWatcher.class, 1, Optional.empty());

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> ARROW_PIERCE_LEVEL = new MetaIndex<>(ArrowWatcher.class, 2, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Integer> AXOLOTL_VARIANT = new MetaIndex<>(AxolotlWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Boolean> AXOLOTL_PLAYING_DEAD = new MetaIndex<>(AxolotlWatcher.class, 1, false);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Boolean> AXOLOTL_FROM_BUCKET = new MetaIndex<>(AxolotlWatcher.class, 2, false);

    /**
     * If the bat is hanging, false/true state
     */
    public static MetaIndex<Byte> BAT_HANGING = new MetaIndex<>(BatWatcher.class, 0, (byte) 1);

    @NmsAddedIn(NmsVersion.v1_15)
    public static MetaIndex<Byte> BEE_META = new MetaIndex<>(BeeWatcher.class, 0, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_15)
    public static MetaIndex<Integer> BEE_ANGER = new MetaIndex<>(BeeWatcher.class, 1, 0);

    /**
     * If the blaze is ignited, false/true state
     */
    public static MetaIndex<Byte> BLAZE_BLAZING = new MetaIndex<>(BlazeWatcher.class, 0, (byte) 0);

    public static MetaIndex<BlockData> BLOCK_DISPLAY_BLOCK_STATE =
        new MetaIndex<>(BlockDisplayWatcher.class, 0, NmsVersion.v1_19_R3.isSupported() ? Bukkit.createBlockData(Material.AIR) : null);

    /**
     * How damaged the boat is
     */
    public static MetaIndex<Float> BOAT_DAMAGE = new MetaIndex<>(BoatWatcher.class, 2, 0F);

    public static MetaIndex<Integer> BOAT_DIRECTION = new MetaIndex<>(BoatWatcher.class, 1, 1);

    public static MetaIndex<Integer> BOAT_LAST_HIT = new MetaIndex<>(BoatWatcher.class, 0, 0);

    public static MetaIndex<Boolean> BOAT_LEFT_PADDLING = new MetaIndex<>(BoatWatcher.class, 5, false);

    public static MetaIndex<Boolean> BOAT_RIGHT_PADDLING = new MetaIndex<>(BoatWatcher.class, 4, false);

    /**
     * The type of the boat, birch, pine, oak, etc.
     */
    public static MetaIndex<Integer> BOAT_TYPE = new MetaIndex<>(BoatWatcher.class, 3, 0);

    @NmsAddedIn(NmsVersion.v1_13)
    public static MetaIndex<Integer> BOAT_SHAKE = new MetaIndex<>(BoatWatcher.class, 6, 0);

    @NmsAddedIn(NmsVersion.v1_19_R2)
    public static MetaIndex<Boolean> CAMEL_DASHING = new MetaIndex<>(CamelWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_19_R2)
    public static MetaIndex<Long> CAMEL_LAST_POSE_CHANGED = new MetaIndex<>(CamelWatcher.class, 1, 0L);

    @NmsAddedIn(NmsVersion.v1_14)
    @NmsRemovedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Integer> CAT_TYPE = new MetaIndex<>(CatWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Cat.Type> CAT_TYPE_NEW = new MetaIndex<>(CatWatcher.class, 0, NmsVersion.v1_19_R1.isSupported() ? Cat.Type.BLACK : null);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> CAT_LYING_DOWN = new MetaIndex<>(CatWatcher.class, 1, false);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> CAT_LOOKING_UP = new MetaIndex<>(CatWatcher.class, 2, false);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> CAT_COLLAR = new MetaIndex<>(CatWatcher.class, 3, AnimalColor.RED.ordinal());

    /**
     * If creeper is ignited, about to blow up
     */
    public static MetaIndex<Boolean> CREEPER_IGNITED = new MetaIndex<>(CreeperWatcher.class, 2, false);

    /**
     * If creeper has glowing aura, struck by lightning
     */
    public static MetaIndex<Boolean> CREEPER_POWERED = new MetaIndex<>(CreeperWatcher.class, 1, false);

    /**
     * No visible effect
     */
    public static MetaIndex<Integer> CREEPER_STATE = new MetaIndex<>(CreeperWatcher.class, 0, -1);

    public static MetaIndex<Integer> DISPLAY_INTERPOLATION_START_DELTA_TICKS = new MetaIndex<>(DisplayWatcher.class, 0, 0);

    public static MetaIndex<Integer> DISPLAY_INTERPOLATION_DURATION = new MetaIndex<>(DisplayWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_19_R3)
    public static MetaIndex<Vector3f> DISPLAY_TRANSLATION = new MetaIndex<>(DisplayWatcher.class, 2, NmsVersion.v1_19_R3.isSupported() ? new Vector3f() : null);

    @NmsAddedIn(NmsVersion.v1_19_R3)
    public static MetaIndex<Vector3f> DISPLAY_SCALE =
        new MetaIndex<>(DisplayWatcher.class, 3, NmsVersion.v1_19_R3.isSupported() ? new Vector3f(1F, 1F, 1F) : null);

    @NmsAddedIn(NmsVersion.v1_19_R3)
    public static MetaIndex<Quaternionf> DISPLAY_LEFT_ROTATION =
        new MetaIndex<>(DisplayWatcher.class, 4, NmsVersion.v1_19_R3.isSupported() ? new Quaternionf() : null);

    @NmsAddedIn(NmsVersion.v1_19_R3)
    public static MetaIndex<Quaternionf> DISPLAY_RIGHT_ROTATION =
        new MetaIndex<>(DisplayWatcher.class, 5, NmsVersion.v1_19_R3.isSupported() ? new Quaternionf() : null);

    public static MetaIndex<Byte> DISPLAY_BILLBOARD_RENDER_CONSTRAINTS = new MetaIndex<>(DisplayWatcher.class, 6, (byte) 0);

    public static MetaIndex<Integer> DISPLAY_BRIGHTNESS_OVERRIDE = new MetaIndex<>(DisplayWatcher.class, 7, -1);

    public static MetaIndex<Float> DISPLAY_VIEW_RANGE = new MetaIndex<>(DisplayWatcher.class, 8, 1F);

    public static MetaIndex<Float> DISPLAY_SHADOW_RADIUS = new MetaIndex<>(DisplayWatcher.class, 9, 0F);

    public static MetaIndex<Float> DISPLAY_SHADOW_STRENGTH = new MetaIndex<>(DisplayWatcher.class, 10, 1F);

    public static MetaIndex<Float> DISPLAY_WIDTH = new MetaIndex<>(DisplayWatcher.class, 11, 0F);

    public static MetaIndex<Float> DISPLAY_HEIGHT = new MetaIndex<>(DisplayWatcher.class, 12, 0F);

    public static MetaIndex<Integer> DISPLAY_GLOW_COLOR_OVERRIDE = new MetaIndex<>(DisplayWatcher.class, 13, -1);

    /**
     * No visible effect
     */
    public static MetaIndex<BlockPosition> DOLPHIN_TREASURE_POS = new MetaIndex<>(DolphinWatcher.class, 0, BlockPosition.ORIGIN);
    /**
     * No visible effect
     */
    public static MetaIndex<Boolean> DOLPHIN_HAS_FISH = new MetaIndex<>(DolphinWatcher.class, 1, false);

    /**
     * No visible effect
     */
    public static MetaIndex<Integer> DOLPHIN_BREATH = new MetaIndex<>(DolphinWatcher.class, 2, 2400);

    /**
     * The itemstack of the dropped item, must be set
     */
    public static MetaIndex<ItemStack> DROPPED_ITEM = new MetaIndex<>(DroppedItemWatcher.class, 0, new ItemStack(Material.AIR));

    public static MetaIndex<Optional<BlockPosition>> ENDER_CRYSTAL_BEAM = new MetaIndex<>(EnderCrystalWatcher.class, 0, Optional.empty());

    /**
     * If the ender crystal has a plate
     */
    public static MetaIndex<Boolean> ENDER_CRYSTAL_PLATE = new MetaIndex<>(EnderCrystalWatcher.class, 1, true);

    public static MetaIndex<Integer> ENDER_DRAGON_PHASE = new MetaIndex<>(EnderDragonWatcher.class, 0, 10);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<ItemStack> ENDER_SIGNAL_ITEM = new MetaIndex<>(EnderSignalWatcher.class, 0, new ItemStack(Material.AIR));

    /**
     * If the enderman is screaming
     */
    public static MetaIndex<Boolean> ENDERMAN_AGRESSIVE = new MetaIndex<>(EndermanWatcher.class, 1, false);

    @NmsAddedIn(NmsVersion.v1_15)
    public static MetaIndex<Boolean> ENDERMAN_UNKNOWN = new MetaIndex<>(EndermanWatcher.class, 2, false);

    /**
     * What block the enderman is holding
     */
    public static MetaIndex<Optional<WrappedBlockData>> ENDERMAN_ITEM = new MetaIndex<>(EndermanWatcher.class, 0, Optional.empty());

    public static MetaIndex<Integer> ENTITY_AIR_TICKS = new MetaIndex<>(FlagWatcher.class, 1, 300);

    /**
     * The custom name of the entity, empty if not set
     */
    @NmsAddedIn(NmsVersion.v1_13)
    public static MetaIndex<Optional<WrappedChatComponent>> ENTITY_CUSTOM_NAME = new MetaIndex<>(FlagWatcher.class, 2, Optional.empty());
    /**
     * The custom name of the entity, empty if not set
     */
    @NmsRemovedIn(NmsVersion.v1_13)
    public static MetaIndex<String> ENTITY_CUSTOM_NAME_OLD = new MetaIndex<>(FlagWatcher.class, 2, "");

    /**
     * If custom name should always be visible even when not looked at
     */
    public static MetaIndex<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new MetaIndex<>(FlagWatcher.class, 3, false);

    /**
     * A bit shifted byte indicating several flags on the entity, sprinting, burning, sneaking, etc
     */
    public static MetaIndex<Byte> ENTITY_META = new MetaIndex<>(FlagWatcher.class, 0, (byte) 0);

    /**
     * If entity is effected by gravity, some visial effects
     */
    public static MetaIndex<Boolean> ENTITY_NO_GRAVITY = new MetaIndex<>(FlagWatcher.class, 5, false);

    /**
     * If entity can make sounds, no noticable effects
     */
    public static MetaIndex<Boolean> ENTITY_SILENT = new MetaIndex<>(FlagWatcher.class, 4, false);

    /**
     * If entity can make sounds, no noticable effects
     */

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<EntityPose> ENTITY_POSE = new MetaIndex<>(FlagWatcher.class, 6, EntityPose.STANDING);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Integer> ENTITY_TICKS_FROZEN = new MetaIndex<>(FlagWatcher.class, 7, 0);

    public static MetaIndex<BlockPosition> FALLING_BLOCK_POSITION = new MetaIndex<>(FallingBlockWatcher.class, 0, BlockPosition.ORIGIN);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<ItemStack> FIREBALL_ITEM = new MetaIndex<>(FireballWatcher.class, 0, new ItemStack(Material.AIR));

    public static MetaIndex<ItemStack> FIREWORK_ITEM =
        new MetaIndex<>(FireworkWatcher.class, 0, new ItemStack(NmsVersion.v1_13.isSupported() ? Material.FIREWORK_ROCKET : Material.AIR));

    public static MetaIndex<Boolean> FISH_FROM_BUCKET = new MetaIndex<>(FishWatcher.class, 0, false);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> FIREWORK_ATTACHED_ENTITY_OLD = new MetaIndex<>(FireworkWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<OptionalInt> FIREWORK_ATTACHED_ENTITY = new MetaIndex<>(FireworkWatcher.class, 1, OptionalInt.empty());

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> FIREWORK_SHOT_AT_ANGLE = new MetaIndex<>(FireworkWatcher.class, 2, false);

    public static MetaIndex<Integer> FISHING_HOOK_HOOKED_ID = new MetaIndex<>(FishingHookWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> FISHING_HOOK_HOOKED = new MetaIndex<>(FishingHookWatcher.class, 1, false);

    /**
     * The type of fox, its coloring
     */
    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> FOX_TYPE = new MetaIndex<>(FoxWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> FOX_META = new MetaIndex<>(FoxWatcher.class, 1, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Optional<UUID>> FOX_TRUSTED_1 = new MetaIndex<>(FoxWatcher.class, 2, Optional.empty());

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Optional<UUID>> FOX_TRUSTED_2 = new MetaIndex<>(FoxWatcher.class, 3, Optional.empty());

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Frog.Variant> FROG_VARIANT =
        new MetaIndex<>(FrogWatcher.class, 0, NmsVersion.v1_19_R1.isSupported() ? Frog.Variant.TEMPERATE : null);

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<OptionalInt> FROG_TONGUE_TARGET = new MetaIndex<>(FrogWatcher.class, 1, OptionalInt.empty());
    /**
     * Changes the face of the ghast
     */
    public static MetaIndex<Boolean> GHAST_AGRESSIVE = new MetaIndex<>(GhastWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Integer> GLOW_SQUID_DARK_TICKS_REMAINING = new MetaIndex<>(GlowSquidWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Boolean> GOAT_SCREAMING = new MetaIndex<>(GoatWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Boolean> GOAT_HAS_LEFT_HORN = new MetaIndex<>(GoatWatcher.class, 1, true);

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Boolean> GOAT_HAS_RIGHT_HORN = new MetaIndex<>(GoatWatcher.class, 2, true);

    /**
     * Switch between the guardian spikes enabled/disabled
     */
    public static MetaIndex<Boolean> GUARDIAN_RETRACT_SPIKES = new MetaIndex<>(GuardianWatcher.class, 0, false);

    /**
     * Play a guardian beam between guardian and target entity id
     */
    public static MetaIndex<Integer> GUARDIAN_TARGET = new MetaIndex<>(GuardianWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> HOGLIN_SHAKING = new MetaIndex<>(HoglinWatcher.class, 0, false);

    /**
     * If horse has chest, set for donkey
     */
    public static MetaIndex<Boolean> HORSE_CHESTED_CARRYING_CHEST = new MetaIndex<>(ChestedHorseWatcher.class, 0, false);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> HORSE_ARMOR = new MetaIndex<>(HorseWatcher.class, 1, 0);
    /**
     * Color of the horse, uses enum not RGB
     */
    public static MetaIndex<Integer> HORSE_COLOR = new MetaIndex<>(HorseWatcher.class, 0, 0);

    /**
     * Sets several bit shifted flags, grazing, rearing, etc
     */
    public static MetaIndex<Byte> HORSE_META = new MetaIndex<>(AbstractHorseWatcher.class, 0, (byte) 0);

    /**
     * Owner of the horse, no visual effect
     */
    @NmsRemovedIn(NmsVersion.v1_19_R3)
    public static MetaIndex<Optional<UUID>> HORSE_OWNER = new MetaIndex<>(AbstractHorseWatcher.class, 1, Optional.empty());

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> ILLAGER_SPELL = new MetaIndex<>(IllagerWizardWatcher.class, 0, (byte) 0);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> ILLAGER_META = new MetaIndex<>(IllagerWatcher.class, 0, (byte) 0);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> ILLAGER_SPELL_TICKS = new MetaIndex<>(IllagerWizardWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> INSENTIENT_META = new MetaIndex<>(InsentientWatcher.class, 0, (byte) 0);

    public static MetaIndex<Byte> IRON_GOLEM_PLAYER_CREATED = new MetaIndex<>(IronGolemWatcher.class, 0, (byte) 0);

    public static MetaIndex<ItemStack> ITEM_DISPLAY_ITEMSTACK = new MetaIndex<>(ItemDisplayWatcher.class, 0, new ItemStack(Material.AIR));

    public static MetaIndex<Byte> ITEM_DISPLAY_TRANSFORM = new MetaIndex<>(ItemDisplayWatcher.class, 1, (byte) 0);

    /**
     * The itemstack inside the itemframe
     */
    public static MetaIndex<ItemStack> ITEMFRAME_ITEM = new MetaIndex<>(ItemFrameWatcher.class, 0, new ItemStack(Material.AIR));

    /**
     * The itemstack rotation inside the itemframe
     */
    public static MetaIndex<Integer> ITEMFRAME_ROTATION = new MetaIndex<>(ItemFrameWatcher.class, 1, 0);

    public static MetaIndex<Float> INTERACTION_WIDTH = new MetaIndex<>(InteractionWatcher.class, 0, 1F);

    public static MetaIndex<Float> INTERACTION_HEIGHT = new MetaIndex<>(InteractionWatcher.class, 1, 1F);

    public static MetaIndex<Boolean> INTERACTION_RESPONSIVE = new MetaIndex<>(InteractionWatcher.class, 2, false);

    /**
     * How many arrows sticking out of the living entity, currently used on player
     */
    public static MetaIndex<Integer> LIVING_ARROWS = new MetaIndex<>(LivingWatcher.class, 4, 0);

    /**
     * The main hand of the living entity
     */
    public static MetaIndex<Byte> LIVING_META = new MetaIndex<>(LivingWatcher.class, 0, (byte) 0);

    /**
     * How much health the living entity has, generally only visible on bosses due to their health bar
     */
    public static MetaIndex<Float> LIVING_HEALTH = new MetaIndex<>(LivingWatcher.class, 1, 1F);

    /**
     * If the potion effect particles should be faded
     */
    public static MetaIndex<Boolean> LIVING_POTION_AMBIENT = new MetaIndex<>(LivingWatcher.class, 3, false);

    /**
     * The RGB color of the potion particles, 0 if not set
     */
    public static MetaIndex<Integer> LIVING_POTIONS = new MetaIndex<>(LivingWatcher.class, 2, 0);

    /**
     * How many bee stings does the entity have
     */
    @NmsAddedIn(NmsVersion.v1_15)
    public static MetaIndex<Integer> LIVING_STINGS = new MetaIndex<>(LivingWatcher.class, 5, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Optional<BlockPosition>> LIVING_BED_POSITION = new MetaIndex<>(LivingWatcher.class, 6, Optional.empty());

    /**
     * If there is no carpet, -1. Otherwise it's a color enum value
     */
    public static MetaIndex<Integer> LLAMA_CARPET = new MetaIndex<>(LlamaWatcher.class, 1, -1);

    /**
     * The color of the llama, color enum value
     */
    public static MetaIndex<Integer> LLAMA_COLOR = new MetaIndex<>(LlamaWatcher.class, 2, 0);

    public static MetaIndex<Integer> LLAMA_STRENGTH = new MetaIndex<>(LlamaWatcher.class, 0, 0);

    /**
     * The block id:data combined id, 0 if no block
     */
    public static MetaIndex<Integer> MINECART_BLOCK = new MetaIndex<>(MinecartWatcher.class, 3, 0);

    /**
     * If there is a block inside the minecart
     */
    public static MetaIndex<Boolean> MINECART_BLOCK_VISIBLE = new MetaIndex<>(MinecartWatcher.class, 5, false);

    /**
     * How much gap there should be between minecart and block, 6 by default
     */
    public static MetaIndex<Integer> MINECART_BLOCK_Y = new MetaIndex<>(MinecartWatcher.class, 4, 6);

    public static MetaIndex<Integer> MINECART_SHAKING_DIRECTION = new MetaIndex<>(MinecartWatcher.class, 1, 1);

    public static MetaIndex<Float> MINECART_SHAKING_MULITPLIER = new MetaIndex<>(MinecartWatcher.class, 2, 0F);

    public static MetaIndex<Integer> MINECART_SHAKING_POWER = new MetaIndex<>(MinecartWatcher.class, 0, 0);

    /**
     * The command run if the minecraft is a command minecart block
     */
    public static MetaIndex<String> MINECART_COMMAND_STRING = new MetaIndex<>(MinecartCommandWatcher.class, 0, "");

    public static MetaIndex<WrappedChatComponent> MINECART_COMMAND_LAST_OUTPUT =
        new MetaIndex<>(MinecartCommandWatcher.class, 1, WrappedChatComponent.fromText(""));

    /**
     * If the minecart furnace is fueled and burning
     */
    public static MetaIndex<Boolean> MINECART_FURANCE_FUELED = new MetaIndex<>(MinecartFurnaceWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<String> MUSHROOM_COW_TYPE = new MetaIndex<>(MushroomCowWatcher.class, 0, "RED");

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> OCELOT_TYPE = new MetaIndex<>(OcelotWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> OCELOT_TRUST = new MetaIndex<>(OcelotWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_19_R1)
    public static MetaIndex<Art> PAINTING = new MetaIndex<>(PaintingWatcher.class, 0, NmsVersion.v1_19_R1.isSupported() ? Art.KEBAB : null);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> PANDA_HEAD_SHAKING = new MetaIndex<>(PandaWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> PANDA_UNKNOWN_1 = new MetaIndex<>(PandaWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> PANDA_UNKNOWN_2 = new MetaIndex<>(PandaWatcher.class, 2, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> PANDA_MAIN_GENE = new MetaIndex<>(PandaWatcher.class, 3, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> PANDA_HIDDEN_GENE = new MetaIndex<>(PandaWatcher.class, 4, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Byte> PANDA_META = new MetaIndex<>(PandaWatcher.class, 5, (byte) 0);

    public static MetaIndex<Integer> PARROT_VARIANT = new MetaIndex<>(ParrotWatcher.class, 0, 0);

    public static MetaIndex<Integer> PHANTOM_SIZE = new MetaIndex<>(PhantomWatcher.class, 0, 0);

    public static MetaIndex<Boolean> PIG_SADDLED = new MetaIndex<>(PigWatcher.class, 0, false);

    /**
     * If pig runs faster, no visible effect
     */
    public static MetaIndex<Integer> PIG_BOOST = new MetaIndex<>(PigWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> PIGLIN_ABSTRACT_SHAKING = new MetaIndex<>(PiglinAbstractWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> PIGLIN_BABY = new MetaIndex<>(PiglinWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> PIGLIN_CROSSBOW = new MetaIndex<>(PiglinWatcher.class, 1, false);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> PIGLIN_DANCING = new MetaIndex<>(PiglinWatcher.class, 2, false);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> PILLAGER_AIMING_BOW = new MetaIndex<>(PillagerWatcher.class, 0, false);

    public static MetaIndex<Float> PLAYER_ABSORPTION = new MetaIndex<>(PlayerWatcher.class, 0, 0F);

    public static MetaIndex<Byte> PLAYER_HAND = new MetaIndex<>(PlayerWatcher.class, 3, (byte) 0);

    public static MetaIndex<Integer> PLAYER_SCORE = new MetaIndex<>(PlayerWatcher.class, 1, 0);

    public static MetaIndex<Byte> PLAYER_SKIN = new MetaIndex<>(PlayerWatcher.class, 2, (byte) 127);

    public static MetaIndex<NbtBase> PLAYER_LEFT_SHOULDER_ENTITY = new MetaIndex<>(PlayerWatcher.class, 4, NbtFactory.ofWrapper(NbtType.TAG_COMPOUND, "None"));

    public static MetaIndex<NbtBase> PLAYER_RIGHT_SHOULDER_ENTITY = new MetaIndex<>(PlayerWatcher.class, 5, NbtFactory.ofWrapper(NbtType.TAG_COMPOUND, "None"));

    public static MetaIndex<Boolean> POLAR_BEAR_STANDING = new MetaIndex<>(PolarBearWatcher.class, 0, false);

    public static MetaIndex<Integer> PUFFERFISH_PUFF_STATE = new MetaIndex<>(PufferFishWatcher.class, 0, 0);

    public static MetaIndex<Integer> RABBIT_TYPE = new MetaIndex<>(RabbitWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> RAIDER_CASTING_SPELL = new MetaIndex<>(RaiderWatcher.class, 0, false);

    public static MetaIndex<Byte> SHEEP_WOOL = new MetaIndex<>(SheepWatcher.class, 0, (byte) 0);

    @NmsRemovedIn(NmsVersion.v1_17)
    public static MetaIndex<Optional<BlockPosition>> SHULKER_ATTACHED = new MetaIndex<>(ShulkerWatcher.class, 1, Optional.empty());

    public static MetaIndex<Byte> SHULKER_COLOR = new MetaIndex<>(ShulkerWatcher.class, 3, (byte) 16);

    public static MetaIndex<Direction> SHULKER_FACING = new MetaIndex<>(ShulkerWatcher.class, 0, Direction.DOWN);

    public static MetaIndex<Byte> SHULKER_PEEKING = new MetaIndex<>(ShulkerWatcher.class, 2, (byte) 0);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> SKELETON_SWING_ARMS = new MetaIndex<>(AbstractSkeletonWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_17)
    public static MetaIndex<Boolean> SKELETON_CONVERTING_STRAY = new MetaIndex<>(SkeletonWatcher.class, 0, false);

    public static MetaIndex<Integer> SLIME_SIZE = new MetaIndex<>(SlimeWatcher.class, 0, 1);

    public static MetaIndex<Byte> SNOWMAN_DERP = new MetaIndex<>(SnowmanWatcher.class, 0, (byte) 16);

    public static MetaIndex<Byte> SNIFFER_STATE = new MetaIndex<>(SnifferWatcher.class, 0, (byte) 0);

    public static MetaIndex<Integer> SNIFFER_DROP_SEED_AT_TICK = new MetaIndex<>(SnifferWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Integer> STRIDER_SADDLE_UNKNOWN = new MetaIndex<>(StriderWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> STRIDER_WARM = new MetaIndex<>(StriderWatcher.class, 1, false);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> STRIDER_SADDLED = new MetaIndex<>(StriderWatcher.class, 2, false);

    public static MetaIndex<Byte> SPIDER_CLIMB = new MetaIndex<>(SpiderWatcher.class, 0, (byte) 0);

    public static MetaIndex<ItemStack> SPLASH_POTION_ITEM = new MetaIndex<>(SplashPotionWatcher.class, 0, new ItemStack(Material.SPLASH_POTION));

    public static MetaIndex<Byte> TAMEABLE_META = new MetaIndex<>(TameableWatcher.class, 0, (byte) 0);

    public static MetaIndex<Optional<UUID>> TAMEABLE_OWNER = new MetaIndex<>(TameableWatcher.class, 1, Optional.empty());

    public static MetaIndex<WrappedChatComponent> TEXT_DISPLAY_TEXT = new MetaIndex<>(TextDisplayWatcher.class, 0, WrappedChatComponent.fromText(""));

    public static MetaIndex<Integer> TEXT_DISPLAY_LINE_WIDTH = new MetaIndex<>(TextDisplayWatcher.class, 1, 200);

    public static MetaIndex<Integer> TEXT_DISPLAY_BACKGROUND_COLOR = new MetaIndex<>(TextDisplayWatcher.class, 2, 1073741824);

    public static MetaIndex<Byte> TEXT_DISPLAY_TEXT_OPACITY = new MetaIndex<>(TextDisplayWatcher.class, 3, (byte) -1);

    public static MetaIndex<Byte> TEXT_DISPLAY_FLAGS = new MetaIndex<>(TextDisplayWatcher.class, 4, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<ItemStack> THROWABLE_ITEM = new MetaIndex<>(ThrowableWatcher.class, 0, new ItemStack(Material.AIR));

    public static MetaIndex<Integer> TIPPED_ARROW_COLOR = new MetaIndex<>(TippedArrowWatcher.class, 0, -1);

    public static MetaIndex<Integer> TNT_FUSE_TICKS = new MetaIndex<>(TNTWatcher.class, 0, Integer.MAX_VALUE);

    public static MetaIndex<Byte> TRIDENT_ENCHANTS = new MetaIndex<>(TridentWatcher.class, 0, (byte) 0);

    @NmsAddedIn(NmsVersion.v1_15)
    public static MetaIndex<Boolean> TRIDENT_ENCHANTED = new MetaIndex<>(TridentWatcher.class, 1, false);

    public static MetaIndex<Integer> TROPICAL_FISH_VARIANT = new MetaIndex<>(TropicalFishWatcher.class, 0, 0);

    public static MetaIndex<BlockPosition> TURTLE_HOME_POSITION = new MetaIndex<>(TurtleWatcher.class, 0, BlockPosition.ORIGIN);

    public static MetaIndex<Boolean> TURTLE_HAS_EGG = new MetaIndex<>(TurtleWatcher.class, 1, false);

    public static MetaIndex<Boolean> TURTLE_UNKNOWN_3 = new MetaIndex<>(TurtleWatcher.class, 2, false);

    public static MetaIndex<BlockPosition> TURTLE_TRAVEL_POSITION = new MetaIndex<>(TurtleWatcher.class, 3, BlockPosition.ORIGIN);

    public static MetaIndex<Boolean> TURTLE_UNKNOWN_1 = new MetaIndex<>(TurtleWatcher.class, 4, false);

    public static MetaIndex<Boolean> TURTLE_UNKNOWN_2 = new MetaIndex<>(TurtleWatcher.class, 5, false);

    public static MetaIndex<Byte> VEX_ANGRY = new MetaIndex<>(VexWatcher.class, 0, (byte) 0);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> VILLAGER_PROFESSION = new MetaIndex<>(VillagerWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> ABSTRACT_VILLAGER_ANGRY = new MetaIndex<>(AbstractVillagerWatcher.class, 0, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<VillagerData> VILLAGER_DATA =
        new MetaIndex<>(VillagerWatcher.class, 0, NmsVersion.v1_14.isSupported() ? new VillagerData(Villager.Type.PLAINS, Villager.Profession.NONE, 1) : null);

    public static MetaIndex<Integer> WARDEN_ANGER = new MetaIndex<>(WardenWatcher.class, 0, 0);

    public static MetaIndex<Boolean> WITCH_AGGRESSIVE = new MetaIndex<>(WitchWatcher.class, 0, false);

    public static MetaIndex<Integer> WITHER_INVUL = new MetaIndex<>(WitherWatcher.class, 3, 0);

    public static MetaIndex<Integer> WITHER_TARGET_1 = new MetaIndex<>(WitherWatcher.class, 0, 0);

    public static MetaIndex<Integer> WITHER_TARGET_2 = new MetaIndex<>(WitherWatcher.class, 1, 0);

    public static MetaIndex<Integer> WITHER_TARGET_3 = new MetaIndex<>(WitherWatcher.class, 2, 0);

    public static MetaIndex<Boolean> WITHER_SKULL_BLUE = new MetaIndex<>(WitherSkullWatcher.class, 0, false);

    public static MetaIndex<Boolean> WOLF_BEGGING = new MetaIndex<>(WolfWatcher.class, 1, false);

    @NmsRemovedIn(NmsVersion.v1_15)
    public static MetaIndex<Float> WOLF_DAMAGE = new MetaIndex<>(WolfWatcher.class, 0, 1F);

    public static MetaIndex<Integer> WOLF_COLLAR = new MetaIndex<>(WolfWatcher.class, 2, 14);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Integer> WOLF_ANGER = new MetaIndex<>(WolfWatcher.class, 3, 0);

    @NmsAddedIn(NmsVersion.v1_16)
    public static MetaIndex<Boolean> ZOGLIN_BABY = new MetaIndex<>(ZoglinWatcher.class, 0, false);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Boolean> ZOMBIE_AGGRESSIVE = new MetaIndex<>(ZombieWatcher.class, 2, false);

    public static MetaIndex<Boolean> ZOMBIE_BABY = new MetaIndex<>(ZombieWatcher.class, 0, false);

    @NmsAddedIn(NmsVersion.v1_13)
    public static MetaIndex<Boolean> ZOMBIE_CONVERTING_DROWNED = new MetaIndex<>(ZombieWatcher.class, 2, false);

    public static MetaIndex<Integer> ZOMBIE_PLACEHOLDER = new MetaIndex<>(ZombieWatcher.class, 1, 0);

    @NmsRemovedIn(NmsVersion.v1_14)
    public static MetaIndex<Integer> ZOMBIE_VILLAGER_PROFESSION_OLD = new MetaIndex<>(ZombieVillagerWatcher.class, 1, 0);

    @NmsAddedIn(NmsVersion.v1_14)
    public static MetaIndex<VillagerData> ZOMBIE_VILLAGER_PROFESSION = new MetaIndex<>(ZombieVillagerWatcher.class, 1,
        NmsVersion.v1_14.isSupported() ? new VillagerData(Villager.Type.PLAINS, Villager.Profession.NONE, 1) : null);

    /**
     * Shown for villager conversion
     */
    public static MetaIndex<Boolean> ZOMBIE_VILLAGER_SHAKING = new MetaIndex<>(ZombieVillagerWatcher.class, 0, false);

    static {
        setValues();
        eliminateBlankIndexes();
        orderMetaIndexes();
    }

    private static void eliminateBlankIndexes() {
        HashMap<Class, ArrayList<MetaIndex>> metas = new HashMap<>();

        for (MetaIndex index : values()) {
            metas.computeIfAbsent(index.getFlagWatcher(), (a) -> new ArrayList<>()).add(index);
        }

        for (ArrayList<MetaIndex> list : metas.values()) {
            list.sort(Comparator.comparingInt(MetaIndex::getIndex));

            int i = 0;

            for (MetaIndex ind : list) {
                ind._index = i++;
            }
        }
    }

    private static void orderMetaIndexes() {
        for (MetaIndex flagType : values()) {
            if (flagType.getFlagWatcher() == FlagWatcher.class) {
                continue;
            }

            flagType._index += getNoIndexes(ReflectionManager.getSuperClass(flagType.getFlagWatcher()));
        }
    }

    /**
     * Simple verification for the dev that he's setting up the FlagType's properly.
     * All flag types should be from 0 to <Max Number> with no empty numbers.
     * All flag types should never occur twice.
     */
    public static void validateMetadata() {
        HashMap<Class, Integer> maxValues = new HashMap<>();

        for (MetaIndex type : values()) {
            if (maxValues.containsKey(type.getFlagWatcher()) && maxValues.get(type.getFlagWatcher()) > type.getIndex()) {
                continue;
            }

            maxValues.put(type.getFlagWatcher(), type.getIndex());
        }

        for (Entry<Class, Integer> entry : maxValues.entrySet()) {
            loop:

            for (int i = 0; i < entry.getValue(); i++) {
                MetaIndex found = null;

                for (MetaIndex type : values()) {
                    if (type.getIndex() != i) {
                        continue;
                    }

                    if (!ReflectionManager.isAssignableFrom(entry.getKey(), type.getFlagWatcher())) {
                        continue;
                    }

                    if (found != null) {
                        DisguiseUtilities.getLogger().severe(entry.getKey().getSimpleName() + " has multiple FlagType's registered for the index " + i + " (" +
                            type.getFlagWatcher().getSimpleName() + ", " + found.getFlagWatcher().getSimpleName() + ")");
                        continue loop;
                    }

                    found = type;
                }

                if (found != null) {
                    continue;
                }

                DisguiseUtilities.getLogger().severe(entry.getKey().getSimpleName() + " has no FlagType registered for the index " + i);
            }
        }
    }

    public String toString() {
        return LibsMsg.META_INFO.get(getName(this), getFlagWatcher().getSimpleName(), getIndex(), getDefault().getClass().getSimpleName(),
            DisguiseUtilities.getGson().toJson(getDefault()));
    }

    /**
     * Used for debugging purposes, prints off the registered MetaIndexes
     */
    public static void printMetadata() {
        ArrayList<String> toPrint = new ArrayList<>();

        try {
            for (Field field : MetaIndex.class.getFields()) {
                if (field.getType() != MetaIndex.class) {
                    continue;
                }

                MetaIndex index = (MetaIndex) field.get(null);

                try {
                    toPrint.add(index.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        toPrint.sort(String.CASE_INSENSITIVE_ORDER);

        for (String s : toPrint) {
            DisguiseUtilities.getLogger().info(s);
        }
    }

    /**
     * @param watcher - A FlagWatcher class
     * @param flagNo  - The meta index number
     * @return The MetaIndex which corresponds to that FlagWatcher at that index
     */
    public static MetaIndex getMetaIndex(FlagWatcher watcher, int flagNo) {
        return getMetaIndex(watcher.getClass(), flagNo);
    }

    /**
     * @param watcherClass - A FlagWatcher class
     * @param flagNo       - The meta index number
     * @return The MetaIndex which corresponds to that FlagWatcher at that index
     */
    public static MetaIndex getMetaIndex(Class<? extends FlagWatcher> watcherClass, int flagNo) {
        for (MetaIndex type : values()) {
            if (type.getIndex() != flagNo) {
                continue;
            }

            if (!ReflectionManager.isAssignableFrom(watcherClass, type.getFlagWatcher())) {
                continue;
            }

            return type;
        }

        return null;
    }

    /**
     * @param watcherClass - A flagwatcher class
     * @return ArrayList<MetaIndex> registered to that FlagWatcher
     */
    public static ArrayList<MetaIndex> getMetaIndexes(Class<? extends FlagWatcher> watcherClass) {
        ArrayList<MetaIndex> list = new ArrayList<>();

        for (MetaIndex type : values()) {
            if (type == null || !ReflectionManager.isAssignableFrom(watcherClass, type.getFlagWatcher())) {
                continue;
            }

            list.add(type);
        }

        list.sort(Comparator.comparingInt(MetaIndex::getIndex));

        return list;
    }

    private static int getNoIndexes(Class c) {
        int found = 0;

        for (MetaIndex type : values()) {
            if (type.getFlagWatcher() != c) {
                continue;
            }

            found++;
        }

        if (c != FlagWatcher.class) {
            found += getNoIndexes(ReflectionManager.getSuperClass(c));
        }

        return found;
    }

    /**
     * Get all the MetaIndex's registered
     *
     * @return MetaIndex[]
     */
    public static MetaIndex[] values() {
        return _values;
    }

    public static MetaIndex getMetaIndexByName(String name) {
        name = name.toUpperCase(Locale.ENGLISH);

        try {
            for (Field field : MetaIndex.class.getFields()) {
                if (!field.getName().equals(name) || field.getType() != MetaIndex.class) {
                    continue;
                }

                return (MetaIndex) field.get(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get the field name of a registered MetaIndex
     */
    public static String getName(MetaIndex metaIndex) {
        try {
            for (Field field : MetaIndex.class.getFields()) {
                if (field.get(null) != metaIndex) {
                    continue;
                }

                return field.getName();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Add @metaIndexes to the existing MetaIndexes, was intended for backwards support
     *
     * @param metaIndexes
     */
    public static void addMetaIndexes(MetaIndex... metaIndexes) {
        _values = Arrays.copyOf(values(), values().length + metaIndexes.length);

        for (int i = values().length - metaIndexes.length, a = 0; i < values().length; i++, a++) {
            MetaIndex index = metaIndexes[a];

            ArrayList<MetaIndex> list = getMetaIndexes(index.getFlagWatcher());

            for (int b = index.getIndex(); b < list.size(); b++) {
                list.get(b)._index++;
            }

            for (MetaIndex metaIndex : values()) {
                if (metaIndex == null || metaIndex.getFlagWatcher() != index.getFlagWatcher() || metaIndex.getIndex() != index.getIndex()) {
                    continue;
                }

                DisguiseUtilities.getLogger().severe(
                    "MetaIndex " + metaIndex.getFlagWatcher().getSimpleName() + " at index " + metaIndex.getIndex() + " has already registered this! (" +
                        metaIndex.getDefault() + "," + index.getDefault() + ")");
            }

            values()[i] = metaIndexes[a];
        }
    }

    /**
     * Resets the metaindex array and regenerates it from the fields
     */
    public static void setValues() {
        try {
            _values = new MetaIndex[0];

            for (Field field : MetaIndex.class.getFields()) {
                if (field.getType() != MetaIndex.class) {
                    continue;
                }

                MetaIndex index = (MetaIndex) field.get(null);

                if (index == null || index.getDefault() == null) {
                    continue;
                }

                if (!ReflectionManager.isSupported(field)) {
                    index._index = -1;
                    continue;
                }

                _values = Arrays.copyOf(_values, _values.length + 1);
                _values[_values.length - 1] = index;

                index.serializer = DisguiseUtilities.getSerializer(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if field was replaced, false if the field doesn't exist or exception occured
     */
    public static boolean setMetaIndex(String name, MetaIndex metaIndex) {
        try {
            Field field = MetaIndex.class.getField(name);
            MetaIndex index = (MetaIndex) field.get(null);

            field.set(null, metaIndex);
            return true;
        } catch (NoSuchFieldException ignored) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private final Y _defaultValue;
    private int _index;
    private final Class<? extends FlagWatcher> _watcher;
    private WrappedDataWatcher.Serializer serializer;

    public MetaIndex(Class<? extends FlagWatcher> watcher, int index, Y defaultValue) {
        _index = index;
        _watcher = watcher;
        _defaultValue = defaultValue;
    }

    public Y getDefault() {
        return _defaultValue;
    }

    /**
     * Used for serializing values to a packet stream
     */
    public WrappedDataWatcher.Serializer getSerializer() {
        return serializer;
    }

    public Class<? extends FlagWatcher> getFlagWatcher() {
        return _watcher;
    }

    public int getIndex() {
        return _index;
    }
}
