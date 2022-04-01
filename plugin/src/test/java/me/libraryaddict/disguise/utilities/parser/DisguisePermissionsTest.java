package me.libraryaddict.disguise.utilities.parser;

import lombok.SneakyThrows;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by libraryaddict on 21/10/2018.
 */
public class DisguisePermissionsTest {
    @Test
    public void testDisguisesExist() {
        Assert.assertNull("There should not be a reindeer disguise", DisguiseParser.getDisguisePerm("Reindeer"));

        Assert.assertNotNull("There should be a cow disguise", DisguiseParser.getDisguisePerm("Cow"));

        Assert.assertNotNull("There should be a firework disguise", DisguiseParser.getDisguisePerm("Firework"));
    }

    @Test
    public void testPermissionNames() {
        Assert.assertFalse("There should not be permissions", createPermissions("Disguise", false).hasPermissions());

        Assert.assertFalse("The commands should not match", createPermissions("Disguise", false, "libsdisguises.disguiseentity.cow").hasPermissions());

        Assert.assertFalse("The commands should not match", createPermissions("Disguised", false, "libsdisguises.disguise.cow").hasPermissions());

        Assert.assertTrue("There should be permissions", createPermissions("Disguise", false, "libsdisguises.*.animal").hasPermissions());
    }

    @Test
    public void testOperatorPermissions() {
        DisguisePermissions permissions = createPermissions("Disguise", true, "-libsdisguises.disguise.sheep", "-libsdisguises.disguise.horse.setBaby");

        Assert.assertTrue("There should be permissions", permissions.hasPermissions());

        Assert.assertTrue("The disguise cow should be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")));

        Assert.assertFalse("The disguise sheep should not be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep")));

        Assert.assertTrue("The disguise horse should be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse")));

        Assert.assertFalse("The disguise horse should not be allowed with setBaby",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse"), Collections.singletonList("setBaby")));
    }

    @Test
    public void testWildcardsPermissions() {
        Assert.assertTrue("The cow disguise should be allowed",
            createPermissions("Disguise", false, "libsdisguises.*.animal").isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")));

        Assert.assertFalse("The firework disguise should not be allowed",
            createPermissions("Disguise", false, "libsdisguises.*.animal").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")));

        Assert.assertTrue("The firework disguise should be allowed",
            createPermissions("Disguise", false, "libsdisguises.*.*").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")));

        Assert.assertTrue("The firework disguise should be allowed",
            createPermissions("Disguise", false, "libsdisguises.disguise.*").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")));

        Assert.assertTrue("The firework disguise should be allowed",
            createPermissions("Disguise", false, "libsdisguises.*.Firework").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")));

        Assert.assertFalse("The firework disguise should not be allowed",
            createPermissions("Disguise", false, "libsdisguises.*.*", "-libsdisguises.*.misc").isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")));

        Assert.assertTrue("The firework disguise should be allowed",
            createPermissions("Disguise", false, "libsdisguises.disguise.*", "-libsdisguises.*.*").isAllowedDisguise(
                DisguiseParser.getDisguisePerm("Firework")));

        Assert.assertTrue("The firework disguise should be allowed",
            createPermissions("Disguise", false, "libsdisguises.disguise.firework", "-libsdisguises.disguise.misc").isAllowedDisguise(
                DisguiseParser.getDisguisePerm("Firework")));
    }

    @Test
    public void testInheritedPermissions() {
        testInheritedPermissions(createPermissions("Disguise", false, "libsdisguises.disguise.animal.setBaby", "-libsdisguises.disguise.sheep.setBaby"));

        testInheritedPermissions(createPermissions("Disguise", false, "libsdisguises.disguise.animal.setBaby", "libsdisguises.disguise.sheep.-setBaby"));
    }

    private void testInheritedPermissions(DisguisePermissions permissions) {
        Assert.assertTrue("The sheep disguise should be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep")));

        Assert.assertTrue("The cow disguise should be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")));

        Assert.assertTrue("The cow disguise should be allowed with setBaby",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBaby")));

        Assert.assertFalse("The sheep disguise should not be allowed with setBaby",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep"), Collections.singletonList("setBaby")));

        Assert.assertFalse("The firework disguise should not be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Firework")));
    }

    @Test
    public void testNegatedPermissions() {
        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.sheep", "-libsdisguises.disguise.cow.setSprinting", "-libsdisguises.disguise.donkey",
                "-libsdisguises.disguise.horse.setRearing", "libsdisguises.disguise.horse");

        Assert.assertFalse("The cow disguise should not be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")));

        Assert.assertTrue("The sheep disguise should be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Sheep")));

        Assert.assertFalse("The donkey disguise should not be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Donkey")));

        Assert.assertTrue("The horse disguise should be allowed", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse")));

        Assert.assertTrue("The horse disguise should be allowed with options",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse"), Collections.singletonList("setBaby")));

        Assert.assertFalse("The horse disguise should not be allowed setRearing",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Horse"), Collections.singletonList("setRearing")));
    }

    @Test
    public void testMultiDisguises() {
        DisguisePermissions permissions = createPermissions("Disguise", false, "libsdisguises.disguise.cow.setBaby", "libsdisguises.disguise.cow.setHealth",
            "libsdisguises.disguise.cow.-setBurning");

        Assert.assertTrue("The cow disguise should be able to use setBaby",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBaby")));

        Assert.assertTrue("The cow disguise should be able to use setHealth",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setHealth")));

        Assert.assertTrue("The cow disguise should be able to use setBaby and setHealth",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Arrays.asList("setBaby", "setHealth")));

        Assert.assertFalse("The cow disguise should not be able to use setBurning",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBurning")));

        Assert.assertFalse("The cow disguise should not be able to use setSprinting",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setSprinting")));

        Assert.assertFalse("The cow disguise should not be able to use setSprinting with setBaby",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Arrays.asList("setSprinting", "setBaby")));
    }

    @Test
    public void testOptions() {
        Assert.assertFalse("The disguise should not be valid",
            createPermissions("Disguise", false, "libsdisguises.disguise.cow", "-libsdisguises.disguise.cow").hasPermissions());

        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.cow", "libsdisguises.disguise.sheep.setColor.setSprinting",
                "libsdisguises.disguise.animal.-setSprinting", "libsdisguises.disguise.sheep.setcolor.blue");

        Assert.assertTrue("There should be a valid disguise", permissions.hasPermissions());

        DisguisePerm cow = DisguiseParser.getDisguisePerm("Cow");

        Assert.assertTrue("The cow disguise should be allowed", permissions.isAllowedDisguise(cow));

        Assert.assertTrue("The cow disguise should be allowed with options", permissions.isAllowedDisguise(cow, Arrays.asList("setBaby", "setBurning")));

        Assert.assertFalse("The cow disguise should not be allowed with options setSprinting",
            permissions.isAllowedDisguise(cow, Arrays.asList("setBaby", "setSprinting")));

        Assert.assertFalse("The cow disguise should not be allowed with options",
            permissions.isAllowedDisguise(cow, Collections.singletonList("setSprinting")));

        DisguisePerm sheep = DisguiseParser.getDisguisePerm("Sheep");

        Assert.assertFalse("The sheep disguise should not be allowed with options",
            permissions.isAllowedDisguise(sheep, Arrays.asList("setBaby", "setBurning")));

        Assert.assertTrue("The sheep disguise should be allowed setColor", permissions.isAllowedDisguise(sheep, Collections.singletonList("setColor")));

        Assert.assertTrue("The sheep disguise should be allowed setSprinting", permissions.isAllowedDisguise(sheep, Collections.singletonList("setSprinting")));

        Assert.assertFalse("The sheep disguise should not be allowed setColor and setBaby",
            permissions.isAllowedDisguise(sheep, Arrays.asList("setColor", "setBaby")));

        DisguisePerm firework = DisguiseParser.getDisguisePerm("Firework");

        Assert.assertFalse("The firework disguise should not be allowed", permissions.isAllowedDisguise(firework));

        Assert.assertFalse("The disguise should not be allowed even with options",
            permissions.isAllowedDisguise(firework, Arrays.asList("setBaby", "setBurning")));

    }

    @Test
    public void testDisguiseParameters() {
        HashMap<String, HashMap<String, Boolean>> disguiseOptions =
            DisguisePermissions.getDisguiseOptions(createPermissionsHolder(false, "libsdisguises.options.disguise.falling_block.setblock.stone"), "Disguise",
                new DisguisePerm(DisguiseType.FALLING_BLOCK));

        Assert.assertTrue("They should be allowed to use true as a disguise option on setBurning",
            DisguisePermissions.hasPermissionOption(disguiseOptions, "setBurning", "true"));

        Assert.assertTrue("They should be allowed to use Material.STONE as a disguise option",
            DisguisePermissions.hasPermissionOption(disguiseOptions, "setBlock", "STONE"));

        Assert.assertFalse("They should be not allowed to use Material.DIRT as a disguise option",
            DisguisePermissions.hasPermissionOption(disguiseOptions, "setBlock", "DIRT"));
    }

    @SneakyThrows
    @Test
    public void testCustomDisguisePermissions() {
        DisguiseConfig.getCustomDisguises().put(new DisguisePerm(DisguiseType.BEE, "babybee"), "bee setbaby");

        DisguisePermissions permissions = createPermissions("disguise", false, "libsdisguises.disguise.bee.-*", "libsdisguises.disguise.babybee.nooptions");

        Assert.assertNotNull("The custom disguise babybee should exist", DisguiseParser.getDisguisePerm("babybee"));

        Assert.assertTrue("They should be allowed to disguise as a bee", permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.BEE)));

        Assert.assertFalse("They should not be allowed to disguise as a bee and call setbaby",
            permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.BEE), Collections.singletonList("setbaby")));

        Assert.assertFalse("They should not be allowed to disguise as a burning bee",
            permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.BEE), Collections.singletonList("setburning")));

        Assert.assertFalse("They should not be allowed to disguise as a slime", permissions.isAllowedDisguise(new DisguisePerm(DisguiseType.SLIME)));

        Assert.assertTrue("They should be allowed to disguise as babybee", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("babybee")));

        Assert.assertFalse("They should not be allowed to disguise as babybee and use setbaby",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("babybee"), Collections.singletonList("setbaby")));

        DisguiseAPI.removeCustomDisguise("babybee");
    }

    @Test
    public void testExplictPermissions() {
        DisguiseConfig.setExplicitDisguisePermissions(true);

        DisguisePermissions permissions =
            createPermissions("Disguise", false, "libsdisguises.disguise.animal", "libsdisguises.disguise.zombie", "libsdisguises.disguise.skeleton.*",
                "libsdisguises.disguise.wither.setburning", "libsdisguises.disguise.silverfish.-setburning");

        Assert.assertTrue("The cow disguise should be usable", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow")));

        Assert.assertFalse("The cow disguise should not be able to use setBurning",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Cow"), Collections.singletonList("setBurning")));

        Assert.assertTrue("The zombie disguise should be usable", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Zombie")));

        Assert.assertFalse("The zombie disguise should not be able to use setBurning",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Zombie"), Collections.singletonList("setBurning")));

        Assert.assertTrue("The skeleton disguise should be usable", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Skeleton")));

        Assert.assertTrue("The skeleton disguise should be able to use setBurning",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Skeleton"), Collections.singletonList("setBurning")));

        Assert.assertTrue("The wither disguise should be usable", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Wither")));

        Assert.assertTrue("The wither disguise should be able to use setBurning",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Wither"), Collections.singletonList("setBurning")));

        Assert.assertFalse("The wither disguise should not be able to use setSprinting",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Wither"), Collections.singletonList("setSprinting")));

        Assert.assertTrue("The silverfish disguise should be usable", permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Silverfish")));

        Assert.assertFalse("The silverfish disguise should not be able to use setBurning",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Silverfish"), Collections.singletonList("setBurning")));

        Assert.assertTrue("The silverfish disguise should be able to use setSprinting",
            permissions.isAllowedDisguise(DisguiseParser.getDisguisePerm("Silverfish"), Collections.singletonList("setSprinting")));

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
