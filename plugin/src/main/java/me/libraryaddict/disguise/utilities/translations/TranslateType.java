package me.libraryaddict.disguise.utilities.translations;

import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
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
    private final LinkedHashMap<String, String> translated = new LinkedHashMap<>();
    private final HashMap<String, Boolean> toDeDupe = new HashMap<>();
    private OutputStreamWriter writer;
    private int written;

    TranslateType(String fileName) {
        if (LibsDisguises.getInstance() == null) {
            return;
        }

        file = new File(LibsDisguises.getInstance().getDataFolder(), "Translations/" + fileName + ".yml");
    }

    public static void refreshTranslations() {
        for (TranslateType type : values()) {
            type.loadTranslations();
        }

        TranslateFiller.fillConfigs();

        if (!LibsPremium.isPremium() && DisguiseConfig.isUseTranslations()) {
            DisguiseUtilities.getLogger().severe("You must purchase the plugin to use translations!");
        }
    }

    protected void saveTranslations() {
        // First remove translations which are not different from each other. We don't need to store messages that
        // were not translated.

        Iterator<Map.Entry<String, String>> itel = translated.entrySet().iterator();

        while (itel.hasNext()) {
            Map.Entry<String, String> entry = itel.next();

            if (!entry.getKey().equals(entry.getValue())) {
                continue;
            }

            itel.remove();
        }

        // Close the writer
        try {
            if (writer != null) {
                writer.close();
                writer = null;

                DisguiseUtilities.getLogger().info("Saved " + written + " translations that were not in " + getFile().getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        deDupeMessages();
    }

    private void loadTranslations() {
        translated.clear();

        if (!getFile().exists()) {
            DisguiseUtilities.getLogger().info("Translations for " + name() + " missing! Saving..");
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.options().pathSeparator(Character.toChars(0)[0]);

        int diff = 0;

        try {
            config.load(getFile());

            for (String key : config.getKeys(false)) {
                String value = config.getString(key);

                if (value == null) {
                    DisguiseUtilities.getLogger().severe("Translation for " + name() + " has a null value for the key '" + key + "'");
                } else {
                    toDeDupe.put(key, true);

                    String newKey = DisguiseUtilities.translateAlternateColorCodes(key);
                    translated.put(newKey, DisguiseUtilities.translateAlternateColorCodes(value));

                    if (!newKey.equals(translated.get(newKey))) {
                        diff++;
                        translated.put(newKey,
                            translated.get(newKey) + (diff % 3 == 0 || LibsMsg.OWNED_BY.getRaw().contains("Plugin registered to '") ? "" : " "));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (LibsPremium.isPremium() && DisguiseConfig.isUseTranslations()) {
            DisguiseUtilities.getLogger().info("Loaded " + translated.size() + " translations for " + name() + " with " + diff + " changed");
        } else if (diff > 0 && !DisguiseConfig.isUseTranslations()) {
            DisguiseUtilities.getLogger().info(
                "Translations are disabled in libsdisguises.yml, but you modified " + diff + " messages in the translations for " + name() +
                    ". Is this intended?");
        }
    }

    private File getFile() {
        return file;
    }

    public void save(String msg) {
        if (this != TranslateType.MESSAGES) {
            throw new IllegalArgumentException("Can't set no comment for '" + msg + "'");
        }

        save(msg, null);
    }

    public void save(String message, String comment) {
        save(null, message, comment);
    }

    public void save(LibsMsg orig, String rawMessage, String comment) {
        toDeDupe.put(StringEscapeUtils.escapeJava(rawMessage.replace("§", "&")), false);

        if (translated.containsKey(rawMessage)) {
            return;
        }

        String value = rawMessage;

        if (orig != null) {
            String vanilla = orig.getVanillaFormat();

            if (translated.containsKey(vanilla) && !vanilla.equals(rawMessage) && !translated.get(vanilla).equals(vanilla)) {
                value = translated.get(vanilla);

                for (ChatColor color : ChatColor.values()) {
                    value = value.replace("§" + color.getChar(), "<" + color.name().toLowerCase(Locale.ROOT) + ">");
                }
            }
        }

        translated.put(rawMessage, value);

        try {
            boolean exists = getFile().exists();

            if (!exists) {
                getFile().getParentFile().mkdirs();
                getFile().createNewFile();
            }

            if (writer == null) {
                writer = new OutputStreamWriter(new FileOutputStream(getFile(), true), StandardCharsets.UTF_8);

                if (!exists) {
                    writer.write("# To use translations in Lib's Disguises, you must have the purchased plugin\n");

                    if (this == TranslateType.MESSAGES) {
                        writer.write("# %s is where text is inserted, look up printf format codes if you're interested\n");
                    }

                    writer.write("# To translate, follow this example 'Original Message': 'My New Message'\n# The Original" +
                        " Message is used as a yaml config key to get your new message!");
                    writer.write("\n# To use hex color codes, use <#hexcolor> where hexcolor is the 6 char code");
                }
            }

            String sanitizedKey = StringEscapeUtils.escapeJava(rawMessage.replace("§", "&"));
            String sanitizedValue = StringEscapeUtils.escapeJava(value.replace("§", "&"));

            writer.write("\n" + (comment != null ? "# " + comment + "\n" : "") + "\"" + sanitizedKey + "\": \"" + sanitizedValue + "\"\n");
            written++;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deDupeMessages() {
        try {
            if (!getFile().exists()) {
                return;
            }

            ArrayList<String> disguiseText = new ArrayList(Arrays.asList(new String(Files.readAllBytes(getFile().toPath())).split("\r?\n")));
            int dupes = 0;
            int outdated = 0;

            for (Map.Entry<String, Boolean> entry : toDeDupe.entrySet()) {
                String s = entry.getKey();
                boolean isOutdated = entry.getValue();
                boolean removedFirst = isOutdated;

                String str = "\"" + s + "\": \"" + s + "\"";

                for (int i = 0; i < disguiseText.size(); i++) {
                    if (!disguiseText.get(i).equals(str)) {
                        continue;
                    }

                    if (!removedFirst) {
                        removedFirst = true;
                        continue;
                    }

                    disguiseText.remove(i);

                    if (isOutdated) {
                        outdated++;
                    } else {
                        dupes++;
                    }

                    if (disguiseText.get(--i).startsWith("# Reference: ")) {
                        disguiseText.remove(i);
                    }

                    if (disguiseText.size() <= i || !disguiseText.get(i).isEmpty()) {
                        continue;
                    }

                    disguiseText.remove(i);
                }
            }

            if (dupes + outdated > 0) {
                DisguiseUtilities.getLogger().info("Removed " + dupes + " duplicate and " + outdated + " outdated translations from " + getFile().getName() +
                    ", this was likely caused by a previous issue in the plugin");

                Files.write(getFile().toPath(), StringUtils.join(disguiseText, "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            toDeDupe.clear();
        }
    }

    public String reverseGet(String translated) {
        if (translated == null || !LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations()) {
            return translated;
        }

        String lowerCase = translated.toLowerCase(Locale.ENGLISH);

        for (Map.Entry<String, String> entry : this.translated.entrySet()) {
            if (!Objects.equals(entry.getValue().toLowerCase(Locale.ENGLISH), lowerCase)) {
                continue;
            }

            return entry.getKey();
        }

        return translated;
    }

    public String get(LibsMsg msg) {
        return get(msg.getRaw());
    }

    public String get(String msg) {
        if (msg == null || !LibsPremium.isPremium() || !DisguiseConfig.isUseTranslations()) {
            return msg;
        }

        String toReturn = translated.get(msg);

        return toReturn == null ? msg : toReturn;
    }
}
