package me.libraryaddict.disguise.utilities.translations;

import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

/**
 * Created by libraryaddict on 15/06/2017.
 */
public enum LibsMsg {
    NO_DISGUISES_IN_USE("<red>There are no disguises in use!"),
    ACTIVE_DISGUISES_COUNT("<dark_green>There are %s disguises active"),
    ACTIVE_DISGUISES_DISGUISE("<green>%s: <aqua>%s"),
    ACTIVE_DISGUISES("<dark_green>The disguises in use are: %s"),
    ACTIVE_DISGUISES_SEPERATOR("<red>, <green>"),
    BLOWN_DISGUISE("<red>Your disguise was blown!"),
    EXPIRED_DISGUISE("<red>Your disguise has expired!"),
    CAN_USE_DISGS("<dark_green>You can use the disguises:<green> %s"),
    CAN_USE_DISGS_SEPERATOR("<red>, <green>"),
    CANNOT_FIND_PLAYER("<red>Cannot find the player/uuid '%s'"),
    CANNOT_FIND_PLAYER_NAME("<red>Cannot find the player '%s'"),
    CANNOT_FIND_PLAYER_UUID("<red>Cannot find the uuid '%s'"),
    CLICK_TIMER("<red>Right click a entity in the next %s seconds to grab the disguise reference!"),
    CLONE_HELP1("<dark_green>Right click a entity to get a disguise reference you can pass to other disguise commands!"),
    CLONE_HELP2("<dark_green>Security note: Any references you create will be available to all players able to use disguise references."),
    CLONE_HELP3("<dark_green>/disguiseclone IgnoreEquipment<dark_green>(<green>Optional<dark_green>)"),
    CUSTOM_DISGUISE_SAVED("<gold>Custom disguise has been saved as '%s'!"),
    D_HELP1("<dark_green>Disguise another player!"),
    D_HELP3("<dark_green>/disguiseplayer <PlayerName> player <Name>"),
    D_HELP4("<dark_green>/disguiseplayer <PlayerName> <DisguiseType> <Baby>"),
    D_HELP5("<dark_green>/disguiseplayer <PlayerName> <Dropped_Item/Falling_Block> <Id> <Durability>"),
    D_PARSE_NOPERM("<red>You do not have permission to use the method %s"),
    DHELP_CANTFIND("<red>Cannot find the disguise %s"),
    DHELP_HELP1("<red>/disguisehelp <DisguiseType> <green>" +
            "- View the methods you can set on a disguise. Add 'show' to reveal the methods you don't have permission to use"),
    DHELP_HELP2("<red>/disguisehelp <DisguiseOption> <green>- View information about the disguise values such as 'RabbitType'"),
    DHELP_HELP3("<red>/disguisehelp <dark_green>%s<green> - %s"),
    DHELP_HELP4("<red>%s: <green>%s"),
    DHELP_HELP4_SEPERATOR("<red>, <green>"),
    DHELP_HELP5("<red>%s: <green>%s"),
    DHELP_HELP6("<red>%s: <dark_green>%s <green>%s"),
    DHELP_OPTIONS("%s options: %s"),
    DISABLED_LIVING_TO_MISC("<red>Can't disguise a living entity as a misc disguise. This has been disabled in the config!"),
    DISG_ENT_CLICK("<red>Right click an entity in the next %s seconds to disguise it as a %s!"),
    DISG_ENT_HELP1("<dark_green>Choose a disguise then right click an entity to disguise it!"),
    DISG_ENT_HELP3("<dark_green>/disguiseentity player <Name>"),
    DISG_ENT_HELP4("<dark_green>/disguiseentity <DisguiseType> <Baby>"),
    DISG_ENT_HELP5("<dark_green>/disguiseentity <Dropped_Item/Falling_Block> <Id> <Durability>"),
    DISG_HELP1("<dark_green>Choose a disguise to become the disguise!"),
    DISG_HELP2("<dark_green>/disguise player <Name>"),
    DISG_HELP3("<dark_green>/disguise <DisguiseType> <Baby>"),
    DISG_HELP4("<dark_green>/disguise <Dropped_Item/Falling_Block> <Id> <Durability>"),
    DISG_PLAYER_AS_DISG("<red>Successfully disguised %s as a %s!"),
    DISG_PLAYER_AS_DISG_FAIL("<red>Failed to disguise %s as a %s!"),
    DISGUISED("<red>Now disguised as %s"),
    DISRADIUS("<red>Successfully disguised %s entities!"),
    DISRADIUS_FAIL("<red>Couldn't find any entities to disguise!"),
    DMODENT_HELP1("<dark_green>Choose the options for a disguise then right click a entity to modify it!"),
    DMODIFY_HELP1("<dark_green>Modify your own disguise as you wear it!"),
    DMODIFY_HELP2("<dark_green>/disguisemodify setBaby true setSprinting true"),
    DMODIFY_HELP3("<dark_green>You can modify the disguises:<green> %s"),
    DMODIFY_MODIFIED("<red>Your disguise has been modified!"),
    DMODIFY_NO_PERM("<red>No permission to modify your disguise!"),
    DMODIFYENT_CLICK("<red>Right click a disguised entity in the next %s seconds to modify their disguise!"),
    DISGUISECOPY_INTERACT("<red>Right click a disguised entity in the next %s seconds to copy their disguise!"),
    DMODPLAYER_HELP1("<dark_green>Modify the disguise of another player!"),
    DMODPLAYER_MODIFIED("<red>Modified the disguise of %s!"),
    DMODPLAYER_NODISGUISE("<red>The player '%s' is not disguised"),
    DMODPLAYER_NOPERM("<red>You do not have permission to modify this disguise"),
    DMODRADIUS("<red>Successfully modified the disguises of %s entities!"),
    DMODRADIUS_HELP1("<dark_green>Modify the disguises in a radius! Caps at %s blocks!"),
    DHELP_SHOW("Show"),
    DHELP_NO_OPTIONS("<red>No options with permission to use"),
    DCLONE_EQUIP("ignoreEquip"),
    DCLONE_ADDEDANIMATIONS("doAddedAnimations"),
    DMODRADIUS_HELP2(
            "<dark_green>/disguisemodifyradius <<green>DisguiseType<dark_green>(<green>Optional<dark_green>)> <<green>Radius<dark_green>> <<green>Disguise " +
                    "Methods<dark_green>>"),
    DMODRADIUS_HELP3("<dark_green>See the DisguiseType's usable by <green>/disguisemodifyradius DisguiseType"),
    DMODRADIUS_NEEDOPTIONS("<red>You need to supply the disguise methods as well as the radius"),
    DMODRADIUS_NEEDOPTIONS_ENTITY("<red>You need to supply the disguise methods as well as the radius and EntityType"),
    DMODRADIUS_NOENTS("<red>Couldn't find any disguised entities!"),
    DMODRADIUS_NOPERM("<red>No permission to modify %s disguises!"),
    DMODRADIUS_UNRECOGNIZED("<red>Unrecognised DisguiseType %s"),
    DMODRADIUS_USABLE("<dark_green>DisguiseTypes usable are: %s<dark_green>."),
    DPLAYER_SUPPLY("<red>You need to supply a disguise as well as the player/uuid"),
    DRADIUS_ENTITIES("<dark_green>EntityTypes usable are: %s"),
    DRADIUS_HELP1("<dark_green>Disguise all entities in a radius! Caps at %s blocks!"),
    DRADIUS_HELP3("<dark_green>/disguiseradius <<green>EntityType<dark_green>(<green>Optional<dark_green>)> <<green>Radius<dark_green>> player " +
            "<<green>Name<dark_green>>"),
    DRADIUS_HELP4("<dark_green>/disguiseradius <<green>EntityType<dark_green>(<green>Optional<dark_green>)> <<green>Radius<dark_green>> " +
            "<<green>DisguiseType<dark_green>> <<green>Baby<dark_green>(<green>Optional<dark_green>)>"),
    DRADIUS_HELP5("<dark_green>/disguiseradius <<green>EntityType<dark_green>(<green>Optional<dark_green>)> <<green>Radius<dark_green>> " +
            "<<green>Dropped_Item/Falling_Block<dark_green>> <<green>Id<dark_green>> <<green>Durability<dark_green>(<green>Optional<dark_green>)" + ">"),
    DRADIUS_HELP6("<dark_green>See the EntityType's usable by <green>/disguiseradius EntityTypes"),
    DRADIUS_MISCDISG("<red>Failed to disguise %s entities because the option to disguise a living entity as a non-living has been disabled in the config"),
    DRADIUS_NEEDOPTIONS("<red>You need to supply a disguise as well as the radius"),
    DRADIUS_NEEDOPTIONS_ENTITY("<red>You need to supply a disguise as well as the radius and EntityType"),
    FAILED_DISGIUSE("<red>Failed to disguise as %s"),
    GRABBED_SKIN("<gold>Grabbed skin and saved as %s!"),
    PLEASE_WAIT("<gray>Please wait..."),
    INVALID_CLONE("<dark_red>Unknown method '%s' - Valid methods are 'IgnoreEquipment' 'DoSneakSprint' 'DoSneak' 'DoSprint'"),
    LIBS_COMMAND_WRONG_ARG("<red>[LibsDisguises] Invalid argument, use /libsdisguises help"),
    LIBS_UPDATE_UNKNOWN_BRANCH("<red>[LibsDisguises] Invalid argument, use 'dev' or 'release' to switch branches"),
    LIMITED_RADIUS("<red>Limited radius to %s! Don't want to make too much lag right?"),
    LISTEN_ENTITY_ENTITY_DISG_ENTITY("<red>Disguised %s as a %s!"),
    LISTEN_ENTITY_ENTITY_DISG_ENTITY_FAIL("<red>Failed to disguise %s as a %s!"),
    LISTEN_ENTITY_ENTITY_DISG_PLAYER("<red>Disguised %s as the player %s!"),
    LISTEN_ENTITY_ENTITY_DISG_PLAYER_FAIL("<red>Failed to disguise %s as the player %s!"),
    LISTEN_ENTITY_PLAYER_DISG_ENTITY("<red>Disguised the player %s as a %s!"),
    LISTEN_ENTITY_PLAYER_DISG_ENTITY_FAIL("<red>Failed to disguise the player %s as a %s!"),
    LISTEN_ENTITY_PLAYER_DISG_PLAYER("<red>Disguised the player %s as the player %s!"),
    LISTEN_ENTITY_PLAYER_DISG_PLAYER_FAIL("<red>Failed to disguise the player %s as the player %s!"),
    LISTEN_UNDISG_ENT("<red>Undisguised the %s"),
    LISTEN_UNDISG_ENT_FAIL("<red>%s isn't disguised!"),
    LISTEN_UNDISG_PLAYER("<red>Undisguised %s"),
    LISTEN_UNDISG_PLAYER_FAIL("<red>The %s isn't disguised!"),
    LISTENER_MODIFIED_DISG("<red>Modified the disguise!"),
    MADE_REF("<red>Constructed a %s disguise! Your reference is %s"),
    MADE_REF_EXAMPLE("<red>Example usage: /disguise %s"),
    NO_CONSOLE("<red>You may not use this command from the console!"),
    TOO_FAST("<red>You are using the disguise command too fast!"),
    NO_MODS("<red>%s is not using any mods!"),
    MODS_LIST("<dark_green>%s has the mods:<aqua> %s"),
    NO_PERM("<red>You are forbidden to use this command."),
    UPDATE_ON_LATEST("<red>You are already on the latest version of LibsDisguises!"),
    UPDATE_ALREADY_DOWNLOADED("<red>That update has already been downloaded!"),
    UPDATE_FAILED("<red>LibsDisguises update failed! Check console for errors."),
    UPDATE_SUCCESS("<dark_green>LibsDisguises update success! Restart server to update!"),
    UPDATE_REQUIRED("<red>LibsDisguises requies an update check before it can give you that!"),
    UPDATE_INFO("<dark_green>Lib's Disguises v%s, build %s, built %s and size %skb"),
    UPDATE_IN_PROGRESS("<dark_green>LibsDisguises is now downloading an update..."),
    NO_PERM_DISGUISE("<red>You do not have permission for that disguise!"),
    NO_MODS_LISTENING("<red>This server is not listening for mods!"),
    NO_PERMS_USE_OPTIONS("<red>Ignored %s methods you do not have permission to use. Add 'show' to view unusable methods."),
    OWNED_BY("<gold>Plugin registered to '%%__USER__%%'!"),
    NOT_DISGUISED("<red>You are not disguised!"),
    DISGUISE_REQUIRED("<red>You must be disguised to run this command!"),
    TARGET_NOT_DISGUISED("<red>That entity is not disguised!"),
    NOT_NUMBER("<red>Error! %s is not a number"),
    PARSE_CANT_DISG_UNKNOWN("<red>Error! You cannot disguise as <green>Unknown!"),
    PARSE_CANT_LOAD("<red>Error! This disguise couldn't be loaded!"),
    PARSE_DISG_NO_EXIST("<red>Error! The disguise <green>%s<red> doesn't exist!"),
    PARSE_EXPECTED_RECEIVED("<red>Expected <green>%s<red>, received <green>%s<red> instead for <green>%s"),
    PARSE_PARTICLE_BLOCK("<red>Expected <green>%s:Material<red>, received <green>%s<red> instead"),
    PARSE_PARTICLE_ITEM("<red>Expected <green>%s:Material,Amount?,Glow?<red>, received <green>%s<red> instead"),
    PARSE_PARTICLE_REDSTONE("<red>Expected <green>%s:Color,Size.0?<red>, received <green>%s<red> instead"),
    PARSE_NO_ARGS("No arguments defined"),
    PARSE_NO_OPTION_VALUE("<red>No value was given for the method %s"),
    PARSE_NO_PERM_NAME("<red>Error! You don't have permission to use that name!"),
    PARSE_NO_PERM_PARAM("<red>Error! You do not have permission to use the parameter %s on the %s disguise!"),
    PARSE_NO_PERM_REF("<red>You do not have permission to use disguise references!"),
    PARSE_NO_REF("<red>Cannot find a disguise under the reference %s"),
    PARSE_OPTION_NA("<red>Cannot find the method '%s'"),
    PARSE_SUPPLY_PLAYER("<red>Error! You need to give a player name!"),
    PARSE_TOO_MANY_ARGS("<red>Error! %s doesn't know what to do with %s!"),
    PARSE_INVALID_TIME("<red>Error! %s is not a valid time! Use s,m,h,d or secs,mins,hours,days"),
    PARSE_INVALID_TIME_SEQUENCE("<red>Error! %s is not a valid time! Do amount then time, eg. 4min10sec"),
    PARSE_USE_SECOND_NUM("<red>Error! Only the disguises %s and %s uses a second number!"),
    REF_TOO_MANY("<red>Failed to store the reference, too many cloned disguises. Please raise the maximum cloned disguises, or lower the time they last"),
    RELOADED_CONFIG("<green>[LibsDisguises] Reloaded config."),
    UND_ENTITY("<red>Right click a disguised entity to undisguise them!"),
    UNDISG("<red>You are no longer disguised"),
    UNDISG_PLAYER("<red>%s is no longer disguised"),
    UNDISG_PLAYER_FAIL("<red>%s not disguised!"),
    UNDISG_PLAYER_HELP("<red>/undisguiseplayer <Name>"),
    UNDISRADIUS("<red>Successfully undisguised %s entities!"),
    UPDATE_READY("<red>[LibsDisguises] <dark_red>There is a update ready to be downloaded! You are using <red>v%s<dark_red>, the new version is " +
            "<red>v%s<dark_red>!"),
    UPDATE_READY_SNAPSHOT("<red>[LibsDisguises] <dark_red>There is a new build of Lib's Disguises! You are using <red>%s<dark_red>, the latest build is " +
            "<red>#%s<dark_red>!"),
    UPDATE_HOW("<dark_aqua>Use <aqua>/libsdisgusies changelog<dark_aqua> to see what changed, use <aqua>/libsdisguises update!" +
            "<dark_aqua> to download the update!"),
    VIEW_SELF_ON("<green>Toggled viewing own disguise on!"),
    VIEW_SELF_OFF("<green>Toggled viewing own disguise off!"),
    VIEW_BAR_ON("<green>Toggled disguised notify bar on!"),
    VIEW_BAR_OFF("<green>Toggled disguised notify bar off!"),
    CLICK_TO_COPY("<green>Click to Copy:"),
    SKIN_DATA("<green>Skin Data: <yellow>%s"),
    CLICK_TO_COPY_DATA("<yellow>Data"),
    CLICK_TO_COPY_WITH_SKIN("<green>Version with skin data:"),
    CLICK_TO_COPY_WITH_SKIN_NO_COPY("<green>Version with skin data: <yellow>%s"),
    COPY_DISGUISE_NO_COPY("<green>Data: <yellow>%s"),
    CLICK_TO_COPY_HOVER("<gold>Click to Copy"),
    CLICK_COPY("<yellow><bold>%s"),
    SKIN_API_UUID_3("<red>Using account with UUID version 3. If skin is incorrect, try change 'UUIDVersion' in protocol.yml" +
            " to 3. If skin is still incorrect and you did not purchase Minecraft, this cannot be fixed."),
    SKIN_API_IN_USE("<red>mineskin.org is currently in use, please try again"),
    SKIN_API_TIMER("<red>mineskin.org can be used again in %s seconds"),
    SKIN_API_FAIL("<red>Unexpected error while accessing mineskin.org, please try again"),
    SKIN_API_FAIL_TOO_FAST("<red>Too many requests accessing mineskin.org, please slow down!"),
    SKIN_API_BAD_URL("<red>Invalid url provided! Please ensure it is a .png file download!"),
    SKIN_API_FAILED_URL("<red>Invalid url provided! mineskin.org failed to grab it!"),
    SKIN_API_FAIL_CODE("<red>Error %s! %s"),
    SKIN_API_403("mineskin.org denied access to that url"),
    SKIN_API_404("mineskin.org unable to find an image at that url"),
    SKIN_API_IMAGE_TIMEOUT("<red>Error! mineskin.org took too long to connect! Is your image valid?"),
    SKIN_API_TIMEOUT_ERROR("<red>Error! Took too long to connect to mineskin.org!"),
    SKIN_API_TIMEOUT_API_KEY_ERROR("<red>Error! Took too long to connect to mineskin.org! Is the API Key correct?"),
    SKIN_API_TIMEOUT("<red>Took too long to connect to mineskin.org!"),
    SKIN_API_IMAGE_HAS_ERROR("Your image has the error: %s"),
    SKIN_API_USING_URL("<gray>Url provided, now attempting to connect to mineskin.org"),
    SKIN_API_BAD_FILE_NAME("<red>Invalid file name provided! File not found!"),
    SKIN_API_BAD_FILE("<red>Invalid file provided! Please ensure it is a valid .png skin!"),
    SKIN_API_USING_FILE("<gray>File provided and found, now attempting to upload to mineskin.org"),
    SKIN_API_INVALID_NAME("<red>Invalid name/file/uuid provided!"),
    SKIN_API_USING_UUID("<gray>UUID successfully parsed, now attempting to connect to mineskin.org"),
    SKIN_API_USING_EXISTING_NAME("<gray>Found a saved skin under that name locally! Using that!"),
    SKIN_API_USING_NAME("<gray>Determined to be player name, now attempting to validate and connect to mineskin.org"),
    SAVE_DISG_HELP_1("<green>The <DisguiseName> is what the disguise will be called in Lib's Disguises"),
    SAVE_DISG_HELP_2("<green>/savedisguise <DisguiseName> - If you don't provide arguments, it'll try make a disguise from your" +
            " current disguise. This will not work if you are not disguised!"),
    SAVE_DISG_HELP_3("<green>/savedisguise <DisguiseName> <Arguments>"),
    SAVE_DISG_HELP_4("<green>Your arguments need to be as if you're using /disguise. So '/disguise player Notch setsneaking' - " +
            "Means '/savedisguise Notch player Notch setsneaking'"),
    SAVE_DISG_HELP_5("<green>Remember! You can upload your own skins, then reference those skins!"),
    SAVE_DISG_HELP_6("<green>If you are using setSkin, you can append :slim to your skin path to get the slim Alex model. So myskin.png:slim"),
    GRAB_DISG_HELP_1("<green>You can choose a name to save the skins under, the names will be usable as if it was an actual player skin"),
    GRAB_DISG_HELP_2("<dark_green>/grabskin <Optional Name> https://somesite.com/myskin.png"),
    GRAB_DISG_HELP_3("<dark_green>/grabskin <Optional Name> myskin.png - Skins must be in the folder!"),
    GRAB_DISG_HELP_4("<dark_green>/grabskin <Optional Name> <Player name or UUID>"),
    GRAB_DISG_HELP_5("<green>If you want the slim Alex version of the skin, append :slim. So 'myskin.png:slim'"),
    GRAB_DISG_HELP_6("<green>You will be sent the skin data, but you can also use the saved names in disguises"),
    GRAB_HEAD_SUCCESS("<green>Head successfully grabbed and added to inventory!"),
    GRAB_HEAD_HELP_1("<green>Grab the head of a file, player or url! This is a Lib's Disguises feature."),
    GRAB_HEAD_HELP_2("<dark_green>/grabhead https://somesite.com/myskin.png"),
    GRAB_HEAD_HELP_3("<dark_green>/grabhead myskin.png - Skins must be in the folder!"),
    GRAB_HEAD_HELP_4("<dark_green>/grabhead <Player name or UUID>"),
    CUSTOM_DISGUISE_NAME_CONFLICT("<red>Cannot create the custom disguise '%s' as there is a name conflict!"),
    ERROR_LOADING_CUSTOM_DISGUISE("<red>Error while loading custom disguise '%s'%s"),
    SKIN_API_INTERNAL_ERROR("<red>Internal error in the skin API, perhaps bad data?"),
    META_INFO("<green>Name: %s, Watcher: %s, Index: %s, Type: %s, Default: %s"),
    META_NOT_FOUND("<red>No meta exists under that name!"),
    META_VALUES("<blue>Metas: <dark_aqua>"),
    META_VALUES_NO_CLICK("<blue>Metas, use as param for more info: <dark_aqua>"),
    META_VALUE_SEPERATOR("<aqua>, <dark_aqua>"),
    META_CLICK_SHOW("<gold>Click to show %s"),
    LIBS_PERM_CHECK_NON_PREM("<red>This server is not premium, non-admins should not be able to use commands"),
    LIBS_PERM_CHECK_CAN_TARGET("<gold>You can specify a player target with /ld permtest <Target> instead!"),
    LIBS_PERM_CHECK_USING_TARGET("<gold>Running the permission test on '%s'"),
    LIBS_PERM_CHECK_INFO_1("<aqua>Now checking for the permission 'libsdisguises.disguise.pig'"),
    LIBS_PERM_CHECK_INFO_2("<aqua>If you did not give this permission, please set it."),
    NORMAL_PERM_CHECK_SUCCESS("<gold>Normal permission check, success."),
    NORMAL_PERM_CHECK_FAIL("<red>Normal permission check, fail."),
    LIBS_PERM_CHECK_SUCCESS("<gold>Lib's Disguises permission check, success. Pig disguise should be usable!"),
    LIBS_PERM_CHECK_FAIL("<gold>Lib's Disguises permission check, fail. Your permission plugin isn't compliant!"),
    LIBS_PERM_CHECK_ZOMBIE_PERMISSIONS(
            "<gold>Tested libsdisguises.disguise.zombie, which your player seems to have! There may be a problem in your permissions setup!"),
    LIBS_PERM_CHECK_COMMAND_UNREGISTERED("<red>The /disguise command seems to be unregistered! Check your config!"),
    LIBS_PERM_COMMAND_SUCCESS("<gold>Tested permission '%s' for /disguise command access, permission success!"),
    LIBS_PERM_COMMAND_FAIL("<red>Tested permission '%s' for /disguise command access, permission failed!"),
    CANT_ATTACK_DISGUISED("<red>Cannot fight while disguised!"),
    CANT_ATTACK_DISGUISED_RECENTLY("<red>You were disguised recently! Can't attack yet!"),
    SWITCH_WORLD_DISGUISE_REMOVED("<red>Disguise removed as you've switched worlds!"),
    ACTION_BAR_MESSAGE("<gold>Currently disguised as %s"),
    ITEM_SERIALIZED("<gold>Json Serialized, click to copy: "),
    ITEM_SERIALIZED_MC("<gold>MC Serialized, click to copy: "),
    ITEM_SERIALIZED_MC_LD("<gold>MC Serialized for LD, click to copy: "),
    ITEM_SIMPLE_STRING("<gold>Simple, click to copy: "),
    ITEM_SERIALIZED_NO_COPY("<gold>Json Serialized: <yellow>%s"),
    ITEM_SERIALIZED_MC_NO_COPY("<gold>MC Serialized: <yellow>%s"),
    ITEM_SERIALIZED_MC_LD_NO_COPY("<gold>MC Serialized for LD: <yellow>%s"),
    ITEM_SIMPLE_STRING_NO_COPY("<gold>Simple: <yellow>%s"),
    LIBS_SCOREBOARD_NO_TEAM("<red>Not on a scoreboard team!"),
    LIBS_SCOREBOARD_SUCCESS("<gold>On scoreboard team '%s' with pushing disabled! If you're still having issues and you are disguised right now, then " +
            "you have a plugin modifying scoreboard through packets. Example of this is a plugin that modifies your " +
            "name above head, or the tablist. Check their configs for pushing disabling options\nSay 'I read to the end' if you " +
            "still need help with this, or we'll assume you can't read."),
    LIBS_SCOREBOARD_NAMES_DISABLED("<red>Scoreboard names has been disabled, the test for player disguises has failed before it started"),
    LIBS_SCOREBOARD_IGNORE_TEST("<green>This was a seperate test from the self disguising collision test that will follow!"),
    USING_DEFAULT_CONFIG("<dark_green>Using the default config!"),
    LIBS_SCOREBOARD_ISSUES("<green>Too many issues found, hidden %s"),
    LIBS_SCOREBOARD_NO_ISSUES("<green>No issues found in player disguise scoreboard name teams"),
    LD_COMMAND_UPDATEPROTOCOLLIB("<blue>/libsdisguises updateprotocollib - <aqua>Updates ProtocolLib to the latest development version"),
    LD_COMMAND_HELP("<blue>/libsdisguises help - <aqua>Returns this!"),
    LD_COMMAND_COUNT("<blue>/libsdisguises count - <aqua>Tells you how many active disguises there are"),
    LD_COMMAND_METAINFO("<blue>/libsdisguises metainfo - <aqua>Debugging info, tells you what the metadata is for a disguise"),
    LD_COMMAND_CONFIG("<blue>/libsdisguises config - <aqua>Tells you what's not normal in your config"),
    LD_COMMAND_UPDATE("<blue>/libsdisguises update - <aqua>" +
            "'update' will fetch an update, 'update dev' will fetch a dev build update, 'update release' will fetch a" +
            " release build update and 'update!' will download that update!"),
    LD_COMMAND_CHANGELOG("<blue>/libsdisguises changelog - <aqua>Gives you the changelog of the current update fetched"),
    LD_DEBUG_MINESKIN("<blue>/libsdisguises mineskin - <aqua>Prints debug information about MineSkin to console"),
    LD_DEBUG_MINESKIN_TOGGLE("<blue>MineSkin debug is now %s, this command toggles the printing of MineSkin information to console"),
    LD_COMMAND_JSON("<blue>/libsdisguises json - <aqua>Turns the current held item into a string format"),
    LD_COMMAND_MODS("<blue>/libsdisguises mods <Player?> - <aqua>" + "If using modded entities, this will tell you what mods a player is using if possible"),
    LD_COMMAND_PERMTEST("<blue>/libsdisguises permtest <Player?> - <aqua>Does a quick test to see if your permissions are working"),
    LD_COMMAND_SCOREBOARD("<blue>/libsdisguises scoreboard <Player?> - <aqua>Does a test to see if there's any scoreboard issues it can detect"),
    LD_COMMAND_RELOAD("<blue>/libsdisguises reload - <aqua>Reload's the plugin config and possibly blows disguises"),
    LD_COMMAND_DEBUG("<blue>/libsdisguises debug - <aqua>Used to help debug scoreboard issues on a player disguise"),
    LD_COMMAND_UPLOAD_LOGS("<blue>/libsdisguises uploadlogs - <aqua>" +
            "Uploads latest.log, disguises.yml and configs and gives you the link to share. Used when seeking assistance."),
    SELF_DISGUISE_HIDDEN("<green>Self disguise hidden as it's too tall..");

    private final String string;
    private final String useString;

    LibsMsg(String string) {
        this.string = string;

        useString = DisguiseUtilities.hasAdventureTextSupport() ? string : getVanillaFormat();
    }

    public String getVanillaFormat() {
        String raw = getRaw();

        for (ChatColor c : ChatColor.values()) {
            raw = raw.replace("<" + c.name().toLowerCase(Locale.ROOT) + ">", "§" + c.getChar());
        }

        return raw;
    }

    public String getRaw() {
        return string;
    }

    public String getStringToUse() {
        return useString;
    }

    public BaseComponent[] getChat(Object... strings) {
        String string = get(strings);

        return DisguiseUtilities.getColoredChat(string);
    }

    public void send(CommandSender player, Object... strings) {
        DisguiseUtilities.sendMessage(player, this, strings);
    }

    @Deprecated
    public String get(Object... strings) {
        int matches = StringUtils.countMatches(getRaw(), "%s");

        if (matches != strings.length) {
            DisguiseUtilities.getLogger()
                    .severe("Mismatch in messages, incorrect parameters supplied for " + name() + ". Please inform plugin author if not using translations.");
        }

        String trans = TranslateType.MESSAGES.get(this);

        if (trans.isEmpty() || strings.length == 0) {
            return trans;
        }

        return String.format(trans, strings);
    }

    public String toString() {
        throw new IllegalStateException("Dont call this");
    }
}
