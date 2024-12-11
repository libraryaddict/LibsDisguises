package me.libraryaddict.disguise.utilities.sounds;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup.SoundType;

import java.util.HashMap;

@Getter
public enum DisguiseSoundEnums {
    ALLAY("entity.allay.hurt", null, "entity.allay.death",
        new String[]{"entity.allay.ambient_without_item", "entity.allay.ambient_with_item"}, "entity.allay.item_given",
        "entity.allay.item_taken", "entity.allay.item_thrown"),

    ARMADILLO(new String[]{"entity.armadillo.hurt", "entity.armadillo.hurt_reduced"}, "entity.armadillo.step", "entity.armadillo.death",
        "entity.armadillo.ambient", "entity.armadillo.brush", "entity.armadillo.peek", "entity.armadillo.roll", "entity.armadillo.land",
        "entity.armadillo.scute_drop", "entity.armadillo.unroll_finish", "entity.armadillo.unroll_start"),

    ARMOR_STAND("entity.armor_stand.hit", null, "entity.armor_stand.break", "entity.armor_stand.fall", "entity.armor_stand.place"),

    ARROW(null, null, null, null, "entity.arrow.hit", "entity.arrow.shoot"),

    AXOLOTL("entity.axolotl.hurt", "entity.axolotl.swim", "entity.axolotl.death",
        new String[]{"entity.axolotl.idle_water", "entity.axolotl.idle_air"}, "entity.axolotl.attack", "entity.axolotl.splash"),

    BAT("entity.bat.hurt", null, "entity.bat.death", "entity.bat.ambient", "entity.player.small_fall", "entity.bat.loop",
        "entity.player.big_fall", "entity.bat.takeoff"),

    BEE("entity.bee.hurt", null, "entity.bee.death", null, "entity.bee.loop", "entity.bee.loop_aggressive", "entity.bee.pollinate",
        "entity.bee.sting"),

    BLAZE("entity.blaze.hurt", null, "entity.blaze.death", "entity.blaze.ambient", "entity.player.small_fall", "entity.player.big_fall",
        "entity.blaze.burn", "entity.blaze.shoot"),

    BLOCK_DISPLAY(null, null, null, null),

    BOAT(null, "entity.boat.paddle_water", null, null, "entity.boat.paddle_land"),

    BOGGED("entity.bogged.hurt", "entity.bogged.step", "entity.bogged.death", "entity.bogged.ambient", "entity.bogged.shear"),

    BREEZE("entity.breeze.hurt", null, "entity.breeze.death", new String[]{"entity.breeze.idle_air", "entity.breeze.idle_ground"},
        "entity.breeze.land", "entity.breeze.jump", "entity.breeze.inhale", "entity.breeze.shoot", "entity.breeze.slide",
        "entity.breeze.wind_burst"),

    BREEZE_WIND_CHARGE(null, null, "entity.wind_charge.wind_burst", null, "entity.wind_charge.throw"),

    CAMEL("entity.camel.hurt", new String[]{"entity.camel.step", "entity.camel.step_sand"}, "entity.camel.death", "entity.camel.ambient",
        "entity.camel.dash", "entity.camel.dash_ready", "entity.camel.eat", "entity.camel.saddle", "entity.camel.sit",
        "entity.camel.stand"),

    CAT("entity.cat.hurt", null, "entity.cat.death", "entity.cat.ambient", "entity.cat.purr", "entity.cat.purreow", "entity.cat.hiss"),

    CAVE_SPIDER("entity.spider.hurt", "entity.spider.step", "entity.spider.death", "entity.spider.ambient"),

    CHEST_BOAT(null, "entity.boat.paddle_water", null, null, "entity.boat.paddle_land"),

    CHICKEN("entity.chicken.hurt", "entity.chicken.step", "entity.chicken.death", "entity.chicken.ambient", "entity.player.small_fall",
        "entity.chicken.egg", "entity.player.big_fall"),

    COD("entity.cod.hurt", null, "entity.cod.death", "entity.cod.ambient", "entity.cod.flop", "entity.fish.swim"),

    COW("entity.cow.hurt", "entity.cow.step", "entity.cow.death", "entity.cow.ambient"),

    CREAKING(null, "entity.creaking.step", "entity.creaking.death", "entity.creaking.ambient", "entity.creaking.sway",
        "entity.creaking.activate", "entity.creaking.deactivate", "entity.creaking.spawn", "entity.creaking.freeze",
        "entity.creaking.unfreeze", "entity.creaking.attack"),

    CREEPER("entity.creeper.hurt", "block.grass.step", "entity.creeper.death", null, "entity.creeper.primed"),

    DOLPHIN("entity.dolphin.hurt", "entity.dolphin.swim", "entity.dolphin.death",
        new String[]{"entity.dolphin.ambient", "entity.dolphin.ambient_water"}, "entity.dolphin.attack", "entity.dolphin.eat",
        "entity.dolphin.splash", "entity.dolphin.play", "entity.dolphin.jump", "entity.fish.swim"),

    DONKEY("entity.donkey.hurt", new String[]{"block.grass.step", "entity.horse.step_wood"}, "entity.donkey.death", "entity.donkey.ambient",
        "entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry", "entity.horse.armor", "entity.horse.land", "entity.horse.jump",
        "entity.horse.angry", "entity.donkey.chest"),

    DROWNED(new String[]{"entity.drowned.hurt", "entity.drowned.hurt_water"}, new String[]{"entity.drowned.step", "entity.drowned.swim"},
        new String[]{"entity.drowned.death", "entity.drowned.death_water"},
        new String[]{"entity.drowned.ambient", "entity.drowned.ambient_water"}, "entity.drowned.shoot"),

    ELDER_GUARDIAN(new String[]{"entity.elder_guardian.hurt", "entity.elder_guardian.hurt_land"}, null,
        new String[]{"entity.elder_guardian.death", "entity.elder_guardian.death_land"},
        new String[]{"entity.elder_guardian.ambient", "entity.elder_guardian.ambient_land"}, "entity.elder_guardian.flop"),

    ENDER_DRAGON("entity.ender_dragon.hurt", null, "entity.ender_dragon.death", "entity.ender_dragon.ambient", "entity.generic.small_fall",
        "entity.generic.big_fall", "entity.ender_dragon.flap", "entity.ender_dragon.growl"),

    ENDERMAN("entity.enderman.hurt", "block.grass.step", "entity.enderman.death", "entity.enderman.ambient", "entity.enderman.scream",
        "entity.enderman.teleport", "entity.enderman.stare"),

    ENDERMITE("entity.endermite.hurt", "entity.endermite.step", "entity.endermite.death", "entity.endermite.ambient"),

    EVOKER("entity.evoker.hurt", null, "entity.evoker.death", "entity.evoker.ambient", "entity.evoker.cast_spell",
        "entity.evoker.prepare_attack", "entity.evoker.prepare_summon", "entity.evoker.prepare_wololo"),

    EVOKER_FANGS(null, null, null, null, "entity.evoker_fangs.attack"),

    FOX("entity.fox.hurt", null, "entity.fox.death", "entity.fox.ambient", "entity.fox.aggro", "entity.fox.bite", "entity.fox.eat",
        "entity.fox.screech", "entity.fox.sleep", "entity.fox.spit", "entity.fox.sniff", "entity.fox.teleport"),

    FROG("entity.frog.hurt", "entity.frog.step", "entity.frog.death", "entity.frog.ambient", "entity.frog.eat", "entity.frog.lay_spawn",
        "entity.frog.long_jump", "entity.frog.tongue"),

    GHAST("entity.ghast.hurt", null, "entity.ghast.death", "entity.ghast.ambient", "entity.player.small_fall", "entity.ghast.shoot",
        "entity.player.big_fall", "entity.ghast.scream", "entity.ghast.warn"),

    GIANT("entity.player.hurt", "block.grass.step", null, null),

    GLOW_SQUID("entity.glow_squid.hurt", null, "entity.glow_squid.death", "entity.glow_squid.ambient", "entity.glow_squid.squirt",
        "entity.fish.swim"),

    GOAT("entity.goat.hurt", "entity.goat.step", "entity.goat.death", "entity.goat.ambient", "entity.goat.milk", "entity.goat.eat",
        "entity.goat.long_jump", "entity.goat.prepare_ram", "entity.goat.prepare_ram", "entity.goat.ram_impact",
        "entity.goat.screaming.ambient", "entity.goat.screaming.death", "entity.goat.screaming.eat", "entity.goat.screaming.milk",
        "entity.goat.screaming.ram_impact", "entity.goat.screaming.prepare_ram", "entity.goat.screaming.long_jump",
        "entity.goat.screaming.hurt"),

    GUARDIAN(new String[]{"entity.guardian.hurt", "entity.guardian.hurt_land"}, null,
        new String[]{"entity.guardian.death", "entity.guardian.death_land"},
        new String[]{"entity.guardian.ambient", "entity.guardian.ambient_land"}, "entity.guardian.flop"),

    HOGLIN("entity.hoglin.hurt", "entity.hoglin.step", "entity.hoglin.death", "entity.hoglin.ambient",
        "entity.hoglin.converted_to_zombified", "entity.hoglin.angry", "entity.hoglin.retreat"),

    HORSE("entity.horse.hurt", new String[]{"entity.horse.step", "entity.horse.step_wood"}, "entity.horse.death", "entity.horse.ambient",
        "entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry", "entity.horse.armor", "entity.horse.land", "entity.horse.jump",
        "entity.horse.angry", "entity.horse.eat", "entity.horse.breathe"),

    HUSK("entity.husk.hurt", "entity.husk.step", "entity.husk.death", "entity.husk.ambient", "entity.husk.converted_to_zombie"),

    ILLUSIONER("entity.illusioner.hurt", null, "entity.illusioner.death", "entity.illusioner.ambient", "entity.illusioner.cast_spell",
        "entity.illusioner.prepare_blindness", "entity.illusioner.prepare_mirror", "entity.illusioner.mirror_move"),

    INTERACTION(null, null, null, null),

    IRON_GOLEM("entity.iron_golem.hurt", "entity.iron_golem.step", "entity.iron_golem.death", "entity.iron_golem.attack"),

    ITEM_DISPLAY(null, null, null, null),

    LLAMA("entity.llama.hurt", "entity.llama.step", "entity.llama.death", "entity.llama.ambient", "entity.llama.angry",
        "entity.llama.chest", "entity.llama.eat", "entity.llama.swag"),

    MAGMA_CUBE("entity.magma_cube.hurt", "entity.magma_cube.jump", new String[]{"entity.magma_cube.death", "entity.magma_cube.death_small"},
        null, "entity.magma_cube.squish", "entity.magma_cube.squish_small"),

    MINECART(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MINECART_CHEST(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MINECART_COMMAND(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MINECART_FURNACE(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MINECART_HOPPER(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MINECART_MOB_SPAWNER(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MINECART_TNT(null, "entity.minecart.riding", null, null, "entity.minecart.inside", "entity.minecart.inside.underwater"),

    MULE("entity.mule.hurt", "block.grass.step", "entity.mule.death", "entity.mule.ambient", "entity.mule.chest"),

    MUSHROOM_COW("entity.cow.hurt", "entity.cow.step", "entity.cow.death", "entity.cow.ambient"),

    OCELOT("entity.cat.hurt", "block.grass.step", "entity.cat.death",
        new String[]{"entity.cat.ambient", "entity.cat.purr", "entity.cat.purreow"}, "entity.cat.hiss"),

    PANDA("entity.panda.hurt", "entity.panda.step", "entity.panda.death",
        new String[]{"entity.panda.ambient", "entity.panda.aggressive_ambient", "entity.panda.worried_ambient"}, "entity.panda.bite",
        "entity.panda.cant_breed", "entity.panda.eat", "entity.panda.pre_sneeze", "entity.panda.sneeze"),

    PARROT("entity.parrot.hurt", "entity.parrot.step", "entity.parrot.death", "entity.parrot.ambient",
        (Object[]) new String[]{"entity.parrot.eat", "entity.parrot.fly", "^entity\\.parrot\\.imitate\\..+"}),

    PIG("entity.pig.hurt", "entity.pig.step", "entity.pig.death", "entity.pig.ambient"),

    PIGLIN("entity.piglin.hurt", "entity.piglin.step", "entity.piglin.death", "entity.piglin.ambient", "entity.piglin.retreat",
        "entity.piglin.jealous", "entity.piglin.admiring_item", "entity.piglin.celebrate", "entity.piglin.angry"),

    PIGLIN_BRUTE("entity.piglin_brute.hurt", "entity.piglin_brute.step", "entity.piglin_brute.death", "entity.piglin_brute.ambient",
        "entity.piglin_brute.converted_to_zombified", "entity.piglin_brute.angry"),

    PIG_ZOMBIE("entity.zombie_pigman.hurt", null, "entity.zombie_pigman.death", "entity.zombie_pigman.ambient",
        "entity.zombie_pigman.angry"),

    PILLAGER("entity.pillager.hurt", "block.grass.step", "entity.pillager.death", "entity.pillager.ambient", "entity.pillager.celebrate"),

    PLAYER("entity.player.hurt", new String[]{"^block\\.[a-z_]+\\.step"}, "entity.player.death", null),

    PHANTOM("entity.phantom.hurt", new String[]{"entity.phantom.flap", "entity.phantom.swoop"}, "entity.phantom.death",
        "entity.phantom.ambient", "entity.phantom.bite"),

    POLAR_BEAR("entity.polar_bear.hurt", "entity.polar_bear.step", "entity.polar_bear.death",
        new String[]{"entity.polar_bear.ambient", "entity.polar_bear.ambient_baby"}, "entity.polar_bear.warning"),

    PUFFERFISH("entity.puffer_fish.hurt", null, "entity.puffer_fish.death", "entity.puffer_fish.ambient", "entity.puffer_fish.blow_out",
        "entity.puffer_fish.blow_up", "entity.puffer_fish.flop", "entity.puffer_fish.sting", "entity.fish.swim"),

    RABBIT("entity.rabbit.hurt", "entity.rabbit.jump", "entity.rabbit.death", "entity.rabbit.ambient", "entity.rabbit.attack"),

    RAVAGER("entity.ravager.hurt", "entity.ravager.step", "entity.ravager.death", "entity.ravager.ambient", "entity.ravager.attack",
        "entity.ravager.celebrate", "entity.ravager.roar", "entity.ravager.stunned"),

    SALMON("entity.salmon.hurt", null, "entity.salmon.death", "entity.salmon.ambient", "entity.salmon.flop", "entity.fish.swim"),

    SHEEP("entity.sheep.hurt", "entity.sheep.step", "entity.sheep.death", "entity.sheep.ambient", "entity.sheep.shear"),

    SHULKER(new String[]{"entity.shulker.hurt", "entity.shulker.hurt_closed"}, null, "entity.shulker.death", "entity.shulker.ambient",
        "entity.shulker.open", "entity.shulker.close", "entity.shulker.teleport"),

    SILVERFISH("entity.silverfish.hurt", "entity.silverfish.step", "entity.silverfish.death", "entity.silverfish.ambient"),

    SKELETON("entity.skeleton.hurt", "entity.skeleton.step", "entity.skeleton.death", "entity.skeleton.ambient"),

    SKELETON_HORSE("entity.skeleton_horse.hurt", new String[]{"block.grass.step", "entity.horse.step_wood"}, "entity.skeleton_horse.death",
        new String[]

            {"entity.skeleton_horse.ambient", "entity.skeleton_horse.ambient_water"}, "entity.horse.gallop", "entity.horse.saddle",
        "entity.horse.armor", "entity.horse.land", "entity.horse.jump", "entity.skeleton_horse.gallop_water",
        "entity.skeleton_horse.jump_water", "entity.skeleton_horse.swim", "entity.skeleton_horse.step_water"),

    SLIME(new String[]{"entity.slime.hurt", "entity.slime.hurt_small"}, new String[]

        {"entity.slime.jump", "entity.slime.jump_small"}, new String[]

        {"entity.slime.death", "entity.slime.death_small"}, null, "entity.slime.attack", "entity.slime.squish",
        "entity.slime.squish_small"),

    SNIFFER("entity.sniffer.hurt", "entity.sniffer.step", "entity.sniffer.death", "entity.sniffer.idle", "entity.sniffer.digging",
        "entity.sniffer.digging_stop", "entity.sniffer.drop_seed", "entity.sniffer.eat", "entity.sniffer.searching",
        "entity.sniffer.scenting", "entity.sniffer.happy", "entity.sniffer.sniffing"),

    SNOWMAN("entity.snow_golem.hurt", null, "entity.snow_golem.death", "entity.snow_golem.ambient", "entity.snow_golem.shoot"),

    SPIDER("entity.spider.hurt", "entity.spider.step", "entity.spider.death", "entity.spider.ambient"),

    STRAY("entity.stray.hurt", "entity.stray.step", "entity.stray.death", "entity.stray.ambient"),

    STRIDER("entity.strider.hurt", new String[]{"entity.strider.step", "entity.strider.step_lava"}, "entity.strider.death",
        "entity.strider.ambient", "entity.strider.eat", "entity.strider.happy", "entity.strider.retreat", "entity.strider.saddle"),

    SQUID("entity.squid.hurt", null, "entity.squid.death", "entity.squid.ambient", "entity.squid.squirt", "entity.fish.swim"),

    TADPOLE("entity.tadpole.hurt", null, "entity.tadpole.death", null, "entity.tadpole.flop", "item.bucket.empty_tadpole",
        "item.bucket.fill_tadpole"),

    TEXT_DISPLAY(null, null, null, null),

    TRADER_LLAMA("entity.llama.hurt", "entity.llama.step", "entity.llama.death", "entity.llama.ambient", "entity.llama.angry",
        "entity.llama.chest", "entity.llama.eat", "entity.llama.swag"),

    TROPICAL_FISH("entity.tropical_fish.hurt", null, "entity.tropical_fish.death", "entity.tropical_fish.ambient",
        "entity.tropical_fish.flop", "entity.fish.swim"),

    TURTLE(new String[]{"entity.turtle.hurt", "entity.turtle.hurt_baby"},
        new String[]{"entity.turtle.shamble", "entity.turtle.shamble_baby"},
        new String[]{"entity.turtle.death", "entity.turtle.death_baby"}, "entity.turtle.ambient_land", "entity.turtle.lay_egg"),

    VEX("entity.vex.hurt", null, "entity.vex.death", "entity.vex.ambient", "entity.vex.charge"),

    VILLAGER("entity.villager.hurt", null, "entity.villager.death", "entity.villager.ambient", "entity.villager.trade",
        "entity.villager.no", "entity.villager.yes"),

    VINDICATOR("entity.vindicator.hurt", null, "entity.vindicator.death", "entity.vindicator.ambient"),

    WANDERING_TRADER("entity.wandering_trader.hurt", null, "entity.wandering_trader.death", "entity.wandering_trader.ambient",
        "entity.wandering_trader.no", "entity.wandering_trader.yes", "entity.wandering_trader.trade", "entity.wandering_trader.trade",
        "entity.wandering_trader.reappeared", "entity.wandering_trader.drink_potion", "entity.wandering_trader.drink_milk",
        "entity.wandering_trader.disappeared"),

    WARDEN("entity.warden.hurt", "entity.warden.step", "entity.warden.death", "entity.warden.ambient", "entity.warden.agitated",
        "entity.warden.angry", "entity.warden.attack_impact", "entity.warden.dig", "entity.warden.emerge", "entity.warden.heartbeat",
        "entity.warden.tendril_clicks", "entity.warden.listening", "entity.warden.listening_angry", "entity.warden.nearby_close",
        "entity.warden.nearby_closer", "entity.warden.nearby_closest", "entity.warden.sonic_boom", "entity.warden.sonic_charge",
        "entity.warden.roar", "entity.warden.sniff"),

    WITCH("entity.witch.hurt", null, "entity.witch.death", "entity.witch.ambient"),

    WITHER("entity.wither.hurt", null, "entity.wither.death", "entity.wither.ambient", "entity.player.small_fall", "entity.wither.spawn",
        "entity.player.big_fall", "entity.wither.shoot"),

    WITHER_SKELETON("entity.wither_skeleton.hurt", "entity.wither_skeleton.step", "entity.wither_skeleton.death",
        "entity.wither_skeleton.ambient"),

    WOLF("entity.wolf.hurt", "entity.wolf.step", "entity.wolf.death", "entity.wolf.ambient", "entity.wolf.growl", "entity.wolf.pant",
        "entity.wolf.howl", "entity.wolf.shake", "entity.wolf.whine"),

    ZOGLIN("entity.zoglin.hurt", "entity.zoglin.step", "entity.zoglin.death", "entity.zoglin.ambient", "entity.zoglin.angry",
        "entity.zoglin.attack"),

    ZOMBIE("entity.zombie.hurt", "entity.zombie.step", "entity.zombie.death", "entity.zombie.ambient", "entity.zombie.infect",
        "entity.zombie.attack_wooden_door", "entity.zombie.break_wooden_door", "entity.zombie.attack_iron_door"),

    ZOMBIE_HORSE("entity.zombie_horse.hurt", new String[]{"block.grass.step", "entity.horse.step_wood"}, "entity.zombie_horse.death",
        "entity.zombie_horse.ambient", "entity.horse.gallop", "entity.horse.saddle", "entity.horse.armor", "entity.horse.land",
        "entity.horse.jump", "entity.horse.angry"),

    ZOMBIE_VILLAGER("entity.zombie_villager.hurt", "entity.zombie_villager.step", "entity.zombie_villager.death",
        "entity.zombie_villager.ambient", "entity.zombie.infect", "entity.zombie.attack_wooden_door", "entity.zombie.break_wooden_door",
        "entity.zombie.attack_iron_door"),

    ZOMBIFIED_PIGLIN("entity.zombified_piglin.hurt", null, "entity.zombified_piglin.death", "entity.zombified_piglin.ambient",
        "entity.zombified_piglin.angry", "entity.piglin.converted_to_zombified");

    private final HashMap<String, SoundType> sounds = new HashMap<>();

    DisguiseSoundEnums(Object hurt, Object step, Object death, Object idle, Object... sounds) {
        if (LibsDisguises.getInstance() != null) {
            throw new IllegalStateException("This cannot be called on a running server");
        }

        addSound(hurt, SoundType.HURT);
        addSound(step, SoundType.STEP);
        addSound(death, SoundType.DEATH);
        addSound(idle, SoundType.IDLE);

        for (Object obj : sounds) {
            addSound(obj, SoundType.CANCEL);
        }
    }

    private void addSound(Object sound, SoundType type) {
        if (sound == null) {
            return;
        }

        if (sound instanceof String[]) {
            for (String s : (String[]) sound) {
                if (s == null) {
                    continue;
                }

                addSound(s, type);
            }
        } else if (sound instanceof String) {
            addSound((String) sound, type);
        } else {
            throw new IllegalArgumentException("Was given an unknown object " + sound);
        }
    }

    private void addSound(String sound, SoundType type) {
        sounds.put(sound, type);
    }
}
