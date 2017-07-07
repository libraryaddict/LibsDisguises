package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.DisguiseConfig;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by libraryaddict on 10/06/2017.
 */
public enum TranslateType {
    DISGUISES("disguises"),
    MESSAGES("messages"),
    DISGUISE_OPTIONS("disguise_options"),
    DISGUISE_OPTIONS_PARAMETERS("disguise_option_parameters");

    private File file;
    private HashMap<String, String> translated = new HashMap<>();
    private FileWriter writer;

    TranslateType(String fileName) {
        file = new File("plugins/LibsDisguises/Translations", fileName + ".yml");
    }

    public static void reloadTranslations() {
        for (TranslateType type : values()) {
            type.reload();
        }

        if (!LibsPremium.isPremium() && DisguiseConfig.isUseTranslations()) {
            System.out.println("[LibsDisguises] You must purchase the plugin to use translations!");
        }

        TranslateFiller.fillConfigs();
    }

    protected void removeDuplicates() {
        Iterator<Map.Entry<String, String>> itel = translated.entrySet().iterator();

        while (itel.hasNext()) {
            Map.Entry<String, String> entry = itel.next();

            if (!entry.getKey().equals(entry.getValue()))
                continue;

            itel.remove();
        }

        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wipeTranslations() {
        translated.clear();
    }

    private void reload() {
        wipeTranslations();

        if (LibsPremium.isPremium() && DisguiseConfig.isUseTranslations()) {
            System.out.println("[LibsDisguises] Loading translations: " + name());
        }

        if (!getFile().exists())
            return;

        YamlConfiguration config = new YamlConfiguration();
        config.options().pathSeparator(Character.toChars(0)[0]);

        try {
            config.load(getFile());

            for (String key : config.getKeys(false)) {
                String value = config.getString(key);

                if (value == null)
                    System.err.println("Translation for " + name() + " has a null value for the key '" + key + "'");
                else {
                    translated.put(ChatColor.translateAlternateColorCodes('&', key),
                            ChatColor.translateAlternateColorCodes('&', value));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        return file;
    }

    public void save(String msg) {
        if (this != TranslateType.MESSAGES)
            throw new IllegalArgumentException("Can't set no comment for '" + msg + "'");

        save(msg, null);
    }

    public void save(String message, String comment) {
        if (translated.containsKey(message)) {
            return;
        }

        translated.put(message, message);

        message = StringEscapeUtils.escapeJava(message.replaceAll(ChatColor.COLOR_CHAR + "", "&"));

        try {
            boolean exists = getFile().exists();

            if (!exists) {
                getFile().getParentFile().mkdirs();
                getFile().createNewFile();
            }

            if (writer == null) {
                writer = new FileWriter(getFile(), true);

                if (!exists) {
                    writer.write("# To use translations in Lib's Disguises, you must have the purchased plugin\n");

                    if (this == TranslateType.MESSAGES) {
                        writer.write(
                                "# %s is where text is inserted, look up printf format codes if you're interested\n");
                    }
                }
            }

            writer.write("\n" + (comment != null ? "# " + comment + "\n" :
                    "") + "\"" + message + "\": \"" + message + "\"\n");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String reverseGet(String translated) {
        if (translated == null || !LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations())
            return translated;

        String lowerCase = translated.toLowerCase();

        for (Map.Entry<String, String> entry : this.translated.entrySet()) {
            if (!Objects.equals(entry.getValue().toLowerCase(), lowerCase))
                continue;

            return entry.getKey();
        }

        return translated;
    }

    public String get(String msg) {
        if (msg == null || !LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations())
            return msg;

        String toReturn = translated.get(msg);

        return toReturn == null ? msg : toReturn;
    }
}
