package me.libraryaddict.disguise.utilities.reflection.annotations;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;

import java.util.Arrays;

@Getter
public enum MethodGroupType {
    NO_LOOK(DisguiseType.AREA_EFFECT_CLOUD, DisguiseType.DRAGON_FIREBALL, DisguiseType.DROPPED_ITEM, DisguiseType.EGG, DisguiseType.ENDER_CRYSTAL,
        DisguiseType.ENDER_PEARL, DisguiseType.ENDER_SIGNAL, DisguiseType.FALLING_BLOCK, DisguiseType.FIREBALL, DisguiseType.FIREWORK,
        DisguiseType.FISHING_HOOK, DisguiseType.LEASH_HITCH, DisguiseType.MARKER, DisguiseType.PRIMED_TNT, DisguiseType.SHULKER, DisguiseType.SMALL_FIREBALL,
        DisguiseType.SNOWBALL, DisguiseType.SPLASH_POTION, DisguiseType.THROWN_EXP_BOTTLE),

    EQUIPPABLE(DisguiseType.ARMOR_STAND, DisguiseType.DROWNED, DisguiseType.GIANT, DisguiseType.HUSK, DisguiseType.MODDED_LIVING, DisguiseType.MODDED_MISC,
        DisguiseType.PIG_ZOMBIE, DisguiseType.PIGLIN, DisguiseType.PIGLIN_BRUTE, DisguiseType.PLAYER, DisguiseType.SKELETON, DisguiseType.STRAY,
        DisguiseType.WITHER_SKELETON, DisguiseType.ZOMBIE),

    HOLDABLE(EQUIPPABLE, DisguiseType.ENDERMAN, DisguiseType.EVOKER, DisguiseType.ILLUSIONER, DisguiseType.IRON_GOLEM, DisguiseType.PILLAGER,
        DisguiseType.RAVAGER, DisguiseType.VEX, DisguiseType.VINDICATOR, DisguiseType.WANDERING_TRADER, DisguiseType.WITCH, DisguiseType.ZOMBIE_VILLAGER,
        DisguiseType.VILLAGER),

    NONE();

    private final DisguiseType[] disguiseTypes;

    MethodGroupType(DisguiseType... types) {
        this.disguiseTypes = types;
    }

    MethodGroupType(MethodGroupType inheritFrom, DisguiseType... types) {
        this.disguiseTypes = Arrays.copyOf(types, types.length + inheritFrom.getDisguiseTypes().length);

        System.arraycopy(inheritFrom.getDisguiseTypes(), 0, getDisguiseTypes(), types.length, inheritFrom.getDisguiseTypes().length);
    }
}
