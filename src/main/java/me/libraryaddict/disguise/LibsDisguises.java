package me.libraryaddict.disguise;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
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
import me.libraryaddict.disguise.utilities.listeners.DisguiseListener;
import me.libraryaddict.disguise.utilities.listeners.PaperDisguiseListener;
import me.libraryaddict.disguise.utilities.listeners.PlayerSkinHandler;
import me.libraryaddict.disguise.utilities.metrics.MetricsInitalizer;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.reflection.asm.WatcherSanitizer;
import me.libraryaddict.disguise.utilities.sounds.SoundManager;
import me.libraryaddict.disguise.utilities.updates.UpdateChecker;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class LibsDisguises extends JavaPlugin {
    private static LibsDisguises instance;
    private DisguiseListener listener;
    private String buildNumber;
    @Getter
    private boolean reloaded;
    @Getter
    private final UpdateChecker updateChecker = new UpdateChecker();
    @Getter
    private PlayerSkinHandler skinHandler;

    @Override
    public void onLoad() {
        instance = this;

        if (!Bukkit.getServer().getWorlds().isEmpty()) {
            reloaded = true;
            getLogger().severe("Server was reloaded! Please do not report any bugs! This plugin can't handle " +
                    "reloads gracefully!");
            return;
        }

        try {
            Class cl = Class.forName("org.bukkit.Server$Spigot");
        } catch (ClassNotFoundException e) {
            getLogger().severe("Oh dear, you seem to be using CraftBukkit. Please use Spigot or Paper instead! This " +
                    "plugin will continue to load, but it will look like a mugging victim");
        }

        WatcherSanitizer.init();
    }

    @Override
    public void onEnable() {
        if (reloaded) {
            getLogger().severe("Server was reloaded! Please do not report any bugs! This plugin can't handle " +
                    "reloads gracefully!");
        }

        try {
            Class cl = Class.forName("org.bukkit.Server$Spigot");
        } catch (ClassNotFoundException e) {
            getLogger().severe("Oh dear, you seem to be using CraftBukkit. Please use Spigot or Paper instead! This " +
                    "plugin will continue to load, but it will look like a mugging victim");
        }

        if (!new File(getDataFolder(), "disguises.yml").exists()) {
            saveResource("disguises.yml", false);
        }

        YamlConfiguration pluginYml = ReflectionManager.getPluginYAML(getFile());
        buildNumber = StringUtils.stripToNull(pluginYml.getString("build-number"));

        getLogger().info("File Name: " + getFile().getName());

        getLogger().info("Discovered nms version: " + ReflectionManager.getBukkitVersion());

        getLogger().info("Jenkins Build: " + (isNumberedBuild() ? "#" : "") + getBuildNo());

        getLogger().info("Build Date: " + pluginYml.getString("build-date"));

        DisguiseConfig.loadInternalConfig();

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

        String requiredProtocolLib = DisguiseUtilities.getProtocolLibRequiredVersion();
        String version = ProtocolLibrary.getPlugin().getDescription().getVersion();

        if (DisguiseUtilities.isOlderThan(requiredProtocolLib, version)) {
            getLogger().severe("!! May I have your attention please !!");
            getLogger().severe("Update your ProtocolLib! You are running " + version +
                    " but the minimum version you should be on is " + requiredProtocolLib + "!");
            getLogger()
                    .severe("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib" +
                            ".jar");
            getLogger().severe("Or! Use /ld updateprotocollib - To update to the latest development build");
            getLogger().severe("!! May I have your attention please !!");

            new BukkitRunnable() {
                @Override
                public void run() {
                    getLogger().severe("!! May I have your attention please !!");
                    getLogger().severe("Update your ProtocolLib! You are running " + version +
                            " but the minimum version you should be on is " + requiredProtocolLib + "!");
                    getLogger().severe("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target" +
                            "/ProtocolLib" + ".jar");
                    getLogger().severe("Or! Use /ld updateprotocollib - To update to the latest development build");
                    getLogger()
                            .severe("This message is on repeat due to the sheer number of people who don't see this.");
                }
            }.runTaskTimer(this, 20, 10 * 60 * 20);
        }

        // If this is a release build, even if jenkins build..
        if (isReleaseBuild()) {
            // If downloaded from spigot, forcibly set release build to true
            if (LibsPremium.getUserID().matches("[0-9]+")) {
                DisguiseConfig.setUsingReleaseBuilds(true);
            }
            // Otherwise leave it untouched as they might've just happened to hit a dev build, which is a release build
        } else {
            DisguiseConfig.setUsingReleaseBuilds(false);
        }

        ReflectionManager.init();

        PacketsManager.init();
        DisguiseUtilities.init();

        ReflectionManager.registerValues();

        new SoundManager().load();

        DisguiseConfig.loadConfig();

        DisguiseParser.createDefaultMethods();

        PacketsManager.addPacketListeners();

        listener = new DisguiseListener(this);
        skinHandler = new PlayerSkinHandler();

        Bukkit.getPluginManager().registerEvents(getSkinHandler(), LibsDisguises.getInstance());

        if (DisguiseUtilities.isRunningPaper()) {
            Bukkit.getPluginManager().registerEvents(new PaperDisguiseListener(), this);
        }

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
            registerCommand("disguiseviewbar", new DisguiseViewBarCommand());
            registerCommand("disguisemodify", new DisguiseModifyCommand());
            registerCommand("disguisemodifyentity", new DisguiseModifyEntityCommand());
            registerCommand("disguisemodifyplayer", new DisguiseModifyPlayerCommand());
            registerCommand("disguisemodifyradius",
                    new DisguiseModifyRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
            registerCommand("copydisguise", new CopyDisguiseCommand());
            registerCommand("grabskin", new GrabSkinCommand());
            registerCommand("savedisguise", new SaveDisguiseCommand());
            registerCommand("grabhead", new GrabHeadCommand());
        } else {
            getLogger().info("Commands has been disabled, as per config");
        }

        unregisterCommands(false);

        new MetricsInitalizer();
    }

    public void unregisterCommands(boolean force) {
        CommandMap map = ReflectionManager.getCommandMap();
        Map<String, Command> commands = ReflectionManager.getCommands(map);

        for (String command : getDescription().getCommands().keySet()) {
            PluginCommand cmd = getCommand("libsdisguises:" + command);

            if (cmd.getExecutor() != this && !force) {
                continue;
            }

            if (cmd.getPermission() != null && cmd.getPermission().startsWith("libsdisguises.seecmd")) {
                Bukkit.getPluginManager().removePermission(cmd.getPermission());
            }

            Iterator<Map.Entry<String, Command>> itel = commands.entrySet().iterator();

            while (itel.hasNext()) {
                Map.Entry<String, Command> entry = itel.next();

                if (entry.getValue() != cmd) {
                    continue;
                }

                itel.remove();
            }
        }
    }

    @Override
    public File getFile() {
        return super.getFile();
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

    public int getBuildNumber() {
        return isNumberedBuild() ? Integer.parseInt(getBuildNo()) : 0;
    }

    public boolean isNumberedBuild() {
        return getBuildNo() != null && getBuildNo().matches("[0-9]+");
    }

    private void registerCommand(String commandName, CommandExecutor executioner) {
        PluginCommand command = getCommand("libsdisguises:" + commandName);

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
