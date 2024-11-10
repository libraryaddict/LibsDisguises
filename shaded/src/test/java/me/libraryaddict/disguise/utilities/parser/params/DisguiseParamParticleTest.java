package me.libraryaddict.disguise.utilities.parser.params;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.color.AlphaColor;
import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleBlockStateData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleColorData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustColorTransitionData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleSculkChargeData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleShriekData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleTrailData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleVibrationData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.manager.server.ServerManagerImpl;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoItemStack;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoParticle;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

public class DisguiseParamParticleTest {
    @BeforeAll
    public static void beforeTests() {
        PacketEvents.setAPI(Mockito.spy(SpigotPacketEventsBuilder.build(null)));

        ServerManager impl = Mockito.spy(new ServerManagerImpl());
        doReturn(impl).when(PacketEvents.getAPI()).getServerManager();
        doReturn(ServerVersion.getLatest()).when(impl).getVersion();
    }

    private void runThrowTest(LibsMsg msg, String toParse) {
        try {
            ParamInfoParticle param = (ParamInfoParticle) ParamInfoManager.getParamInfo(Particle.class);
            param.fromString(toParse);

            Assertions.fail("Failed to throw a DisguiseParseException with LibsMsg." + msg.name() + " and parsed of: " + toParse);
        } catch (DisguiseParseException ex) {
            if (ex.getMsg() == msg) {
                return;
            }

            Assertions.fail("Failed to throw the correct DisguiseParseException with LibsMsg." + msg.name() + ", was instead LibsMsg." +
                ex.getMsg().name() + ". Parsed of: " + toParse);

        }
    }

    @SneakyThrows
    private void runTest(String asString, Particle asParticle) {
        runTest(asString, asString, asParticle);
    }

    @SneakyThrows
    private void runTest(String asString, String asSerializedString, Particle asParticle) {
        ParamInfoParticle param = (ParamInfoParticle) ParamInfoManager.getParamInfo(Particle.class);

        String fromParticle = param.toString(asParticle);
        Particle<ParticleBlockStateData> fromString = (Particle<ParticleBlockStateData>) param.fromString(asString);

        assertEquals(asSerializedString.toLowerCase(Locale.ENGLISH), fromParticle.toLowerCase(Locale.ENGLISH));
        testEquality(asParticle, fromString);
    }

    private void testEquality(Particle expected, Particle parsed) {
        assertEquals(expected.getType(), parsed.getType());
        assertEquals(expected.getData().getClass(), parsed.getData().getClass());

        ParticleData data = expected.getData();

        if (data instanceof ParticleDustData p1) {
            ParticleDustData p2 = (ParticleDustData) parsed.getData();

            assertEquals(p1.getScale(), p2.getScale());
            assertEquals(p1.getRed(), p2.getRed());
            assertEquals(p1.getBlue(), p2.getBlue());
            assertEquals(p1.getGreen(), p2.getGreen());
        } else if (data instanceof ParticleDustColorTransitionData p1) {
            ParticleDustColorTransitionData p2 = (ParticleDustColorTransitionData) parsed.getData();

            assertEquals(p1.getScale(), p2.getScale(), "Particle Scale");
            assertEquals(p1.getStartRed(), p2.getStartRed(), "Particle Start Red");
            assertEquals(p1.getStartGreen(), p2.getStartGreen(), "Particle Start Green");
            assertEquals(p1.getStartBlue(), p2.getStartBlue(), "Particle Start Blue");
            assertEquals(p1.getEndRed(), p2.getEndRed(), "Particle End Red");
            assertEquals(p1.getEndGreen(), p2.getEndGreen(), "Particle End Green");
            assertEquals(p1.getEndBlue(), p2.getEndBlue(), "Particle End Blue");
        } else if (data instanceof ParticleColorData p1) {
            ParticleColorData p2 = (ParticleColorData) parsed.getData();

            assertEquals(p1.getColor(), p2.getColor());
        } else if (data instanceof ParticleVibrationData p1) {
            ParticleVibrationData p2 = (ParticleVibrationData) parsed.getData();

            assertEquals(p1.getTicks(), p2.getTicks());
            assertEquals(p1.getSourceType(), p2.getSourceType());

            Vector3i v1 = p1.getBlockPosition().orElse(null);
            Vector3i v2 = p2.getBlockPosition().orElse(null);

            assertEquals(v1.getX(), v2.getX());
            assertEquals(v1.getY(), v2.getY());
            assertEquals(v1.getZ(), v2.getZ());

            v1 = p1.getStartingPosition();
            v2 = p2.getStartingPosition();

            if (v1 == null) {
                Assertions.assertNull(v2);
            } else {
                Assertions.assertNotNull(v2);
                assertEquals(v1.getX(), v2.getX());
                assertEquals(v1.getY(), v2.getY());
                assertEquals(v1.getZ(), v2.getZ());
            }
        } else if (data instanceof ParticleShriekData p1) {
            ParticleShriekData p2 = (ParticleShriekData) parsed.getData();

            assertEquals(p1.getDelay(), p2.getDelay());
        } else if (data instanceof ParticleSculkChargeData p1) {
            ParticleSculkChargeData p2 = (ParticleSculkChargeData) parsed.getData();

            assertEquals(p1.getRoll(), p2.getRoll());
        } else if (data instanceof ParticleBlockStateData p1) {
            ParticleBlockStateData p2 = (ParticleBlockStateData) parsed.getData();

            assertEquals(p1.getBlockState(), p2.getBlockState());
        } else if (data instanceof ParticleItemStackData p1) {
            ParticleItemStackData p2 = (ParticleItemStackData) parsed.getData();

            assertNotNull(p1.getItemStack());
            assertNotNull(p2.getItemStack());
            assertEquals(p1.getItemStack().getType(), p2.getItemStack().getType());
            assertEquals(p1.getItemStack().getAmount(), p2.getItemStack().getAmount());
            assertEquals(p1.getItemStack().getDamageValue(), p2.getItemStack().getDamageValue());
        } else if (data instanceof ParticleTrailData p1) {
            ParticleTrailData p2 = (ParticleTrailData) parsed.getData();

            assertEquals(p1.getTarget().getX(), p2.getTarget().getX());
            assertEquals(p1.getTarget().getY(), p2.getTarget().getY());
            assertEquals(p1.getTarget().getZ(), p2.getTarget().getZ());

            assertEquals(p1.getColor().red(), p2.getColor().red());
            assertEquals(p1.getColor().green(), p2.getColor().green());
            assertEquals(p1.getColor().blue(), p2.getColor().blue());
        } else {
            throw new IllegalArgumentException("Unrecognized class " + data.getClass() + " when testing particle equality");
        }
    }

    private static ParticleType<ParticleBlockStateData>[] blockStateParticles() {
        return new ParticleType[]{ParticleTypes.BLOCK, ParticleTypes.BLOCK_CRUMBLE, ParticleTypes.BLOCK_MARKER};
    }

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("blockStateParticles")
    public void testParticleBlockStateData(ParticleType<ParticleBlockStateData> particleType) {
        WrappedBlockState openGate = WrappedBlockState.getDefaultState(StateTypes.ACACIA_FENCE_GATE).clone();
        openGate.setOpen(true);
        Particle asParticle = new Particle<>(particleType, new ParticleBlockStateData(openGate));
        String particleName = particleType.getName().getKey().toUpperCase(Locale.ENGLISH);

        runTest(particleName + ":ACACIA_FENCE_GATE[open=true]", particleName + ":ACACIA_FENCE_GATE[open=true]", asParticle);
        runTest(particleName + ":ACACIA_FENCE_GATE[facing=north,in_wall=false,open=true,powered=false]",
            particleName + ":ACACIA_FENCE_GATE[open=true]", asParticle);
        // Default of stone
        runTest(particleName, particleName.toLowerCase(Locale.ENGLISH) + ":stone",
            new Particle<>(particleType, new ParticleBlockStateData(WrappedBlockState.getDefaultState(StateTypes.STONE))));

        // Invalid block
        runThrowTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK, particleName + ":libraryaddict");
        runThrowTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK, particleName + ":1");
        runThrowTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, particleName + ":STONE:STONE");
        runThrowTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, particleName + ":1:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_BLOCK, particleName + ":");
    }

    @Test
    @SneakyThrows
    public void testParticleTrailData() {
        Color red = new Color(org.bukkit.Color.RED.asRGB());

        // We allow no params, no vector, no color
        runTest("TRAIL:0.5,0,0", "trail:0.5,0.0,0.0:95,95,255",
            new Particle<>(ParticleTypes.TRAIL, new ParticleTrailData(new Vector3d(0.5, 0, 0), new Color(95, 95, 255))));
        runTest("TRAIL:12,15,5", "trail:12.0,15.0,5.0:95,95,255",
            new Particle<>(ParticleTypes.TRAIL, new ParticleTrailData(new Vector3d(12, 15, 5), new Color(95, 95, 255))));
        runTest("TRAIL:12,15,5,RED", "trail:12.0,15.0,5.0:red",
            new Particle<>(ParticleTypes.TRAIL, new ParticleTrailData(new Vector3d(12, 15, 5), red)));
        runTest("TRAIL:12,15,5:red", "trail:12.0,15.0,5.0:red",
            new Particle<>(ParticleTypes.TRAIL, new ParticleTrailData(new Vector3d(12, 15, 5), red)));
        runTest("TRAIL:12,15,5:black", "trail:12.0,15.0,5.0:black",
            new Particle<>(ParticleTypes.TRAIL, new ParticleTrailData(new Vector3d(12, 15, 5), new Color(0, 0, 0))));
        runTest("TRAIL:12,15,5:0,0,0", "trail:12.0,15.0,5.0:black",
            new Particle<>(ParticleTypes.TRAIL, new ParticleTrailData(new Vector3d(12, 15, 5), new Color(0, 0, 0))));

        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:RED,RED,RED,RED");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:RED");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:RED:12,15,5");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:RED:12,15,5,5");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:12,15,5,5:RED");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:12,15,5:0,0");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:12,15,5:0,0,0,0");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:12,15,5:0,0,0:RED");
        runThrowTest(LibsMsg.PARSE_PARTICLE_TRAIL, "TRAIL:12,15,5:0,0,0,0,0");
    }

    @Test
    @SneakyThrows
    public void testParticleDustData() {
        org.bukkit.Color bRed = org.bukkit.Color.RED;
        // No args
        runTest("DUST", "dust:black", new Particle<>(ParticleTypes.DUST, new ParticleDustData(1, 0, 0, 0)));
        // With args
        runTest("DUST:black", new Particle<>(ParticleTypes.DUST, new ParticleDustData(1, 0, 0, 0)));
        runTest("DUST:RED", new Particle<>(ParticleTypes.DUST,
            new ParticleDustData(1, bRed.getRed() / 255f, bRed.getGreen() / 255f, bRed.getBlue() / 255f)));
        runTest("DUST:7.5:RED", new Particle<>(ParticleTypes.DUST,
            new ParticleDustData(7.5f, bRed.getRed() / 255f, bRed.getGreen() / 255f, bRed.getBlue() / 255f)));
        // That it can drop the scale
        runTest("DUST:12,32,23", new Particle<>(ParticleTypes.DUST, new ParticleDustData(1, new Color(12, 32, 23))));
        runTest("DUST:7.5:12,32,23", new Particle<>(ParticleTypes.DUST, new ParticleDustData(7.5f, 12, 32, 23)));

        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST, "DUST:RandomString");
        // Not Enough Args
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST, "DUST:0,0");
        // Too many args
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST, "DUST:RED,0");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST, "DUST:0,RED,0,0,0");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST, "DUST:0,RED,RED");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST, "DUST:0,0,0,0,0");
    }

    @Test
    @SneakyThrows
    public void testParticleDustColorTransitionData() {
        Color c1 = new Color(org.bukkit.Color.RED.asRGB());
        Color c2 = new Color(org.bukkit.Color.BLUE.asRGB());

        // Test a simple <Color1> <Color2>
        runTest("DUST_COLOR_TRANSITION:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1, c1, c2)));
        // With a scale
        runTest("DUST_COLOR_TRANSITION:1.6:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1.6f, c1, c2)));
        // Without scale, the first is 3 numbers
        runTest("DUST_COLOR_TRANSITION:255,0,0:BLUE", "DUST_COLOR_TRANSITION:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1, c1, c2)));
        runTest("DUST_COLOR_TRANSITION:1.0,0,0:BLUE", "DUST_COLOR_TRANSITION:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1, c1, c2)));
        // With scale, the first is 3 numbers
        runTest("DUST_COLOR_TRANSITION:1.6:255,0,0:BLUE", "DUST_COLOR_TRANSITION:1.6:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1.6f, c1, c2)));
        runTest("DUST_COLOR_TRANSITION:1.6:1.0,0,0:BLUE", "DUST_COLOR_TRANSITION:1.6:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1.6f, c1, c2)));
        // Without scale, the last is 3 numbers
        runTest("DUST_COLOR_TRANSITION:RED:0,0,255", "DUST_COLOR_TRANSITION:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1, c1, c2)));
        // With scale
        runTest("DUST_COLOR_TRANSITION:1.6:RED:0,0,255", "DUST_COLOR_TRANSITION:1.6:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1.6f, c1, c2)));
        // Without scale, only number colors
        runTest("DUST_COLOR_TRANSITION:255,0,0:0,0,255", "DUST_COLOR_TRANSITION:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1, c1, c2)));
        // With scale
        runTest("DUST_COLOR_TRANSITION:1.6:255,0,0:0,0,255", "DUST_COLOR_TRANSITION:1.6:RED:BLUE",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1.6f, c1, c2)));
        // Negatives
        runTest("DUST_COLOR_TRANSITION:black:black",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1f, 0, 0, 0, 0, 0, 0)));
        runTest("DUST_COLOR_TRANSITION:1.6:black:black",
            new Particle(ParticleTypes.DUST_COLOR_TRANSITION, new ParticleDustColorTransitionData(1.6f, 0, 0, 0, 0, 0, 0)));

        // Invalid args
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:FIRE");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:FIRE:FIRE");
        // Only one arg
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:WHITE");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1:WHITE");
        // Not enough args
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2,3");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2,3,4");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2,3,4,5");
        // Too many args (6 = 2 colors, 7 = 2 colors and size)
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2,3,4,5,6,7,8");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2,3,4,5,6,7,8,9");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1,2,3,4,5,6,7,8,9,10");
        // Wrong format
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:WHITE:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:WHITE:WHITE:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:WHITE:1:WHITE");
        runThrowTest(LibsMsg.PARSE_PARTICLE_DUST_TRANSITION, "DUST_COLOR_TRANSITION:1:2:WHITE:WHITE");
    }

    @Test
    @SneakyThrows
    public void testParticleColorData() {
        org.bukkit.Color bRed = org.bukkit.Color.RED;
        org.bukkit.Color bPurple = org.bukkit.Color.PURPLE;
        // Will default to black
        runTest("entity_effect", "entity_effect:-1", new Particle<>(ParticleTypes.ENTITY_EFFECT, new ParticleColorData(-1)));
        runTest("entity_effect:black", "entity_effect:255:black",
            new Particle<>(ParticleTypes.ENTITY_EFFECT, new ParticleColorData(new AlphaColor(255, 0, 0, 0))));
        runTest("entity_effect:RED", "entity_effect:255:RED", new Particle<>(ParticleTypes.ENTITY_EFFECT,
            new ParticleColorData(new AlphaColor(255, bRed.getRed(), bRed.getGreen(), bRed.getBlue()))));
        runTest("entity_effect:0.5,RED", "entity_effect:127:RED", new Particle<>(ParticleTypes.ENTITY_EFFECT,
            new ParticleColorData(new AlphaColor(127, bRed.getRed(), bRed.getGreen(), bRed.getBlue()))));
        // Test with a color that's not known by name
        runTest("entity_effect:127:239,251,196",
            new Particle<>(ParticleTypes.ENTITY_EFFECT, new ParticleColorData(new AlphaColor(0.5f, 20, 40, 60))));
        runTest("entity_effect:PURPLE", "entity_effect:255:PURPLE", new Particle<>(ParticleTypes.ENTITY_EFFECT,
            new ParticleColorData(new AlphaColor(255, bPurple.getRed(), bPurple.getGreen(), bPurple.getBlue()))));
        runTest("entity_effect:16711620", "entity_effect:0:254,255,196",
            new Particle<>(ParticleTypes.ENTITY_EFFECT, new ParticleColorData(16711620)));

        runThrowTest(LibsMsg.PARSE_PARTICLE_COLOR, "entity_effect:WHITE:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_COLOR, "entity_effect:Cat");
        runThrowTest(LibsMsg.PARSE_PARTICLE_COLOR, "entity_effect:1:1");
    }

    @Test
    @SneakyThrows
    public void testParticleSculkChargeData() {
        runTest("SCULK_CHARGE:5.5", new Particle<>(ParticleTypes.SCULK_CHARGE, new ParticleSculkChargeData(5.5f)));
        runTest("SCULK_CHARGE", "SCULK_CHARGE:60.0", new Particle<>(ParticleTypes.SCULK_CHARGE, new ParticleSculkChargeData(60f)));

        runThrowTest(LibsMsg.PARSE_PARTICLE_SHULK_CHARGE, "SCULK_CHARGE:Cat");
        runThrowTest(LibsMsg.PARSE_PARTICLE_SHULK_CHARGE, "SCULK_CHARGE:1:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_SHULK_CHARGE, "SCULK_CHARGE:-1");
    }

    @Test
    @SneakyThrows
    public void testParticleVibrationData() {
        runTest("VIBRATION:5,4,3:5,6,7:90",
            new Particle<>(ParticleTypes.VIBRATION, new ParticleVibrationData(new Vector3i(5, 6, 7), new Vector3i(5, 4, 3), 90)));
        runTest("VIBRATION:5,-4,3:90",
            new Particle<>(ParticleTypes.VIBRATION, new ParticleVibrationData(null, new Vector3i(5, -4, 3), 90)));

        // Wrong count
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION");
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:-1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:1:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:1:1:1");
        // Invalid char
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:a:1:1:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:1:1:1:a");
        // Wrong count
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:1:1:1:1:1");
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:1:1:1:1:1:1");
        // Invalid char
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:a:1:1:1:1:1:1");
        // Wrong count
        runThrowTest(LibsMsg.PARSE_PARTICLE_VIBRATION, "VIBRATION:1:1:1:1:1:1:1:1");
    }

    @Test
    @SneakyThrows
    public void testParticleShriekData() {
        runTest("SHRIEK:55", new Particle<>(ParticleTypes.SHRIEK, new ParticleShriekData(55)));
        runTest("SHRIEK", "SHRIEK:60", new Particle<>(ParticleTypes.SHRIEK, new ParticleShriekData(60)));

        runThrowTest(LibsMsg.PARSE_PARTICLE_SHRIEK, "SHRIEK:-50");
        runThrowTest(LibsMsg.PARSE_PARTICLE_SHRIEK, "SHRIEK:10,10");
    }

    @Test
    @SneakyThrows
    @Disabled("ItemStack doesn't really work for JUnit")
    public void testParticleItemStack() {
        testParticleItemStack(new org.bukkit.inventory.ItemStack(Material.ENDER_PEARL),
            new ItemStack.Builder().type(ItemTypes.ENDER_PEARL).build(), "item:ender_pearl", "item:ender_pearl");
        testParticleItemStack(new org.bukkit.inventory.ItemStack(Material.STONE), new ItemStack.Builder().type(ItemTypes.STONE).build(),
            "item", "item:stone");

        // No further tests because itemstack needs a running server
    }

    @SneakyThrows
    private void testParticleItemStack(org.bukkit.inventory.ItemStack bPearl, ItemStack pPearl, String asString, String asSerialized) {
        Particle<ParticleItemStackData> expectedParticle = new Particle(ParticleTypes.ITEM, new ParticleItemStackData(pPearl));

        try (MockedStatic<SpigotConversionUtil> mockedStatic = Mockito.mockStatic(SpigotConversionUtil.class);
             MockedStatic<ParamInfoItemStack> mockedParam = Mockito.mockStatic(ParamInfoItemStack.class)) {
            // Mock itemstack conversion
            mockedStatic.when(() -> DisguiseUtilities.fromBukkitItemStack(ArgumentMatchers.any())).thenReturn(pPearl);
            mockedStatic.when(() -> DisguiseUtilities.toBukkitItemStack(ArgumentMatchers.any())).thenReturn(bPearl);

            ParamInfoParticle param = (ParamInfoParticle) ParamInfoManager.getParamInfo(Particle.class);

            runTest(asString, asSerialized, expectedParticle);
        }
    }
}
