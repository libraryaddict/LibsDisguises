package me.libraryaddict.disguise.utilities;

import org.bukkit.Sound;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Only living disguises go in here!
 */
public enum DisguiseSound {

    ARROW(null, null, null, null, "random.bowhit"),
    BAT("mob.bat.hurt", null, "mob.bat.death", "mob.bat.idle", "damage.fallsmall", "mob.bat.loop", "damage.fallbig",
            "mob.bat.takeoff"),
    BLAZE("mob.blaze.hit", null, "mob.blaze.death", "mob.blaze.breathe", "damage.fallsmall", "damage.fallbig"),
    CAVE_SPIDER("mob.spider.say", "mob.spider.step", "mob.spider.death", "mob.spider.say"),
    CHICKEN("mob.chicken.hurt", "mob.chicken.step", "mob.chicken.hurt", "mob.chicken.say", "damage.fallsmall",
            "mob.chicken.plop", "damage.fallbig"),
    COW("mob.cow.hurt", "mob.cow.step", "mob.cow.hurt", "mob.cow.say"),
    CREEPER("mob.creeper.say", "step.grass", "mob.creeper.death", null),
    DONKEY("mob.horse.donkey.hit", "step.grass", "mob.horse.donkey.death", "mob.horse.donkey.idle", "mob.horse.gallop",
            "mob.horse.leather", "mob.horse.donkey.angry", "mob.horse.wood", "mob.horse.armor", "mob.horse.soft",
            "mob.horse.land", "mob.horse.jump", "mob.horse.angry"),
    ELDER_GUARDIAN("mob.guardian.elder.hit", null, "mob.guardian.elder.death", "mob.guardian.elder.death"),
    ENDER_DRAGON("mob.enderdragon.hit", null, "mob.enderdragon.end", "mob.enderdragon.growl", "damage.fallsmall",
            "mob.enderdragon.wings", "damage.fallbig"),
    ENDERMAN("mob.endermen.hit", "step.grass", "mob.endermen.death", "mob.endermen.idle", "mob.endermen.scream",
            "mob.endermen.portal", "mob.endermen.stare"),
    ENDERMITE("mob.silverfish.hit", "mob.silverfish.step", "mob.silverfish.kill", "mob.silverfish.say"),
    GHAST("mob.ghast.scream", null, "mob.ghast.death", "mob.ghast.moan", "damage.fallsmall", "mob.ghast.fireball",
            "damage.fallbig", "mob.ghast.affectionate_scream", "mob.ghast.charge"),
    GIANT("damage.hit", "step.grass", null, null),
    GUARDIAN("mob.guardian.hit", null, "mob.guardian.death", "mob.guardian.death"),
    HORSE("mob.horse.hit", "step.grass", "mob.horse.death", "mob.horse.idle", "mob.horse.gallop", "mob.horse.leather",
            "mob.horse.wood", "mob.horse.armor", "mob.horse.soft", "mob.horse.land", "mob.horse.jump", "mob.horse.angry",
            "mob.horse.leather"),
    IRON_GOLEM("mob.irongolem.hit", "mob.irongolem.walk", "mob.irongolem.death", "mob.irongolem.throw"),
    MAGMA_CUBE("mob.slime.attack", "mob.slime.big", null, null, "mob.slime.small"),
    MULE("mob.horse.donkey.hit", "step.grass", "mob.horse.donkey.death", "mob.horse.donkey.idle"),
    MUSHROOM_COW("mob.cow.hurt", "mob.cow.step", "mob.cow.hurt", "mob.cow.say"),
    OCELOT("mob.cat.hitt", "step.grass", "mob.cat.hitt", "mob.cat.meow", "mob.cat.purreow", "mob.cat.purr"),
    PIG("mob.pig.say", "mob.pig.step", "mob.pig.death", "mob.pig.say"),
    PIG_ZOMBIE("mob.zombiepig.zpighurt", null, "mob.zombiepig.zpigdeath", "mob.zombiepig.zpig", "mob.zombiepig.zpigangry"),
    PLAYER("game.player.hurt", "step.grass", "game.player.hurt", null),
    RABBIT("mob.rabbit.hurt", "mob.rabbit.hop", "mob.rabbit.death", "mob.rabbit.idle"),
    SHEEP("mob.sheep.say", "mob.sheep.step", null, "mob.sheep.say", "mob.sheep.shear"),
    SILVERFISH("mob.silverfish.hit", "mob.silverfish.step", "mob.silverfish.kill", "mob.silverfish.say"),
    SKELETON("mob.skeleton.hurt", "mob.skeleton.step", "mob.skeleton.death", "mob.skeleton.say"),
    SKELETON_HORSE("mob.horse.skeleton.hit", "step.grass", "mob.horse.skeleton.death", "mob.horse.skeleton.idle",
            "mob.horse.gallop", "mob.horse.leather", "mob.horse.wood", "mob.horse.armor", "mob.horse.soft", "mob.horse.land",
            "mob.horse.jump", "mob.horse.angry"),
    SLIME("mob.slime.attack", "mob.slime.big", null, null, "mob.slime.small"),
    SNOWMAN(),
    SPIDER("mob.spider.say", "mob.spider.step", "mob.spider.death", "mob.spider.say"),
    SQUID(),
    UNDEAD_HORSE("mob.horse.zombie.hit", "step.grass", "mob.horse.zombie.death", "mob.horse.zombie.idle", "mob.horse.gallop",
            "mob.horse.leather", "mob.horse.wood", "mob.horse.armor", "mob.horse.soft", "mob.horse.land", "mob.horse.jump",
            "mob.horse.angry"),
    VILLAGER("mob.villager.hit", null, "mob.villager.death", "mob.villager.idle", "mob.villager.haggle", "mob.villager.no",
            "mob.villager.yes"),
    WITCH("mob.witch.hurt", null, "mob.witch.death", "mob.witch.idle"),
    WITHER("mob.wither.hurt", null, "mob.wither.death", "mob.wither.idle", "damage.fallsmall", "mob.wither.spawn",
            "damage.fallbig", "mob.wither.shoot"),
    WITHER_SKELETON("mob.skeleton.hurt", "mob.skeleton.step", "mob.skeleton.death", "mob.skeleton.say"),
    WOLF("mob.wolf.hurt", "mob.wolf.step", "mob.wolf.death", "mob.wolf.bark", "mob.wolf.panting", "mob.wolf.whine",
            "mob.wolf.howl", "mob.wolf.growl", "mob.wolf.shake"),
    ZOMBIE("mob.zombie.hurt", "mob.zombie.step", "mob.zombie.death", "mob.zombie.say", "mob.zombie.infect",
            "mob.zombie.woodbreak", "mob.zombie.metal", "mob.zombie.wood"),
    ZOMBIE_VILLAGER("mob.zombie.hurt", "mob.zombie.step", "mob.zombie.death", "mob.zombie.say", "mob.zombie.infect",
            "mob.zombie.woodbreak", "mob.zombie.metal", "mob.zombie.wood");

    public enum SoundType {

        CANCEL, DEATH, HURT, IDLE, STEP
    }

    public static DisguiseSound getType(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }

    private HashSet<String> cancelSounds = new HashSet<>();
    private float damageSoundVolume = 1F;
    private HashMap<SoundType, String> disguiseSounds = new HashMap<>();

    DisguiseSound(Object... sounds) {
        for (int i = 0; i < sounds.length; i++) {
            Object obj = sounds[i];
            String s;
            if (obj == null) {
                continue;
            } else if (obj instanceof String) {
                s = (String) obj;
            } else if (obj instanceof Sound) {
                s = ReflectionManager.getCraftSound((Sound) obj);
                System.out.print("Warning! The sound " + obj + " needs to be converted to a string");
            } else {
                throw new RuntimeException("Was given a unknown object " + obj);
            }
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
        } else {
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
        } else {
            disguiseSounds.put(type, sound);
        }
    }
}
