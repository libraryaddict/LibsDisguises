package me.libraryaddict.disguise.utilities.config;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by libraryaddict on 28/01/2021.
 */
public class DisguiseCommandConfig {
    @Getter
    @Setter
    public static class DisguiseCommand {
        private String name;
        private String description;
        private String permission;
        private List<String> aliases = new ArrayList<>();
        private boolean enabled;
    }

    private final File commandConfig = new File(LibsDisguises.getInstance().getDataFolder(), "configs/plugin-commands.yml");
    private final HashMap<String, DisguiseCommand> commands = new HashMap<>();
    private boolean modifyCommands = false;

    private void loadConfig() {
        if (!commandConfig.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(commandConfig);

        for (String name : config.getKeys(false)) {
            DisguiseCommand command = commands.get(name);

            if (!config.isConfigurationSection(name)) {
                continue;
            }

            if (command == null) {
                DisguiseUtilities.getLogger().warning("Config defines '" + name + "' as a command but that command doesn't exist?");
                continue;
            }

            if (!config.isConfigurationSection(name)) {
                DisguiseUtilities.getLogger().warning("Improper config for " + name);
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(name);

            if (!name.equals("libsdisguises")) {
                command.setEnabled(section.getBoolean("enabled", true));
                command.setName(section.getString("name", name));
                command.setPermission(section.getString("permission", command.getPermission()));
            }

            command.setDescription(section.getString("description", command.getDescription()));

            if (section.contains("aliases")) {
                command.setAliases(new ArrayList<>(section.getStringList("aliases")));
            }
        }

        modifyCommands = config.getBoolean("ModifyCommands", false);
    }

    private void saveConfig() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, DisguiseCommand> entry : commands.entrySet()) {
            ConfigurationSection section = config.createSection(entry.getKey());

            DisguiseCommand command = entry.getValue();

            if (!command.getName().equals("libsdisguises")) {
                section.set("name", command.getName());
                section.set("permission", command.getPermission());
                section.set("enabled", command.isEnabled());
            }

            section.set("description", command.getDescription());
            section.set("aliases", command.getAliases());
        }

        String configString = config.saveToString();

        configString = configString.replaceAll("\n([a-zA-Z])", "\n\n$1");

        String s =
            "# The following can be changed to modify how the disguise commands are registered\n# This will only work on server startup\nModifyCommands: " +
                modifyCommands + "\n\n" + configString;

        commandConfig.delete();

        try {
            commandConfig.getParentFile().mkdirs();

            commandConfig.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(commandConfig, "UTF-8")) {
            writer.write(s);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        loadPlugin();
        loadConfig();
        saveConfig();

        if (!modifyCommands) {
            return;
        }

        registerCommands();
    }

    private void registerCommands() {
        PluginDescriptionFile desc = LibsDisguises.getInstance().getDescription();
        Map<String, Map<String, Object>> newMap = new HashMap<>();

        for (DisguiseCommand command : commands.values()) {
            if (!command.isEnabled()) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            newMap.put(command.getName(), map);

            if (!command.getAliases().isEmpty()) {
                map.put("aliases", command.getAliases());
            }

            if (!Strings.isNullOrEmpty(command.getPermission())) {
                map.put("permission", command.getPermission());
            }

            if (!Strings.isNullOrEmpty(command.getDescription())) {
                map.put("description", command.getDescription());
            }
        }

        try {
            Field f = PluginDescriptionFile.class.getDeclaredField("commands");
            f.setAccessible(true);
            f.set(desc, newMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCommand(String name) {
        if (!modifyCommands) {
            return name;
        }

        DisguiseCommand command = commands.get(name);

        if (command == null || !command.isEnabled()) {
            return null;
        }

        return command.getName();
    }

    private void loadPlugin() {
        PluginDescriptionFile desc = LibsDisguises.getInstance().getDescription();

        for (Map.Entry<String, Map<String, Object>> entry : desc.getCommands().entrySet()) {
            DisguiseCommand command = new DisguiseCommand();
            command.setName(entry.getKey());

            Map<String, Object> map = entry.getValue();

            command.setPermission((String) map.get("permission"));

            if (map.containsKey("aliases")) {
                command.setAliases(new ArrayList<>((Collection) map.get("aliases")));
            }

            command.setDescription((String) map.getOrDefault("description", "No description set"));
            command.setEnabled(true);

            commands.put(entry.getKey(), command);
        }
    }
}
