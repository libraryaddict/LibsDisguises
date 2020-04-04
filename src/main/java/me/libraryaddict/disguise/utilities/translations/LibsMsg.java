package me.libraryaddict.disguise.utilities.translations;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

/**
 * Created by libraryaddict on 15/06/2017.
 */
public enum LibsMsg {
    BLOWN_DISGUISE(ChatColor.RED + "Your disguise was blown!"),
    EXPIRED_DISGUISE(ChatColor.RED + "Your disguise has expired!"),
    CAN_USE_DISGS(ChatColor.DARK_GREEN + "You can use the disguises: %s"),
    CANNOT_FIND_PLAYER(ChatColor.RED + "Cannot find the player/uuid '%s'"),
    CANNOT_FIND_PLAYER_NAME(ChatColor.RED + "Cannot find the player '%s'"),
    CANNOT_FIND_PLAYER_UUID(ChatColor.RED + "Cannot find the uuid '%s'"),
    CLICK_TIMER(ChatColor.RED + "Right click a entity in the next %s seconds to grab the disguise reference!"),
    CLONE_HELP1(ChatColor.DARK_GREEN +
            "Right click a entity to get a disguise reference you can pass to other disguise commands!"),
    CLONE_HELP2(ChatColor.DARK_GREEN +
            "Security note: Any references you create will be available to all players able to use disguise " +
            "references."),
    CLONE_HELP3(ChatColor.DARK_GREEN + "/disguiseclone IgnoreEquipment" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN +
            "Optional" + ChatColor.DARK_GREEN + ")"),
    CUSTOM_DISGUISE_SAVED(ChatColor.GOLD + "Custom disguise has been saved as '%s'!"),
    D_HELP1(ChatColor.DARK_GREEN + "Disguise another player!"),
    D_HELP3(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> player <Name>"),
    D_HELP4(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> <DisguiseType> <Baby>"),
    D_HELP5(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> <Dropped_Item/Falling_Block> <Id> <Durability>"),
    D_PARSE_NOPERM(ChatColor.RED + "You do not have permission to use the method %s"),
    DHELP_CANTFIND(ChatColor.RED + "Cannot find the disguise %s"),
    DHELP_HELP1(ChatColor.RED + "/disguisehelp <DisguiseType> " + ChatColor.GREEN +
            "- View the methods you can set on a disguise. Add 'show' to reveal the methods you don't have permission" +
            " to use"),
    DHELP_HELP2(ChatColor.RED + "/disguisehelp <DisguiseOption> " + ChatColor.GREEN + "- View information about the " +
            "disguise values such as 'RabbitType'"),
    DHELP_HELP3(ChatColor.RED + "/disguisehelp " + ChatColor.DARK_GREEN + "%s" + ChatColor.GREEN + " - %s"),
    DHELP_HELP4(ChatColor.RED + "%s: " + ChatColor.GREEN + "%s"),
    DHELP_HELP4_SEPERATOR(ChatColor.RED + ", " + ChatColor.GREEN),
    DHELP_HELP5(ChatColor.RED + "%s: " + ChatColor.GREEN + "%s"),
    DHELP_HELP6(ChatColor.RED + "%s: " + ChatColor.DARK_GREEN + "%s " + ChatColor.GREEN + "%s"),
    DHELP_OPTIONS("%s options: %s"),
    DISABLED_LIVING_TO_MISC(
            ChatColor.RED + "Can't disguise a living entity as a misc disguise. This has been disabled in the config!"),
    DISG_ENT_CLICK(ChatColor.RED + "Right click an entity in the next %s seconds to disguise it as a %s!"),
    DISG_ENT_HELP1(ChatColor.DARK_GREEN + "Choose a disguise then right click an entity to disguise it!"),
    DISG_ENT_HELP3(ChatColor.DARK_GREEN + "/disguiseentity player <Name>"),
    DISG_ENT_HELP4(ChatColor.DARK_GREEN + "/disguiseentity <DisguiseType> <Baby>"),
    DISG_ENT_HELP5(ChatColor.DARK_GREEN + "/disguiseentity <Dropped_Item/Falling_Block> <Id> <Durability>"),
    DISG_HELP1(ChatColor.DARK_GREEN + "Choose a disguise to become the disguise!"),
    DISG_HELP2(ChatColor.DARK_GREEN + "/disguise player <Name>"),
    DISG_HELP3(ChatColor.DARK_GREEN + "/disguise <DisguiseType> <Baby>"),
    DISG_HELP4(ChatColor.DARK_GREEN + "/disguise <Dropped_Item/Falling_Block> <Id> <Durability>"),
    DISG_PLAYER_AS_DISG(ChatColor.RED + "Successfully disguised %s as a %s!"),
    DISG_PLAYER_AS_DISG_FAIL(ChatColor.RED + "Failed to disguise %s as a %s!"),
    DISGUISED(ChatColor.RED + "Now disguised as a %s"),
    DISRADIUS(ChatColor.RED + "Successfully disguised %s entities!"),
    DISRADIUS_FAIL(ChatColor.RED + "Couldn't find any entities to disguise!"),
    DMODENT_HELP1(ChatColor.DARK_GREEN + "Choose the options for a disguise then right click a entity to modify it!"),
    DMODIFY_HELP1(ChatColor.DARK_GREEN + "Modify your own disguise as you wear it!"),
    DMODIFY_HELP2(ChatColor.DARK_GREEN + "/disguisemodify setBaby true setSprinting true"),
    DMODIFY_HELP3(ChatColor.DARK_GREEN + "You can modify the disguises: %s"),
    DMODIFY_MODIFIED(ChatColor.RED + "Your disguise has been modified!"),
    DMODIFY_NO_PERM(ChatColor.RED + "No permission to modify your disguise!"),
    DMODIFYENT_CLICK(ChatColor.RED + "Right click a disguised entity in the next %s seconds to modify their disguise!"),
    DISGUISECOPY_INTERACT(ChatColor.RED + "Right click a disguised entity in the next %s seconds to copy their disguise!"),
    DMODPLAYER_HELP1(ChatColor.DARK_GREEN + "Modify the disguise of another player!"),
    DMODPLAYER_MODIFIED(ChatColor.RED + "Modified the disguise of %s!"),
    DMODPLAYER_NODISGUISE(ChatColor.RED + "The player '%s' is not disguised"),
    DMODPLAYER_NOPERM(ChatColor.RED + "You do not have permission to modify this disguise"),
    DMODRADIUS(ChatColor.RED + "Successfully modified the disguises of %s entities!"),
    DMODRADIUS_HELP1(ChatColor.DARK_GREEN + "Modify the disguises in a radius! Caps at %s blocks!"),
    DHELP_SHOW("Show"),
    DHELP_NO_OPTIONS(ChatColor.RED + "No options with permission to use"),
    DCLONE_EQUIP("ignoreEquip"),
    DCLONE_SNEAKSPRINT("doSneakSprint"),
    DCLONE_SNEAK("doSneak"),
    DCLONE_SPRINT("doSprint"),
    DMODRADIUS_HELP2((ChatColor.DARK_GREEN + "/disguisemodifyradius <DisguiseType" + ChatColor.DARK_GREEN + "(" +
            ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")> <Radius> <Disguise Methods>")
            .replace("<", "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">")),
    DMODRADIUS_HELP3(ChatColor.DARK_GREEN + "See the DisguiseType's usable by " + ChatColor.GREEN +
            "/disguisemodifyradius DisguiseType"),
    DMODRADIUS_NEEDOPTIONS(ChatColor.RED + "You need to supply the disguise methods as well as the radius"),
    DMODRADIUS_NEEDOPTIONS_ENTITY(
            ChatColor.RED + "You need to supply the disguise methods as well as the radius and EntityType"),
    DMODRADIUS_NOENTS(ChatColor.RED + "Couldn't find any disguised entities!"),
    DMODRADIUS_NOPERM(ChatColor.RED + "No permission to modify %s disguises!"),
    DMODRADIUS_UNRECOGNIZED(ChatColor.RED + "Unrecognised DisguiseType %s"),
    DMODRADIUS_USABLE(ChatColor.DARK_GREEN + "DisguiseTypes usable are: %s" + ChatColor.DARK_GREEN + "."),
    DPLAYER_SUPPLY(ChatColor.RED + "You need to supply a disguise as well as the player/uuid"),
    DRADIUS_ENTITIES(ChatColor.DARK_GREEN + "EntityTypes usable are: %s"),
    DRADIUS_HELP1(ChatColor.DARK_GREEN + "Disguise all entities in a radius! Caps at %s blocks!"),
    DRADIUS_HELP3((ChatColor.DARK_GREEN + "/disguiseradius <EntityType" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN +
            "Optional" + ChatColor.DARK_GREEN + ")> <Radius> player <Name>").replace("<", "<" + ChatColor.GREEN)
            .replace(">", ChatColor.DARK_GREEN + ">")),
    DRADIUS_HELP4((ChatColor.DARK_GREEN + "/disguiseradius <EntityType" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN +
            "Optional" + ChatColor.DARK_GREEN + ")> <Radius> <DisguiseType> <Baby" + ChatColor.DARK_GREEN + "(" +
            ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")>").replace("<", "<" + ChatColor.GREEN)
            .replace(">", ChatColor.DARK_GREEN + ">")),
    DRADIUS_HELP5((ChatColor.DARK_GREEN + "/disguiseradius <EntityType" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN +
            "Optional" + ChatColor.DARK_GREEN + ")> <Radius> <Dropped_Item/Falling_Block> <Id> <Durability" +
            ChatColor.DARK_GREEN + "(" + ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")>")
            .replace("<", "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">")),
    DRADIUS_HELP6(
            ChatColor.DARK_GREEN + "See the EntityType's usable by " + ChatColor.GREEN + "/disguiseradius EntityTypes"),
    DRADIUS_MISCDISG(ChatColor.RED +
            "Failed to disguise %s entities because the option to disguise a living entity as a non-living has been " +
            "disabled in the config"),
    DRADIUS_NEEDOPTIONS(ChatColor.RED + "You need to supply a disguise as well as the radius"),
    DRADIUS_NEEDOPTIONS_ENTITY(ChatColor.RED + "You need to supply a disguise as well as the radius and EntityType"),
    FAILED_DISGIUSE(ChatColor.RED + "Failed to disguise as a %s"),
    GRABBED_SKIN(ChatColor.GOLD + "Grabbed skin and saved as %s!"),
    PLEASE_WAIT(ChatColor.GRAY + "Please wait..."),
    INVALID_CLONE(ChatColor.DARK_RED + "Unknown method '%s' - Valid methods are 'IgnoreEquipment' 'DoSneakSprint' " +
            "'DoSneak' 'DoSprint'"),
    LIBS_COMMAND_WRONG_ARG(
            ChatColor.RED + "[LibsDisguises] Did you mean 'reload', 'scoreboard', 'permtest', 'json' or 'metainfo'?"),
    LIMITED_RADIUS(ChatColor.RED + "Limited radius to %s! Don't want to make too much lag right?"),
    LISTEN_ENTITY_ENTITY_DISG_ENTITY(ChatColor.RED + "Disguised %s as a %s!"),
    LISTEN_ENTITY_ENTITY_DISG_ENTITY_FAIL(ChatColor.RED + "Failed to disguise %s as a %s!"),
    LISTEN_ENTITY_ENTITY_DISG_PLAYER(ChatColor.RED + "Disguised %s as the player %s!"),
    LISTEN_ENTITY_ENTITY_DISG_PLAYER_FAIL(ChatColor.RED + "Failed to disguise %s as the player %s!"),
    LISTEN_ENTITY_PLAYER_DISG_ENTITY(ChatColor.RED + "Disguised the player %s as a %s!"),
    LISTEN_ENTITY_PLAYER_DISG_ENTITY_FAIL(ChatColor.RED + "Failed to disguise the player %s as a %s!"),
    LISTEN_ENTITY_PLAYER_DISG_PLAYER(ChatColor.RED + "Disguised the player %s as the player %s!"),
    LISTEN_ENTITY_PLAYER_DISG_PLAYER_FAIL(ChatColor.RED + "Failed to disguise the player %s as the player %s!"),
    LISTEN_UNDISG_ENT(ChatColor.RED + "Undisguised the %s"),
    LISTEN_UNDISG_ENT_FAIL(ChatColor.RED + "%s isn't disguised!"),
    LISTEN_UNDISG_PLAYER(ChatColor.RED + "Undisguised %s"),
    LISTEN_UNDISG_PLAYER_FAIL(ChatColor.RED + "The %s isn't disguised!"),
    LISTENER_MODIFIED_DISG(ChatColor.RED + "Modified the disguise!"),
    MADE_REF(ChatColor.RED + "Constructed a %s disguise! Your reference is %s"),
    MADE_REF_EXAMPLE(ChatColor.RED + "Example usage: /disguise %s"),
    NO_CONSOLE(ChatColor.RED + "You may not use this command from the console!"),
    NO_PERM(ChatColor.RED + "You are forbidden to use this command."),
    NO_PERM_DISGUISE(ChatColor.RED + "You do not have permission for that disguise!"),
    NO_PERMS_USE_OPTIONS(ChatColor.RED +
            "Ignored %s methods you do not have permission to use. Add 'show' to view unusable methods."),
    OWNED_BY(ChatColor.GOLD + "Plugin registered to '%%__USER__%%'!"),
    NOT_DISGUISED(ChatColor.RED + "You are not disguised!"),
    TARGET_NOT_DISGUISED(ChatColor.RED + "That entity is not disguised!"),
    NOT_NUMBER(ChatColor.RED + "Error! %s is not a number"),
    PARSE_CANT_DISG_UNKNOWN(ChatColor.RED + "Error! You cannot disguise as " + ChatColor.GREEN + "Unknown!"),
    PARSE_CANT_LOAD(ChatColor.RED + "Error! This disguise couldn't be loaded!"),
    PARSE_DISG_NO_EXIST(
            ChatColor.RED + "Error! The disguise " + ChatColor.GREEN + "%s" + ChatColor.RED + " doesn't exist!"),
    PARSE_EXPECTED_RECEIVED(
            ChatColor.RED + "Expected " + ChatColor.GREEN + "%s" + ChatColor.RED + ", received " + ChatColor.GREEN +
                    "%s" + ChatColor.RED + " instead for " + ChatColor.GREEN + "%s"),
    PARSE_PARTICLE_BLOCK(ChatColor.RED + "Expected " + ChatColor.GREEN + "%s:Material" + ChatColor.RED + ", received " +
            ChatColor.GREEN + "%s" + ChatColor.RED + " instead"),
    PARSE_PARTICLE_ITEM(ChatColor.RED + "Expected " + ChatColor.GREEN + "%s:Material,Amount?,Glow?" + ChatColor.RED +
            ", received " + ChatColor.GREEN + "%s" + ChatColor.RED + " instead"),
    PARSE_PARTICLE_REDSTONE(
            ChatColor.RED + "Expected " + ChatColor.GREEN + "%s:Color,Size.0?" + ChatColor.RED + ", received " +
                    ChatColor.GREEN + "%s" + ChatColor.RED + " instead"),
    PARSE_NO_ARGS("No arguments defined"),
    PARSE_NO_OPTION_VALUE(ChatColor.RED + "No value was given for the method %s"),
    PARSE_NO_PERM_NAME(ChatColor.RED + "Error! You don't have permission to use that name!"),
    PARSE_NO_PERM_PARAM(
            ChatColor.RED + "Error! You do not have permission to use the parameter %s on the %s disguise!"),
    PARSE_NO_PERM_REF(ChatColor.RED + "You do not have permission to use disguise references!"),
    PARSE_NO_REF(ChatColor.RED + "Cannot find a disguise under the reference %s"),
    PARSE_OPTION_NA(ChatColor.RED + "Cannot find the method '%s'"),
    PARSE_SUPPLY_PLAYER(ChatColor.RED + "Error! You need to give a player name!"),
    PARSE_TOO_MANY_ARGS(ChatColor.RED + "Error! %s doesn't know what to do with %s!"),
    PARSE_INVALID_TIME(ChatColor.RED + "Error! %s is not a valid time! Use s,m,h,d or secs,mins,hours,days"),
    PARSE_INVALID_TIME_SEQUENCE(ChatColor.RED + "Error! %s is not a valid time! Do amount then time, eg. 4min10sec"),
    PARSE_USE_SECOND_NUM(ChatColor.RED + "Error! Only the disguises %s and %s uses a second number!"),
    REF_TOO_MANY(ChatColor.RED +
            "Failed to store the reference, too many cloned disguises. Please raise the maximum cloned disguises, or " +
            "lower the time they last"),
    RELOADED_CONFIG(ChatColor.GREEN + "[LibsDisguises] Reloaded config."),
    UND_ENTITY(ChatColor.RED + "Right click a disguised entity to undisguise them!"),
    UNDISG(ChatColor.RED + "You are no longer disguised"),
    UNDISG_PLAYER(ChatColor.RED + "%s is no longer disguised"),
    UNDISG_PLAYER_FAIL(ChatColor.RED + "%s not disguised!"),
    UNDISG_PLAYER_HELP(ChatColor.RED + "/undisguiseplayer <Name>"),
    UNDISRADIUS(ChatColor.RED + "Successfully undisguised %s entities!"),
    UPDATE_READY(ChatColor.RED + "[LibsDisguises] " + ChatColor.DARK_RED +
            "There is a update ready to be downloaded! You are using " + ChatColor.RED + "v%s" + ChatColor.DARK_RED +
            ", the new version is " + ChatColor.RED + "v%s" + ChatColor.DARK_RED + "!"),
    UPDATE_READY_SNAPSHOT(ChatColor.RED + "[LibsDisguises] " + ChatColor.DARK_RED +
            "There is a new build of Lib's Disguises! You are using " + ChatColor.RED + "#%s" + ChatColor.DARK_RED +
            ", the latest build is " + ChatColor.RED + "#%s" + ChatColor.DARK_RED + "!" + ChatColor.RED +
            "\nhttps://ci.md-5.net/job/LibsDisguises/lastSuccessfulBuild/"),
    VIEW_SELF_ON(ChatColor.GREEN + "Toggled viewing own disguise on!"),
    VIEW_SELF_OFF(ChatColor.GREEN + "Toggled viewing own disguise off!"),
    CLICK_TO_COPY(ChatColor.GREEN + "Click to Copy:"),
    SKIN_DATA(ChatColor.GREEN + "Skin Data: " + ChatColor.YELLOW + "%s"),
    CLICK_TO_COPY_DATA(ChatColor.YELLOW + "Data"),
    CLICK_TO_COPY_WITH_SKIN(ChatColor.GREEN + "Version with skin data:"),
    CLICK_TO_COPY_WITH_SKIN_NO_COPY(ChatColor.GREEN + "Version with skin data: " + ChatColor.YELLOW + "%s"),
    COPY_DISGUISE_NO_COPY(ChatColor.GREEN + "Data: " + ChatColor.YELLOW + "%s"),
    CLICK_TO_COPY_HOVER(ChatColor.GOLD + "Click to Copy"),
    CLICK_COPY(ChatColor.YELLOW + "" + ChatColor.BOLD + "%s"),
    SKIN_API_IN_USE(ChatColor.RED + "mineskin.org is currently in use, please try again"),
    SKIN_API_TIMER(ChatColor.RED + "mineskin.org can be used again in %s seconds"),
    SKIN_API_FAIL(ChatColor.RED + "Unexpected error while accessing mineskin.org, please try again"),
    SKIN_API_BAD_URL(ChatColor.RED + "Invalid url provided! Please ensure it is a .png file download!"),
    SKIN_API_FAILED_URL(ChatColor.RED + "Invalid url provided! mineskin.org failed to grab it!"),
    SKIN_API_FAIL_CODE(ChatColor.RED + "Error %s! %s"),
    SKIN_API_403("mineskin.org denied access to that url"),
    SKIN_API_404("mineskin.org unable to find an image at that url"),
    SKIN_API_IMAGE_TIMEOUT(ChatColor.RED + "Error! mineskin.org took too long to connect! Is your image valid?"),
    SKIN_API_TIMEOUT_ERROR(ChatColor.RED + "Error! Took too long to connect to mineskin.org!"),
    SKIN_API_TIMEOUT(ChatColor.RED + "Took too long to connect to mineskin.org!"),
    SKIN_API_IMAGE_HAS_ERROR("Your image has the error: %s"),
    SKIN_API_USING_URL(ChatColor.GRAY + "Url provided, now attempting to connect to mineskin.org"),
    SKIN_API_BAD_FILE_NAME(ChatColor.RED + "Invalid file name provided! File not found!"),
    SKIN_API_BAD_FILE(ChatColor.RED + "Invalid file provided! Please ensure it is a valid .png skin!"),
    SKIN_API_USING_FILE(ChatColor.GRAY + "File provided and found, now attempting to upload to mineskin.org"),
    SKIN_API_INVALID_NAME(ChatColor.RED + "Invalid name/file/uuid provided!"),
    SKIN_API_USING_UUID(ChatColor.GRAY + "UUID successfully parsed, now attempting to connect to mineskin.org"),
    SKIN_API_USING_NAME(
            ChatColor.GRAY + "Determined to be player name, now attempting to validate and connect to mineskin.org"),
    SAVE_DISG_HELP_1(ChatColor.GREEN + "The <DisguiseName> is what the disguise will be called in Lib's Disguises"),
    SAVE_DISG_HELP_2(ChatColor.GREEN +
            "/savedisguise <DisguiseName> - If you don't provide arguments, it'll try make a disguise from your" +
            " current disguise. This will not work if you are not disguised!"),
    SAVE_DISG_HELP_3(ChatColor.GREEN + "/savedisguise <DisguiseName> <Arguments>"),
    SAVE_DISG_HELP_4(ChatColor.GREEN +
            "Your arguments need to be as if you're using /disguise. So '/disguise player Notch setsneaking' - " +
            "Means '/savedisguise Notch player Notch setsneaking'"),
    SAVE_DISG_HELP_5(ChatColor.GREEN + "Remember! You can upload your own skins, then reference those skins!"),
    GRAB_DISG_HELP_1(ChatColor.GREEN +
            "You can choose a name to save the skins under, the names will be usable as if it was an actual player " +
            "skin"),
    GRAB_DISG_HELP_2(ChatColor.DARK_GREEN + "/grabskin https://somesite.com/myskin.png <Optional Name>"),
    GRAB_DISG_HELP_3(ChatColor.DARK_GREEN + "/grabskin myskin.png <Optional Name> - Skins must be in the folder!"),
    GRAB_DISG_HELP_4(ChatColor.DARK_GREEN + "/grabskin <Player name or UUID> <Optional Name>"),
    GRAB_DISG_HELP_5(
            ChatColor.GREEN + "You will be sent the skin data, but you can also use the saved names in disguises"),
    CUSTOM_DISGUISE_NAME_CONFLICT(
            ChatColor.RED + "Cannot create the custom disguise '%s' as there is a name conflict!"),
    ERROR_LOADING_CUSTOM_DISGUISE(ChatColor.RED + "Error while loading custom disguise '%s'%s"),
    SKIN_API_INTERNAL_ERROR(ChatColor.RED + "Internal error in the skin API, perhaps bad data?"),
    META_INFO(ChatColor.GREEN + "Name: %s, Watcher: %s, Index: %s, Type: %s, Default: %s"),
    META_NOT_FOUND(ChatColor.RED + "No meta exists under that name!"),
    META_VALUES(ChatColor.BLUE + "Metas: " + ChatColor.DARK_AQUA),
    META_VALUES_NO_CLICK(ChatColor.BLUE + "Metas, use as param for more info: " + ChatColor.DARK_AQUA),
    META_VALUE_SEPERATOR(ChatColor.AQUA + ", " + ChatColor.DARK_AQUA),
    META_CLICK_SHOW(ChatColor.GOLD + "Click to show %s"),
    LIBS_PERM_CHECK_INFO_1(ChatColor.AQUA + "Now checking for the permission 'libsdisguises.disguise.pig'"),
    LIBS_PERM_CHECK_INFO_2(ChatColor.AQUA +
            "If you did not give this permission, please set it. You can supply a player name to test another player."),
    NORMAL_PERM_CHECK_SUCCESS(ChatColor.GOLD + "Normal permission check, success."),
    NORMAL_PERM_CHECK_FAIL(ChatColor.RED + "Normal permission check, fail."),
    LIBS_PERM_CHECK_SUCCESS(
            ChatColor.GOLD + "Lib's Disguises permission check, success. Pig disguise should be usable!"),
    LIBS_PERM_CHECK_FAIL(
            ChatColor.GOLD + "Lib's Disguises permission check, fail. Your permission plugin isn't compliant!"),
    CANT_ATTACK_DISGUISED(ChatColor.RED + "Cannot fight while disguised!"),
    CANT_ATTACK_DISGUISED_RECENTLY(ChatColor.RED + "You were disguised recently! Can't attack yet!"),
    SWITCH_WORLD_DISGUISE_REMOVED(ChatColor.RED + "Disguise removed as you've switched worlds!"),
    ACTION_BAR_MESSAGE(ChatColor.GOLD + "Currently disguised as %s"),
    ITEM_SERIALIZED(ChatColor.GOLD + "Json Serialized, click to copy: "),
    ITEM_SERIALIZED_MC(ChatColor.GOLD + "MC Serialized, click to copy: "),
    ITEM_SERIALIZED_MC_LD(ChatColor.GOLD + "MC Serialized for LD, click to copy: "),
    ITEM_SIMPLE_STRING(ChatColor.GOLD + "Simple, click to copy: "),
    ITEM_SERIALIZED_NO_COPY(ChatColor.GOLD + "Json Serialized: " + ChatColor.YELLOW + "%s"),
    ITEM_SERIALIZED_MC_NO_COPY(ChatColor.GOLD + "MC Serialized: " + ChatColor.YELLOW + "%s"),
    ITEM_SERIALIZED_MC_LD_NO_COPY(ChatColor.GOLD + "MC Serialized for LD: " + ChatColor.YELLOW + "%s"),
    ITEM_SIMPLE_STRING_NO_COPY(ChatColor.GOLD + "Simple: " + ChatColor.YELLOW + "%s"),
    LIBS_SCOREBOARD_NO_TEAM(ChatColor.RED + "Not on a scoreboard team!"),
    LIBS_SCOREBOARD_NO_TEAM_PUSH(ChatColor.RED + "On scoreboard team '%s' and pushing is enabled!"),
    LIBS_SCOREBOARD_SUCCESS(ChatColor.GOLD +
            "On scoreboard team '%s' with pushing disabled! If you're still having issues and you are disguised right" +
            " now, then " +
            "you have a plugin modifying scoreboard through packets. Example of this is a plugin that modifies your " +
            "name above head, or the tablist. Check their configs for pushing disabling options"),
    LIBS_SCOREBOARD_DISABLED(
            "The scoreboard modification has been disabled in config, will continue the debug incase this is intended" +
                    "."),
    USING_DEFAULT_CONFIG(ChatColor.DARK_GREEN + "Using the default config!");

    private String string;

    LibsMsg(String string) {
        this.string = string;
    }

    public String getRaw() {
        return string;
    }

    public String get(Object... strings) {
        if (StringUtils.countMatches(getRaw(), "%s") != strings.length) {
            DisguiseUtilities.getLogger().severe("Mismatch in messages, incorrect parameters supplied for " + name() +
                    ". Please inform plugin author.");
        }

        if (strings.length == 0) {
            return TranslateType.MESSAGES.get(getRaw());
        }

        return String.format(TranslateType.MESSAGES.get(getRaw()), strings);
    }

    public String toString() {
        throw new RuntimeException("Dont call this");
    }
}
