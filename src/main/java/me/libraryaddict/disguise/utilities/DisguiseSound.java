package me.libraryaddict.disguise.utilities;

import org.bukkit.Sound;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Only living disguises go in here!
 */
public enum DisguiseSound {

    //TODO: Sounds need to be updated to reflect new 1.9 sounds... oh god...
    ARROW(null, null, null, null, "random.bowhit"),
    BAT("entity.bat.hurt", null, "entity.bat.death", "entity.bat.idle", "damage.fallsmall", "entity.bat.loop", "damage.fallbig",
            "entity.bat.takeoff"),
    BLAZE("entity.blaze.hit", null, "entity.blaze.death", "entity.blaze.breathe", "damage.fallsmall", "damage.fallbig"),
    CAVE_SPIDER("entity.spider.ambient", "entity.spider.step", "entity.spider.death", "entity.spider.ambient"),
    CHICKEN("entity.chicken.hurt", "entity.chicken.step", "entity.chicken.hurt", "entity.chicken.ambient", "damage.fallsmall",
            "entity.chicken.plop", "damage.fallbig"),
    COW("entity.cow.hurt", "entity.cow.step", "entity.cow.hurt", "entity.cow.ambient"),
    CREEPER("entity.creeper.ambient", "step.grass", "entity.creeper.death", null),
    DONKEY("entity.horse.donkey.hit", "step.grass", "entity.horse.donkey.death", "entity.horse.donkey.idle", "entity.horse.gallop",
            "entity.horse.leather", "entity.horse.donkey.angry", "entity.horse.wood", "entity.horse.armor", "entity.horse.soft",
            "entity.horse.land", "entity.horse.jump", "entity.horse.angry"),
    ELDER_GUARDIAN("entity.guardian.elder.hit", null, "entity.guardian.elder.death", "entity.guardian.elder.death"),
    ENDER_DRAGON("entity.enderdragon.hit", null, "entity.enderdragon.end", "entity.enderdragon.growl", "damage.fallsmall",
            "entity.enderdragon.wings", "damage.fallbig"),
    ENDERMAN("entity.endermen.hit", "step.grass", "entity.endermen.death", "entity.endermen.idle", "entity.endermen.scream",
            "entity.endermen.portal", "entity.endermen.stare"),
    ENDERMITE("entity.silverfish.hit", "entity.silverfish.step", "entity.silverfish.kill", "entity.silverfish.ambient"),
    GHAST("entity.ghast.scream", null, "entity.ghast.death", "entity.ghast.moan", "damage.fallsmall", "entity.ghast.fireball",
            "damage.fallbig", "entity.ghast.affectionate_scream", "entity.ghast.charge"),
    GIANT("damage.hit", "step.grass", null, null),
    GUARDIAN("entity.guardian.hit", null, "entity.guardian.death", "entity.guardian.death"),
    HORSE("entity.horse.hit", "step.grass", "entity.horse.death", "entity.horse.idle", "entity.horse.gallop", "entity.horse.leather",
            "entity.horse.wood", "entity.horse.armor", "entity.horse.soft", "entity.horse.land", "entity.horse.jump", "entity.horse.angry",
            "entity.horse.leather"),
    IRON_GOLEM("entity.irongolem.hit", "entity.irongolem.walk", "entity.irongolem.death", "entity.irongolem.throw"),
    MAGMA_CUBE("entity.slime.attack", "entity.slime.big", null, null, "entity.slime.small"),
    MULE("entity.horse.donkey.hit", "step.grass", "entity.horse.donkey.death", "entity.horse.donkey.idle"),
    MUSHROOM_COW("entity.cow.hurt", "entity.cow.step", "entity.cow.hurt", "entity.cow.ambient"),
    OCELOT("entity.cat.hitt", "step.grass", "entity.cat.hitt", "entity.cat.meow", "entity.cat.purreow", "entity.cat.purr"),
    PIG("entity.pig.ambient", "entity.pig.step", "entity.pig.death", "entity.pig.ambient"),
    PIG_ZOMBIE("entity.zombiepig.zpighurt", null, "entity.zombiepig.zpigdeath", "entity.zombiepig.zpig", "entity.zombiepig.zpigangry"),
    PLAYER("entity.player.hurt", "step.grass", "entity.player.hurt", null),
    RABBIT("entity.rabbit.hurt", "entity.rabbit.hop", "entity.rabbit.death", "entity.rabbit.idle"),
    SHEEP("entity.sheep.ambient", "entity.sheep.step", null, "entity.sheep.ambient", "entity.sheep.shear"),
    SHULKER("entity.shulker.hurt", null, "entity.shulker.death", "entity.shulker.ambient", "entity.shulker.open",
            "entity.shulker.hurt_closed", "entity.shulker.close", "entity.shulker.teleport", "entity.shulker_bullet.hit",
            "entity.shulker_bullet.hurt"),
    SILVERFISH("entity.silverfish.hit", "entity.silverfish.step", "entity.silverfish.kill", "entity.silverfish.ambient"),
    SKELETON("entity.skeleton.hurt", "entity.skeleton.step", "entity.skeleton.death", "entity.skeleton.ambient"),
    SKELETON_HORSE("entity.horse.skeleton.hit", "step.grass", "entity.horse.skeleton.death", "entity.horse.skeleton.idle",
            "entity.horse.gallop", "entity.horse.leather", "entity.horse.wood", "entity.horse.armor", "entity.horse.soft", "entity.horse.land",
            "entity.horse.jump", "entity.horse.angry"),
    SLIME("entity.slime.attack", "entity.slime.big", null, null, "entity.slime.small"),
    SNOWMAN(),
    SPIDER("entity.spider.ambient", "entity.spider.step", "entity.spider.death", "entity.spider.ambient"),
    SQUID(),
    UNDEAD_HORSE("entity.horse.zombie.hit", "step.grass", "entity.horse.zombie.death", "entity.horse.zombie.idle", "entity.horse.gallop",
            "entity.horse.leather", "entity.horse.wood", "entity.horse.armor", "entity.horse.soft", "entity.horse.land", "entity.horse.jump",
            "entity.horse.angry"),
    VILLAGER("entity.villager.hit", null, "entity.villager.death", "entity.villager.idle", "entity.villager.haggle", "entity.villager.no",
            "entity.villager.yes"),
    WITCH("entity.witch.hurt", null, "entity.witch.death", "entity.witch.idle"),
    WITHER("entity.wither.hurt", null, "entity.wither.death", "entity.wither.idle", "damage.fallsmall", "entity.wither.spawn",
            "damage.fallbig", "entity.wither.shoot"),
    WITHER_SKELETON("entity.skeleton.hurt", "entity.skeleton.step", "entity.skeleton.death", "entity.skeleton.ambient"),
    WOLF("entity.wolf.hurt", "entity.wolf.step", "entity.wolf.death", "entity.wolf.bark", "entity.wolf.panting", "entity.wolf.whine",
            "entity.wolf.howl", "entity.wolf.growl", "entity.wolf.shake"),
    ZOMBIE("entity.zombie.hurt", "entity.zombie.step", "entity.zombie.death", "entity.zombie.ambient", "entity.zombie.infect",
            "entity.zombie.break_wood_door", "entity.zombie.attack_wood_door", "entity.zombie.break_wood_door"),
    ZOMBIE_VILLAGER("entity.zombie.hurt", "entity.zombie.step", "entity.zombie.death", "entity.zombie.ambient", "entity.zombie.infect",
            "entity.zombie.break_wood_door", "entity.zombie.attack_wood_door", "entity.zombie.break_wood_door");

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
        if (sound == null) return SoundType.CANCEL;
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
