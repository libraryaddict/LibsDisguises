package me.libraryaddict.disguise.utilities.parser;

import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

public class DisguisePermissions {
    private static class PermissionStorage {
        private final DisguisePerm disguisePerm;
        private final List<String> permittedOptions = new ArrayList<>();
        private final List<String> negatedOptions = new ArrayList<>();
        private final Set<String> externalNegatedOptions = new HashSet<>();
        private boolean wildcardAllow = false;

        public PermissionStorage(DisguisePerm disguisePerm) {
            this.disguisePerm = disguisePerm;
        }

        public DisguisePerm getDisguise() {
            return this.disguisePerm;
        }
    }

    private static class ParsedPermission {
        private final Vector<DisguisePerm> disguisePerm;
        private final HashMap<String, Boolean> options;
        @Setter
        @Getter
        private boolean negated;
        /**
         * 0 = Names a specific disguise
         * 1 = Animal
         * 2 = Monster
         * 3... = etc
         * 4 = * = Disguise wildcard
         */
        @Getter
        private final byte inheritance;
        @Getter
        private final boolean wildcardCommand;

        public ParsedPermission(DisguisePerm[] disguisePerm, HashMap<String, Boolean> options, byte inheritance, boolean wildcardCommand) {
            this.disguisePerm = new Vector<>(Arrays.asList(disguisePerm));
            this.options = options;
            this.inheritance = inheritance;
            this.wildcardCommand = wildcardCommand;
        }

        public boolean isDisguise(DisguisePerm perm) {
            return disguisePerm.contains(perm);
        }
    }

    @Getter
    static class DisguisePermitted {
        private final boolean strictAllowed;
        private final List<String> optionsAllowed;
        private final List<String> optionsForbidden;

        public DisguisePermitted(List<String> optionsAllowed, List<String> optionsForbidden, boolean strict) {
            this.strictAllowed = strict;
            this.optionsAllowed = optionsAllowed;
            this.optionsForbidden = optionsForbidden;
        }
    }

    /**
     * List of PermissionStorage that the permission holder is able to use
     */
    private final List<PermissionStorage> disguises = new ArrayList<>();
    @Getter
    private final Set<DisguiseType> disabledInConfigDisguises = new HashSet<>();
    @Getter
    private boolean isOperator = false;
    private final static Map<String, DisguisePermissions> CONSOLE_PERMISSIONS = new HashMap<>();
    private static boolean warnedFlyingRemoved = false;

    /**
     * @param permissionHolder The permissions to check
     * @param commandName      A lowercase string consisting of the name of one of Lib's Disguises commands
     */
    public DisguisePermissions(Permissible permissionHolder, String commandName) {
        loadPermissions(permissionHolder, commandName.toLowerCase(Locale.ENGLISH));

        if (permissionHolder == Bukkit.getConsoleSender() && hasPermissions()) {
            CONSOLE_PERMISSIONS.put(commandName, this);
        }
    }

    public static DisguisePermissions getPermissions(Permissible permissionHolder, String commandName) {
        if (permissionHolder == Bukkit.getConsoleSender() && CONSOLE_PERMISSIONS.containsKey(commandName)) {
            return CONSOLE_PERMISSIONS.get(commandName);
        }

        return new DisguisePermissions(permissionHolder, commandName);
    }

    public static void onReload() {
        CONSOLE_PERMISSIONS.clear();
    }

    /**
     * @return If any of the disguises can be used
     */
    public boolean hasPermissions() {
        return !disguises.isEmpty();
    }

    public Collection<DisguisePerm> getAllowed() {
        ArrayList<DisguisePerm> list = new ArrayList<>();

        for (PermissionStorage allowed : disguises) {
            if (list.contains(allowed.getDisguise())) {
                continue;
            }

            list.add(allowed.getDisguise());
        }

        list.sort((perm1, perm2) -> String.CASE_INSENSITIVE_ORDER.compare(perm1.toReadable(), perm2.toReadable()));

        return list;
    }

    private ParsedPermission parsePermission(String permission) {
        // libsdisguises.disguise.sheep
        String[] split = permission.split("\\.");

        String disguiseName = split[2];

        HashMap<String, Boolean> options = getOptions(permission);

        DisguisePerm dPerm = DisguiseParser.getDisguisePerm(disguiseName);

        // If this refers to a specific disguise
        if (dPerm != null) {
            return new ParsedPermission(new DisguisePerm[]{dPerm}, options, (byte) (options.containsKey("*") ? 1 : 0),
                split[1].equals("*"));
        }

        // If the disguise can't be found, it may be refering to a range
        List<DisguisePerm> disguisePerms = new ArrayList<>();
        int inheritance = 0;

        for (DisguisePerm disguisePerm : DisguiseParser.getDisguisePerms()) {
            int inherit = getInheritance(disguisePerm, disguiseName);

            if (inherit < 0) {
                continue;
            }

            inheritance = inherit;

            disguisePerms.add(disguisePerm);
        }

        // If there were no disguises that can be found by that name
        if (disguisePerms.isEmpty()) {
            return null;
        }

        return new ParsedPermission(disguisePerms.toArray(new DisguisePerm[0]), options, (byte) inheritance, split[1].equals("*"));
    }

    /**
     * Calculate permissions.
     * <p>
     * A specified disguise (cow) and disguise range (animal) differs in that
     * A disguise range cannot negate a specific disguise, players will be allowed to use cow if animal is negated
     * <p>
     * Options on a permission limits the player, if the options start with a - then only those options can't be used
     * on a disguise
     * If they're using multiple permissions targetting the same disguise, it attempts to check for a permission that
     * can be used with the provided requirements
     * If a permission is negated, then unless specifically permitted, those permissions can't be used. It obeys the
     * laws of ranges and specific disguises
     */
    private void loadPermissions(Permissible sender, String commandName) {
        String permissionNode = "libsdisguises." + commandName + ".";
        Map<String, Boolean> permissions = new HashMap<>();

        // If the command sender is OP, then this will work even as the below code doesn't
        // libsdisguises.[command].[disguise].[options]
        // They can use all commands, all disguises, all options
        if (sender == Bukkit.getConsoleSender() || sender.hasPermission("libsdisguises.*.*.*") || "%%__USER__%%".equals("12345")) {
            permissions.put("libsdisguises.*.*.*", true);
            isOperator = true;
        } else {
            isOperator = sender == Bukkit.getConsoleSender() || sender.isOp();
        }

        for (PermissionAttachmentInfo permission : sender.getEffectivePermissions()) {
            String perm = permission.getPermission().toLowerCase(Locale.ENGLISH);

            String[] split = perm.split("\\.");

            // If there are not enough arguments
            if (split.length < 3) {
                continue;
            }

            // If this is not a lib's disguises permission
            if (!split[0].equals("libsdisguises")) {
                continue;
            }

            // If the command name does not match
            if (!split[1].equals("*") && !split[1].equals(commandName)) {
                continue;
            }

            // If it's already contained in the map, and is true. Allow negating permissions to continue
            if (permissions.containsKey(perm) && permission.getValue()) {
                continue;
            }

            permissions.put(perm, permission.getValue());
        }

        // First get all the disguises that can be affected
        // Then load all the permissions we can
        // Each time there's a parent permission set, the child inherits unless specified in a child of that parent

        // DisguisePerm[]
        // Option[]
        // Negated

        List<ParsedPermission> list = new ArrayList<>();

        ArrayList<String> valids = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String key = entry.getKey();

            if (key.split("\\.").length > 2 && key.split("\\.")[2].equalsIgnoreCase("valid")) {
                valids.add(key);
                continue;
            }

            ParsedPermission temp = parsePermission(entry.getKey());

            if (temp == null) {
                continue;
            }

            temp.setNegated(!entry.getValue());

            list.add(temp);
        }

        for (String valid : valids) {
            String[] spl = valid.split("\\.");
            String key = spl[1];

            if (!key.equals("*") && !key.equalsIgnoreCase(commandName)) {
                continue;
            }

            if (spl.length > 3 && !spl[3].equals("*")) {
                List<DisguisePerm> validFor =
                    Arrays.stream(DisguiseParser.getDisguisePerms()).filter(perm -> getInheritance(perm, spl[3]) >= 0)
                        .collect(Collectors.toList());

                for (ParsedPermission perms : list) {
                    if (perms.disguisePerm.stream().anyMatch(p -> !validFor.contains(p))) {
                        continue;
                    }

                    HashMap<String, Boolean> options = getOptions(valid.replaceFirst("\\.valid", ""));

                    perms.options.putAll(options);
                }

                if (!validFor.isEmpty()) {
                    continue;
                }
            }

            HashMap<String, Boolean> options = getOptions(valid);

            for (ParsedPermission perms : list) {
                perms.options.putAll(options);
            }
        }

        // Sorted from 5 to 0 where "*" is first and "Cow" is last
        // Negated permissions are last in each inheritance, so false, false, true, true

        list.sort((t1, t2) -> {
            // Wilcard commands have little say, so they go first so they can be negated by following permissions
            if (t1.isWildcardCommand() != t2.isWildcardCommand()) {
                return Boolean.compare(t2.isWildcardCommand(), t1.isWildcardCommand());
            }

            if (t1.getInheritance() == t2.getInheritance()) {
                return Boolean.compare(t1.isNegated(), t2.isNegated());
            }

            return t2.getInheritance() - t1.getInheritance();
        });

        for (DisguisePerm disguisePerm : DisguiseParser.getDisguisePerms()) {
            // Use boolean instead of setting to null, to inherit
            boolean disabled = true;
            PermissionStorage storage = new PermissionStorage(disguisePerm);
            byte lastOptionInheritance = -1;

            for (ParsedPermission parsedPermission : list) {
                // If this parsed permission doesn't handle this disguise type
                if (!parsedPermission.isDisguise(disguisePerm)) {
                    continue;
                }

                // A negated permission with no options, disables the disguise
                if (parsedPermission.isNegated() && parsedPermission.options.isEmpty()) {
                    // Remove disguise
                    disabled = true;
                    continue;
                }

                // The permission is negated, and only has negated options. Should mean something, but to most people
                // it's nonsense and should be ignored.
                if (parsedPermission.isNegated() && !parsedPermission.options.containsValue(true)) {
                    continue;
                }

                if (!parsedPermission.isNegated()) {
                    // Enable the disguise if permission isn't negated
                    // A negated permission cannot enable access
                    if (disabled) {
                        disabled = false;
                    }

                    // If the child disguise does not have any options defined
                    // If the config doesn't require them to be given the permissions explictly
                    // If they already have wildcard (Prevent next if)
                    // Or this parsed permission is at a higher level than the last
                    // That prevents 'cow' overriding 'cow.setBurning'
                    if (parsedPermission.options.isEmpty() && !DisguiseConfig.isExplicitDisguisePermissions() &&
                        (storage.wildcardAllow || lastOptionInheritance != parsedPermission.inheritance)) {
                        storage.wildcardAllow = true;

                        // If this disguise has options defined, unless wildcard was explictly given then remove it
                    } else if (!storage.permittedOptions.contains("*")) {
                        storage.wildcardAllow = false;
                    }
                }

                for (Map.Entry<String, Boolean> entry : parsedPermission.options.entrySet()) {
                    // If permission is negated, reverse the option from 'allowed' to 'denied' or vice versa
                    boolean allowUse = parsedPermission.isNegated() != entry.getValue();

                    storage.permittedOptions.remove(entry.getKey());
                    storage.negatedOptions.remove(entry.getKey());

                    // Handle wildcard options
                    if (entry.getKey().equals("*")) {
                        // If it's a negated wildcard, then they don't want the user to use anything
                        // If it's a permitted wildcard, then they want the user to use everything

                        // Both want to clear the existing restrictions
                        storage.permittedOptions.clear();
                        storage.negatedOptions.clear();

                        // Add wildcard allow so if the user later defines "setBaby" just to be sure, it doesn't
                        // limit them to setbaby
                        storage.wildcardAllow = allowUse;

                        // Negated wants to prevent the use of all options
                        if (!allowUse) {
                            storage.permittedOptions.add("nooptions");
                        }
                    }

                    if (allowUse) {
                        storage.permittedOptions.add(entry.getKey());
                    } else {
                        storage.negatedOptions.add(entry.getKey());
                    }
                }

                if (!parsedPermission.options.isEmpty()) {
                    lastOptionInheritance = parsedPermission.inheritance;
                }
            }

            // Disguise is not allowed, continue
            if (disabled) {
                continue;
            }

            // If invisibility was disabled in the config, ignore permissions and make sure it's disabled
            if (DisguiseConfig.isDisabledInvisibility()) {
                storage.externalNegatedOptions.add("setinvisible");
            }

            if (sender instanceof Player && !isOperator()) {
                storage.externalNegatedOptions.addAll(DisguiseConfig.getDisabledMethods());
            }

            storage.permittedOptions.removeAll(storage.externalNegatedOptions);

            disguises.add(storage);
        }

        if (sender.isOp() || DisguiseConfig.getDisabledDisguises().isEmpty()) {
            return;
        }

        disguises.removeIf(storage -> {
            if (!DisguiseConfig.getDisabledDisguises().contains(storage.getDisguise().getType())) {
                return false;
            }

            disabledInConfigDisguises.add(storage.getDisguise().getType());
            return true;
        });
    }

    private int getInheritance(DisguisePerm disguisePerm, String permissionName) {
        DisguiseType disguiseType = disguisePerm.getType();

        if (permissionName.equals("ageable")) {
            if (Ageable.class.isAssignableFrom(disguiseType.getEntityClass())) {
                return 2;
            }
        } else if (permissionName.equals("monster") || permissionName.equals("monsters")) {
            if (Monster.class.isAssignableFrom(disguiseType.getEntityClass())) {
                return 3;
            }
        } else if (permissionName.equals("animal") || permissionName.equals("animals")) {
            if (Animals.class.isAssignableFrom(disguiseType.getEntityClass())) {
                return 3;
            }
        } else if (permissionName.equals("flying")) {
            if (!warnedFlyingRemoved && LibsDisguises.getInstance() != null) {
                warnedFlyingRemoved = true;
                LibsDisguises.getInstance().getLogger()
                    .warning("The permission node 'flying' for Lib's Disguises permissions, will be removed in a future update.");
            }

            if (Flying.class.isAssignableFrom(disguiseType.getEntityClass())) {
                return 3;
            }
        } else if (permissionName.equals("mob")) {
            if (disguiseType.isMob()) {
                return 4;
            }
        } else if (permissionName.equals("misc")) {
            if (disguiseType.isMisc()) {
                return 4;
            }
        } else if (permissionName.equals("custom")) {
            if (disguisePerm.isCustomDisguise()) {
                return 4;
            }
        } else if (permissionName.equals("vanilla")) {
            if (!disguisePerm.isCustomDisguise()) {
                return 5;
            }
        } else if (permissionName.equals("*")) {
            return 6;
        }

        return -1;
    }

    private HashMap<String, Boolean> getOptions(String perm) {
        HashMap<String, Boolean> options = new HashMap<>();
        String[] split = perm.split("\\.");

        for (int i = 3; i < split.length; i++) {
            String option = split[i];
            boolean negated = option.startsWith("-");

            if (negated) {
                option = option.substring(1);
            }

            if (option.equals("baby")) {
                option = "setbaby";
            }

            options.put(option, !negated);
        }

        return options;
    }

    /**
     * If this DisguisePermission can use the provided disguise and options
     *
     * @param disguisePerm
     * @param disguiseOptions
     * @return true if permitted
     */
    public boolean isAllowedDisguise(DisguisePerm disguisePerm, Collection<String> disguiseOptions) {
        return getReasonNotAllowed(disguisePerm, disguiseOptions) == null;
    }

    /**
     * Returns a method that was disabled in config if it was the reason why isAllowedDisguise was false
     */
    public DisguiseParseException getReasonNotAllowed(DisguisePerm disguisePerm, Collection<String> disguiseOptions) {
        PermissionStorage storage = getStorage(disguisePerm);

        if (storage == null) {
            return new DisguiseParseException(LibsMsg.NO_PERM_DISGUISE);
        }

        // If user cannot use all permitted options naturally
        if (!storage.wildcardAllow) {
            // If their permitted options are defined, or the denied options are not defined
            // If they don't have permitted options defined, but they have denied options defined then they probably
            // have an invisible wildcard allow
            if (!storage.permittedOptions.isEmpty() || storage.negatedOptions.isEmpty()) {
                // Check if they're trying to use anything they shouldn't
                for (String option : disguiseOptions) {
                    String lower = option.toLowerCase(Locale.ENGLISH);

                    if (storage.permittedOptions.contains(lower)) {
                        continue;
                    }

                    // Not allowed this method
                    return new DisguiseParseException(LibsMsg.D_PARSE_NOPERM, option);
                }
            }
        }

        // If the user is using a forbidden option, return false. Otherwise true
        for (String option : disguiseOptions) {
            String lower = option.toLowerCase(Locale.ENGLISH);

            // It was because of this
            if (storage.negatedOptions.contains(lower)) {
                return new DisguiseParseException(LibsMsg.D_PARSE_NOPERM, option);
            }

            // It is allowed, so it wasn't this
            if (!storage.externalNegatedOptions.contains(lower)) {
                continue;
            }

            // This method was disabled
            return new DisguiseParseException(LibsMsg.DISABLED_CONFIG_METHOD, option);
        }

        return null;
    }

    public boolean isAllowedDisguise(DisguisePerm disguisePerm) {
        return getStorage(disguisePerm) != null;
    }

    private PermissionStorage getStorage(DisguisePerm disguisePerm) {
        return disguises.stream().filter(disguise -> disguise.getDisguise().equals(disguisePerm)).findAny().orElse(null);
    }

    public static HashMap<String, HashMap<String, Boolean>> getDisguiseOptions(Permissible permissionsHolder, String permNode,
                                                                               DisguisePerm type) {
        HashMap<String, HashMap<String, Boolean>> returns = new HashMap<>();

        // libsdisguises.options.<command>.<disguise>.<method>.<options>
        for (PermissionAttachmentInfo permission : permissionsHolder.getEffectivePermissions()) {
            String lowerPerm = permission.getPermission().toLowerCase(Locale.ENGLISH);

            if (!lowerPerm.startsWith("libsdisguises.options.")) {
                continue;
            }

            // Ensure that . can be escaped
            String[] split = lowerPerm.split("(?<=(?<!\\\\)(?:\\\\{2}){0,1000})\\.");

            // 0             1       2         3          4        5+
            // libsdisguises.options.<command>.<disguise>.<method>.<options>
            if (split.length < 4) {
                continue;
            }

            if (!split[2].equalsIgnoreCase(permNode) && !split[2].equalsIgnoreCase("*")) {
                continue;
            }

            boolean applicable = false;

            for (String s : split[3].split("/")) {
                if (!s.equals("*") && !s.replaceAll("[ _]+", "").equalsIgnoreCase(type.toReadable().replaceAll("[ _]+", ""))) {
                    continue;
                }

                applicable = true;
                break;
            }

            if (!applicable) {
                continue;
            }

            HashMap<String, Boolean> options = new HashMap<>();

            for (int i = 5; i < split.length; i++) {
                options.put(split[i].replace("\\.", "."), permission.getValue());
            }

            for (String s : split[4].split("/")) {
                if (returns.containsKey(s)) {
                    returns.get(s).putAll(options);
                } else {
                    returns.put(s, options);
                }
            }
        }

        return returns;
    }

    /**
     * Returns true if the string is found in the map, or it's not a whitelisted setup
     * <p>
     * Returns if command user can access the disguise creation permission type
     */
    public static boolean hasMethodOption(HashMap<String, Boolean> methodValues, String value) {
        value = value.toLowerCase(Locale.ENGLISH);

        // If they were explictly defined, can just return the value
        if (methodValues.containsKey(value)) {
            return methodValues.get(value);
        }

        // If there is at least one whitelisted value, then they needed the whitelist to use it
        return !methodValues.containsValue(true);
    }

    /**
     * Returns true if the string is found in the map, or it's not a whitelisted setup
     * <p>
     * Returns if command user can access the disguise creation permission type
     */
    public static boolean hasPermissionOption(HashMap<String, HashMap<String, Boolean>> disguiseOptions, String method, String value) {
        method = method.toLowerCase(Locale.ENGLISH);

        // If no permissions were defined, return true
        if (!disguiseOptions.containsKey(method)) {
            return true;
        }

        return hasMethodOption(disguiseOptions.get(method), value);
    }
}
