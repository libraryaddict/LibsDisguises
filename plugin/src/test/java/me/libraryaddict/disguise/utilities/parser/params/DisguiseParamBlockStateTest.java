package me.libraryaddict.disguise.utilities.parser.params;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.manager.server.ServerManagerImpl;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.params.types.custom.ParamInfoWrappedBlockData;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.doReturn;

public class DisguiseParamBlockStateTest {
    @BeforeAll
    public static void beforeTests() {
        PacketEvents.setAPI(Mockito.spy(SpigotPacketEventsBuilder.build(null)));

        ServerManager impl = Mockito.spy(new ServerManagerImpl());
        doReturn(impl).when(PacketEvents.getAPI()).getServerManager();
        doReturn(ServerVersion.getLatest()).when(impl).getVersion();
    }

    private void throwsTest(LibsMsg msg, String toParse) {
        try {
            parse(toParse);

            Assertions.fail(
                "Expected WrappedBlockState test to fail when parsing '" + toParse + "', but LibsMsg." + msg.name() + " was not thrown.");
        } catch (DisguiseParseException ex) {
            if (ex.getMsg() == msg) {
                return;
            }

            Assertions.fail("Expected WrappedBlockState test to fail when parsing '" + toParse + "', but wrong LibsMsg was returned. " +
                ex.getMsg().name() + " instead of " + msg.name());
        }
    }

    private WrappedBlockState parse(String string) throws DisguiseParseException {
        return (WrappedBlockState) ((ParamInfoWrappedBlockData) ParamInfoManager.getParamInfo(WrappedBlockState.class)).fromString(string);
    }

    private String parse(WrappedBlockState state) {
        return ParamInfoManager.getParamInfo(WrappedBlockState.class).toString(state);
    }

    @Test
    public void canRefuseToAcceptIllegalStates() {
        this.throwsTest(LibsMsg.PARSE_BLOCK_STATE_ILLEGAL_BLOCK, "acacia_button[facing=down]");
    }

    @SneakyThrows
    @Test
    public void canParseSimples() {
        Assertions.assertEquals(WrappedBlockState.getDefaultState(StateTypes.STONE), parse("Stone"));
        Assertions.assertEquals(WrappedBlockState.getDefaultState(StateTypes.STONE), parse("Stone[]"));
        Assertions.assertEquals(WrappedBlockState.getDefaultState(StateTypes.DIRT), parse("dirt"));
        Assertions.assertEquals(WrappedBlockState.getDefaultState(StateTypes.ACACIA_FENCE_GATE), parse("ACACIA_FENCE_GATE"));
        Assertions.assertEquals("acacia_fence_gate", parse(WrappedBlockState.getDefaultState(StateTypes.ACACIA_FENCE_GATE)));
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK, "bulldozer");
    }

    private String[] getTabs(String request) {
        Set<String> response = ParamInfoManager.getParamInfo(WrappedBlockState.class).getEnums(request);

        if (response == null) {
            return null;
        }

        List<String> list = new ArrayList<>(response);
        list.sort(String.CASE_INSENSITIVE_ORDER);

        return list.toArray(new String[0]);
    }

    @Test
    public void canSuggestTabs() {
        Assertions.assertNull(getTabs("bulldozer"));
        Assertions.assertNull(getTabs("dirt[bulldozer"));
        Assertions.assertNull(getTabs("acacia_fence_gate[opener"));
        Assertions.assertNull(getTabs("acacia_fence_gate[has_record"));
        Assertions.assertNull(getTabs("acacia_fence_gate[open=bulldozer"));
        Assertions.assertNull(getTabs("acacia_fence_gate[open=1"));
        Assertions.assertNull(getTabs("acacia_fence_gate[open=true]"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate"}, getTabs("acacia_fence_ga"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate["}, getTabs("acacia_fence_gate"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=", "acacia_fence_gate[in_wall=", "acacia_fence_gate[open=",
            "acacia_fence_gate[powered="}, getTabs("acacia_fence_gate["));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[open=false", "acacia_fence_gate[open=true"},
            getTabs("acacia_fence_gate[open="));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=north"}, getTabs("acacia_fence_gate[facing=no"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=north,", "acacia_fence_gate[facing=north,in_wall=",
                "acacia_fence_gate[facing=north,open=", "acacia_fence_gate[facing=north,powered=", "acacia_fence_gate[facing=north]"},
            getTabs("acacia_fence_gate[facing=north"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=north,in_wall=", "acacia_fence_gate[facing=north,open=",
            "acacia_fence_gate[facing=north,powered="}, getTabs("acacia_fence_gate[facing=north,"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=north,in_wall=", "acacia_fence_gate[facing=north,open=",
            "acacia_fence_gate[facing=north,powered="}, getTabs("acacia_fence_gate[facing=north,"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=north,open="}, getTabs("acacia_fence_gate[facing=north,o"));
        Assertions.assertArrayEquals(new String[]{"acacia_fence_gate[facing=north,open=false", "acacia_fence_gate[facing=north,open=true"},
            getTabs("acacia_fence_gate[facing=north,open="));
    }

    @SneakyThrows
    @Test
    public void canHandleDatas() {
        WrappedBlockState openGate = WrappedBlockState.getDefaultState(StateTypes.ACACIA_FENCE_GATE).clone();
        openGate.setOpen(true);

        Assertions.assertEquals(openGate, parse("ACACIA_FENCE_GATE[open=true]"));
        // Should default to the last arg
        Assertions.assertEquals(openGate, parse("ACACIA_FENCE_GATE[open=false,open=true]"));
        Assertions.assertEquals("acacia_fence_gate[open=true]", parse(openGate));

        // Add a second arg
        openGate.setFacing(BlockFace.EAST);

        Assertions.assertEquals(openGate, parse("ACACIA_FENCE_GATE[facing=east,open=true]"));
        // Test different order
        Assertions.assertEquals(openGate, parse("ACACIA_FENCE_GATE[open=true,facing=east]"));
        Assertions.assertEquals("acacia_fence_gate[facing=east,open=true]", parse(openGate));

        // Add a third arg
        openGate.setPowered(true);

        Assertions.assertEquals(openGate, parse("ACACIA_FENCE_GATE[facing=east,open=true,powered=true]"));
        // Test different order
        Assertions.assertEquals(openGate, parse("ACACIA_FENCE_GATE[powered=true,open=true,facing=east]"));
        Assertions.assertEquals("acacia_fence_gate[facing=east,open=true,powered=true]", parse(openGate));

        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_KEY, "ACACIA_FENCE_GATE[dancer=true]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_VALUE, "ACACIA_FENCE_GATE[open=sherlock]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true,");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true,]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true,powered]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true,powered=]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_VALUE, "ACACIA_FENCE_GATE[open=true,powered=yes]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true,powered=true");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[open=true,powered,true]");
        throwsTest(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, "ACACIA_FENCE_GATE[,open=true]");
    }
}
