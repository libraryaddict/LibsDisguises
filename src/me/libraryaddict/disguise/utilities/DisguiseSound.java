package me.libraryaddict.disguise.utilities;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Sound;

/**
 * Only living disguises go in here!
 */
public enum DisguiseSound {

    ARROW(null, null, null, null, Sound.ENTITY_ARROW_HIT, Sound.ENTITY_ARROW_SHOOT),

    BAT(Sound.ENTITY_BAT_HURT, null, Sound.ENTITY_BAT_DEATH, Sound.ENTITY_BAT_AMBIENT, Sound.ENTITY_PLAYER_SMALL_FALL,
            Sound.ENTITY_BAT_LOOP, Sound.ENTITY_PLAYER_BIG_FALL, Sound.ENTITY_BAT_TAKEOFF),

    BLAZE(Sound.ENTITY_BLAZE_HURT, null, Sound.ENTITY_BLAZE_DEATH, Sound.ENTITY_BLAZE_AMBIENT, Sound.ENTITY_PLAYER_SMALL_FALL,
            Sound.ENTITY_PLAYER_BIG_FALL),

    CAVE_SPIDER(Sound.ENTITY_SPIDER_AMBIENT, Sound.ENTITY_SPIDER_STEP, Sound.ENTITY_SPIDER_DEATH, Sound.ENTITY_SPIDER_AMBIENT),

    CHICKEN(Sound.ENTITY_CHICKEN_HURT, Sound.ENTITY_CHICKEN_STEP, Sound.ENTITY_CHICKEN_HURT, Sound.ENTITY_CHICKEN_AMBIENT,
            Sound.ENTITY_PLAYER_SMALL_FALL, Sound.ENTITY_CHICKEN_EGG, Sound.ENTITY_PLAYER_BIG_FALL),

    COW(Sound.ENTITY_COW_HURT, Sound.ENTITY_COW_STEP, Sound.ENTITY_COW_DEATH, Sound.ENTITY_COW_AMBIENT),

    CREEPER(Sound.ENTITY_CREEPER_HURT, "step.grass", Sound.ENTITY_CREEPER_DEATH, null),

    DONKEY(Sound.ENTITY_DONKEY_HURT, "step.grass", Sound.ENTITY_DONKEY_DEATH, Sound.ENTITY_DONKEY_AMBIENT,
            Sound.ENTITY_HORSE_GALLOP, Sound.ENTITY_HORSE_SADDLE, Sound.ENTITY_DONKEY_ANGRY, Sound.ENTITY_HORSE_STEP_WOOD,
            Sound.ENTITY_HORSE_ARMOR, Sound.ENTITY_HORSE_LAND, Sound.ENTITY_HORSE_JUMP, Sound.ENTITY_HORSE_ANGRY),

    ELDER_GUARDIAN(Sound.ENTITY_ELDER_GUARDIAN_HURT, null, Sound.ENTITY_ELDER_GUARDIAN_DEATH,
            Sound.ENTITY_ELDER_GUARDIAN_AMBIENT),

    ENDER_DRAGON(Sound.ENTITY_ENDERDRAGON_HURT, null, Sound.ENTITY_ENDERDRAGON_DEATH, Sound.ENTITY_ENDERDRAGON_AMBIENT,
            Sound.ENTITY_PLAYER_SMALL_FALL, Sound.ENTITY_ENDERDRAGON_FLAP, Sound.ENTITY_PLAYER_BIG_FALL),

    ENDERMAN(Sound.ENTITY_ENDERMEN_HURT, "step.grass", Sound.ENTITY_ENDERMEN_DEATH, Sound.ENTITY_ENDERMEN_AMBIENT,
            Sound.ENTITY_ENDERMEN_SCREAM, Sound.ENTITY_ENDERMEN_TELEPORT, Sound.ENTITY_ENDERMEN_STARE),

    ENDERMITE(Sound.ENTITY_SILVERFISH_HURT, Sound.ENTITY_ENDERMITE_STEP, Sound.ENTITY_ENDERMITE_DEATH,
            Sound.ENTITY_ENDERMITE_AMBIENT),

    EVOKER(Sound.ENTITY_EVOCATION_ILLAGER_HURT, null, Sound.ENTITY_EVOCATION_ILLAGER_DEATH,
            Sound.ENTITY_EVOCATION_ILLAGER_AMBIENT, Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL,
            Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK, Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON,
            Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_WOLOLO),

    EVOKER_FANGS(null, null, null, null, Sound.ENTITY_EVOCATION_FANGS_ATTACK),

    GHAST(Sound.ENTITY_GHAST_HURT, null, Sound.ENTITY_GHAST_DEATH, Sound.ENTITY_GHAST_AMBIENT, Sound.ENTITY_PLAYER_SMALL_FALL,
            Sound.ENTITY_GHAST_SHOOT, Sound.ENTITY_PLAYER_BIG_FALL, Sound.ENTITY_GHAST_SCREAM, Sound.ENTITY_GHAST_WARN),

    GIANT(Sound.ENTITY_PLAYER_HURT, "step.grass", null, null),

    GUARDIAN(Sound.ENTITY_GUARDIAN_HURT, null, Sound.ENTITY_GUARDIAN_DEATH, Sound.ENTITY_ELDER_GUARDIAN_AMBIENT),

    HORSE(Sound.ENTITY_HORSE_HURT, "step.grass", Sound.ENTITY_HORSE_DEATH, Sound.ENTITY_HORSE_AMBIENT, Sound.ENTITY_HORSE_GALLOP,
            Sound.ENTITY_HORSE_SADDLE, Sound.ENTITY_DONKEY_ANGRY, Sound.ENTITY_HORSE_STEP_WOOD, Sound.ENTITY_HORSE_ARMOR,
            Sound.ENTITY_HORSE_LAND, Sound.ENTITY_HORSE_JUMP, Sound.ENTITY_HORSE_ANGRY),

    IRON_GOLEM(Sound.ENTITY_IRONGOLEM_HURT, Sound.ENTITY_IRONGOLEM_STEP, Sound.ENTITY_IRONGOLEM_DEATH,
            Sound.ENTITY_IRONGOLEM_ATTACK),

    LLAMA(Sound.ENTITY_LLAMA_HURT, Sound.ENTITY_LLAMA_STEP, Sound.ENTITY_LLAMA_DEATH, Sound.ENTITY_LLAMA_AMBIENT,
            Sound.ENTITY_LLAMA_ANGRY, Sound.ENTITY_LLAMA_CHEST, Sound.ENTITY_LLAMA_EAT, Sound.ENTITY_LLAMA_SWAG),

    MAGMA_CUBE(Sound.ENTITY_MAGMACUBE_HURT, Sound.ENTITY_MAGMACUBE_JUMP, null, null),

    MULE(Sound.ENTITY_MULE_HURT, "step.grass", Sound.ENTITY_MULE_DEATH, Sound.ENTITY_MULE_AMBIENT),

    MUSHROOM_COW(Sound.ENTITY_COW_HURT, Sound.ENTITY_COW_STEP, Sound.ENTITY_COW_HURT, Sound.ENTITY_COW_AMBIENT),

    OCELOT(Sound.ENTITY_CAT_HURT, "step.grass", Sound.ENTITY_CAT_HURT, Sound.ENTITY_CAT_AMBIENT, Sound.ENTITY_CAT_PURR,
            Sound.ENTITY_CAT_PURREOW),

    PIG(Sound.ENTITY_PIG_HURT, Sound.ENTITY_PIG_STEP, Sound.ENTITY_PIG_DEATH, Sound.ENTITY_PIG_AMBIENT),

    PIG_ZOMBIE(Sound.ENTITY_ZOMBIE_PIG_HURT, null, Sound.ENTITY_ZOMBIE_PIG_DEATH, Sound.ENTITY_ZOMBIE_PIG_AMBIENT,
            Sound.ENTITY_ZOMBIE_PIG_ANGRY),

    PLAYER(Sound.ENTITY_PLAYER_HURT, "step.grass", Sound.ENTITY_PLAYER_DEATH, null),

    RABBIT(Sound.ENTITY_RABBIT_HURT, Sound.ENTITY_RABBIT_JUMP, Sound.ENTITY_RABBIT_DEATH, Sound.ENTITY_RABBIT_AMBIENT),

    SHEEP(Sound.ENTITY_SHEEP_HURT, Sound.ENTITY_SHEEP_STEP, null, Sound.ENTITY_SHEEP_AMBIENT, Sound.ENTITY_SHEEP_SHEAR),

    SHULKER(Sound.ENTITY_SHULKER_HURT, null, Sound.ENTITY_SHULKER_DEATH, Sound.ENTITY_SHULKER_AMBIENT, Sound.ENTITY_SHULKER_OPEN,
            Sound.ENTITY_SHULKER_CLOSE, Sound.ENTITY_SHULKER_HURT_CLOSED, Sound.ENTITY_SHULKER_TELEPORT),

    SILVERFISH(Sound.ENTITY_SILVERFISH_HURT, Sound.ENTITY_SILVERFISH_STEP, Sound.ENTITY_SILVERFISH_DEATH,
            Sound.ENTITY_SILVERFISH_AMBIENT),

    SKELETON(Sound.ENTITY_SKELETON_HURT, Sound.ENTITY_SKELETON_STEP, Sound.ENTITY_SKELETON_DEATH, Sound.ENTITY_SKELETON_AMBIENT),

    SKELETON_HORSE(Sound.ENTITY_SKELETON_HORSE_HURT, "step.grass", Sound.ENTITY_SKELETON_HORSE_DEATH,
            Sound.ENTITY_SKELETON_HORSE_AMBIENT, Sound.ENTITY_HORSE_GALLOP, Sound.ENTITY_HORSE_SADDLE, Sound.ENTITY_DONKEY_ANGRY,
            Sound.ENTITY_HORSE_STEP_WOOD, Sound.ENTITY_HORSE_ARMOR, Sound.ENTITY_HORSE_LAND, Sound.ENTITY_HORSE_JUMP,
            Sound.ENTITY_HORSE_ANGRY),

    SLIME(Sound.ENTITY_SLIME_HURT, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_SLIME_DEATH, null),

    SNOWMAN(Sound.ENTITY_SNOWMAN_HURT, null, Sound.ENTITY_SNOWMAN_DEATH, Sound.ENTITY_SNOWMAN_AMBIENT,
            Sound.ENTITY_SNOWMAN_SHOOT),

    SPIDER(Sound.ENTITY_SPIDER_AMBIENT, Sound.ENTITY_SPIDER_STEP, Sound.ENTITY_SPIDER_DEATH, Sound.ENTITY_SPIDER_AMBIENT),

    SQUID(Sound.ENTITY_SQUID_HURT, null, Sound.ENTITY_SQUID_DEATH, Sound.ENTITY_SQUID_AMBIENT),

    UNDEAD_HORSE(Sound.ENTITY_ZOMBIE_HORSE_HURT, "step.grass", Sound.ENTITY_ZOMBIE_HORSE_DEATH, Sound.ENTITY_ZOMBIE_HORSE_AMBIENT,
            Sound.ENTITY_HORSE_GALLOP, Sound.ENTITY_HORSE_SADDLE, Sound.ENTITY_DONKEY_ANGRY, Sound.ENTITY_HORSE_STEP_WOOD,
            Sound.ENTITY_HORSE_ARMOR, Sound.ENTITY_HORSE_LAND, Sound.ENTITY_HORSE_JUMP, Sound.ENTITY_HORSE_ANGRY),

    VEX(Sound.ENTITY_VEX_HURT, null, Sound.ENTITY_VEX_DEATH, Sound.ENTITY_VEX_AMBIENT, Sound.ENTITY_VEX_CHARGE),

    VILLAGER(Sound.ENTITY_VILLAGER_HURT, null, Sound.ENTITY_VILLAGER_DEATH, Sound.ENTITY_VILLAGER_AMBIENT,
            Sound.ENTITY_VILLAGER_TRADING, Sound.ENTITY_VILLAGER_NO, Sound.ENTITY_VILLAGER_YES),

    VINDICATOR(Sound.ENTITY_VINDICATION_ILLAGER_HURT, null, Sound.ENTITY_VINDICATION_ILLAGER_DEATH,
            Sound.ENTITY_VINDICATION_ILLAGER_AMBIENT),

    WITCH(Sound.ENTITY_WITCH_HURT, null, Sound.ENTITY_WITCH_DEATH, Sound.ENTITY_WITCH_AMBIENT),

    WITHER(Sound.ENTITY_WITHER_HURT, null, Sound.ENTITY_WITHER_DEATH, Sound.ENTITY_WITHER_AMBIENT, Sound.ENTITY_PLAYER_SMALL_FALL,
            Sound.ENTITY_WITHER_SPAWN, Sound.ENTITY_PLAYER_BIG_FALL, Sound.ENTITY_WITHER_SHOOT),

    WITHER_SKELETON(Sound.ENTITY_SKELETON_HURT, Sound.ENTITY_SKELETON_STEP, Sound.ENTITY_SKELETON_DEATH,
            Sound.ENTITY_SKELETON_AMBIENT),

    WOLF(Sound.ENTITY_WOLF_HURT, Sound.ENTITY_WOLF_STEP, Sound.ENTITY_WOLF_DEATH, Sound.ENTITY_WOLF_AMBIENT,
            Sound.ENTITY_WOLF_GROWL, Sound.ENTITY_WOLF_PANT, Sound.ENTITY_WOLF_HOWL, Sound.ENTITY_WOLF_SHAKE,
            Sound.ENTITY_WOLF_WHINE),

    ZOMBIE(Sound.ENTITY_ZOMBIE_HURT, Sound.ENTITY_ZOMBIE_STEP, Sound.ENTITY_ZOMBIE_DEATH, Sound.ENTITY_ZOMBIE_AMBIENT,
            Sound.ENTITY_ZOMBIE_INFECT, Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD,
            Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR), ZOMBIE_VILLAGER(Sound.ENTITY_ZOMBIE_VILLAGER_HURT,
                    Sound.ENTITY_ZOMBIE_VILLAGER_STEP, Sound.ENTITY_ZOMBIE_VILLAGER_DEATH, Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT,
                    Sound.ENTITY_ZOMBIE_INFECT, Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD,
                    Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR);

    public enum SoundType {
        CANCEL, DEATH, HURT, IDLE, STEP
    }

    public static DisguiseSound getType(String name) {
        try {
            return valueOf(name);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private HashSet<String> cancelSounds = new HashSet<>();
    private float damageSoundVolume = 1F;
    private HashMap<SoundType, String> disguiseSounds = new HashMap<>();

    DisguiseSound(Object hurt, Object step, Object death, Object idle, Object... sounds) {
        addSound(hurt, SoundType.HURT);
        addSound(step, SoundType.STEP);
        addSound(death, SoundType.DEATH);
        addSound(idle, SoundType.IDLE);

        for (Object obj : sounds) {
            addSound(obj, SoundType.CANCEL);
        }

    }

    private void addSound(Object sound, SoundType type) {
        String s;

        if (sound == null) {
            return;
        }
        else if (sound instanceof String) {
            s = (String) sound;
        }
        else if (sound instanceof Sound) {
            s = ReflectionManager.getCraftSound((Sound) sound);
        }
        else {
            throw new RuntimeException("Was given a unknown object " + sound);
        }

        switch (type) {
        case HURT:
            disguiseSounds.put(SoundType.HURT, s);
            break;
        case STEP:
            disguiseSounds.put(SoundType.STEP, s);
            break;
        case DEATH:
            disguiseSounds.put(SoundType.DEATH, s);
            break;
        case IDLE:
            disguiseSounds.put(SoundType.IDLE, s);
            break;
        case CANCEL:
            cancelSounds.add(s);
        }
    }

    public float getDamageAndIdleSoundVolume() {
        return damageSoundVolume;
    }

    public String getSound(SoundType type) {
        if (type == null || !disguiseSounds.containsKey(type)) {
            return null;
        }

        return disguiseSounds.get(type);
    }

    public HashSet<String> getSoundsToCancel() {
        return cancelSounds;
    }

    /**
     * Used to check if this sound name is owned by this disguise sound.
     */
    public SoundType getType(String sound, boolean ignoreDamage) {
        if (sound == null)
            return SoundType.CANCEL;

        if (isCancelSound(sound)) {
            return SoundType.CANCEL;
        }

        if (disguiseSounds.containsKey(SoundType.STEP) && disguiseSounds.get(SoundType.STEP).startsWith("step.")
                && sound.startsWith("step.")) {
            return SoundType.STEP;
        }

        for (SoundType type : SoundType.values()) {
            if (!disguiseSounds.containsKey(type) || type == SoundType.DEATH || (ignoreDamage && type == SoundType.HURT)) {
                continue;
            }

            String s = disguiseSounds.get(type);

            if (s != null) {
                if (s.equals(sound)) {
                    return type;
                }
            }
        }

        return null;
    }

    public boolean isCancelSound(String sound) {
        return getSoundsToCancel().contains(sound);
    }

    public void removeSound(SoundType type, Sound sound) {
        removeSound(type, ReflectionManager.getCraftSound(sound));
    }

    public void removeSound(SoundType type, String sound) {
        if (type == SoundType.CANCEL) {
            cancelSounds.remove(sound);
        }
        else {
            disguiseSounds.remove(type);
        }
    }

    public void setDamageAndIdleSoundVolume(float strength) {
        this.damageSoundVolume = strength;
    }

    public void setSound(SoundType type, Sound sound) {
        setSound(type, ReflectionManager.getCraftSound(sound));
    }

    public void setSound(SoundType type, String sound) {
        if (type == SoundType.CANCEL) {
            cancelSounds.add(sound);
        }
        else {
            disguiseSounds.put(type, sound);
        }
    }
}
