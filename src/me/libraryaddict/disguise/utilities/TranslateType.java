package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.DisguiseConfig;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by libraryaddict on 10/06/2017.
 */
public enum TranslateType {
    DISGUISE("disguises"), MESSAGE("messages"), METHOD_PARAM("disguise_options"), METHOD("disguise_option_parameters");
    private File file;
    private HashMap<String, String> translated = new HashMap<>();

    TranslateType(String fileName) {
        file = new File("plugins/LibsDisguises/Translations", fileName + ".yml");
        reload();
    }

    public static void reloadTranslations() {
        for (TranslateType type : values()) {
            type.reload();
        }

        TranslateFiller.fillConfigs();
    }

    private void reload() {
        if (!LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations())
            return;

        translated.clear();

        if (!file.exists())
            return;

        System.out.println("[LibsDisguises] Loading translations: " + name());
        YamlConfiguration config = new YamlConfiguration();
        config.options().pathSeparator(Character.toChars(0)[0]);

        try {
            config.load(file);

            for (String key : config.getKeys(false)) {
                String value = config.getString(key);

                if (value == null)
                    System.err.println("Translation for " + name() + " has a null value for the key '" + key + "'");
                else
                    translated.put(key, ChatColor.translateAlternateColorCodes('&', value));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        return file;
    }

    private void save(String message, String comment) {
        if (translated.containsKey(message))
            return;

        translated.put(message, message);

        message = StringEscapeUtils.escapeJava(message.replaceAll(ChatColor.COLOR_CHAR + "", "&"));

        try {
            boolean exists = file.exists();

            if (!exists) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(getFile(), true);

            if (!exists)
                writer.write("# To use translations in Lib's Disguises, you must have the purchased plugin\n");

            writer.write("\n" + (comment != null ? "# " + comment + "\n" :
                    "") + "\"" + message + "\": \"" + message + "\"\n");

            writer.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String reverseGet(String translated) {
        translated = translated.toLowerCase();

        for (Map.Entry<String, String> entry : this.translated.entrySet()) {
            if (!Objects.equals(entry.getValue().toLowerCase(), translated))
                continue;

            return entry.getKey();
        }

        return translated;
    }

    public String get(String msg) {
        if (this != TranslateType.MESSAGE)
            throw new IllegalArgumentException("Can't set no comment for '" + msg + "'");

        return get(msg, null);
    }

    public String get(String msg, String comment) {
        if (!LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations())
            return msg;

        String toReturn = translated.get(msg);

        if (toReturn != null)
            return toReturn;

        save(msg, comment);

        return msg;
    }
}
