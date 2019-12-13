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
    CLICK_TIMER(ChatColor.RED + "Right click a entity in the next %s seconds to grab the disguise reference!"),
    CLONE_HELP1(ChatColor.DARK_GREEN +
            "Right click a entity to get a disguise reference you can pass to other disguise commands!"),
    CLONE_HELP2(ChatColor.DARK_GREEN +
            "Security note: Any references you create will be available to all players able to use disguise " +
            "references."),
    CLONE_HELP3(ChatColor.DARK_GREEN + "/disguiseclone IgnoreEquipment" + ChatColor.DARK_GREEN + "(" + ChatColor.GREEN +
            "Optional" + ChatColor.DARK_GREEN + ")"),
    D_HELP1(ChatColor.DARK_GREEN + "Disguise another player!"),
    D_HELP3(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> player <Name>"),
    D_HELP4(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> <DisguiseType> <Baby>"),
    D_HELP5(ChatColor.DARK_GREEN + "/disguiseplayer <PlayerName> <Dropped_Item/Falling_Block> <Id> <Durability>"),
    D_PARSE_NOPERM(ChatColor.RED + "You do not have permission to use the option %s"),
    DHELP_CANTFIND(ChatColor.RED + "Cannot find the disguise %s"),
    DHELP_HELP1(ChatColor.RED + "/disguisehelp <DisguiseType> " + ChatColor.GREEN +
            "- View the options you can set on a disguise. Add 'show' to reveal the options you don't have permission" +
            " to use"),
    DHELP_HELP2(ChatColor.RED + "/disguisehelp <DisguiseOption> " + ChatColor.GREEN + "- View information about the " +
            "disguise options such as 'RabbitType'"),
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
            ChatColor.GREEN + "Optional" + ChatColor.DARK_GREEN + ")> <Radius> <Disguise Options>")
            .replace("<", "<" + ChatColor.GREEN).replace(">", ChatColor.DARK_GREEN + ">")),
    DMODRADIUS_HELP3(ChatColor.DARK_GREEN + "See the DisguiseType's usable by " + ChatColor.GREEN +
            "/disguisemodifyradius DisguiseType"),
    DMODRADIUS_NEEDOPTIONS(ChatColor.RED + "You need to supply the disguise options as well as the radius"),
    DMODRADIUS_NEEDOPTIONS_ENTITY(
            ChatColor.RED + "You need to supply the disguise options as well as the radius and EntityType"),
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
    INVALID_CLONE(ChatColor.DARK_RED + "Unknown option '%s' - Valid options are 'IgnoreEquipment' 'DoSneakSprint' " +
            "'DoSneak' 'DoSprint'"),
    LIBS_RELOAD_WRONG(ChatColor.RED + "[LibsDisguises] Did you mean 'reload'?"),
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
            "Ignored %s options you do not have permission to use. Add 'show' to view unusable options."),
    OWNED_BY(ChatColor.GOLD + "Plugin registered to '%%__USER__%%'!"),
    NOT_DISGUISED(ChatColor.RED + "You are not disguised!"),
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
    PARSE_NO_OPTION_VALUE(ChatColor.RED + "No value was given for the option %s"),
    PARSE_NO_PERM_NAME(ChatColor.RED + "Error! You don't have permission to use that name!"),
    PARSE_NO_PERM_PARAM(
            ChatColor.RED + "Error! You do not have permission to use the parameter %s on the %s disguise!"),
    PARSE_NO_PERM_REF(ChatColor.RED + "You do not have permission to use disguise references!"),
    PARSE_NO_REF(ChatColor.RED + "Cannot find a disguise under the reference %s"),
    PARSE_OPTION_NA(ChatColor.RED + "Cannot find the option %s"),
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
    VIEW_SELF_OFF(ChatColor.GREEN + "Toggled viewing own disguise off!");

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

        return String.format(TranslateType.MESSAGES.get(getRaw()), (Object[]) strings);
    }

    public String toString() {
        throw new RuntimeException("Dont call this");
    }
}
