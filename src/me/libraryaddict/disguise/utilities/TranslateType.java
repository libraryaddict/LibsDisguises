package me.libraryaddict.disguise.utilities;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

/**
 * Created by libraryaddict on 10/06/2017.
 */
public enum TranslateType {
    DISGUISE("disguise_names"), MESSAGE("messages"), METHOD_PARAM("option_names"), METHOD("disguise_options");
    private File file;
    private YamlConfiguration config;

    TranslateType(String fileName) {
        file = new File("translate", fileName + ".yml");
        reload();
    }

    public void reload() {
        if (!file.exists())
            file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    private YamlConfiguration getConfig() {
        return config;
    }

    private File getFile() {
        return file;
    }

    public void save(String message, String comment) {
        message = StringEscapeUtils.escapeJson(message);

        if (getConfig().contains(message))
            return;

        try {
            PrintWriter writer = new PrintWriter(getFile());
            writer.write((comment != null ? "# " + comment + "\n" : "") + message + ": " + message + "\n");

            writer.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String reverseGet(String translated) {
        translated = StringEscapeUtils.unescapeJson(translated).toLowerCase();

        for (Map.Entry<String, Object> entry : getConfig().getValues(false).entrySet()) {
            if (!Objects.equals(entry.getValue().toString().toLowerCase(), translated))
                continue;

            return entry.getKey();
        }

        return translated;
    }

    public String get(String message) {
        if (this != TranslateType.MESSAGE)
            throw new IllegalArgumentException("Can't set no comment for '" + message + "'");

        return get(message, null);
    }

    public String get(String message, String comment) {
        String msg = getConfig().getString(StringEscapeUtils.escapeJson(message));

        if (msg != null)
            return msg;

        save(message, comment);

        return message;
    }
}
