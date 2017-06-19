package me.libraryaddict.disguise.utilities;

/**
 * Created by libraryaddict on 15/06/2017.
 */
public enum LibsMessages {
    // Format being CLASS_STRING. So no perm = DISG_COMMAND_NO_PERM. Or DISG_PARSE_NO_PERM_OPTION
    TEST("This is a test string");

    private String string;

    LibsMessages(String string) {
        this.string = string;
    }

    public String get(String... strings) {
        if (strings.length == 0)
            return TranslateType.MESSAGE.get(string);

        return String.format(TranslateType.MESSAGE.get(string), (Object[]) strings);
    }
}
