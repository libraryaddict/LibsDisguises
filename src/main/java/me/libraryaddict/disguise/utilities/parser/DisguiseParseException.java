package me.libraryaddict.disguise.utilities.parser;

import me.libraryaddict.disguise.utilities.translations.LibsMsg;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public class DisguiseParseException extends Exception {
    private static final long serialVersionUID = 1276971370793124510L;

    public DisguiseParseException() {
        super();
    }

    public DisguiseParseException(LibsMsg message, String... params) {
        super(message.get((Object[]) params));
    }

    public DisguiseParseException(String message) {
        super(message);
    }

    public DisguiseParseException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DisguiseParseException(Throwable throwable) {
        super(throwable);
    }
}
