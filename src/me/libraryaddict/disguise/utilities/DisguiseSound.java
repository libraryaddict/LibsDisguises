package me.libraryaddict.disguise.utilities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.Sound;

/**
 * Only living disguises go in here!
 */
public enum DisguiseSound {
    ARROW(null, null, null, null, "entity.arrow.hit", "entity.arrow.shoot"),

    BAT("entity.bat.hurt", null, "entity.bat.death", "entity.bat.ambient", "entity.player.small_fall",
            "entity.bat.loop", "entity.player.big_fall", "entity.bat.takeoff"),

    BLAZE("entity.blaze.hurt", null, "entity.blaze.death", "entity.blaze.ambient", "entity.player.small_fall",
            "entity.player.big_fall"),

    CAVE_SPIDER("entity.spider.ambient", "entity.spider.step", "entity.spider.death", "entity.spider.ambient"),

    CHICKEN("entity.chicken.hurt", "entity.chicken.step", "entity.chicken.hurt", "entity.chicken.ambient",
            "entity.player.small_fall", "entity.chicken.egg", "entity.player.big_fall"),

    COW("entity.cow.hurt", "entity.cow.step", "entity.cow.death", "entity.cow.ambient"),

    CREEPER("entity.creeper.hurt", "block.grass.step", "entity.creeper.death", null, "entity.creeper.primed"),

    DONKEY("entity.donkey.hurt", "block.grass.step", "entity.donkey.death", "entity.donkey.ambient",
            "entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry", "entity.horse.step_wood",
            "entity.horse.armor", "entity.horse.land", "entity.horse.jump", "entity.horse.angry"),

    ELDER_GUARDIAN("entity.elder_guardian.hurt", null, "entity.elder_guardian.death", "entity.elder_guardian.ambient"),

    ENDER_DRAGON("entity.enderdragon.hurt", null, "entity.enderdragon.death", "entity.enderdragon.ambient",
            "entity.player.small_fall", "entity.enderdragon.flap", "entity.player.big_fall"),

    ENDERMAN("entity.endermen.hurt", "block.grass.step", "entity.endermen.death", "entity.endermen.ambient",
            "entity.endermen.scream", "entity.endermen.teleport", "entity.endermen.stare"),

    ENDERMITE("entity.silverfish.hurt", "entity.endermite.step", "entity.endermite.death", "entity.endermite.ambient"),

    EVOKER("entity.evocation_illager.hurt", null, "entity.evocation_illager.death", "entity.evocation_illager.ambient",
            "entity.evocation_illager.cast_spell", "entity.evocation_illager.prepare_attack",
            "entity.evocation_illager.prepare_summon", "entity.evocation_illager.prepare_wololo"),

    EVOKER_FANGS(null, null, null, null, "entity.evocation_fangs.attack"),

    GHAST("entity.ghast.hurt", null, "entity.ghast.death", "entity.ghast.ambient", "entity.player.small_fall",
            "entity.ghast.shoot", "entity.player.big_fall", "entity.ghast.scream", "entity.ghast.warn"),

    GIANT("entity.player.hurt", "block.grass.step", null, null),

    GUARDIAN("entity.guardian.hurt", null, "entity.guardian.death", "entity.elder_guardian.ambient"),

    HORSE("entity.horse.hurt", "block.grass.step", "entity.horse.death", "entity.horse.ambient", "entity.horse.gallop",
            "entity.horse.saddle", "entity.donkey.angry", "entity.horse.step_wood", "entity.horse.armor",
            "entity.horse.land", "entity.horse.jump", "entity.horse.angry"),

    ILLUSIONER("entity.illusion_illager.hurt", null, "entity.illusion_illager.death", "entity.illusion_illager.ambient",
            "entity.illusion_illager.cast_spell", "entity.illusion_illager.prepare_blindness",
            "entity.illusion_illager.prepare_mirror", "entity.illusion_illager.mirror_move"),

    IRON_GOLEM("entity.irongolem.hurt", "entity.irongolem.step", "entity.irongolem.death", "entity.irongolem.attack"),

    LLAMA("entity.llama.hurt", "entity.llama.step", "entity.llama.death", "entity.llama.ambient", "entity.llama.angry",
            "entity.llama.chest", "entity.llama.eat", "entity.llama.swag"),

    MAGMA_CUBE("entity.magmacube.hurt", "entity.magmacube.jump", null, null),

    MULE("entity.mule.hurt", "block.grass.step", "entity.mule.death", "entity.mule.ambient"),

    MUSHROOM_COW("entity.cow.hurt", "entity.cow.step", "entity.cow.hurt", "entity.cow.ambient"),

    OCELOT("entity.cat.hurt", "block.grass.step", "entity.cat.hurt", "entity.cat.ambient", "entity.cat.purr",
            "entity.cat.purreow"),

    PARROT("entity.parrot.hurt", "entity.parrot.step", "entity.parrot.death", "entity.parrot.ambient",
            "entity.parrot.eat", "entity.parrot.fly", "entity.parrot.imitate.blaze", "entity.parrot.imitate.creeper",
            "entity.parrot.imitate.elder_guardian", "entity.parrot.imitate.enderdragon",
            "entity.parrot.imitate.enderman", "entity.parrot.imitate.endermite",
            "entity.parrot.imitate.evocation_illager", "entity.parrot.imitate.ghast", "entity.parrot.imitate.husk",
            "entity.parrot.imitate.illusion_illager", "entity.parrot.imitate.magmacube",
            "entity.parrot.imitate.polar_bear", "entity.parrot.imitate.shulker", "entity.parrot.imitate.silverfish",
            "entity.parrot.imitate.skeleton", "entity.parrot.imitate.slime", "entity.parrot.imitate.spider",
            "entity.parrot.imitate.stray", "entity.parrot.imitate.vex", "entity.parrot.imitate.vindication_illager",
            "entity.parrot.imitate.witch", "entity.parrot.imitate.wither", "entity.parrot.imitate.wither_skeleton",
            "entity.parrot.imitate.wolf", "entity.parrot.imitate.zombie", "entity.parrot.imitate.zombie_pigman",
            "entity.parrot.imitate.zombie_villager"),

    PIG("entity.pig.hurt", "entity.pig.step", "entity.pig.death", "entity.pig.ambient"),

    PIG_ZOMBIE("entity.zombie_pig.hurt", null, "entity.zombie_pig.death", "entity.zombie_pig.ambient",
            "entity.zombie_pig.angry"),

    PLAYER("entity.player.hurt",
            new String[]{"block.stone.step", "block.grass.step", "block.anvil.step", "block.cloth.step",
                    "block.glass.step", "block.gravel.step", "block.ladder.step", "block.metal.step", "block.sand.step",
                    "block.slime.step", "block.snow.step", "block.wood.step"}, "entity.player.death", null),

    RABBIT("entity.rabbit.hurt", "entity.rabbit.jump", "entity.rabbit.death", "entity.rabbit.ambient"),

    SHEEP("entity.sheep.hurt", "entity.sheep.step", null, "entity.sheep.ambient", "entity.sheep.shear"),

    SHULKER("entity.shulker.hurt", null, "entity.shulker.death", "entity.shulker.ambient", "entity.shulker.open",
            "entity.shulker.close", "entity.shulker.hurt_closed", "entity.shulker.teleport"),

    SILVERFISH("entity.silverfish.hurt", "entity.silverfish.step", "entity.silverfish.death",
            "entity.silverfish.ambient"),

    SKELETON("entity.skeleton.hurt", "entity.skeleton.step", "entity.skeleton.death", "entity.skeleton.ambient"),

    SKELETON_HORSE("entity.skeleton_horse.hurt", "block.grass.step", "entity.skeleton_horse.death",
            "entity.skeleton_horse.ambient", "entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry",
            "entity.horse.step_wood", "entity.horse.armor", "entity.horse.land", "entity.horse.jump",
            "entity.horse.angry"),

    SLIME("entity.slime.hurt", "entity.slime.jump", "entity.slime.death", null),

    SNOWMAN("entity.snowman.hurt", null, "entity.snowman.death", "entity.snowman.ambient", "entity.snowman.shoot"),

    SPIDER("entity.spider.ambient", "entity.spider.step", "entity.spider.death", "entity.spider.ambient"),

    SQUID("entity.squid.hurt", null, "entity.squid.death", "entity.squid.ambient"),

    ZOMBIE_HORSE("entity.zombie_horse.hurt", "block.grass.step", "entity.zombie_horse.death",
            "entity.zombie_horse.ambient", "entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry",
            "entity.horse.step_wood", "entity.horse.armor", "entity.horse.land", "entity.horse.jump",
            "entity.horse.angry"),

    VEX("entity.vex.hurt", null, "entity.vex.death", "entity.vex.ambient", "entity.vex.charge"),

    VILLAGER("entity.villager.hurt", null, "entity.villager.death", "entity.villager.ambient",
            "entity.villager.trading", "entity.villager.no", "entity.villager.yes"),

    VINDICATOR("entity.vindication_illager.hurt", null, "entity.vindication_illager.death",
            "entity.vindication_illager.ambient"),

    WITCH("entity.witch.hurt", null, "entity.witch.death", "entity.witch.ambient"),

    WITHER("entity.wither.hurt", null, "entity.wither.death", "entity.wither.ambient", "entity.player.small_fall",
            "entity.wither.spawn", "entity.player.big_fall", "entity.wither.shoot"),

    WITHER_SKELETON("entity.skeleton.hurt", "entity.skeleton.step", "entity.skeleton.death", "entity.skeleton.ambient"),

    WOLF("entity.wolf.hurt", "entity.wolf.step", "entity.wolf.death", "entity.wolf.ambient", "entity.wolf.growl",
            "entity.wolf.pant", "entity.wolf.howl", "entity.wolf.shake", "entity.wolf.whine"),

    ZOMBIE("entity.zombie.hurt", "entity.zombie.step", "entity.zombie.death", "entity.zombie.ambient",
            "entity.zombie.infect", "entity.zombie.break_door_wood", "entity.zombie.attack_door_wood",
            "entity.zombie.attack_iron_door"),

    ZOMBIE_VILLAGER("entity.zombie_villager.hurt", "entity.zombie_villager.step", "entity.zombie_villager.death",
            "entity.zombie_villager.ambient", "entity.zombie.infect", "entity.zombie.break_door_wood",
            "entity.zombie.attack_door_wood", "entity.zombie.attack_iron_door");

    public enum SoundType {
        CANCEL,
        DEATH,
        HURT,
        IDLE,
        STEP
    }

    public static DisguiseSound getType(String name) {
        try {
            return valueOf(name);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private float damageSoundVolume = 1F;
    private HashMap<Object, Object> disguiseSounds = new HashMap<>();

    DisguiseSound(Object hurt, Object step, Object death, Object idle, Object... sounds) {
        addSound(hurt, SoundType.HURT);
        addSound(step, SoundType.STEP);
        addSound(death, SoundType.DEATH);
        addSound(idle, SoundType.IDLE);

        for (Object obj : sounds) {
            addSound(obj, SoundType.CANCEL);
        }
    }

    DisguiseSound(Object hurt, Object[] step, Object death, Object idle, Object... sounds) {
        addSound(hurt, SoundType.HURT);

        if (step != null) {
            for (Object obj : step) {
                addSound(obj, SoundType.STEP);
            }
        }

        addSound(death, SoundType.DEATH);
        addSound(idle, SoundType.IDLE);

        for (Object obj : sounds) {
            addSound(obj, SoundType.CANCEL);
        }
    }

    public static void replace(String oldString, String newString) {
        for (DisguiseSound sound : DisguiseSound.values()) {
            if (sound.disguiseSounds.containsKey(oldString)) {
                sound.disguiseSounds.put(newString, sound.disguiseSounds.get(oldString));
            }

            for (Entry<Object, Object> entry : sound.disguiseSounds.entrySet()) {
                if (entry.getValue() == null || !entry.getValue().equals(oldString))
                    continue;

                entry.setValue(newString);
            }
        }
    }

    private void addSound(Object sound, SoundType type) {
        String s;

        if (sound == null) {
            return;
        } else if (sound instanceof String) {
            s = (String) sound;
        } else if (sound instanceof Sound) {
            s = ReflectionManager.getCraftSound((Sound) sound);
        } else {
            throw new RuntimeException("Was given a unknown object " + sound);
        }

        switch (type) {
            case HURT:
                disguiseSounds.put(SoundType.HURT, s);
                break;
            case STEP:
                disguiseSounds.put(s, SoundType.STEP);
                break;
            case DEATH:
                disguiseSounds.put(SoundType.DEATH, s);
                break;
            case IDLE:
                disguiseSounds.put(SoundType.IDLE, s);
                break;
            case CANCEL:
                disguiseSounds.put(s, SoundType.CANCEL);
        }
    }

    public float getDamageAndIdleSoundVolume() {
        return damageSoundVolume;
    }

    public String getSound(SoundType type) {
        if (type == null) {
            return null;
        }

        if (disguiseSounds.containsKey(type)) {
            return (String) disguiseSounds.get(type);
        } else if (disguiseSounds.containsValue(type)) {
            for (Entry<Object, Object> entry : disguiseSounds.entrySet()) {
                if (entry.getValue() != type)
                    continue;

                return (String) entry.getKey();
            }
        }

        return null;
    }

    public SoundType getSound(String sound) {
        if (sound == null) {
            return null;
        }

        if (disguiseSounds.containsKey(sound)) {
            return (SoundType) disguiseSounds.get(sound);
        } else if (disguiseSounds.containsValue(sound)) {
            for (Entry<Object, Object> entry : disguiseSounds.entrySet()) {
                if (!Objects.equals(sound, entry.getValue()))
                    continue;

                return (SoundType) entry.getKey();
            }
        }

        return null;
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

        /*if (disguiseSounds.containsKey(SoundType.STEP) && disguiseSounds.get(SoundType.STEP).startsWith("step.")
                && sound.startsWith("step.")) {
            return SoundType.STEP;
        }*/

        for (SoundType type : SoundType.values()) {
            if (!disguiseSounds
                    .containsKey(type) || type == SoundType.DEATH || (ignoreDamage && type == SoundType.HURT)) {
                continue;
            }

            Object s = disguiseSounds.get(type);

            if (s != null) {
                if (Objects.equals(s, sound)) {
                    return type;
                }
            } else {
                for (Entry<Object, Object> entry : disguiseSounds.entrySet()) {
                    if (!Objects.equals(sound, entry.getKey()))
                        continue;

                    return (SoundType) entry.getValue();
                }
            }
        }

        return null;
    }

    public boolean isCancelSound(String sound) {
        return getSound(sound) == SoundType.CANCEL;
    }

    /*  public void removeSound(SoundType type, Sound sound) {
        removeSound(type, ReflectionManager.getCraftSound(sound));
    }

    public void removeSound(SoundType type, String sound) {
        if (type == SoundType.CANCEL) {
            cancelSounds.remove(sound);
        }
        else {
            disguiseSounds.remove(type);
        }
    }*/

    public void setDamageAndIdleSoundVolume(float strength) {
        this.damageSoundVolume = strength;
    }

    /* public void setSound(SoundType type, Sound sound) {
        setSound(type, ReflectionManager.getCraftSound(sound));
    }

    public void setSound(SoundType type, String sound) {
        if (type == SoundType.CANCEL) {
            cancelSounds.add(sound);
        }
        else {
            disguiseSounds.put(type, sound);
        }
    }*/
}
