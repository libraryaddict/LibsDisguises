package me.libraryaddict.disguise.utilities.animations;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import lombok.AccessLevel;
import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.AllayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ArmorStandWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DolphinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EggWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.EvokerFangsWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FireworkWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FishingHookWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FoxWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GoatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.GuardianWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.HoglinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.InsentientWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.IronGolemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartSpawnerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MinecartTntWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.OcelotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RabbitWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RavagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnifferWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnowballWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SquidWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TameableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.TippedArrowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VillagerWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WardenWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WitchWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZoglinWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieVillagerWatcher;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum DisguiseAnimation {
    // Values as taken from https://minecraft.wiki/w/Java_Edition_protocol/Entity_statuses
    // Note that each animation was not tested and this behavior cannot be relied on.
    ALLAY_HEARTS(AllayWatcher.class, 18),
    ANIMAL_HEARTS(AgeableWatcher.class, 18),
    ARMORSTAND_HIT(ArmorStandWatcher.class, 32), // Short shiver animation
    CRITICAL_HIT(LivingWatcher.class, -4) {
        @Override
        public @Nullable WrapperPlayServerEntityAnimation.EntityAnimationType getAnimationType() {
            return WrapperPlayServerEntityAnimation.EntityAnimationType.CRITICAL_HIT;
        }
    }, // Note this is a fake animation, added for easy api access
    DEATH(LivingWatcher.class, 3),
    DEATH_PARTICLES(LivingWatcher.class, 60),
    DOLPHIN_HAPPY(DolphinWatcher.class, 38),
    EGG_PARTICLES(EggWatcher.class, 3),
    EVOKER_FANGS_SNAP(EvokerFangsWatcher.class, 4),
    FIREWORKS_EXPLOSION(FireworkWatcher.class, 17),
    FISHING_HOOK_DRAG(FishingHookWatcher.class, 31), // Requires a hooked entity?
    FOX_CHEW(FoxWatcher.class, 45), // No animation, just particles
    GOAT_LOWER_HEAD(GoatWatcher.class, 58),
    GOAT_RAISE_HEAD(GoatWatcher.class, 59),
    GUARDIAN_ATTACK(GuardianWatcher.class, 21, AnimationType.NO_EFFECT),
    HOGLIN_ATTACK(HoglinWatcher.class, 4, AnimationType.ATTACK),
    HONEY(FlagWatcher.class, 53), // Less particles than than LIVING version
    HORSE_TAMING_FAILED(AbstractHorseWatcher.class, 6),
    HORSE_TAMING_SUCCESS(AbstractHorseWatcher.class, 7),
    HURT(LivingWatcher.class), // Note this is a fake animation, added for easy api access
    IRON_GOLEM_ATTACK(IronGolemWatcher.class, 4, AnimationType.ATTACK),
    IRON_GOLEM_ROSE_START(IronGolemWatcher.class, 11),
    IRON_GOLEM_ROSE_STOP(IronGolemWatcher.class, 34),
    LIVING_HONEY(LivingWatcher.class, 54), //  More particles than the non-living version
    MAGIC_CRITICAL_HIT(LivingWatcher.class) {
        @Override
        public @Nullable WrapperPlayServerEntityAnimation.EntityAnimationType getAnimationType() {
            return WrapperPlayServerEntityAnimation.EntityAnimationType.MAGIC_CRITICAL_HIT;
        }
    }, // Note this is a fake animation, added for easy api access
    MINECART_SPAWNER_RESET_DELAY(MinecartSpawnerWatcher.class, 1),
    MINECART_TNT_IGNITE(MinecartTntWatcher.class, 10), // Last few frames will replay instead of animation ending (tested 1.21.8)
    MOB_SPAWN(InsentientWatcher.class, 20),
    OCELOT_TAMING_FAILED(OcelotWatcher.class, 40),
    OCELOT_TAMING_SUCCESS(OcelotWatcher.class, 41),
    PLAYER_BAD_OMEN_CLOUD(PlayerWatcher.class, 43, AnimationType.NO_EFFECT),
    PLAYER_DISABLE_REDUCED_DEBUG_SCREEN(PlayerWatcher.class, 23, AnimationType.NO_EFFECT),
    PLAYER_ENABLE_REDUCED_DEBUG_SCREEN(PlayerWatcher.class, 22, AnimationType.NO_EFFECT),
    PLAYER_ITEM_USED(PlayerWatcher.class, 9, AnimationType.NO_EFFECT),
    PORTAL(LivingWatcher.class, 46),
    RABBIT_HOP(RabbitWatcher.class, 1),
    RAVAGER_ATTACK(RavagerWatcher.class, 4, AnimationType.ATTACK),
    RAVAGER_STUNNED(RavagerWatcher.class, 39),
    SHEEP_EAT_GRASS(SheepWatcher.class, 10),
    SHIELD_BLOCK(LivingWatcher.class, 29, AnimationType.NO_EFFECT),
    SHIELD_BREAK(LivingWatcher.class, 30, AnimationType.NO_EFFECT),
    SNIFFER_DIG(SnifferWatcher.class, 63, AnimationType.NO_EFFECT),
    SNOWBALL_PARTICLES(SnowballWatcher.class, 3),
    SQUID_SWIM(SquidWatcher.class, 19),
    SWAP_HELD_ITEMS(LivingWatcher.class, 55, AnimationType.NO_EFFECT),
    SWING_MAIN_HAND(LivingWatcher.class) {
        @Override
        public @Nullable WrapperPlayServerEntityAnimation.EntityAnimationType getAnimationType() {
            return WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM;
        }
    }, // Note this is a fake animation, added for easy api access
    SWING_OFF_HAND(LivingWatcher.class) {
        @Override
        public @Nullable WrapperPlayServerEntityAnimation.EntityAnimationType getAnimationType() {
            return WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND;
        }
    }, // Note this is a fake animation, added for easy api access
    TAMING_FAILED(TameableWatcher.class, 6),
    TAMING_SUCCESS(TameableWatcher.class, 7),
    TIPPED_ARROW_PARTICLES(TippedArrowWatcher.class, 0), // Requires color to be set on arrow
    TOTEM_UNDYING(LivingWatcher.class, 35), // Plays particles and sound
    VILLAGER_ANGRY(VillagerWatcher.class, 13),
    VILLAGER_HAPPY(VillagerWatcher.class, 14),
    VILLAGER_HEART(VillagerWatcher.class, 12),
    VILLAGER_SPLASH(VillagerWatcher.class, 42),
    WARDEN_ATTACK(WardenWatcher.class, 4, AnimationType.ATTACK),
    WARDEN_SONIC_BOOM(WardenWatcher.class, 62),
    WARDEN_TENDRILS(WardenWatcher.class, 61),
    WITCH_MAGIC_PARTICLES(WitchWatcher.class, 15),
    WOLF_SHAKING_START(WolfWatcher.class, 8),
    WOLF_SHAKING_STOP(WolfWatcher.class, 56),
    ZOGLIN_ATTACK(ZoglinWatcher.class, 4, AnimationType.ATTACK),
    ZOMBIE_VILLAGER_CURED_SOUND(ZombieVillagerWatcher.class, 16);

    private enum AnimationType {
        ATTACK,
        PACKET,
        NO_EFFECT, // Hidden from commands
        VISIBLE_EFFECT,
    }

    private static final Map<Class<? extends FlagWatcher>, List<DisguiseAnimation>> animationCache = new HashMap<>();
    private static final Map<Class<? extends FlagWatcher>, DisguiseAnimation> attackAnimations = new HashMap<>();
    private final Class<? extends FlagWatcher> watcher;
    private final int status;
    @Getter(AccessLevel.PRIVATE)
    private final AnimationType type;

    DisguiseAnimation(Class<? extends FlagWatcher> flagWatcher) {
        this(flagWatcher, -1, AnimationType.PACKET);
    }

    DisguiseAnimation(Class<? extends FlagWatcher> flagWatcher, int status) {
        this(flagWatcher, status, AnimationType.VISIBLE_EFFECT);
    }

    DisguiseAnimation(Class<? extends FlagWatcher> flagWatcher, int status, AnimationType animationType) {
        this.watcher = flagWatcher;
        this.status = status;
        this.type = animationType;
    }

    /**
     * @return null unless it is one of the few animations relevant for this packet
     */
    public @Nullable WrapperPlayServerEntityAnimation.EntityAnimationType getAnimationType() {
        return null;
    }

    public boolean isAttack() {
        return type == AnimationType.ATTACK;
    }

    public boolean isHidden() {
        return type == AnimationType.NO_EFFECT;
    }

    public boolean isFake() {
        // The only recognized fake animations at this point are for WrapperPlayServerEntityAnimation
        return type == AnimationType.PACKET;
    }

    public boolean isUsable(DisguiseType disguiseType) {
        return isUseable(disguiseType.getWatcherClass());
    }

    public boolean isUseable(Class<? extends FlagWatcher> flagWatcher) {
        return getAnimations(flagWatcher).contains(this);
    }

    public static List<DisguiseAnimation> getAnimations(DisguiseType disguiseType) {
        return getAnimations(disguiseType.getWatcherClass());
    }

    public static List<DisguiseAnimation> getAnimations(Class<? extends FlagWatcher> clss) {
        return animationCache.computeIfAbsent(clss, key -> {
            Map<Integer, DisguiseAnimation> bestAnimations = new HashMap<>();
            int fakeCounter = -1;

            for (DisguiseAnimation animation : values()) {
                if (!ReflectionManager.isAssignableFrom(animation.getWatcher(), clss)) {
                    continue;
                }

                int id = animation.isFake() ? fakeCounter-- : animation.getStatus();
                DisguiseAnimation existing = bestAnimations.get(id);

                // If already set and 'animation' is not a child class of 'existing'
                if (existing != null && !ReflectionManager.isAssignableFrom(existing.getWatcher(), animation.getWatcher())) {
                    continue;
                }

                bestAnimations.put(id, animation);
            }

            return Collections.unmodifiableList(new ArrayList<>(bestAnimations.values()));
        });
    }

    public static DisguiseAnimation getAttackAnimation(Class<? extends FlagWatcher> clss) {
        return attackAnimations.computeIfAbsent(clss,
            key -> getAnimations(clss).stream().filter(DisguiseAnimation::isAttack).findAny().orElse(null));
    }

    public static @Nullable DisguiseAnimation getAnimation(Class<? extends FlagWatcher> clss, int id) {
        return getAnimations(clss).stream().filter(animation -> animation.getStatus() == id).findAny().orElse(null);
    }
}
