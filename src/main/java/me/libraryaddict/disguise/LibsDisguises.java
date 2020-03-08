package me.libraryaddict.disguise;

import me.libraryaddict.disguise.commands.LibsDisguisesCommand;
import me.libraryaddict.disguise.commands.disguise.DisguiseCommand;
import me.libraryaddict.disguise.commands.disguise.DisguiseEntityCommand;
import me.libraryaddict.disguise.commands.disguise.DisguisePlayerCommand;
import me.libraryaddict.disguise.commands.disguise.DisguiseRadiusCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyEntityCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyPlayerCommand;
import me.libraryaddict.disguise.commands.modify.DisguiseModifyRadiusCommand;
import me.libraryaddict.disguise.commands.undisguise.UndisguiseCommand;
import me.libraryaddict.disguise.commands.undisguise.UndisguiseEntityCommand;
import me.libraryaddict.disguise.commands.undisguise.UndisguisePlayerCommand;
import me.libraryaddict.disguise.commands.undisguise.UndisguiseRadiusCommand;
import me.libraryaddict.disguise.commands.utils.*;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.metrics.MetricsInitalizer;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.asm.WatcherSanitizer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LibsDisguises extends JavaPlugin {
    private static LibsDisguises instance;
    private DisguiseListener listener;
    private String buildNumber;
    private boolean reloaded;

    @Override
    public void onLoad() {
        instance = this;

        if (!Bukkit.getServer().getWorlds().isEmpty()) {
            reloaded = true;
            getLogger()
                    .severe("Lib's Disguises was reloaded! Please do not report any bugs! This plugin can't handle " +
                            "reloads gracefully!");
            return;
        }

        WatcherSanitizer.init();
    }

    @Override
    public void onEnable() {
        if (reloaded) {
            getLogger()
                    .severe("Lib's Disguises was reloaded! Please do not report any bugs! This plugin can't handle " +
                            "reloads gracefully!");
        }

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

        if (ReflectionManager.getVersion() == null) {
            getLogger().severe("You're using the wrong version of Lib's Disguises for your server! This is " +
                    "intended for " + StringUtils
                    .join(Arrays.stream(NmsVersion.values()).map(v -> v.name().replace("_", "."))
                            .collect(Collectors.toList()), " & ") + "!");
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

        registerCommand("libsdisguises", new LibsDisguisesCommand());

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
