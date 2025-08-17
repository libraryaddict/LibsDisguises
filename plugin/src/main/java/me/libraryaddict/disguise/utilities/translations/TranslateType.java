package me.libraryaddict.disguise.utilities.translations;

import lombok.AccessLevel;
import lombok.Getter;
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

public enum TranslateType {
    DISGUISES("disguises", "This file covers the name of the entity types"),
    MESSAGES("messages",
        "This file covers the messages that are sent to the player, mostly via chat.\n# To use hex color codes, use <#hexcolor> where " +
            "hexcolor is the 6 char code\n# %s is where text is inserted, look up printf format codes if you are interested."),
    DISGUISE_OPTIONS("disguise_options", "This file covers the names of the disguise options, such as 'setBurning' and 'setSprinting'"),
    DISGUISE_OPTIONS_PARAMETERS("disguise_option_parameters",
        "This file covers the names of the disguise option parameters, such as 'true' + 'false' being remapped to 'positive' + 'negative'"),
    DISGUISE_ANIMATIONS("disguise_animations",
        "# You can read more about what these animations represent here: https://minecraft.wiki/w/Java_Edition_protocol/Entity_statuses");

    @Getter(AccessLevel.PRIVATE)
    private File file;
    @Getter(AccessLevel.PRIVATE)
    private String typeDescriptor;
    private final LinkedHashMap<String, String> translated = new LinkedHashMap<>();
    private final HashMap<String, Boolean[]> toDeDupe = new HashMap<>();
    private OutputStreamWriter writer;
    private int written;

    TranslateType(String fileName, String desciptor) {
        if (LibsDisguises.getInstance() == null) {
            return;
        }

        file = new File(LibsDisguises.getInstance().getDataFolder(), "Translations/" + fileName + ".yml");
        typeDescriptor = desciptor;
    }

    public static void refreshTranslations() {
        for (TranslateType type : values()) {
            type.loadTranslations();
        }

        TranslateFiller.fillConfigs();

        if (!LibsPremium.isPremium() && DisguiseConfig.isUseTranslations()) {
            LibsDisguises.getInstance().getLogger().severe("You must purchase the plugin to use translations!");
        }
    }

    void saveTranslations() {
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

                LibsDisguises.getInstance().getLogger().info("Saved " + written + " translations that were not in " + getFile().getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        deDupeMessages();
    }

    private void loadTranslations() {
        translated.clear();

        if (!getFile().exists()) {
            LibsDisguises.getInstance().getLogger().info("Translations for " + name() + " missing! Saving..");
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
                    LibsDisguises.getInstance().getLogger()
                        .severe("Translation for " + name() + " has a null value for the key '" + key + "'");
                    continue;
                }

                addDedupe(key, true);

                String newKey = DisguiseUtilities.translateAlternateColorCodes(key);
                translated.put(newKey, DisguiseUtilities.translateAlternateColorCodes(value));

                if (!newKey.equals(translated.get(newKey))) {
                    diff++;
                    translated.put(newKey, translated.get(newKey) +
                        (diff % 3 == 0 || LibsMsg.OWNED_BY.getRaw().contains("Plugin registered to '") ? "" : " "));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (LibsPremium.isPremium() && DisguiseConfig.isUseTranslations()) {
            LibsDisguises.getInstance().getLogger()
                .info("Loaded " + translated.size() + " translations for " + name() + " with " + diff + " changed");
        } else if (diff > 0 && !DisguiseConfig.isUseTranslations()) {
            LibsDisguises.getInstance().getLogger().info(
                "Translations are disabled in libsdisguises.yml, but you modified " + diff + " messages in the translations for " + name() +
                    ". Is this intended?");
        }
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
        if (rawMessage.trim().isEmpty() && LibsDisguises.getInstance().isJenkins()) {
            LibsDisguises.getInstance().getLogger()
                .info("Skipping a translate type as it's empty, for " + name() + " with comment " + comment);
            return;
        }

        addDedupe(StringEscapeUtils.escapeJava(rawMessage.replace("§", "&")), false);

        if (translated.containsKey(rawMessage)) {
            return;
        }

        String value = rawMessage;

        if (orig != null) {
            String vanilla = orig.getVanillaFormat();

            if (translated.containsKey(vanilla) && !vanilla.equals(rawMessage) && !translated.get(vanilla).equals(vanilla)) {
                value = translated.get(vanilla);

                for (ChatColor color : ChatColor.values()) {
                    value = value.replace("§" + color.getChar(), "<" + DisguiseUtilities.getName(color) + ">");
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
                    writer.write("# To translate, follow this example 'Original Message': 'My New Message'\n# The Original" +
                        " Message is used as a yaml config key to get your new message!\n");

                    if (this.getTypeDescriptor() != null) {
                        writer.write("\n# " + this.getTypeDescriptor() + "\n");
                    }
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

    private void addDedupe(String text, boolean isOutdated) {
        // The first boolean in this array of 2, is "outdated"
        // The second is if we've already removed it or not
        // If it's outdated, then we claim we already removed the expected one, so it'll actually remove the outdated one
        toDeDupe.put("\"" + text + "\": \"" + text + "\"", new Boolean[]{isOutdated, isOutdated});
    }

    private void deDupeMessages() {
        try {
            if (!getFile().exists()) {
                return;
            }

            ArrayList<String> disguiseText =
                new ArrayList(Arrays.asList(new String(Files.readAllBytes(getFile().toPath())).split("\r?\n")));
            int dupes = 0;
            int outdated = 0;

            for (int i = 0; i < disguiseText.size(); i++) {
                Boolean[] bools = toDeDupe.get(disguiseText.get(i));

                if (bools == null) {
                    continue;
                }

                // If we need to remove the first occurance
                if (!bools[1]) {
                    bools[1] = true;
                    continue;
                }

                disguiseText.remove(i);

                // If this was outdated
                if (bools[0]) {
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

            String[] removedLinesPrefixes =
                new String[]{"# Found in the disguise options for ", "# Used for the disguise option ", "# Name for the ", "# Reference: ",
                    "# A disguise option name, has description ", "# Description for the disguise option "};
            String lastNonEmptyLine = "";

            for (int i = 0; i < disguiseText.size() - 1; i++) {
                String line = disguiseText.get(i);

                // If line isn't a comment
                if (!line.startsWith("# ")) {
                    continue;
                }

                // The next non-empty line
                String nextLine = null;

                // Loop over the lines ahead, find the first non-empty line
                for (int a = i + 1; a < disguiseText.size(); a++) {
                    nextLine = disguiseText.get(a);

                    if (nextLine.isEmpty()) {
                        nextLine = null;
                        continue;
                    }

                    break;
                }

                // If next line not found, then we reached end of file
                if (nextLine == null) {
                    break;
                }

                // If next line is not a comment, then we are not removing anything
                if (!nextLine.startsWith("# ")) {
                    continue;
                }

                boolean genericLine1 = false;
                boolean genericLine2 = false;

                for (String s : removedLinesPrefixes) {
                    if (!genericLine1) {
                        genericLine1 = line.startsWith(s);
                    }

                    if (!genericLine2) {
                        genericLine2 = nextLine.startsWith(s);
                    }
                }

                // If either line was not auto-generated, then it is either a config field, or a custom comment
                if (!genericLine1 || !genericLine2) {
                    continue;
                }

                // Remove the current line
                disguiseText.remove(i--);
                dupes++;
            }

            if (dupes + outdated > 0) {
                LibsDisguises.getInstance().getLogger().info(
                    "Removed " + dupes + " duplicate and " + outdated + " outdated translations from " + getFile().getName() +
                        ", this was likely caused by a previous issue in the plugin or switching between MC versions");

                // Join list into a string, remove duplicate empty lines
                String toWrite = StringUtils.join(disguiseText, "\n").replaceAll("\n{3,}", "\n\n");
                Files.write(getFile().toPath(), toWrite.getBytes());
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
