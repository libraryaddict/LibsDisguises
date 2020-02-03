package me.libraryaddict.disguise;

import me.libraryaddict.disguise.commands.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.metrics.MetricsInitalizer;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;

public class LibsDisguises extends JavaPlugin {
    private static LibsDisguises instance;
    private DisguiseListener listener;
    private String buildNumber;
    private boolean reloaded;

    @Override
    public void onLoad() {
        if (Bukkit.getServer().getWorlds().isEmpty()) {
            return;
        }

        reloaded = true;
        getLogger().severe("Lib's Disguises was reloaded! Please do not report any bugs! This plugin can't handle " +
                "reloads gracefully!");
    }

    @Override
    public void onEnable() {
        if (reloaded) {
            getLogger()
                    .severe("Lib's Disguises was reloaded! Please do not report any bugs! This plugin can't handle " +
                            "reloads gracefully!");
        }

        instance = this;

        if (!new File(getDataFolder(), "disguises.yml").exists()) {
            saveResource("disguises.yml", false);
        }

        YamlConfiguration pluginYml = ReflectionManager.getPluginYaml(getClassLoader());
        buildNumber = StringUtils.stripToNull(pluginYml.getString("build-number"));

        getLogger().info("Discovered nms version: " + ReflectionManager.getBukkitVersion());

        getLogger().info("Jenkins Build: " + (isNumberedBuild() ? "#" : "") + getBuildNo());

        getLogger().info("Build Date: " + pluginYml.getString("build-date"));

        LibsPremium.check(getDescription().getVersion(), getFile());

        if (!LibsPremium.isPremium()) {
            getLogger()
                    .info("You are running the free version, commands limited to non-players and operators. (Console," +
                            " Command " + "Blocks, Admins)");
        }

        if (!ReflectionManager.getMinecraftVersion().startsWith("1.15")) {
            getLogger().severe("You're using the wrong version of Lib's Disguises for your server! This is " +
                    "intended for 1.15!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        ReflectionManager.init();

        PacketsManager.init();
        DisguiseUtilities.init();

        ReflectionManager.registerValues();

        DisguiseConfig.loadConfig();

        DisguiseParser.createDefaultMethods();

        PacketsManager.addPacketListeners();

        listener = new DisguiseListener(this);

        if (!DisguiseConfig.isDisableCommands()) {
            registerCommand("disguise", new DisguiseCommand());
            registerCommand("undisguise", new UndisguiseCommand());
            registerCommand("disguiseplayer", new DisguisePlayerCommand());
            registerCommand("undisguiseplayer", new UndisguisePlayerCommand());
            registerCommand("undisguiseentity", new UndisguiseEntityCommand());
            registerCommand("disguiseentity", new DisguiseEntityCommand());
            registerCommand("disguiseradius", new DisguiseRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
            registerCommand("undisguiseradius", new UndisguiseRadiusCommand(getConfig().getInt("UndisguiseRadiusMax")));
            registerCommand("disguisehelp", new DisguiseHelpCommand());
            registerCommand("disguiseclone", new DisguiseCloneCommand());
            registerCommand("libsdisguises", new LibsDisguisesCommand());
            registerCommand("disguiseviewself", new DisguiseViewSelfCommand());
            registerCommand("disguisemodify", new DisguiseModifyCommand());
            registerCommand("disguisemodifyentity", new DisguiseModifyEntityCommand());
            registerCommand("disguisemodifyplayer", new DisguiseModifyPlayerCommand());
            registerCommand("disguisemodifyradius",
                    new DisguiseModifyRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
            registerCommand("copydisguise", new CopyDisguiseCommand());
            registerCommand("grabskin", new GrabSkinCommand());
            registerCommand("savedisguise", new SaveDisguiseCommand());
        } else {
            getLogger().info("Commands has been disabled, as per config");
        }

        new MetricsInitalizer();
    }

    @Override
    public void onDisable() {
        DisguiseUtilities.saveDisguises();

        for (Player player : Bukkit.getOnlinePlayers()) {
            DisguiseUtilities.removeSelfDisguiseScoreboard(player);
        }
    }

    public boolean isReleaseBuild() {
        return !getDescription().getVersion().contains("-SNAPSHOT");
    }

    public String getBuildNo() {
        return buildNumber;
    }

    public boolean isNumberedBuild() {
        return getBuildNo() != null && getBuildNo().matches("[0-9]+");
    }

    private void registerCommand(String commandName, CommandExecutor executioner) {
        PluginCommand command = getCommand(commandName);

        command.setExecutor(executioner);

        if (executioner instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executioner);
        }
    }

    /**
     * Reloads the config with new config options.
     */
    @Deprecated
    public void reload() {
        DisguiseConfig.loadConfig();
    }

    public DisguiseListener getListener() {
        return listener;
    }

    /**
     * External APIs shouldn't actually need this instance. DisguiseAPI should be enough to handle most cases.
     *
     * @return The instance of this plugin
     */
    public static LibsDisguises getInstance() {
        return instance;
    }
}
