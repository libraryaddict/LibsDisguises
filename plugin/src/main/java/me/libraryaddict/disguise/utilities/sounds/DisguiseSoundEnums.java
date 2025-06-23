package me.libraryaddict.disguise.utilities.sounds;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup.SoundType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class DisguiseSoundEnums {
    @Getter
    private static final List<DisguiseSoundEnums> values = new ArrayList<>();
    private final String name;
    private final HashMap<String, SoundType> sounds = new HashMap<>();
    private String[] variants;

    private DisguiseSoundEnums(String name) {
        // We seperate the sound loading and definition logic because the server is likely to be missing sounds via outdated packetevents
        // It's not really worth the hassle
        if (LibsDisguises.getInstance() != null) {
            throw new IllegalStateException("This should not be called on a running server");
        }

        this.name = name;

        getValues().add(this);
    }

    public DisguiseSoundEnums setVariants(String... variants) {
        this.variants = variants;

        return this;
    }

    public DisguiseSoundEnums setHurt(String... sounds) {
        return setSounds(SoundGroup.SoundType.HURT, sounds);
    }

    public DisguiseSoundEnums setIdle(String... sounds) {
        return setSounds(SoundGroup.SoundType.IDLE, sounds);
    }

    public DisguiseSoundEnums setStep(String... sounds) {
        return setSounds(SoundGroup.SoundType.STEP, sounds);
    }

    public DisguiseSoundEnums setDeath(String... sounds) {
        return setSounds(SoundGroup.SoundType.DEATH, sounds);
    }

    public DisguiseSoundEnums setIgnored(String... sounds) {
        return setSounds(SoundGroup.SoundType.CANCEL, sounds);
    }

    private DisguiseSoundEnums setSounds(SoundGroup.SoundType type, String[] sounds) {
        for (String s : sounds) {
            this.sounds.put(s, type);
        }

        return this;
    }

    private static DisguiseSoundEnums register(String name) {
        return new DisguiseSoundEnums(name);
    }

    static {
        register("ALLAY").setHurt("entity.allay.hurt").setDeath("entity.allay.death")
            .setIdle("entity.allay.ambient_without_item", "entity.allay.ambient_with_item")
            .setIgnored("entity.allay.item_given", "entity.allay.item_taken", "entity.allay.item_thrown");

        register("ARMADILLO").setHurt("entity.armadillo.hurt", "entity.armadillo.hurt_reduced").setDeath("entity.armadillo.death")
            .setStep("entity.armadillo.step").setIdle("entity.armadillo.ambient")
            .setIgnored("entity.armadillo.brush", "entity.armadillo.peek", "entity.armadillo.roll", "entity.armadillo.land",
                "entity.armadillo.scute_drop", "entity.armadillo.unroll_finish", "entity.armadillo.unroll_start");

        register("ARMOR_STAND").setHurt("entity.armor_stand.hit").setDeath("entity.armor_stand.break").setIdle("entity.armor_stand.fall")
            .setIgnored("entity.armor_stand.place");

        register("ARROW").setIgnored("entity.arrow.hit", "entity.arrow.shoot");

        register("AXOLOTL").setHurt("entity.axolotl.hurt").setDeath("entity.axolotl.death").setStep("entity.axolotl.swim")
            .setIdle("entity.axolotl.idle_water", "entity.axolotl.idle_air").setIgnored("entity.axolotl.attack", "entity.axolotl.splash");

        register("BAT").setHurt("entity.bat.hurt").setDeath("entity.bat.death").setIdle("entity.bat.ambient")
            .setIgnored("entity.player.small_fall", "entity.bat.loop", "entity.player.big_fall", "entity.bat.takeoff");

        register("BEE").setHurt("entity.bee.hurt").setDeath("entity.bee.death")
            .setIgnored("entity.bee.loop", "entity.bee.loop_aggressive", "entity.bee.pollinate", "entity.bee.sting");

        register("BLAZE").setHurt("entity.blaze.hurt").setDeath("entity.blaze.death").setIdle("entity.blaze.ambient")
            .setIgnored("entity.player.small_fall", "entity.player.big_fall", "entity.blaze.burn", "entity.blaze.shoot");

        register("BLOCK_DISPLAY");

        register("BOAT").setStep("entity.boat.paddle_water").setIgnored("entity.boat.paddle_land");

        register("BOGGED").setHurt("entity.bogged.hurt").setDeath("entity.bogged.death").setStep("entity.bogged.step")
            .setIdle("entity.bogged.ambient").setIgnored("entity.bogged.shear");

        register("BREEZE").setHurt("entity.breeze.hurt").setDeath("entity.breeze.death")
            .setIdle("entity.breeze.idle_air", "entity.breeze.idle_ground")
            .setIgnored("entity.breeze.land", "entity.breeze.jump", "entity.breeze.inhale", "entity.breeze.shoot", "entity.breeze.slide",
                "entity.breeze.wind_burst");

        register("BREEZE_WIND_CHARGE").setDeath("entity.wind_charge.wind_burst").setIgnored("entity.wind_charge.throw");

        register("CAMEL").setHurt("entity.camel.hurt").setDeath("entity.camel.death").setStep("entity.camel.step", "entity.camel.step_sand")
            .setIdle("entity.camel.ambient")
            .setIgnored("entity.camel.dash", "entity.camel.dash_ready", "entity.camel.eat", "entity.camel.saddle", "entity.camel.sit",
                "entity.camel.stand");

        register("CAT").setHurt("entity.cat.hurt").setDeath("entity.cat.death").setIdle("entity.cat.ambient")
            .setIgnored("entity.cat.purr", "entity.cat.purreow", "entity.cat.hiss");

        register("CAVE_SPIDER").setHurt("entity.spider.hurt").setDeath("entity.spider.death").setStep("entity.spider.step")
            .setIdle("entity.spider.ambient");

        register("CHEST_BOAT").setStep("entity.boat.paddle_water").setIgnored("entity.boat.paddle_land");

        register("CHICKEN").setHurt("entity.chicken.hurt").setDeath("entity.chicken.death").setStep("entity.chicken.step")
            .setIdle("entity.chicken.ambient").setIgnored("entity.player.small_fall", "entity.chicken.egg", "entity.player.big_fall");

        register("COD").setHurt("entity.cod.hurt").setDeath("entity.cod.death").setIdle("entity.cod.ambient")
            .setIgnored("entity.cod.flop", "entity.fish.swim");

        register("COW").setHurt("entity.cow.hurt").setDeath("entity.cow.death").setStep("entity.cow.step").setIdle("entity.cow.ambient");

        register("CREAKING").setDeath("entity.creaking.death").setStep("entity.creaking.step").setIdle("entity.creaking.ambient")
            .setIgnored("entity.creaking.sway", "entity.creaking.activate", "entity.creaking.deactivate", "entity.creaking.spawn",
                "entity.creaking.freeze", "entity.creaking.unfreeze", "entity.creaking.attack");

        register("CREEPER").setHurt("entity.creeper.hurt").setDeath("entity.creeper.death").setStep("block.grass.step")
            .setIgnored("entity.creeper.primed");

        register("DOLPHIN").setHurt("entity.dolphin.hurt").setDeath("entity.dolphin.death").setStep("entity.dolphin.swim")
            .setIdle("entity.dolphin.ambient", "entity.dolphin.ambient_water")
            .setIgnored("entity.dolphin.attack", "entity.dolphin.eat", "entity.dolphin.splash", "entity.dolphin.play",
                "entity.dolphin.jump", "entity.fish.swim");

        register("DONKEY").setHurt("entity.donkey.hurt").setDeath("entity.donkey.death")
            .setStep("block.grass.step", "entity.horse.step_wood").setIdle("entity.donkey.ambient")
            .setIgnored("entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry", "entity.horse.armor", "entity.horse.land",
                "entity.horse.jump", "entity.horse.angry", "entity.donkey.chest");

        register("DROWNED").setHurt("entity.drowned.hurt", "entity.drowned.hurt_water")
            .setDeath("entity.drowned.death", "entity.drowned.death_water").setStep("entity.drowned.step", "entity.drowned.swim")
            .setIdle("entity.drowned.ambient", "entity.drowned.ambient_water").setIgnored("entity.drowned.shoot");

        register("ELDER_GUARDIAN").setHurt("entity.elder_guardian.hurt", "entity.elder_guardian.hurt_land")
            .setDeath("entity.elder_guardian.death", "entity.elder_guardian.death_land")
            .setIdle("entity.elder_guardian.ambient", "entity.elder_guardian.ambient_land").setIgnored("entity.elder_guardian.flop");

        register("ENDERMAN").setHurt("entity.enderman.hurt").setDeath("entity.enderman.death").setStep("block.grass.step")
            .setIdle("entity.enderman.ambient").setIgnored("entity.enderman.scream", "entity.enderman.teleport", "entity.enderman.stare");

        register("ENDERMITE").setHurt("entity.endermite.hurt").setDeath("entity.endermite.death").setStep("entity.endermite.step")
            .setIdle("entity.endermite.ambient");

        register("ENDER_DRAGON").setHurt("entity.ender_dragon.hurt").setDeath("entity.ender_dragon.death")
            .setIdle("entity.ender_dragon.ambient")
            .setIgnored("entity.generic.small_fall", "entity.generic.big_fall", "entity.ender_dragon.flap", "entity.ender_dragon.growl");

        register("EVOKER").setHurt("entity.evoker.hurt").setDeath("entity.evoker.death").setIdle("entity.evoker.ambient")
            .setIgnored("entity.evoker.cast_spell", "entity.evoker.prepare_attack", "entity.evoker.prepare_summon",
                "entity.evoker.prepare_wololo");

        register("EVOKER_FANGS").setIgnored("entity.evoker_fangs.attack");

        register("FOX").setHurt("entity.fox.hurt").setDeath("entity.fox.death").setIdle("entity.fox.ambient")
            .setIgnored("entity.fox.aggro", "entity.fox.bite", "entity.fox.eat", "entity.fox.screech", "entity.fox.sleep",
                "entity.fox.spit", "entity.fox.sniff", "entity.fox.teleport");

        register("FROG").setHurt("entity.frog.hurt").setDeath("entity.frog.death").setStep("entity.frog.step")
            .setIdle("entity.frog.ambient")
            .setIgnored("entity.frog.eat", "entity.frog.lay_spawn", "entity.frog.long_jump", "entity.frog.tongue");

        register("GHAST").setHurt("entity.ghast.hurt").setDeath("entity.ghast.death").setIdle("entity.ghast.ambient")
            .setIgnored("entity.player.small_fall", "entity.ghast.shoot", "entity.player.big_fall", "entity.ghast.scream",
                "entity.ghast.warn");

        register("GHASTLING").setHurt("entity.ghastling.hurt").setDeath("entity.ghastling.death").setIdle("entity.ghastling.ambient")
            .setIgnored("entity.ghastling.spawn");

        register("GIANT").setHurt("entity.player.hurt").setStep("block.grass.step");

        register("GLOW_SQUID").setHurt("entity.glow_squid.hurt").setDeath("entity.glow_squid.death").setIdle("entity.glow_squid.ambient")
            .setIgnored("entity.glow_squid.squirt", "entity.fish.swim");

        register("GOAT").setHurt("entity.goat.hurt").setDeath("entity.goat.death").setStep("entity.goat.step")
            .setIdle("entity.goat.ambient")
            .setIgnored("entity.goat.milk", "entity.goat.eat", "entity.goat.long_jump", "entity.goat.prepare_ram",
                "entity.goat.prepare_ram", "entity.goat.ram_impact", "entity.goat.screaming.ambient", "entity.goat.screaming.death",
                "entity.goat.screaming.eat", "entity.goat.screaming.milk", "entity.goat.screaming.ram_impact",
                "entity.goat.screaming.prepare_ram", "entity.goat.screaming.long_jump", "entity.goat.screaming.hurt");

        register("GUARDIAN").setHurt("entity.guardian.hurt", "entity.guardian.hurt_land")
            .setDeath("entity.guardian.death", "entity.guardian.death_land")
            .setIdle("entity.guardian.ambient", "entity.guardian.ambient_land").setIgnored("entity.guardian.flop");

        register("HAPPY_GHAST").setHurt("entity.happy_ghast.hurt").setDeath("entity.happy_ghast.death")
            .setIdle("entity.happy_ghast.ambient")
            .setIgnored("entity.happy_ghast.riding", "entity.happy_ghast.harness_goggles_down", "entity.happy_ghast.harness_goggles_up",
                "entity.happy_ghast.equip", "entity.happy_ghast.unequip");

        register("HOGLIN").setHurt("entity.hoglin.hurt").setDeath("entity.hoglin.death").setStep("entity.hoglin.step")
            .setIdle("entity.hoglin.ambient")
            .setIgnored("entity.hoglin.converted_to_zombified", "entity.hoglin.angry", "entity.hoglin.retreat");

        register("HORSE").setHurt("entity.horse.hurt").setDeath("entity.horse.death").setStep("entity.horse.step", "entity.horse.step_wood")
            .setIdle("entity.horse.ambient")
            .setIgnored("entity.horse.gallop", "entity.horse.saddle", "entity.donkey.angry", "entity.horse.armor", "entity.horse.land",
                "entity.horse.jump", "entity.horse.angry", "entity.horse.eat", "entity.horse.breathe");

        register("HUSK").setHurt("entity.husk.hurt").setDeath("entity.husk.death").setStep("entity.husk.step")
            .setIdle("entity.husk.ambient").setIgnored("entity.husk.converted_to_zombie");

        register("ILLUSIONER").setHurt("entity.illusioner.hurt").setDeath("entity.illusioner.death").setIdle("entity.illusioner.ambient")
            .setIgnored("entity.illusioner.cast_spell", "entity.illusioner.prepare_blindness", "entity.illusioner.prepare_mirror",
                "entity.illusioner.mirror_move");

        register("INTERACTION");

        register("IRON_GOLEM").setHurt("entity.iron_golem.hurt").setDeath("entity.iron_golem.death").setStep("entity.iron_golem.step")
            .setIdle("entity.iron_golem.attack");

        register("ITEM_DISPLAY");

        register("LLAMA").setHurt("entity.llama.hurt").setDeath("entity.llama.death").setStep("entity.llama.step")
            .setIdle("entity.llama.ambient")
            .setIgnored("entity.llama.angry", "entity.llama.chest", "entity.llama.eat", "entity.llama.swag");

        register("MAGMA_CUBE").setHurt("entity.magma_cube.hurt").setDeath("entity.magma_cube.death", "entity.magma_cube.death_small")
            .setStep("entity.magma_cube.jump").setIgnored("entity.magma_cube.squish", "entity.magma_cube.squish_small");

        register("MINECART").setStep("entity.minecart.riding").setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MINECART_CHEST").setStep("entity.minecart.riding")
            .setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MINECART_COMMAND").setStep("entity.minecart.riding")
            .setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MINECART_FURNACE").setStep("entity.minecart.riding")
            .setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MINECART_HOPPER").setStep("entity.minecart.riding")
            .setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MINECART_MOB_SPAWNER").setStep("entity.minecart.riding")
            .setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MINECART_TNT").setStep("entity.minecart.riding")
            .setIgnored("entity.minecart.inside", "entity.minecart.inside.underwater");

        register("MULE").setHurt("entity.mule.hurt").setDeath("entity.mule.death").setStep("block.grass.step")
            .setIdle("entity.mule.ambient").setIgnored("entity.mule.chest");

        register("MUSHROOM_COW").setHurt("entity.cow.hurt").setDeath("entity.cow.death").setStep("entity.cow.step")
            .setIdle("entity.cow.ambient");

        register("OCELOT").setHurt("entity.cat.hurt").setDeath("entity.cat.death").setStep("block.grass.step")
            .setIdle("entity.cat.ambient", "entity.cat.purr", "entity.cat.purreow").setIgnored("entity.cat.hiss");

        register("PANDA").setHurt("entity.panda.hurt").setDeath("entity.panda.death").setStep("entity.panda.step")
            .setIdle("entity.panda.ambient", "entity.panda.aggressive_ambient", "entity.panda.worried_ambient")
            .setIgnored("entity.panda.bite", "entity.panda.cant_breed", "entity.panda.eat", "entity.panda.pre_sneeze",
                "entity.panda.sneeze");

        register("PARROT").setHurt("entity.parrot.hurt").setStep("entity.parrot.step").setDeath("entity.parrot.death")
            .setIdle("entity.parrot.ambient").setIgnored("entity.parrot.eat", "entity.parrot.fly", "^entity\\.parrot\\.imitate\\..+");

        register("PHANTOM").setHurt("entity.phantom.hurt").setDeath("entity.phantom.death")
            .setStep("entity.phantom.flap", "entity.phantom.swoop").setIdle("entity.phantom.ambient").setIgnored("entity.phantom.bite");

        register("PIG").setHurt("entity.pig.hurt").setDeath("entity.pig.death").setStep("entity.pig.step").setIdle("entity.pig.ambient");

        register("PIGLIN").setHurt("entity.piglin.hurt").setDeath("entity.piglin.death").setStep("entity.piglin.step")
            .setIdle("entity.piglin.ambient")
            .setIgnored("entity.piglin.retreat", "entity.piglin.jealous", "entity.piglin.admiring_item", "entity.piglin.celebrate",
                "entity.piglin.angry");

        register("PIGLIN_BRUTE").setHurt("entity.piglin_brute.hurt").setDeath("entity.piglin_brute.death")
            .setStep("entity.piglin_brute.step").setIdle("entity.piglin_brute.ambient")
            .setIgnored("entity.piglin_brute.converted_to_zombified", "entity.piglin_brute.angry");

        register("PIG_ZOMBIE").setHurt("entity.zombie_pigman.hurt").setDeath("entity.zombie_pigman.death")
            .setIdle("entity.zombie_pigman.ambient").setIgnored("entity.zombie_pigman.angry");

        register("PILLAGER").setHurt("entity.pillager.hurt").setDeath("entity.pillager.death").setStep("block.grass.step")
            .setIdle("entity.pillager.ambient").setIgnored("entity.pillager.celebrate");

        register("PLAYER").setHurt("entity.player.hurt").setDeath("entity.player.death").setStep("^block\\.[a-z_]+\\.step");

        register("POLAR_BEAR").setHurt("entity.polar_bear.hurt").setDeath("entity.polar_bear.death").setStep("entity.polar_bear.step")
            .setIdle("entity.polar_bear.ambient", "entity.polar_bear.ambient_baby").setIgnored("entity.polar_bear.warning");

        register("PUFFERFISH").setHurt("entity.puffer_fish.hurt").setDeath("entity.puffer_fish.death").setIdle("entity.puffer_fish.ambient")
            .setIgnored("entity.puffer_fish.blow_out", "entity.puffer_fish.blow_up", "entity.puffer_fish.flop", "entity.puffer_fish.sting",
                "entity.fish.swim");

        register("RABBIT").setHurt("entity.rabbit.hurt").setDeath("entity.rabbit.death").setStep("entity.rabbit.jump")
            .setIdle("entity.rabbit.ambient").setIgnored("entity.rabbit.attack");

        register("RAVAGER").setHurt("entity.ravager.hurt").setDeath("entity.ravager.death").setStep("entity.ravager.step")
            .setIdle("entity.ravager.ambient")
            .setIgnored("entity.ravager.attack", "entity.ravager.celebrate", "entity.ravager.roar", "entity.ravager.stunned");

        register("SALMON").setHurt("entity.salmon.hurt").setDeath("entity.salmon.death").setIdle("entity.salmon.ambient")
            .setIgnored("entity.salmon.flop", "entity.fish.swim");

        register("SHEEP").setHurt("entity.sheep.hurt").setDeath("entity.sheep.death").setStep("entity.sheep.step")
            .setIdle("entity.sheep.ambient").setIgnored("entity.sheep.shear");

        register("SHULKER").setHurt("entity.shulker.hurt", "entity.shulker.hurt_closed").setDeath("entity.shulker.death")
            .setIdle("entity.shulker.ambient").setIgnored("entity.shulker.open", "entity.shulker.close", "entity.shulker.teleport");

        register("SILVERFISH").setHurt("entity.silverfish.hurt").setDeath("entity.silverfish.death").setStep("entity.silverfish.step")
            .setIdle("entity.silverfish.ambient");

        register("SKELETON").setHurt("entity.skeleton.hurt").setDeath("entity.skeleton.death").setStep("entity.skeleton.step")
            .setIdle("entity.skeleton.ambient");

        register("SKELETON_HORSE").setHurt("entity.skeleton_horse.hurt").setDeath("entity.skeleton_horse.death")
            .setStep("block.grass.step", "entity.horse.step_wood")
            .setIdle("entity.skeleton_horse.ambient", "entity.skeleton_horse.ambient_water")
            .setIgnored("entity.horse.gallop", "entity.horse.saddle", "entity.horse.armor", "entity.horse.land", "entity.horse.jump",
                "entity.skeleton_horse.gallop_water", "entity.skeleton_horse.jump_water", "entity.skeleton_horse.swim",
                "entity.skeleton_horse.step_water");

        register("SLIME").setHurt("entity.slime.hurt", "entity.slime.hurt_small").setDeath("entity.slime.death", "entity.slime.death_small")
            .setStep("entity.slime.jump", "entity.slime.jump_small")
            .setIgnored("entity.slime.attack", "entity.slime.squish", "entity.slime.squish_small");

        register("SNIFFER").setHurt("entity.sniffer.hurt").setDeath("entity.sniffer.death").setStep("entity.sniffer.step")
            .setIdle("entity.sniffer.idle")
            .setIgnored("entity.sniffer.digging", "entity.sniffer.digging_stop", "entity.sniffer.drop_seed", "entity.sniffer.eat",
                "entity.sniffer.searching", "entity.sniffer.scenting", "entity.sniffer.happy", "entity.sniffer.sniffing");

        register("SNOWMAN").setHurt("entity.snow_golem.hurt").setDeath("entity.snow_golem.death").setIdle("entity.snow_golem.ambient")
            .setIgnored("entity.snow_golem.shoot");

        register("SPIDER").setHurt("entity.spider.hurt").setDeath("entity.spider.death").setStep("entity.spider.step")
            .setIdle("entity.spider.ambient");

        register("SQUID").setHurt("entity.squid.hurt").setDeath("entity.squid.death").setIdle("entity.squid.ambient")
            .setIgnored("entity.squid.squirt", "entity.fish.swim");

        register("STRAY").setHurt("entity.stray.hurt").setDeath("entity.stray.death").setStep("entity.stray.step")
            .setIdle("entity.stray.ambient");

        register("STRIDER").setHurt("entity.strider.hurt").setDeath("entity.strider.death")
            .setStep("entity.strider.step", "entity.strider.step_lava").setIdle("entity.strider.ambient")
            .setIgnored("entity.strider.eat", "entity.strider.happy", "entity.strider.retreat", "entity.strider.saddle");

        register("TADPOLE").setHurt("entity.tadpole.hurt").setDeath("entity.tadpole.death")
            .setIgnored("entity.tadpole.flop", "item.bucket.empty_tadpole", "item.bucket.fill_tadpole");

        register("TEXT_DISPLAY");

        register("TRADER_LLAMA").setHurt("entity.llama.hurt").setDeath("entity.llama.death").setStep("entity.llama.step")
            .setIdle("entity.llama.ambient")
            .setIgnored("entity.llama.angry", "entity.llama.chest", "entity.llama.eat", "entity.llama.swag");

        register("TROPICAL_FISH").setHurt("entity.tropical_fish.hurt").setDeath("entity.tropical_fish.death")
            .setIdle("entity.tropical_fish.ambient").setIgnored("entity.tropical_fish.flop", "entity.fish.swim");

        register("TURTLE").setHurt("entity.turtle.hurt", "entity.turtle.hurt_baby")
            .setDeath("entity.turtle.death", "entity.turtle.death_baby").setStep("entity.turtle.shamble", "entity.turtle.shamble_baby")
            .setIdle("entity.turtle.ambient_land").setIgnored("entity.turtle.lay_egg");

        register("VEX").setHurt("entity.vex.hurt").setDeath("entity.vex.death").setIdle("entity.vex.ambient")
            .setIgnored("entity.vex.charge");

        register("VILLAGER").setHurt("entity.villager.hurt").setDeath("entity.villager.death").setIdle("entity.villager.ambient")
            .setIgnored("entity.villager.trade", "entity.villager.no", "entity.villager.yes");

        register("VINDICATOR").setHurt("entity.vindicator.hurt").setDeath("entity.vindicator.death").setIdle("entity.vindicator.ambient");

        register("WANDERING_TRADER").setHurt("entity.wandering_trader.hurt").setDeath("entity.wandering_trader.death")
            .setIdle("entity.wandering_trader.ambient")
            .setIgnored("entity.wandering_trader.no", "entity.wandering_trader.yes", "entity.wandering_trader.trade",
                "entity.wandering_trader.trade", "entity.wandering_trader.reappeared", "entity.wandering_trader.drink_potion",
                "entity.wandering_trader.drink_milk", "entity.wandering_trader.disappeared");

        register("WARDEN").setHurt("entity.warden.hurt").setDeath("entity.warden.death").setStep("entity.warden.step")
            .setIdle("entity.warden.ambient")
            .setIgnored("entity.warden.agitated", "entity.warden.angry", "entity.warden.attack_impact", "entity.warden.dig",
                "entity.warden.emerge", "entity.warden.heartbeat", "entity.warden.tendril_clicks", "entity.warden.listening",
                "entity.warden.listening_angry", "entity.warden.nearby_close", "entity.warden.nearby_closer",
                "entity.warden.nearby_closest", "entity.warden.sonic_boom", "entity.warden.sonic_charge", "entity.warden.roar",
                "entity.warden.sniff");

        register("WITCH").setHurt("entity.witch.hurt").setDeath("entity.witch.death").setIdle("entity.witch.ambient");

        register("WITHER").setHurt("entity.wither.hurt").setDeath("entity.wither.death").setIdle("entity.wither.ambient")
            .setIgnored("entity.player.small_fall", "entity.wither.spawn", "entity.player.big_fall", "entity.wither.shoot");

        register("WITHER_SKELETON").setHurt("entity.wither_skeleton.hurt").setDeath("entity.wither_skeleton.death")
            .setStep("entity.wither_skeleton.step").setIdle("entity.wither_skeleton.ambient");

        register("WOLF").setHurt("entity.wolf.hurt").setStep("entity.wolf.step").setDeath("entity.wolf.death")
            .setIdle("entity.wolf.ambient").setIgnored("entity.wolf.growl", "entity.wolf.pant", "entity.wolf.white", "entity.wolf.howl")
            .setVariants("puglin", "sad", "angry", "grumpy", "big", "cute");

        register("ZOGLIN").setHurt("entity.zoglin.hurt").setDeath("entity.zoglin.death").setStep("entity.zoglin.step")
            .setIdle("entity.zoglin.ambient").setIgnored("entity.zoglin.angry", "entity.zoglin.attack");

        register("ZOMBIE").setHurt("entity.zombie.hurt").setDeath("entity.zombie.death").setStep("entity.zombie.step")
            .setIdle("entity.zombie.ambient")
            .setIgnored("entity.zombie.infect", "entity.zombie.attack_wooden_door", "entity.zombie.break_wooden_door",
                "entity.zombie.attack_iron_door");

        register("ZOMBIE_HORSE").setHurt("entity.zombie_horse.hurt").setDeath("entity.zombie_horse.death")
            .setStep("block.grass.step", "entity.horse.step_wood").setIdle("entity.zombie_horse.ambient")
            .setIgnored("entity.horse.gallop", "entity.horse.saddle", "entity.horse.armor", "entity.horse.land", "entity.horse.jump",
                "entity.horse.angry");

        register("ZOMBIE_VILLAGER").setHurt("entity.zombie_villager.hurt").setDeath("entity.zombie_villager.death")
            .setStep("entity.zombie_villager.step").setIdle("entity.zombie_villager.ambient")
            .setIgnored("entity.zombie.infect", "entity.zombie.attack_wooden_door", "entity.zombie.break_wooden_door",
                "entity.zombie.attack_iron_door");

        register("ZOMBIFIED_PIGLIN").setHurt("entity.zombified_piglin.hurt").setDeath("entity.zombified_piglin.death")
            .setIdle("entity.zombified_piglin.ambient").setIgnored("entity.zombified_piglin.angry", "entity.piglin.converted_to_zombified");
    }
}
