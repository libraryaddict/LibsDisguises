package me.libraryaddict.disguise.disguisetypes;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_6_R3.CraftSound;

public enum DisguiseSound {

    BAT(Sound.BAT_HURT, null, Sound.BAT_DEATH, Sound.BAT_IDLE, Sound.BAT_LOOP, Sound.BAT_TAKEOFF, Sound.FALL_BIG,
            Sound.FALL_SMALL),

    BLAZE(Sound.BLAZE_HIT, null, Sound.BLAZE_DEATH, Sound.BLAZE_BREATH, Sound.FALL_BIG, Sound.FALL_SMALL),

    CAVE_SPIDER(Sound.SPIDER_IDLE, Sound.SPIDER_WALK, Sound.SPIDER_DEATH, Sound.SPIDER_IDLE),

    CHICKEN(Sound.CHICKEN_HURT, Sound.CHICKEN_WALK, Sound.CHICKEN_HURT, Sound.CHICKEN_IDLE, Sound.CHICKEN_EGG_POP,
            Sound.FALL_BIG, Sound.FALL_SMALL),

    COW(Sound.COW_HURT, Sound.COW_WALK, Sound.COW_HURT, Sound.COW_IDLE),

    CREEPER(Sound.CREEPER_HISS, Sound.STEP_GRASS, Sound.CREEPER_DEATH),

    DONKEY(Sound.DONKEY_HIT, Sound.STEP_GRASS, Sound.DONKEY_DEATH, Sound.DONKEY_IDLE, Sound.DONKEY_ANGRY, Sound.HORSE_GALLOP,
            Sound.HORSE_ANGRY, Sound.HORSE_ARMOR, Sound.HORSE_JUMP, Sound.HORSE_LAND, Sound.HORSE_SADDLE, Sound.HORSE_SOFT,
            Sound.HORSE_WOOD),

    ENDER_DRAGON(Sound.ENDERDRAGON_HIT, null, Sound.ENDERDRAGON_DEATH, Sound.ENDERDRAGON_GROWL, Sound.ENDERDRAGON_WINGS,
            Sound.FALL_BIG, Sound.FALL_SMALL),

    ENDERMAN(Sound.ENDERMAN_HIT, Sound.STEP_GRASS, Sound.ENDERMAN_DEATH, Sound.ENDERMAN_IDLE, Sound.ENDERMAN_STARE,
            Sound.ENDERMAN_TELEPORT, Sound.ENDERMAN_SCREAM),

    GHAST(Sound.GHAST_SCREAM, null, Sound.GHAST_DEATH, Sound.GHAST_MOAN, Sound.GHAST_CHARGE, Sound.GHAST_FIREBALL,
            Sound.GHAST_SCREAM2, Sound.FALL_BIG, Sound.FALL_SMALL),

    GIANT(Sound.HURT_FLESH, Sound.STEP_GRASS),

    HORSE(Sound.HORSE_HIT, Sound.STEP_GRASS, "mob.horse.death", Sound.HORSE_IDLE, Sound.HORSE_GALLOP, Sound.HORSE_ANGRY,
            Sound.HORSE_ARMOR, Sound.HORSE_JUMP, Sound.HORSE_LAND, Sound.HORSE_SADDLE, Sound.HORSE_SOFT, Sound.HORSE_WOOD),

    IRON_GOLEM(Sound.IRONGOLEM_HIT, Sound.IRONGOLEM_WALK, Sound.IRONGOLEM_DEATH, Sound.IRONGOLEM_THROW),

    LEASH_HITCH(),

    MAGMA_CUBE(Sound.SLIME_ATTACK, Sound.SLIME_WALK2, null, null, Sound.SLIME_WALK),

    MULE(Sound.DONKEY_HIT, Sound.STEP_GRASS, Sound.DONKEY_DEATH, Sound.DONKEY_IDLE),

    MUSHROOM_COW(Sound.COW_HURT, Sound.COW_WALK, Sound.COW_HURT, Sound.COW_IDLE),

    OCELOT(Sound.CAT_HIT, Sound.STEP_GRASS, Sound.CAT_HIT, Sound.CAT_MEOW, Sound.CAT_PURR, Sound.CAT_PURREOW),

    PIG(Sound.PIG_IDLE, Sound.PIG_WALK, Sound.PIG_DEATH, Sound.PIG_IDLE),

    PIG_ZOMBIE(Sound.ZOMBIE_PIG_HURT, null, Sound.ZOMBIE_PIG_DEATH, Sound.ZOMBIE_PIG_IDLE, Sound.ZOMBIE_PIG_ANGRY),

    PLAYER(Sound.HURT_FLESH, Sound.STEP_GRASS, Sound.HURT_FLESH),

    SHEEP(Sound.SHEEP_IDLE, Sound.SHEEP_WALK, null, Sound.SHEEP_IDLE, Sound.SHEEP_SHEAR),

    SILVERFISH(Sound.SILVERFISH_HIT, Sound.SILVERFISH_WALK, Sound.SILVERFISH_KILL, Sound.SILVERFISH_IDLE),

    SKELETON(Sound.SKELETON_HURT, Sound.SKELETON_WALK, Sound.SKELETON_DEATH, Sound.SKELETON_IDLE),

    SKELETON_HORSE("mob.horse.skeleton.hit", Sound.STEP_GRASS, Sound.HORSE_SKELETON_DEATH, Sound.HORSE_SKELETON_IDLE,
            Sound.HORSE_GALLOP, Sound.HORSE_ANGRY, Sound.HORSE_ARMOR, Sound.HORSE_JUMP, Sound.HORSE_LAND, Sound.HORSE_SADDLE,
            Sound.HORSE_SOFT, Sound.HORSE_WOOD),

    SLIME(Sound.SLIME_ATTACK, Sound.SLIME_WALK2, null, null, Sound.SLIME_WALK),

    SNOWMAN(),

    SPIDER(Sound.SPIDER_IDLE, Sound.SPIDER_WALK, Sound.SPIDER_DEATH, Sound.SPIDER_IDLE),

    SQUID(),

    UNDEAD_HORSE(Sound.HORSE_ZOMBIE_HIT, Sound.STEP_GRASS, Sound.HORSE_ZOMBIE_DEATH, Sound.HORSE_ZOMBIE_IDLE, Sound.HORSE_GALLOP,
            Sound.HORSE_ANGRY, Sound.HORSE_ARMOR, Sound.HORSE_JUMP, Sound.HORSE_LAND, Sound.HORSE_SADDLE, Sound.HORSE_SOFT,
            Sound.HORSE_WOOD),

    VILLAGER(Sound.VILLAGER_HIT, null, Sound.VILLAGER_DEATH, Sound.VILLAGER_IDLE, Sound.VILLAGER_HAGGLE, Sound.VILLAGER_YES,
            Sound.VILLAGER_NO),

    WITCH("mob.witch.hurt", null, "mob.witch.death", "mob.witch.idle"),

    WITHER(Sound.WITHER_HURT, null, Sound.WITHER_DEATH, Sound.WITHER_IDLE, Sound.WITHER_SHOOT, Sound.WITHER_SPAWN,
            Sound.FALL_BIG, Sound.FALL_SMALL),

    WITHER_SKELETON(Sound.SKELETON_HURT, Sound.SKELETON_WALK, Sound.SKELETON_DEATH, Sound.SKELETON_IDLE),

    WOLF(Sound.WOLF_HURT, Sound.WOLF_WALK, Sound.WOLF_DEATH, Sound.WOLF_BARK, Sound.WOLF_WHINE, Sound.WOLF_GROWL,
            Sound.WOLF_HOWL, Sound.WOLF_PANT, Sound.WOLF_SHAKE),

    ZOMBIE(Sound.ZOMBIE_HURT, Sound.STEP_GRASS, Sound.ZOMBIE_DEATH, Sound.ZOMBIE_IDLE, Sound.ZOMBIE_INFECT, Sound.ZOMBIE_METAL,
            Sound.ZOMBIE_WOODBREAK, Sound.ZOMBIE_WOOD);

    public enum SoundType {
        CANCEL, DEATH, HURT, IDLE, STEP;
    }

    public static String getSoundName(Sound sound) {
        return CraftSound.getSound(sound);
    }

    public static DisguiseSound getType(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }

    private HashSet<String> cancelSounds = new HashSet<String>();
    private float damageSoundVolume = 1F;
    private HashMap<SoundType, String> disguiseSounds = new HashMap<SoundType, String>();

    private DisguiseSound(Object... sounds) {
        for (int i = 0; i < sounds.length; i++) {
            Object obj = sounds[i];
            String s;
            if (obj == null)
                continue;
            else if (obj instanceof String)
                s = (String) obj;
            else if (obj instanceof Sound)
                s = CraftSound.getSound((Sound) obj);
            else
                throw new RuntimeException("Was given a unknown object " + obj);
            switch (i) {
            case 0:
                disguiseSounds.put(SoundType.HURT, s);
                break;
            case 1:
                disguiseSounds.put(SoundType.STEP, s);
                break;
            case 2:
                disguiseSounds.put(SoundType.DEATH, s);
                break;
            case 3:
                disguiseSounds.put(SoundType.IDLE, s);
                break;
            default:
                cancelSounds.add(s);
                break;
            }
        }
    }

    public float getDamageSoundVolume() {
        return damageSoundVolume;
    }

    public String getSound(SoundType type) {
        if (type == null || !disguiseSounds.containsKey(type))
            return null;
        return disguiseSounds.get(type);
    }

    public HashSet<String> getSoundsToCancel() {
        return cancelSounds;
    }

    /**
     * Used to check if this sound name is owned by this disguise sound.
     */
    public SoundType getType(String sound, boolean ignoreDamage) {
        if (isCancelSound(sound))
            return SoundType.CANCEL;
        if (disguiseSounds.containsKey(SoundType.STEP) && disguiseSounds.get(SoundType.STEP).startsWith("step.")
                && sound.startsWith("step."))
            return SoundType.STEP;
        for (SoundType type : SoundType.values()) {
            if (!disguiseSounds.containsKey(type) || type == SoundType.DEATH || (ignoreDamage && type == SoundType.HURT))
                continue;
            String s = disguiseSounds.get(type);
            if (s != null) {
                if (s.equals(sound))
                    return type;
            }
        }
        return null;
    }

    public boolean isCancelled(String soundName) {
        return cancelSounds.contains(soundName);
    }

    public boolean isCancelSound(String sound) {
        return getSoundsToCancel().contains(sound);
    }

    public void removeSound(SoundType type, Sound sound) {
        removeSound(type, CraftSound.getSound(sound));
    }

    public void removeSound(SoundType type, String sound) {
        if (type == SoundType.CANCEL)
            cancelSounds.remove(sound);
        else {
            disguiseSounds.remove(type);
        }
    }

    public void setDamageSoundVolume(float strength) {
        this.damageSoundVolume = strength;
    }

    public void setSound(SoundType type, Sound sound) {
        setSound(type, CraftSound.getSound(sound));
    }

    public void setSound(SoundType type, String sound) {
        if (type == SoundType.CANCEL)
            cancelSounds.add(sound);
        else {
            disguiseSounds.put(type, sound);
        }
    }
}
