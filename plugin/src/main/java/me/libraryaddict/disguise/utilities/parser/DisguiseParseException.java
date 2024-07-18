package me.libraryaddict.disguise.utilities.parser;

import lombok.Getter;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.bukkit.command.CommandSender;

@Getter
public class DisguiseParseException extends Exception {
    private static final long serialVersionUID = 1276971370793124510L;
    private LibsMsg msg;
    private String[] params;

    public DisguiseParseException() {
        super();
    }

    public DisguiseParseException(LibsMsg message, String... params) {
        super(message.get((Object[]) params));

        if (message != null) {
            message.validateArgCount(params);
        }

        this.msg = message;
        this.params = params;
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

    public void send(CommandSender sender) {
        if (this.getMsg() == null) {
            return;
        }

        this.msg.send(sender, (Object[]) params);
    }
}
