package me.libraryaddict.disguise.utilities.parser;

import lombok.SneakyThrows;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DisguisePermissionsTest {
    @BeforeAll
    public static void beforeAll() {
        Mockito.mockStatic(Bukkit.class).when(Bukkit::getConsoleSender).thenReturn(null);
    }

    @Test
    public void testDisguisesExist() {
        assertNull(DisguiseParser.getDisguisePerm("Reindeer"), "There should not be a reindeer disguise");

        assertNotNull(DisguiseParser.getDisguisePerm("Cow"), "There should be a cow disguise");

        assertNotNull(DisguiseParser.getDisguisePerm("Firework"), "There should be a firework disguise");
    }

    @Test
    public void testPermissionNames() {
        assertFalse(createPermissions("Disguise", false).hasPermissions(), "There should not be permissions");

        assertFalse(createPermissions("Disguise", false, "libsdisguises.disguiseentity.cow").hasPermissions(),
            "The commands should not match");

        assertFalse(createPermissions("Disguised", false, "libsdisguises.disguise.cow").hasPermissions(), "The commands should not match");

        assertTrue(createPermissions("Disguise", false, "libsdisguises.*.animal").hasPermissions(), "There should be permissions");
    }

    @Test
    public void testOperatorPermissions() {
        DisguisePermissions permissions =
            createPermissions("Disguise", true, "-libsdisguises.disguise.sheep", "-libsdisguises.disguise.horse.setBaby");

        assertTrue(permissions.hasPermissions(), "There should be permissions");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")), "The disguise cow should be allowed");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep")), "The disguise sheep should not be allowed");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse")), "The disguise horse should be allowed");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse"), Collections.singletonList("setBaby")),
            "The disguise horse should not be allowed with setBaby");
    }

    @Test
    public void testWildcardsPermissions() {
        assertTrue(createPermissions("Disguise", false, "libsdisguises.*.animal").isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")),
            "The cow disguise should be allowed");

        assertFalse(
            createPermissions("Disguise", false, "libsdisguises.*.animal").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")),
            "The firework disguise should not be allowed");

        assertTrue(createPermissions("Disguise", false, "libsdisguises.*.*").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")),
            "The firework disguise should be allowed");

        assertTrue(
            createPermissions("Disguise", false, "libsdisguises.disguise.*").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")),
            "The firework disguise should be allowed");

        assertTrue(
            createPermissions("Disguise", false, "libsdisguises.*.Firework").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")),
            "The firework disguise should be allowed");

        assertFalse(createPermissions("Disguise", false, "libsdisguises.*.*", "-libsdisguises.*.misc").isAllowedDisguise(
            DisguiseParser.getDisguisePerm("Firework")), "The firework disguise should not be allowed");

        assertTrue(createPermissions("Disguise", false, "libsdisguises.disguise.*", "-libsdisguises.*.*").isAllowedDisguise(
            DisguiseParser.getDisguisePerm("Firework")), "The firework disguise should be allowed");

        assertTrue(
            createPermissions("Disguise", false, "libsdisguises.disguise.firework", "-libsdisguises.disguise.misc").isAllowedDisguise(
                DisguiseParser.getDisguisePerm("Firework")), "The firework disguise should be allowed");
    }

    @Test
    public void testInheritedPermissions() {
        testInheritedPermissions(
            createPermissions("Disguise", false, "libsdisguises.disguise.animal.setBaby", "-libsdisguises.disguise.sheep.setBaby"));

        testInheritedPermissions(
            createPermissions("Disguise", false, "libsdisguises.disguise.animal.setBaby", "libsdisguises.disguise.sheep.-setBaby"));
    }

    private void testInheritedPermissions(DisguisePermissions permissions) {
        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep")), "The sheep disguise should be allowed");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")), "The cow disguise should be allowed");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBaby")),
            "The cow disguise should be allowed with setBaby");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep"), Collections.singletonList("setBaby")),
            "The sheep disguise should not be allowed with setBaby");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")),
            "The firework disguise should not be allowed");
    }

    @Test
    public void testNegatedPermissions() {
        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.sheep", "-libsdisguises.disguise.cow.setSprinting",
                "-libsdisguises.disguise.donkey", "-libsdisguises.disguise.horse.setRearing", "libsdisguises.disguise.horse");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")), "The cow disguise should not be allowed");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep")), "The sheep disguise should be allowed");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Donkey")), "The donkey disguise should not be allowed");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse")), "The horse disguise should be allowed");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse"), Collections.singletonList("setBaby")),
            "The horse disguise should be allowed with options");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse"), Collections.singletonList("setRearing")),
            "The horse disguise should not be allowed setRearing");
    }

    @Test
    public void testMultiDisguises() {
        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.cow.setBaby", "libsdisguises.disguise.cow.setHealth",
                "libsdisguises.disguise.cow.-setBurning");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBaby")),
            "The cow disguise should be able to use setBaby");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setHealth")),
            "The cow disguise should be able to use setHealth");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Arrays.asList("setBaby", "setHealth")),
            "The cow disguise should be able to use setBaby and setHealth");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBurning")),
            "The cow disguise should not be able to use setBurning");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setSprinting")),
            "The cow disguise should not be able to use setSprinting");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Arrays.asList("setSprinting", "setBaby")),
            "The cow disguise should not be able to use setSprinting with setBaby");
    }

    @Test
    public void testOptions() {
        assertFalse(createPermissions("Disguise", false, "libsdisguises.disguise.cow", "-libsdisguises.disguise.cow").hasPermissions(),
            "The disguise should not be valid");

        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.cow", "libsdisguises.disguise.sheep.setColor.setSprinting",
                "libsdisguises.disguise.animal.-setSprinting", "libsdisguises.disguise.sheep.setcolor.blue");

        assertTrue(permissions.hasPermissions(), "There should be a valid disguise");

        DisguisePerm cow = DisguiseParser.getDisguisePerm("Cow");

        assertTrue(permissions.isAllowedDisguise(cow), "The cow disguise should be allowed");

        assertTrue(permissions.isAllowedDisguise(cow, Arrays.asList("setBaby", "setBurning")),
            "The cow disguise should be allowed with options");

        assertFalse(permissions.isAllowedDisguise(cow, Arrays.asList("setBaby", "setSprinting")),
            "The cow disguise should not be allowed with options setSprinting");

        assertFalse(permissions.isAllowedDisguise(cow, Collections.singletonList("setSprinting")),
            "The cow disguise should not be allowed with options");

        DisguisePerm sheep = DisguiseParser.getDisguisePerm("Sheep");

        assertFalse(permissions.isAllowedDisguise(sheep, Arrays.asList("setBaby", "setBurning")),
            "The sheep disguise should not be allowed with options");

        assertTrue(permissions.isAllowedDisguise(sheep, Collections.singletonList("setColor")),
            "The sheep disguise should be allowed setColor");

        assertTrue(permissions.isAllowedDisguise(sheep, Collections.singletonList("setSprinting")),
            "The sheep disguise should be allowed setSprinting");

        assertFalse(permissions.isAllowedDisguise(sheep, Arrays.asList("setColor", "setBaby")),
            "The sheep disguise should not be allowed setColor and setBaby");

        DisguisePerm firework = DisguiseParser.getDisguisePerm("Firework");

        assertFalse(permissions.isAllowedDisguise(firework), "The firework disguise should not be allowed");

        assertFalse(permissions.isAllowedDisguise(firework, Arrays.asList("setBaby", "setBurning")),
            "The disguise should not be allowed even with options");

    }

    @Test
    public void testDisguiseParameters() {
        HashMap<String, HashMap<String, Boolean>> disguiseOptions = DisguisePermissions.getDisguiseOptions(
            createPermissionsHolder(false, "libsdisguises.options.disguise.falling_block.setblock.stone"), "Disguise",
            new DisguisePerm(DisguiseType.FALLING_BLOCK));

        assertTrue(DisguisePermissions.hasPermissionOption(disguiseOptions, "setBurning", "true"),
            "They should be allowed to use true as a disguise option on setBurning");

        assertTrue(DisguisePermissions.hasPermissionOption(disguiseOptions, "setBlock", "STONE"),
            "They should be allowed to use Material.STONE as a disguise option");

        assertFalse(DisguisePermissions.hasPermissionOption(disguiseOptions, "setBlock", "DIRT"),
            "They should be not allowed to use Material.DIRT as a disguise option");
    }

    @Test
    public void testDisguiseValidWorksToGiveMore() {
        DisguisePermissions permissions = createPermissions("Disguise", false, "libsdisguises.disguise.falling_block.setCustomName",
            "libsdisguises.disguise.valid.falling_block.setblock");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.FALLING_BLOCK), Arrays.asList("setBurning")),
            "The falling block disguise should not allow setBurning");

        assertTrue(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.FALLING_BLOCK), Arrays.asList("setcustomname")),
            "The falling block disguise should allow setCustomName");

        assertTrue(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.FALLING_BLOCK), Arrays.asList("setBlock")),
            "The falling block disguise should allow setBlock");
    }

    @Test
    public void testDisguiseValidDoesntGiveExtra() {
        DisguisePermissions permissions = createPermissions("Disguise", false, "libsdisguises.disguise.valid.falling_block.setblock");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.FALLING_BLOCK)),
            "The falling block disguise should not be allowed");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.FALLING_BLOCK), Arrays.asList("setBurning")),
            "The falling block disguise should not allow setBurning");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.FALLING_BLOCK), Arrays.asList("setBlock")),
            "The falling block disguise should not allow setBlock");
    }

    @SneakyThrows
    @Test
    public void testCustomDisguisePermissions() {
        DisguiseConfig.getCustomDisguises().put(new DisguisePerm(DisguiseType.BEE, "babybee"), "bee setbaby");

        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.bee.-*", "libsdisguises.disguise.babybee.nooptions");

        assertNotNull(DisguiseParser.getDisguisePerm("babybee"), "The custom disguise babybee should exist");

        assertTrue(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.BEE)), "They should be allowed to disguise as a bee");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.BEE), Collections.singletonList("setbaby")),
            "They should not be allowed to disguise as a bee and call setbaby");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.BEE), Collections.singletonList("setburning")),
            "They should not be allowed to disguise as a burning bee");

        assertFalse(permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.SLIME)),
            "They should not be allowed to disguise as a slime");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("babybee")),
            "They should be allowed to disguise as babybee");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("babybee"), Collections.singletonList("setbaby")),
            "They should not be allowed to disguise as babybee and use setbaby");

        DisguiseAPI.removeCustomDisguise("babybee");
    }

    @Test
    public void testExplictPermissions() {
        DisguiseConfig.setExplicitDisguisePermissions(true);

        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.animal", "libsdisguises.disguise.zombie",
                "libsdisguises.disguise.skeleton.*", "libsdisguises.disguise.wither.setburning",
                "libsdisguises.disguise.silverfish.-setburning");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")), "The cow disguise should be usable");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBurning")),
            "The cow disguise should not be able to use setBurning");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Zombie")), "The zombie disguise should be usable");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Zombie"), Collections.singletonList("setBurning")),
            "The zombie disguise should not be able to use setBurning");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Skeleton")), "The skeleton disguise should be usable");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Skeleton"), Collections.singletonList("setBurning")),
            "The skeleton disguise should be able to use setBurning");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Wither")), "The wither disguise should be usable");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Wither"), Collections.singletonList("setBurning")),
            "The wither disguise should be able to use setBurning");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Wither"), Collections.singletonList("setSprinting")),
            "The wither disguise should not be able to use setSprinting");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Silverfish")), "The silverfish disguise should be usable");

        assertFalse(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Silverfish"), Collections.singletonList("setBurning")),
            "The silverfish disguise should not be able to use setBurning");

        assertTrue(permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Silverfish"), Collections.singletonList("setSprinting")),
            "The silverfish disguise should be able to use setSprinting");

        DisguiseConfig.setExplicitDisguisePermissions(false);
    }

    private Permissible createPermissionsHolder(boolean isOp, String... perms) {
        List<String> permitted = new ArrayList<>();
        List<String> negated = new ArrayList<>();
        Set<PermissionAttachmentInfo> attachments = new HashSet<>();

        Permissible permissible = new Permissible() {
            @Override
            public boolean isPermissionSet(String s) {
                return permitted.contains(s) || negated.contains(s);
            }

            @Override
            public boolean isPermissionSet(Permission permission) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public boolean hasPermission(String s) {
                return permitted.contains(s) || (isOp() && !negated.contains(s));
            }

            @Override
            public boolean hasPermission(Permission permission) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int i) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public void removeAttachment(PermissionAttachment permissionAttachment) {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public void recalculatePermissions() {
                throw new UnsupportedOperationException("Not Supported");
            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return attachments;
            }

            @Override
            public boolean isOp() {
                return isOp;
            }

            @Override
            public void setOp(boolean b) {
                throw new UnsupportedOperationException("Not Supported");
            }
        };

        // If permission starts with a - then it was negated
        Arrays.stream(perms).forEach(perm -> {
            boolean setTrue = !perm.startsWith("-");

            if (setTrue) {
                permitted.add(perm);
            } else {
                negated.add(perm = perm.substring(1));
            }

            attachments.add(new PermissionAttachmentInfo(permissible, perm, null, setTrue));
        });

        return permissible;
    }

    private DisguisePermissions createPermissions(String commandName, boolean isOp, String... perms) {
        return new DisguisePermissions(createPermissionsHolder(isOp, perms), commandName);
    }
}
