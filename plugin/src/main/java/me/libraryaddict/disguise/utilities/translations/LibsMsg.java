package me.libraryaddict.disguise.utilities.translations;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public enum LibsMsg {
    ACTION_BAR_MESSAGE("<gold>Currently disguised as %s</gold>"),
    ACTIVE_DISGUISES("<dark_green>The disguises in use are:</dark_green> <green>%s</green>"),
    ACTIVE_DISGUISES_COUNT("<dark_green>There are %s disguises active"),
    ACTIVE_DISGUISES_DISGUISE("<green>%s:</green> <aqua>%s</aqua>"),
    ACTIVE_DISGUISES_SEPERATOR("<red>,</red>"),
    ANIMATE_ENTITY_CLICK("<red>Right click a disguised entity in the next %s seconds to play the animation!</red>"),
    BLOWN_DISGUISE("<red>Your disguise was blown!"),
    BLOWN_DISGUISE_BLOCK_BREAK("<red>Breaking a block blew your disguise!"),
    BLOWN_DISGUISE_BLOCK_PLACE("<red>Placing a block blew your disguise!"),
    CANNOT_FIND_PLAYER("<red>Cannot find the player/uuid '%s'"),
    CANNOT_FIND_PLAYER_NAME("<red>Cannot find the player '%s'"),
    CANNOT_FIND_PLAYER_UUID("<red>Cannot find the uuid '%s'"),
    CANT_ATTACK_DISGUISED("<red>Cannot fight while disguised!"),
    CANT_ATTACK_DISGUISED_RECENTLY("<red>You were disguised recently! Can't attack yet!"),
    CAN_USE_DISGS("<dark_green>You can use the disguises:</dark_green> <green>%s</green>"),
    CAN_USE_DISGS_SEPERATOR("<red>, </red>"),
    CLICK_COPY("<yellow><bold>%s"),
    CLICK_TIMER("<red>Right click an entity in the next %s seconds to grab the disguise reference!</red>"),
    CLICK_TO_COPY("<green>Click to Copy: </green>"),
    CLICK_TO_COPY_DATA("<yellow>Data"),
    CLICK_TO_COPY_HOVER("<gold>%s / %s. Click to Copy</gold>\n<green>%s</green>"),
    CLICK_TO_COPY_HOVER_CLIPBOARD("<gold>%s / %s. Click to Copy to Clipboard</gold>\n<green>%s</green>"),
    CLICK_TO_COPY_WITH_SKIN("<green>Version with skin data:"),
    CLICK_TO_COPY_WITH_SKIN_NO_COPY("<green>Version with skin data: <yellow>%s"),
    CLONE_HELP1("<dark_green>Right click an entity to get a disguise reference you can pass to other disguise commands!</dark_green>"),
    CLONE_HELP2("<dark_green>Security note: Any references you create will be available to all players able to use disguise references."),
    CLONE_HELP3("<dark_green>/disguiseclone IgnoreEquipment<dark_green>(<green>Optional</green>)"),
    COPY_DISGUISE_NO_COPY("<green>Data: <yellow>%s"),
    CUSTOM_DISGUISE_NAME_CONFLICT("<red>Cannot create the custom disguise '%s' as there is a name conflict!"),
    CUSTOM_DISGUISE_SAVED("<gold>Custom disguise has been saved as '%s'!"),
    DCLONE_ADDEDANIMATIONS("doAddedAnimations"),
    DCLONE_EQUIP("ignoreEquip"),
    DHELP_CANTFIND("<red>Cannot find the disguise %s"),
    DHELP_HELP1("<red>/disguisehelp <DisguiseType> <green>" +
        "- View the methods you can set on a disguise. Add 'show' to reveal the methods you don't have permission to use"),
    DHELP_HELP2("<red>/disguisehelp <DisguiseOption> <green>- View information about the disguise values such as 'RabbitType'"),
    DHELP_HELP3("<red>/disguisehelp <dark_green>%s</dark_green> - <green>%s</green>"),
    DHELP_HELP4("<red>%s:</red> <green>%s</green>"),
    DHELP_HELP4_SEPERATOR("<red>, </red>"),
    DHELP_HELP5("<red>%s:</red> <green>%s</green>"),
    DHELP_HELP6("<red>%s:</red> <dark_green>%s</dark_green> <green>%s</green>"),
    DHELP_NO_OPTIONS("<red>No options with permission to use"),
    DHELP_OPTIONS("<gold>%s options:</gold> <green>%s</green>"),
    DHELP_OPTIONS_JOINER("<dark_red>, </dark_red>"),
    DHELP_OPTIONS_METHOD("%s<dark_red>(<green>%s</green>)</dark_red>"),
    DHELP_OPTIONS_METHOD_GENERIC("<gray>%s</gray>"),
    DHELP_OPTIONS_METHOD_SOMEWHAT_SPECIFIC("<aqua>%s</aqua>"),
    DHELP_OPTIONS_METHOD_VERY_SPECIFIC("<yellow>%s</yellow>"),
    DHELP_SHOW("Show"),
    DISABLED_CONFIG_DISGUISE("<red>That disguise was disabled in the config!</red>"),
    DISABLED_CONFIG_METHOD("<red>The disguise method %s was disabled in the config!</red>"),
    DISABLED_LIVING_TO_MISC("<red>Can't disguise a living entity as a misc disguise. This has been disabled in the config!"),
    DISGUISECOPY_INTERACT("<red>Right click a disguised entity in the next %s seconds to copy their disguise!"),
    DISGUISED("<red>Now disguised as %s"),
    DISGUISE_ANIMATE_HELP_COMMAND("<green>/%s <animation></green> <dark_green>- play a supported animation on the disguise.</dark_green>"),
    DISGUISE_ANIMATE_HELP_DISGUISED("<dark_green>Disguise supports the following animations:</dark_green> <green>%s</green>"),
    DISGUISE_ANIMATE_HELP_DISGUISED_SEPERATOR("<red>, </red>"),
    DISGUISE_ANIMATE_SEE_ALL_ANIMATIONS("<dark_green>To see all animations, use</dark_green> <green>/disguisehelp %s</green>"),
    DISGUISE_ENTITY_SELECTOR("<red>Successfully disguised %s entities!</red>"),
    DISGUISE_ENTITY_SELECTOR_INVALID("<red>The entity selector \"<dark_red>%s</dark_red>\" is invalid!</red>"),
    DISGUISE_ENTITY_SELECTOR_NO_ENTITIES("<red>Couldn't find any entities to disguise!</red>"),
    DISGUISE_REQUIRED("<red>You must be disguised to run this command!"),
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
    DISRADIUS("<red>Successfully disguised %s entities!"),
    DISRADIUS_FAIL("<red>Couldn't find any entities to disguise!"),
    DMODENT_HELP1("<dark_green>Choose the options for a disguise then right click an entity to modify it!</dark_green>"),
    DMODIFYENT_CLICK("<red>Right click a disguised entity in the next %s seconds to modify their disguise!"),
    DMODIFY_HELP1("<dark_green>Modify your own disguise as you wear it!"),
    DMODIFY_HELP2("<dark_green>/disguisemodify setBaby true setSprinting true"),
    DMODIFY_HELP3("<dark_green>You can modify the disguises:</dark_green> <green>%s</green>"),
    DMODIFY_MODIFIED("<red>Your disguise has been modified!"),
    DMODIFY_NO_PERM("<red>No permission to modify your disguise!"),
    DMODSELECTOR_HELP3("<dark_green>You can modify the disguises:</dark_green> <green>%s</green>"),
    DMODPLAYER_HELP1("<dark_green>Modify the disguise of another player!"),
    DMODPLAYER_MODIFIED("<red>Modified the disguise of %s!"),
    DMODPLAYER_NODISGUISE("<red>The player '%s' is not disguised"),
    DMODPLAYER_NOPERM("<red>You do not have permission to modify this disguise"),
    DMODRADIUS("<red>Successfully modified the disguises of %s entities!"),
    DMODRADIUS_HELP1("<dark_green>Modify nearby disguises! Caps at <green>%s</green> blocks!"),
    DMODRADIUS_HELP2("<dark_green>/disguisemodifyradius <<green>DisguiseType</green>(<green>Optional</green>)> <<green>Radius</green>> " +
        "<<green>Disguise " + "Methods</green>>"),
    DMODRADIUS_HELP3("<dark_green>See the DisguiseType's usable by <green>/disguisemodifyradius DisguiseType"),
    DMODRADIUS_NEEDOPTIONS("<red>You need to supply the disguise methods as well as the radius"),
    DMODRADIUS_NEEDOPTIONS_ENTITY("<red>You need to supply the disguise methods as well as the radius and EntityType"),
    DMODRADIUS_NOENTS("<red>Couldn't find any disguised entities!"),
    DMODRADIUS_NOPERM("<red>No permission to modify %s disguises!"),
    DMODRADIUS_USABLE("<dark_green>DisguiseTypes usable are: %s.</dark_green>"),
    DMODSELECTOR_HELP1(
        "<dark_green>Modify entity disguises via entity selector! If the selector contains spaces, \"quote\" it.</dark_green"),
    DMODSELECTOR_HELP2("<dark_green>/disguisemodifyselector <green>Selector</green> <<green>Arguments</green>></dark_green>"),
    DPLAYER_SUPPLY("<red>You need to supply a disguise as well as the player/uuid"),
    DRADIUS_ENTITIES("<dark_green>EntityTypes usable are:</dark_green> <green>%s</green>"),
    DRADIUS_HELP1("<dark_green>Disguise nearby entities! Caps at <green>%s</green> blocks!"),
    DRADIUS_HELP3("<dark_green>/disguiseradius <<green>EntityType</green>(<green>Optional</green>)> <<green>Radius</green>> player " +
        "<<green>Name<dark_green>>"),
    DRADIUS_HELP4("<dark_green>/disguiseradius <<green>EntityType</green>(<green>Optional</green>)> <<green>Radius</green>> " +
        "<<green>DisguiseType</green>> <<green>Baby</green>(<green>Optional</green>)>"),
    DRADIUS_HELP5("</green>/disguiseradius <<green>EntityType</green>(<green>Optional</green>)> <<green>Radius</green>> " +
        "<<green>Dropped_Item/Falling_Block<dark_green>> <<green>Id</green>> <<green>Durability</green>" + "(<green>Optional</green>)>"),
    DRADIUS_HELP6("<dark_green>See the EntityType's usable by <green>/disguiseradius EntityTypes"),
    DRADIUS_JOINER("<dark_green>, </dark_green>"),
    DRADIUS_MISCDISG(
        "<red>Failed to disguise %s entities because the option to disguise a living entity as a non-living has been disabled in the " +
            "config"),
    DRADIUS_NEEDOPTIONS("<red>You need to supply a disguise as well as the radius"),
    DRADIUS_NEEDOPTIONS_ENTITY("<red>You need to supply a disguise as well as the radius and EntityType"),
    DSELECTOR_HELP1("<dark_green>Disguise entities via entity selector! If the selector contains spaces, \"quote\" it.</dark_green"),
    DSELECTOR_HELP2("<dark_green>/disguiseselector <green>Selector</green> <<green>Disguise Args</green>></dark_green>"),
    D_ANIMATE_ENTITY("<dark_green>Provide the animation to play, then right click a disguised entity!</dark_green>"),
    D_ANIMATE_PLAYER(
        "<green>/%s <Player/UUID> <Animation></green> <dark_green>- Play an animation on a disguised player/uuid!</dark_green>"),
    D_ANIM_RADIUS_FAIL("<red>No disguises in range support that animation!</red>"),
    D_ANIM_SELECTOR_FAIL("<red>No disguises in range support that animation!</red>"),
    D_ANIM_RADIUS_HELP_1("<dark_green>Play animations on nearby disguises! Caps at <green>%s</green> blocks!</dark_green>"),
    D_ANIM_RADIUS_HELP_2("<dark_green>/%s <DisguiseType(Optional)> <Radius> <Animation></dark_green>"),
    D_ANIM_SELECTOR_HELP_1(
        "<dark_green>Play animations on nearby disguises! \"Quote\" the entity selector if it contains spaces.</dark_green>"),
    D_ANIM_SELECTOR_HELP_2("<dark_green>/%s <Entity Selector> <Animation></dark_green>"),
    D_ANIM_RADIUS_NEED_ANIMS("<red>You need to supply the animation as well as the radius</red>"),
    D_ANIM_RADIUS_NEED_ANIMS_ENTITY("<red>You need to supply the animation as well as the radius and EntityType"),
    D_ANIM_SELECTOR_NEED_ANIMS_ENTITY("<red>You need to supply the animation as well as the entity selector</red>"),
    D_ANIM_RADIUS_SUCCESS("<red>Successfully played animation on %s disguises!</red>"),
    D_ANIM_SELECTOR_SUCCESS("<red>Successfully played animation on %s disguises!</red>"),
    D_HELP1("<dark_green>Disguise another player!"),
    D_HELP3("<dark_green>/disguiseplayer <PlayerName> player <Name>"),
    D_HELP4("<dark_green>/disguiseplayer <PlayerName> <DisguiseType> <Baby>"),
    D_HELP5("<dark_green>/disguiseplayer <PlayerName> <Dropped_Item/Falling_Block> <Id> <Durability>"),
    D_PARSE_NOPERM("<red>You do not have permission to use the method %s"),
    ERROR_LOADING_CUSTOM_DISGUISE("<red>Error while loading custom disguise '%s'%s"),
    EXPIRED_DISGUISE("<red>Your disguise has expired!"),
    FAILED_DISGIUSE("<red>Failed to disguise as %s"),
    GRABBED_SKIN("<gold>Grabbed skin and saved as %s!"),
    GRAB_DISG_HELP_1("<green>You can choose a name to save the skins under, the names will be usable as if it was an actual player skin"),
    GRAB_DISG_HELP_2("<dark_green>/grabskin <Optional Name> https://somesite.com/myskin.png"),
    GRAB_DISG_HELP_3("<dark_green>/grabskin <Optional Name> myskin.png - Skins must be in the folder!"),
    GRAB_DISG_HELP_4("<dark_green>/grabskin <Optional Name> <Player name or UUID>"),
    GRAB_DISG_HELP_5("<green>If you want the slim Alex version of the skin, append :slim. So 'myskin.png:slim'"),
    GRAB_DISG_HELP_6("<green>You will be sent the skin data, but you can also use the saved names in disguises"),
    GRAB_HEAD_HELP_1("<green>Grab the head of a file, player or url! This is a Lib's Disguises feature."),
    GRAB_HEAD_HELP_2("<dark_green>/grabhead https://somesite.com/myskin.png"),
    GRAB_HEAD_HELP_3("<dark_green>/grabhead myskin.png - Skins must be in the folder!"),
    GRAB_HEAD_HELP_4("<dark_green>/grabhead <Player name or UUID>"),
    GRAB_HEAD_SUCCESS("<green>Head successfully grabbed and added to inventory!"),
    INVALID_CLONE("<dark_red>Unknown method '%s' - Valid methods are 'IgnoreEquipment' 'DoSneakSprint' 'DoSneak' 'DoSprint'"),
    ITEM_SERIALIZED("<gold>Json Serialized, click to copy: </gold>"),
    ITEM_SERIALIZED_MC("<gold>MC Serialized, click to copy: </gold>"),
    ITEM_SERIALIZED_MC_LD("<gold>MC Serialized for LD, click to copy: </gold>"),
    ITEM_SERIALIZED_MC_LD_NO_COPY("<gold>MC Serialized for LD:</gold> <yellow>%s</yellow>"),
    ITEM_SERIALIZED_MC_NO_COPY("<gold>MC Serialized:</gold> <yellow>%s</yellow>"),
    ITEM_SERIALIZED_NO_COPY("<gold>Json Serialized:</gold> <yellow>%s</yellow>"),
    ITEM_SIMPLE_STRING("<gold>Simple, click to copy: </gold>"),
    ITEM_SIMPLE_STRING_NO_COPY("<gold>Simple: <yellow>%s"),
    LD_COMMAND_CHANGELOG("<blue>/libsdisguises changelog - <aqua>Gives you the changelog of the current update fetched"),
    LD_COMMAND_CONFIG("<blue>/libsdisguises config - <aqua>Checks for modified values in Lib's Disguises config"),
    LD_COMMAND_COUNT("<blue>/libsdisguises count - <aqua>Tells you how many active disguises there are"),
    LD_COMMAND_DEBUG("<blue>/libsdisguises debug - <aqua>Used to help debug scoreboard issues on a player disguise"),
    LD_COMMAND_HELP("<blue>/libsdisguises help - <aqua>Returns this!"),
    LD_COMMAND_JSON("<blue>/libsdisguises json - <aqua>Turns the current held item into a string format"),
    LD_COMMAND_METAINFO("<blue>/libsdisguises metainfo - <aqua>Debugging info, tells you what the metadata is for a disguise"),
    LD_COMMAND_MODS("<blue>/libsdisguises mods <Player?> - <aqua>" +
        "If using modded entities, this will tell you what mods a player is using if possible"),
    LD_COMMAND_PERMTEST("<blue>/libsdisguises permtest <Player?> - <aqua>Does a quick test to see if your permissions are working"),
    LD_COMMAND_RELOAD("<blue>/libsdisguises reload - <aqua>Reload's the plugin config and possibly blows disguises"),
    LD_COMMAND_SCOREBOARD(
        "<blue>/libsdisguises scoreboard <Player?> - <aqua>Does a test to see if there's any scoreboard issues it can detect"),
    LD_COMMAND_UPDATE("<blue>/libsdisguises update - <aqua>" +
        "'update' will fetch an update, 'update dev' will fetch a dev build update, 'update release' will fetch a" +
        " release build update and 'update!' will download that update!"),
    LD_COMMAND_UPDATE_PACKET_EVENTS("<blue>/libsdisguises updatepacketevents - <aqua>Updates PacketEvents to the latest release"),
    LD_COMMAND_UPLOAD_LOGS("<blue>/libsdisguises uploadlogs - <aqua>" +
        "Uploads latest.log, disguises.yml and configs and gives you the link to share. Used when seeking assistance."),
    LD_DEBUG_DISGUISE_LOOP("<blue>/libsdisguises disguiseloop - <aqua>Used to quickly loop through every disguise for problematic ones"),
    LD_DEBUG_MINESKIN("<blue>/libsdisguises mineskin - <aqua>Prints debug information about MineSkin to console"),
    LD_DEBUG_MINESKIN_TOGGLE("<blue>MineSkin debug is now %s, this command toggles the printing of MineSkin information to console"),
    LD_DEBUG_MODE("<blue>/libsdisguises debugging - <aqua>For internal use to debug stuff"),
    LIBS_COMMAND_WRONG_ARG("<red>[LibsDisguises] Invalid argument, use /libsdisguises help"),
    LIBS_PERM_CHECK_CAN_TARGET("<gold>You can specify a player target with /ld permtest <Target> instead!"),
    LIBS_PERM_CHECK_COMMAND_UNREGISTERED("<red>The /disguise command seems to be unregistered! Check your config!"),
    LIBS_PERM_CHECK_FAIL("<gold>Lib's Disguises permission check, fail. Your permission plugin isn't compliant!"),
    LIBS_PERM_CHECK_INFO_1("<aqua>Now checking for the permission 'libsdisguises.disguise.pig'"),
    LIBS_PERM_CHECK_INFO_2("<aqua>If you did not give this permission, please set it."),
    LIBS_PERM_CHECK_NON_PREM("<red>This server is not premium, non-admins should not be able to use commands"),
    LIBS_PERM_CHECK_SUCCESS("<gold>Lib's Disguises permission check, success. Pig disguise should be usable!"),
    LIBS_PERM_CHECK_USING_TARGET("<gold>Running the permission test on '%s'"),
    LIBS_PERM_CHECK_ZOMBIE_PERMISSIONS(
        "<gold>Tested libsdisguises.disguise.zombie, which your player seems to have! There may be a problem in your permissions setup!"),
    LIBS_PERM_COMMAND_FAIL("<red>Tested permission '%s' for /disguise command access, permission failed!"),
    LIBS_PERM_COMMAND_SUCCESS("<gold>Tested permission '%s' for /disguise command access, permission success!"),
    LIBS_SCOREBOARD_IGNORE_TEST("<green>This was a seperate test from the self disguising collision test that will follow!"),
    LIBS_SCOREBOARD_ISSUES("<green>Too many issues found, hidden %s</green>"),
    LIBS_SCOREBOARD_NAMES_DISABLED("<red>Scoreboard names has been disabled, the test for player disguises has failed before it started"),
    LIBS_SCOREBOARD_NO_ISSUES("<green>No issues found in player disguise scoreboard name teams"),
    LIBS_SCOREBOARD_NO_TEAM("<red>Not on a scoreboard team!"),
    LIBS_SCOREBOARD_SUCCESS(
        "<gold>On scoreboard team '%s' with pushing disabled! If you're still having issues and you are disguised right now, then " +
            "you have a plugin modifying scoreboard through packets. Example of this is a plugin that modifies your " +
            "name above head, or the tablist. Check their configs for pushing disabling options\nSay 'I read to the end' if you " +
            "still need help with this, or we'll assume you can't read."),
    LIBS_UPDATE_CANT_SWITCH_BRANCH(
        "<red>[LibsDisguises] Invalid argument, update branch has been set to %s in libsdisguises.yml and cannot be ignored. Change this " +
            "to SAME_BUILDS and use /ld reload to restore normal behavior."),
    LIBS_UPDATE_UNKNOWN_BRANCH("<red>[LibsDisguises] Invalid argument, use 'dev' or 'release' to switch branches"),
    LIMITED_RADIUS("<red>Limited radius to %s! Don't want to make too much lag right?"),
    LISTENER_MODIFIED_DISG("<red>Modified the disguise!"),
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
    MADE_REF("<red>Constructed a %s disguise! Your reference is %s"),
    MADE_REF_EXAMPLE("<red>Example usage: /disguise %s"),
    META_CLICK_SHOW("<gold>Click to show %s</gold>"),
    META_INFO("<green>Name: %s, Watcher: %s, Index: %s, Type: %s, Serializer Type: %s, Default: %s"),
    META_NOT_FOUND("<red>No meta exists under that name!"),
    META_VALUES("<blue>Metas: <dark_aqua>"),
    META_VALUES_NO_CLICK("<blue>Metas, use as param for more info: <dark_aqua>"),
    META_VALUE_SEPERATOR("<aqua>, </aqua>"),
    MODS_LIST("<dark_green>%s has the mods:<aqua> %s"),
    NORMAL_PERM_CHECK_FAIL("<red>Normal permission check, fail."),
    NORMAL_PERM_CHECK_SUCCESS("<gold>Normal permission check, success."),
    NOT_DISGUISED("<red>You are not disguised!"),
    NOT_DISGUISED_FAIL("<red>%s not disguised!"),
    NOT_DISGUISED_SAVE_DISGUISE("<red>You are not disguised! To save a disguise in disguises.yml, you must be disguised!"),
    NOT_NUMBER("<red>Error! %s is not a number"),
    NO_CONSOLE("<red>You may not use this command from the console!"),
    NO_DISGUISES_IN_USE("<red>There are no disguises in use!"),
    NO_MODS("<red>%s is not using any mods!"),
    NO_MODS_LISTENING("<red>This server is not listening for mods!"),
    NO_PERM("<red>You are forbidden to use this command."),
    NO_PERMISSION_VIEW_SELF("<red>You do not have the permission to view self disguises!"),
    NO_PERMS_USE_OPTIONS("<red>Ignored %s methods you do not have permission to use. Add 'show' to view unusable methods."),
    NO_PERM_DISGUISE("<red>You do not have permission for that disguise!"),
    OWNED_BY("<gold>Plugin registered to '%%__USER__%%'!"),
    PARSE_BLOCK_STATE_ILLEGAL_BLOCK("<red>Error! <green>%s</green> can not be parsed to a valid block state!"),
    PARSE_BLOCK_STATE_UNKNOWN_BLOCK("<red>Error! The block <green>%s</green> could not be found when parsing <green>%s</green>"),
    PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_KEY(
        "<red>Error! Unknown block data key <green>%s</green> was not found on block <green>%s</green> when parsing <green>%s</green>!"),
    PARSE_BLOCK_STATE_UNKNOWN_BLOCK_DATA_VALUE(
        "<red>Error! Unknown block data value <green>%s</green> for key <green>%s</green> on block <green>%s</green> when parsing " +
            "<green>%s</green>!"),
    PARSE_BLOCK_STATE_UNKNOWN_BLOCK_SYNTAX(
        "<red>Error! Invalid <green>block_data<dark_green>[</dark_green>key<dark_green>=</dark_green>value<dark_green>," +
            "</dark_green>key<dark_green>=</dark_green>value<dark_green>]</dark_green></green> syntax was encountered when parsing " +
            "<green>%s</green>!"),
    PARSE_CANT_DISG_UNKNOWN("<red>Error! You cannot disguise as <green>Unknown!</green>>"),
    PARSE_CANT_LOAD("<red>Error! This disguise couldn't be loaded!"),
    PARSE_CANT_LOAD_DETAILS("<red>Error! This disguise couldn't be loaded! Tried to parse <green>%s</green> for <green>%s</green>"),
    PARSE_COLOR("<red>Expected <green>Color(3 numbers or color name)</green>, received <green>%s</green> instead</red>"),
    PARSE_DISG_NO_EXIST("<red>Error! The disguise <green>%s</green> doesn't exist!"),
    PARSE_DISPLAY_BRIGHTNESS("<red>Expected <green>(Block Light),(Sky Light)</green> from 0-15, received <green>%s</green> instead"),
    PARSE_EXPECTED_RECEIVED("<red>Expected <green>%s</green>, received <green>%s</green> instead for <green>%s"),
    PARSE_INVALID_ANIMATION("<red>Error! The animation <green>%s</green> does not exist!</red>"),
    PARSE_INVALID_TIME("<red>Error! <green>%s</green> is not a valid time! Use s,m,h,d or secs,mins,hours,days"),
    PARSE_INVALID_TIME_SEQUENCE("<red>Error! <green>%s</green> is not a valid time! Do amount then time, eg. 4min10sec"),
    PARSE_MISMATCHED_ANIMATION("<red>Error! Animation <green>%s</green> is not compatible with that disguise!</red>"),
    PARSE_NO_ARGS("No arguments defined"),
    PARSE_NO_OPTION_VALUE("<red>No value was given for the method %s"),
    PARSE_NO_PERM_NAME("<red>Error! You don't have permission to use that name!"),
    PARSE_NO_PERM_PARAM("<red>Error! You do not have permission to use the parameter %s on the %s disguise!"),
    PARSE_NO_PERM_REF("<red>You do not have permission to use disguise references!"),
    PARSE_NO_REF("<red>Cannot find a disguise under the reference %s"),
    PARSE_OPTION_NA("<red>Cannot find the method '%s'"),
    PARSE_PARTICLE_BLOCK("<red>Expected <green>%s:Material</green>, received <green>%s</green> instead"),
    PARSE_PARTICLE_COLOR("<red>Expected <green>%s:Color(3 numbers, 1 number, or color name)</green>, received <green>%s</green> instead"),
    PARSE_PARTICLE_DUST(
        "<red>Expected <green>%s:Size(Optional Number),Color(3 numbers or red/blue/etc)</green>, received <green>%s</green> instead"),
    PARSE_PARTICLE_DUST_TRANSITION(
        "<red>Expected <green>%s:Size(Optional),Color(3 numbers or red/blue/etc),Color(Same)</green>, received <green>%s</green> instead"),
    PARSE_PARTICLE_ITEM("<red>Expected <green>%s:Material,Amount?,Glow?<red>, received <green>%s</green> instead"),
    PARSE_PARTICLE_SHRIEK("<red>Expected <green>%s:Delay(number.0)</green>, received <green>%s</green> instead"),
    PARSE_PARTICLE_SHULK_CHARGE("<red>Expected <green>%s:Roll(number.0)</green>, received <green>%s</green> instead"),
    PARSE_PARTICLE_TRAIL(
        "<red>Expected <green>%s:Target(3 numbers),Color(3 numbers or red/blue/etc)</green> eg <green>X,Y,Z:RED</green>, received " +
            "<green>%s</green> instead"),
    PARSE_PARTICLE_VIBRATION(
        "<red>Expected <green>%s:SourceBlockX,SourceBlockY,SourceBlockZ,(Optional: StartBlockX,StartBlockY,StartBlockZ),Ticks</green>, " +
            "received <green>%s</green> instead"),
    PARSE_SUPPLY_PLAYER("<red>Error! You need to give a player name!"),
    PARSE_TOO_MANY_ARGS("<red>Error! %s doesn't know what to do with %s!"),
    PARSE_USE_SECOND_NUM("<red>Error! Only the disguises %s and %s uses a second number!"),
    PLEASE_WAIT("<gray>Please wait..."),
    REF_TOO_MANY(
        "<red>Failed to store the reference, too many cloned disguises. Please raise the maximum cloned disguises, or lower the time they" +
            " last"),
    RELOADED_CONFIG("<green>[LibsDisguises] Reloaded config."),
    SAVE_DISG_HELP_1("<green>The <DisguiseName> is what the disguise will be called in Lib's Disguises"),
    SAVE_DISG_HELP_2("<green>/savedisguise <DisguiseName> - If you don't provide arguments, it'll try make a disguise from your" +
        " current disguise. This will not work if you are not disguised!"),
    SAVE_DISG_HELP_3("<green>/savedisguise <DisguiseName> <Arguments>"),
    SAVE_DISG_HELP_4("<green>Your arguments need to be as if you're using /disguise. So '/disguise player Notch setsneaking' - " +
        "Means '/savedisguise Notch player Notch setsneaking'"),
    SAVE_DISG_HELP_5("<green>Remember! You can upload your own skins, then reference those skins!"),
    SAVE_DISG_HELP_6(
        "<green>If you are using setSkin, you can append :slim to your skin path to get the slim Alex model. So myskin.png:slim"),
    SELF_DISGUISE_HIDDEN("<green>Self disguise hidden as it's too tall.."),
    SKIN_API_BAD_FILE("<red>Invalid file provided! Please ensure it is a valid .png skin!"),
    SKIN_API_BAD_FILE_NAME("<red>Invalid file name provided! File not found!"),
    SKIN_API_FAIL("<red>Unexpected error while accessing mineskin.org, please try again"),
    SKIN_API_FAIL_CODE("<red>Error! Mineskin gave code %s with message %s"),
    SKIN_API_FAIL_CODE_EXCEPTIONAL("<red>Error! Mineskin gave a response code of %s and message %s"),
    SKIN_API_FAIL_TOO_FAST("<red>Too many requests accessing mineskin.org, please slow down!%s"),
    SKIN_API_IMAGE_TIMEOUT("<red>Error! mineskin.org took too long to connect! Is your image valid?"),
    SKIN_API_INVALID_NAME("<red>Invalid name/file/uuid provided!"),
    SKIN_API_IN_USE("<red>mineskin.org is currently in use, please try again."),
    SKIN_API_SUGGEST_KEY("<red>Try setting up a MineSkin API key for improved access!"),
    SKIN_API_TIMEOUT("<red>Took too long to connect to mineskin.org!"),
    SKIN_API_TIMEOUT_API_KEY_ERROR("<red>Error! Took too long to connect to mineskin.org! Is the API Key correct?"),
    SKIN_API_TIMEOUT_ERROR("<red>Error! Took too long to connect to mineskin.org!"),
    SKIN_API_TIMER("<red>mineskin.org can be used again in %s seconds."),
    SKIN_API_TOO_MANY_FAILURES(
        "<red>Too many failures when trying to resolve skin for <light_purple>%s</light_purple>, to prevent backend spam you will not be " +
            "able to make" + " any more requests until" +
            " a hour has passed from your last failed request. Try /grabskin if you need to test."),
    SKIN_API_TOO_MANY_FAILURES_NON_PLAYER(
        "<red>Too many failures when trying to resolve skin for <light_purple>%s</light_purple>, Lib's Disguises will not attempt to grab" +
            " this skin. " + "This timeout is only for that specific skin and will increase with every failure."),
    SKIN_API_USING_EXISTING_NAME("<gray>Found a saved skin under that name locally! Using that!"),
    SKIN_API_USING_FILE("<gray>File provided and found, now attempting to upload to mineskin.org"),
    SKIN_API_USING_NAME("<gray>Determined to be player name, now attempting to validate and connect to mineskin.org"),
    SKIN_API_USING_URL("<gray>Url provided, now attempting to connect to mineskin.org"),
    SKIN_API_USING_UUID("<gray>UUID successfully parsed, now attempting to connect to mineskin.org"),
    SKIN_API_UUID_3("<red>Using account with UUID version 3. If skin is incorrect, try change 'UUIDVersion' in protocol.yml" +
        " to 3. If skin is still incorrect and you did not purchase Minecraft, this cannot be fixed."),
    SKIN_DATA("<green>Skin Data: <yellow>%s"),
    SWITCH_WORLD_DISGUISE_REMOVED("<red>Disguise removed as you've switched worlds!"),
    TARGET_NOT_DISGUISED("<red>That entity is not disguised!"),
    TOO_FAST("<red>You are using the disguise command too fast!"),
    UNDISG("<red>You are no longer disguised"),
    UNDISG_PLAYER("<red>%s is no longer disguised"),
    UNDISG_PLAYER_HELP("<red>/undisguiseplayer <Name>"),
    UNDISG_SELECTOR_HELP("<red>/undisguiseselector <Entity Selector></red> - <green>Undisguise entities via entity selector</green>"),
    UNDISRADIUS("<red>Successfully undisguised %s entities!"),
    UNDISG_SELECTOR_SUCCESS("<red>Successfully undisguised %s entities!</red>"),
    UND_ENTITY("<red>Right click a disguised entity to undisguise them!"),
    UNRECOGNIZED_DISGUISE_TYPE("<red>Unrecognised DisguiseType %s"),
    UPDATE_ALREADY_DOWNLOADED("<red>That update has already been downloaded!"),
    UPDATE_FAILED("<red>LibsDisguises update failed! Check console for errors."),
    UPDATE_HOW("<dark_aqua>Use <aqua>/libsdisgusies changelog</aqua> to see what changed, use <aqua>/libsdisguises update!" +
        "</aqua> to download the update!"),
    UPDATE_INFO("<dark_green>Lib's Disguises v%s, build %s, built %s and size %skb"),
    UPDATE_IN_PROGRESS("<dark_green>LibsDisguises is now downloading an update..."),
    UPDATE_ON_LATEST("<red>You are already on the latest version of LibsDisguises!"),
    UPDATE_READY(
        "<red>[LibsDisguises]</red> <dark_red>There is an update ready to be downloaded! You are using <red>v%s</red>, the new version is" +
            " <red>v%s</red>!"),
    UPDATE_READY_SNAPSHOT(
        "<red>[LibsDisguises]</red> <dark_red>There is a new build of Lib's Disguises! You are using <red>%s</red>, the latest build " +
            "is</dark_red> <red>#%s</red>!"),
    UPDATE_REQUIRED("<red>LibsDisguises requies an update check before it can give you that!"),
    UPDATE_SUCCESS("<dark_green>LibsDisguises update success! Restart server to update!"),
    USING_DEFAULT_CONFIG("<dark_green>Using the default config!</dark_green>"),
    VIEW_BAR_OFF("<green>Toggled disguised notify bar off!"),
    VIEW_BAR_ON("<green>Toggled disguised notify bar on!"),
    VIEW_SELF_OFF("<green>Toggled viewing own disguise off!"),
    VIEW_SELF_ON("<green>Toggled viewing own disguise on!"),
    VIEW_SELF_TALL_NOTE(
        "<green>Your disguise is too tall, self disguise is automatically disabled. You wouldn't be able to see anything otherwise!");

    private final String string;

    LibsMsg(String string) {
        this.string = string;
    }

    public String getVanillaFormat() {
        String raw = getRaw();

        for (ChatColor c : ChatColor.values()) {
            raw = raw.replace("<" + DisguiseUtilities.getName(c) + ">", "§" + c.getChar());
        }

        return raw;
    }

    public String getRaw() {
        return string;
    }

    public void send(CommandSender player, Object... strings) {
        DisguiseUtilities.sendMessage(player, this, strings);
    }

    @Deprecated
    public BaseComponent[] getBase(Object... strings) {
        return DisguiseUtilities.getColoredChat(get(strings));
    }

    public Component getAdv(Object... strings) {
        return DisguiseUtilities.getAdventureChat(get(strings));
    }

    public void validateArgCount(String... args) {
        int matches = StringUtils.countMatches(getRaw(), "%s");

        if (matches == args.length) {
            return;
        }

        if (Bukkit.getServer() == null) {
            throw new IllegalArgumentException(
                "Error for " + name() + ", incorrect arg count supplied. Expected " + matches + " args, received " + args.length);
        } else {
            LibsDisguises.getInstance().getLogger().severe("Mismatch in messages, incorrect parameters supplied for " + name() +
                ". Please inform plugin author if not using translations.");
        }
    }

    @Deprecated
    public String get(Object... strings) {
        int matches = StringUtils.countMatches(getRaw(), "%s");

        if (matches != strings.length) {
            LibsDisguises.getInstance().getLogger().severe("Mismatch in messages, incorrect parameters supplied for " + name() +
                ". Please inform plugin author if not using translations.");
        }

        String trans = TranslateType.MESSAGES.get(this);

        if (trans.isEmpty() || strings.length == 0) {
            return trans;
        }

        return String.format(trans, strings);
    }

    public String toString() {
        if (LibsDisguises.getInstance() == null) {
            return name();
        }

        throw new IllegalStateException(getClass().getSimpleName() + ".toString() cannot be used, was invoked on " + name());
    }
}
