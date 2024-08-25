package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParamInfoWrappedBlockData extends ParamInfo {
    @RequiredArgsConstructor
    @Getter
    private static class WrappedData {
        private final Map<StateValue, Object> data;
        private final StateType stateType;

        public StateValue getKey(String key) {
            for (StateValue v : data.keySet()) {
                if (!v.getName().equalsIgnoreCase(key)) {
                    continue;
                }

                return v;
            }

            return null;
        }

        public String getBlockName() {
            return stateType.getName();
        }
    }

    private final List<WrappedData> defaultBlockStates = new ArrayList<>();
    private final Method methodGlobalIdNoCache;
    private final Method cloneBlockstate;

    @SneakyThrows
    public ParamInfoWrappedBlockData(Class paramClass, String name, String description) {
        super(paramClass, name, description);

        fillMap();

        // But libraryaddict, why do you access this? Couldn't you make a PR or something?
        // Because this whole thing is hacky, realistically I should probably be parsing bukkit..
        // But bukkit is a whole new headache with the whole multi-version thing
        // If this breaks in the future, I'll probably have a better idea on how to do things
        methodGlobalIdNoCache = WrappedBlockState.class.getDeclaredMethod("getGlobalIdNoCache");
        methodGlobalIdNoCache.setAccessible(true);
        cloneBlockstate = WrappedBlockState.class.getDeclaredMethod("checkIfCloneNeeded");
        cloneBlockstate.setAccessible(true);
    }

    @SneakyThrows
    private void fillMap() {
        ClientVersion version = PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();

        for (Field field : StateTypes.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()) ||
                field.getType() != StateType.class) {
                continue;
            }

            StateType type = (StateType) field.get(null);

            if (type != StateTypes.AIR && type.getMapped().getId(version) <= 0) {
                continue;
            }

            WrappedBlockState state =
                WrappedBlockState.getDefaultState(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), type, false);

            LinkedHashMap<StateValue, Object> map = new LinkedHashMap<>();

            List<StateValue> list = new ArrayList<>(state.getInternalData().keySet());
            list.sort((v1, v2) -> v1.getName().compareToIgnoreCase(v2.getName()));

            for (StateValue v : list) {
                map.put(v, state.getInternalData().get(v));
            }

            defaultBlockStates.add(new WrappedData(map, type));
        }
    }

    @Override
    public boolean hasTabCompletion() {
        return true;
    }

    @Override
    public Set<String> getEnums(String string) {
        // Refuse to tab complete if the blockdata is supposed to be ended
        if (string.endsWith("]")) {
            return null;
        }

        List<String> parseSplit = asSplit(string.toLowerCase(Locale.ENGLISH), false);
        Set<String> set = new HashSet<>();

        WrappedData bData =
            defaultBlockStates.stream().filter(b -> b.getBlockName().equalsIgnoreCase(parseSplit.get(0))).findAny().orElse(null);

        // If they're beyond the block stage
        if (bData == null && (parseSplit.size() > 2 || (parseSplit.size() == 2 && !parseSplit.get(1).isEmpty()))) {
            return null;
        }

        // If they're still at the block stage
        if (parseSplit.size() == 1 || (parseSplit.size() == 2 && parseSplit.get(1).isEmpty() && !string.contains("["))) {
            for (WrappedData b : defaultBlockStates) {
                if (!b.getBlockName().toLowerCase(Locale.ENGLISH).startsWith(string.toLowerCase(Locale.ENGLISH))) {
                    continue;
                }

                set.add(b.getBlockName() + (b.getBlockName().equalsIgnoreCase(string) ? "[" : ""));
            }

            // No valid block names
            if (set.isEmpty()) {
                return null;
            }

            return set;
        }

        // They're not at block stage, but block is invalid
        if (bData == null) {
            // Invalid
            return null;
        }

        List<String> validData = new ArrayList<>();
        Set<StateValue> handled = new HashSet<>();

        // Validate the args we've filled out
        for (int i = 1; i < parseSplit.size() - 1; i++) {
            // This is ensured key=value
            String[] spl = parseSplit.get(i).split("=", -1);

            StateValue v = bData.getKey(spl[0]);

            if (v == null) {
                return null;
            }

            handled.add(v);
            Object dValue = bData.getData().get(v);

            // Boolean doesn't actually check for a literal match
            if ((dValue instanceof Boolean || Boolean.TYPE.isInstance(dValue)) &&
                !spl[1].toLowerCase(Locale.ENGLISH).matches("true|false")) {
                return null;
            }

            try {
                dValue = v.getParser().apply(spl[1].toUpperCase(Locale.ENGLISH));
            } catch (Exception ex) {
                // If this is an invalid arg and they could be doing a tab complete on it..
                if (i + 2 == parseSplit.size() && parseSplit.get(i + 1).isEmpty() && !string.endsWith(",") && !string.endsWith("]")) {
                    // Remove the last param to make this the last param
                    parseSplit.remove(i + 1);
                    break;
                }

                return null;
            }

            validData.add(v.getName() + "=" + dValue.toString().toLowerCase(Locale.ENGLISH));
        }

        // So we're at the final arg

        String finalArg = parseSplit.get(parseSplit.size() - 1);

        // If it's empty, so the rest is all valid

        // Now, are we filling out a key, or a value?
        if (!finalArg.contains("=")) {
            // If the final arg is empty, we may as well suggest either a closing ] or a comma
            if (!string.endsWith(",") && !string.endsWith("]") && finalArg.isEmpty() && !handled.isEmpty()) {
                if (handled.size() < bData.getData().size()) {
                    set.add(",");
                }

                set.add("]");
            }

            // We're filling out a key
            for (StateValue key : bData.getData().keySet()) {
                if (handled.contains(key) || !key.getName().startsWith(finalArg)) {
                    continue;
                }

                set.add(key.getName() + "=");
            }
        } else {
            String[] spl = finalArg.split("=", -1);

            // More than one =? Invalid
            if (spl.length > 2) {
                return null;
            }

            StateValue key = bData.getKey(spl[0]);

            // Invalid key? Invalid
            if (key == null) {
                return null;
            }

            Object dVal = bData.getData().get(key);

            // Number? We can't autocomplete that
            if (dVal instanceof Number) {
                return null;
            }

            if (dVal instanceof Boolean || Boolean.TYPE.isInstance(dVal)) {
                for (String s : new String[]{"true", "false"}) {
                    if (!s.startsWith(spl[1])) {
                        continue;
                    }

                    set.add(s);
                }
            } else {
                for (Enum e : (Enum[]) dVal.getClass().getEnumConstants()) {
                    if (!e.name().toLowerCase(Locale.ENGLISH).startsWith(spl[1].toLowerCase(Locale.ENGLISH))) {
                        continue;
                    }

                    set.add(e.name().toLowerCase(Locale.ENGLISH));
                }
            }

            // Add the key
            set = set.stream().map(s -> key.getName() + "=" + s).collect(Collectors.toSet());
        }

        // No suggestions, return null
        if (set.isEmpty()) {
            return null;
        }

        String prefix = bData.getBlockName() + "[" + String.join(",", validData);

        if (set.size() == 1) {
            String v = set.iterator().next();

            // If the last arg is unchanged, then they might be wanting to cap off the blockdata
            if (v.equalsIgnoreCase(parseSplit.get(parseSplit.size() - 1))) {
                set = new HashSet<>();
                // Only add a comma if they could do more values
                if (handled.size() < bData.getData().keySet().size()) {
                    set.add(v + ",");
                }

                set.add(v + "]");
            }
        }

        Set<String> toReturn = new HashSet<>();

        for (String s : set) {
            if (!validData.isEmpty() && s.length() > 1) {
                s = "," + s;
            }

            toReturn.add(prefix + s);
        }

        return toReturn;
    }

    @SneakyThrows
    @Override
    public Object fromString(String string) throws DisguiseParseException {
        List<String> parseSplit = asSplit(string, true);

        // If invalid syntax
        if (!parseSplit.get(parseSplit.size() - 1).isEmpty()) {
            throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX, string);
        }

        WrappedData bData =
            defaultBlockStates.stream().filter(b -> b.getBlockName().equalsIgnoreCase(parseSplit.get(0))).findAny().orElse(null);

        if (bData == null) {
            throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK, parseSplit.get(0), string);
        }

        WrappedBlockState blockState =
            WrappedBlockState.getDefaultState(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), bData.getStateType(),
                true);
        cloneBlockstate.invoke(blockState);

        for (int i = 1; i < parseSplit.size() - 1; i++) {
            // This is ensured key=value
            String[] spl = parseSplit.get(i).split("=", -1);

            StateValue v = bData.getKey(spl[0]);

            if (v == null) {
                throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_KEY, spl[0], parseSplit.get(0), string);
            }

            if (spl[1].isEmpty()) {
                throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_VALUE, "{Empty}", spl[0], parseSplit.get(0),
                    string);
            }

            Object dValue = bData.getData().get(v);

            // Boolean doesn't actually check for a literal match
            if ((dValue instanceof Boolean || Boolean.TYPE.isInstance(dValue)) &&
                !spl[1].toLowerCase(Locale.ENGLISH).matches("true|false")) {
                throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_VALUE, spl[1], spl[0], parseSplit.get(0),
                    string);
            }

            try {
                dValue = v.getParser().apply(spl[1].toUpperCase(Locale.ENGLISH));
            } catch (Exception ex) {
                throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_VALUE, spl[1], spl[0], parseSplit.get(0),
                    string);
            }

            blockState.getInternalData().put(v, dValue);
        }

        int globalId = (int) methodGlobalIdNoCache.invoke(blockState);

        if (globalId < 0) {
            throw new DisguiseParseException(LibsMsg.PARSE_BLOCK_STATE_ILLEGAL_BLOCK, string);
        }

        if (blockState.getGlobalId() != globalId) {
            return WrappedBlockState.getByGlobalId(globalId);
        }

        return blockState;
    }

    @Override
    public String toString(Object object) {
        WrappedBlockState state = (WrappedBlockState) object;

        WrappedData defaultState = defaultBlockStates.stream().filter(b -> b.getStateType() == state.getType()).findAny().orElse(null);

        if (defaultState == null) {
            return null;
        }

        List<String> list = new ArrayList<>();

        // Don't bother adding the defaults
        for (Map.Entry<StateValue, Object> entry : defaultState.getData().entrySet()) {
            if (state.getInternalData().get(entry.getKey()) == entry.getValue()) {
                continue;
            }

            list.add(entry.getKey().getName() + "=" + state.getInternalData().get(entry.getKey()).toString().toLowerCase(Locale.ENGLISH));
        }

        String returns = state.getType().getName();

        // Only add blockdata info if there's some
        if (!list.isEmpty()) {
            returns += "[";
            returns += String.join(",", list);
            returns += "]";
        }

        return returns;
    }

    /**
     * Returns an array of strings split into block, data, data, invalidstring
     * dirt will return "dirt", "" which signifies that the syntax is valid
     * dirt:, dirt[, dirt] will return "dirt:", "dirt[", "dirt]"
     * dirt[] will return "dirt", ""
     * dirt[dirty] will return "dirt", "dirty"
     * dirt[dirty=] returns "dirt", "dirty="
     * dirt[dirty=true] returns "dirt", "dirty=true", ""
     * dirt[dirty,cat=true] returns "dirt", "dirty,cat=true" because , is not a valid key
     */
    public List<String> asSplit(String string, boolean demandEnd) {
        Matcher regex = Pattern.compile("^([a-z\\d_]+)([\\[\\]\\da-z=_,]*)$", Pattern.CASE_INSENSITIVE).matcher(string);

        // The first arg is not valid
        if (!regex.find()) {
            return Collections.singletonList(string);
        }

        List<String> args = new ArrayList<>();
        args.add(regex.group(1));
        String secondArg = regex.group(2);

        // Data is empty
        if (secondArg == null || secondArg.isEmpty() || secondArg.equals("[]")) {
            args.add("");

            return args;
        }

        if (!secondArg.startsWith("[")) {
            args.add(secondArg);

            return args;
        }

        // Remove first [
        String[] data = secondArg.substring(1).split(",");

        for (int i = 0; i < data.length; i++) {
            boolean lastArg = data.length - 1 == i;
            Matcher match = Pattern.compile("^([a-z\\d_]+)=([a-z\\d_]+)" + (lastArg ? "\\]" + (demandEnd ? "" : "?") : "") + "$",
                Pattern.CASE_INSENSITIVE).matcher(data[i]);

            if (match.find()) {
                args.add(data[i].replace("]", ""));
                continue;
            }

            // We hit invalid, add remaining data into a single string and return
            args.add(String.join(",", Arrays.copyOfRange(data, i, data.length)));
            return args;
        }

        // All valid
        args.add("");

        return args;
    }
}
