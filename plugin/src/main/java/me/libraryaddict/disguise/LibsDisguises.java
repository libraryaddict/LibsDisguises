package me.libraryaddict.disguise;

import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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
import me.libraryaddict.disguise.commands.utils.CopyDisguiseCommand;
import me.libraryaddict.disguise.commands.utils.DisguiseCloneCommand;
import me.libraryaddict.disguise.commands.utils.DisguiseHelpCommand;
import me.libraryaddict.disguise.commands.utils.DisguiseViewBarCommand;
import me.libraryaddict.disguise.commands.utils.DisguiseViewSelfCommand;
import me.libraryaddict.disguise.commands.utils.GrabHeadCommand;
import me.libraryaddict.disguise.commands.utils.GrabSkinCommand;
import me.libraryaddict.disguise.commands.utils.SaveDisguiseCommand;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.config.DisguiseCommandConfig;
import me.libraryaddict.disguise.utilities.listeners.DisguiseListener;
import me.libraryaddict.disguise.utilities.listeners.DisguiseListener1_18;
import me.libraryaddict.disguise.utilities.listeners.PaperDisguiseListener;
import me.libraryaddict.disguise.utilities.listeners.PlayerSkinHandler;
import me.libraryaddict.disguise.utilities.metrics.MetricsInitalizer;
import me.libraryaddict.disguise.utilities.packets.PacketsManager;
import me.libraryaddict.disguise.utilities.params.ParamInfoManager;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import me.libraryaddict.disguise.utilities.placeholderapi.DisguisePlaceholders;
import me.libraryaddict.disguise.utilities.reflection.ClassMappings;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.sounds.SoundManager;
import me.libraryaddict.disguise.utilities.updates.PacketEventsUpdater;
import me.libraryaddict.disguise.utilities.updates.UpdateChecker;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LibsDisguises extends JavaPlugin {
    /**
     * -- GETTER --
     * External APIs shouldn't actually need this instance. DisguiseAPI should be enough to handle most cases.
     *
     * @return The instance of this plugin
     */
    @Getter
    private static LibsDisguises instance;
    @Getter
    private DisguiseListener listener;
    private String buildNumber;
    @Getter
    private String buildDate;
    @Getter
    private boolean reloaded;
    @Getter
    private final UpdateChecker updateChecker = new UpdateChecker();
    @Getter
    private PlayerSkinHandler skinHandler;
    private DisguiseCommandConfig commandConfig;
    @Setter
    @Getter
    private boolean packetEventsUpdateDownloaded;
    @Getter
    private final long serverStarted = System.currentTimeMillis();

    @Override
    public void onLoad() {
        try {
            boolean hasInstanceAlready = instance != null;

            if (hasInstanceAlready || !Bukkit.getServer().getWorlds().isEmpty() || !Bukkit.getOnlinePlayers().isEmpty()) {
                reloaded = true;
                getLogger().severe("Server was reloaded! Please do not report any bugs! This plugin can't handle reloads gracefully!");
            }

            instance = this;

            Plugin plugin = Bukkit.getPluginManager().getPlugin("packetevents");

            DisguiseConfig.loadPreConfig();

            // Skipping the isPacketEventsOutdated check cos DisguiseConfig wouldn't be loaded
            if (plugin == null || PacketEventsUpdater.isPacketEventsOutdated()) {
                // I don't think anyone will ever see this plugin message, DisguiseConfig isn't loaded at this point
                if (DisguiseConfig.isNeverUpdatePacketEvents()) {
                    getLogger().warning(
                        "Defined in plugins/LibsDisguises/configs/sanity.yml, you have requested that Lib's Disguises never updates or " +
                            "installs PacketEvents. Please do not report any issues with this plugin.");
                } else if (!DisguiseConfig.isAutoUpdate()) {
                    getLogger().warning(
                        "Defined in plugins/LibsDisguises/configs/libsdisguises.yml, you have requested that Lib's Disguises never auto " +
                            "updates, which includes PacketEvents. Please do not report any issues with this plugin.");
                } else {
                    String reason = getPacketEventsFailedReason(plugin);

                    getLogger().warning(
                        "An issue occured when trying to load PacketEvents: " + reason + ". Lib's Disguises will attempt to update it.");

                    try {
                        PacketEventsUpdater updater = new PacketEventsUpdater();
                        boolean attempt = updater.doUpdate();

                        if (!attempt) {
                            getLogger().severe(
                                "PacketEvents download has failed, please install PacketEvents manually from https://www.spigotmc" +
                                    ".org/resources/packetevents-api.80279/");
                        } else if (plugin == null) {
                            getLogger().info("PacketEvents downloaded and stuck in plugins folder! Now trying to load it!");
                            plugin = Bukkit.getPluginManager().loadPlugin(updater.getDestination());
                            plugin.onLoad();

                            Bukkit.getPluginManager().enablePlugin(plugin);
                        } else {
                            getLogger().severe("Please restart the server to complete the PacketEvents update!");
                        }
                    } catch (Exception e) {
                        getLogger().severe(
                            "Looks like PacketEvents's site may be down! Try download it manually from https://www.spigotmc" +
                                ".org/resources/packetevents-api.80279/");
                        e.printStackTrace();
                    }
                }
            }

            try {
                Class cl = Class.forName("org.bukkit.Server$Spigot");
            } catch (ClassNotFoundException e) {
                getLogger().severe(
                    "Oh dear, you seem to be using CraftBukkit. Please upgrade to use Spigot or Paper instead! This plugin will continue " +
                        "to load, but it will look like a mugging victim");
            }

            // Note that Forge/Hybrid is unsupported
            // Previously was supported but changes in both LD and nms has made it unsustainable
            // The time and complexibility it would take is far too high for the amount of usage it would receive.
            // Any extra income from sales would never cover the time spent implementing this.
            if (Bukkit.getVersion().toLowerCase(Locale.ENGLISH).matches(".*(arclight|mohist|magma).*")) {
                getLogger().severe(
                    "Lib's Disguises will not work correctly on Forge/Hybrid servers, due to complexibility and time commitment there is " +
                        "no support for this. Do not report any issues to Lib's Disguises.");
            }

            commandConfig = new DisguiseCommandConfig();

            if (!hasInstanceAlready && (isReleaseBuild() || LibsPremium.getPaidInformation() != null || !LibsPremium.isPremium())) {
                commandConfig.load();
            }
        } catch (Throwable throwable) {
            try {
                if (isJenkins() && DisguiseConfig.isAutoUpdate()) {
                    getUpdateChecker().doUpdate();
                }
            } catch (Throwable t) {
                getLogger().severe("Failed to even do a forced update");
            }

            throw throwable;
        }
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        try {
            if (isReloaded()) {
                getLogger().severe("Server was reloaded! Please do not report any bugs! This plugin can't handle reloads gracefully!");
            }

            runWarnings();
            updateOldDisguisesYaml();
            loadYamlWarnVirus();
            logInfo();

            DisguiseConfig.loadInternalConfig();

            LibsPremium.check(getDescription().getVersion(), getFile());

            someMoreLogging();

            if (ReflectionManager.getVersion() == null) {
                getLogger().severe("You're using the wrong version of Lib's Disguises for your server! This is intended for " +
                    StringUtils.join(Arrays.stream(NmsVersion.values()).filter(v -> v != NmsVersion.UNSUPPORTED)
                        .map(v -> String.join(", ", v.getSupportedVersions())).collect(Collectors.toList()), ", ") + "!");
                getPluginLoader().disablePlugin(this);
                return;
            }

            startOutdatedPacketevents();

            // If this is a release build, even if jenkins build..
            if (isReleaseBuild()) {
                // If downloaded from spigot, forcibly set release build to true
                if (LibsPremium.getUserID().matches("\\d+")) {
                    DisguiseConfig.setUsingReleaseBuilds(true);
                }
                // Otherwise leave it untouched as they might've just happened to hit a dev build, which is a release build
            } else {
                DisguiseConfig.setUsingReleaseBuilds(false);
            }

            runBackendStuff();

            DisguiseConfig.loadConfig();

            registerListeners();
            registerCommands();

            integratePlaceholderApi();

            new MetricsInitalizer();
        } catch (Throwable throwable) {
            try {
                if (isJenkins() && DisguiseConfig.isAutoUpdate()) {
                    getUpdateChecker().doUpdate();
                }
            } catch (Throwable t) {
                getLogger().severe("Lib's Disguises couldn't update itself");
            }

            throw throwable;
        }
    }

    private void runBackendStuff() {
        ClassMappings.loadMappingsCache(getDataFolder());

        if (SpigotReflectionUtil.VERSION == null) {
            SpigotReflectionUtil.init();
        }

        ReflectionManager.init();

        PacketsManager.init();
        DisguiseUtilities.init();

        new SoundManager().load();

        ReflectionManager.registerValues();
        DisguiseParser.createDefaultMethods();

        ParamInfoManager.getParamInfoSoundGroup().recalculate();
    }

    private void registerListeners() {
        PacketsManager.addPacketListeners();

        listener = new DisguiseListener(this);
        skinHandler = new PlayerSkinHandler();

        Bukkit.getPluginManager().registerEvents(getSkinHandler(), LibsDisguises.getInstance());

        if (NmsVersion.v1_18.isSupported()) {
            Bukkit.getPluginManager().registerEvents(new DisguiseListener1_18(), this);
        }

        if (DisguiseUtilities.isRunningPaper()) {
            Bukkit.getPluginManager().registerEvents(new PaperDisguiseListener(), this);
        }
    }

    @NotNull
    private String getPacketEventsFailedReason(Plugin plugin) {
        if (plugin == null) {
            return "PacketEvents not installed";
        } else if (!plugin.isEnabled()) {
            return "PacketEvents is not enabled";
        } else if (PacketEventsUpdater.isPacketEventsOutdated()) {
            return "PacketEvents is outdated";
        }

        return "Lib's Disguises couldn't access PacketEvents properly";
    }

    private void runWarnings() {
        if (Bukkit.getVersion().contains("(MC: 1.17)")) {
            getLogger().severe("Please update from MC 1.17! You should be using 1.17.1!");
        }

        if (Bukkit.getVersion().contains("(MC: 1.18)") || Bukkit.getVersion().contains("(MC: 1.18.1)")) {
            getLogger().severe(
                "Please update from MC 1.18 and MC 1.18.1! You should be using 1.18.2! Support will eventually be dropped for your " +
                    "specific version!");
        }

        if (Bukkit.getVersion().contains("(MC: 1.19)")) {
            getLogger().severe(
                "Please update from MC 1.19.0! You should be using at least 1.19.3! 1.19.1 is the lowest supported 1.19 version!");
        }

        // Creates more problems than it solves
       /* if (NmsVersion.v1_21_R1.isSupported()) {
            getLogger().info(
                "Detected you are running 1.21, you may need to enable `enum-compatibility-mode` in your server's bukkit.yml if you are
                experiencing errors on Lib's Disguises load");
        }*/

        try {
            Class cl = Class.forName("org.bukkit.Server$Spigot");
        } catch (ClassNotFoundException e) {
            getLogger().severe(
                "Oh dear, you seem to be using CraftBukkit. Please use Spigot or Paper instead! This plugin will continue to load, but it" +
                    " will look like a mugging victim");
        }
    }

    private void updateOldDisguisesYaml() {
        File disguiseFile = new File(getDataFolder(), "configs/disguises.yml");

        if (!disguiseFile.exists()) {
            disguiseFile.getParentFile().mkdirs();

            File oldFile = new File(getDataFolder(), "disguises.yml");

            if (oldFile.exists()) {
                oldFile.renameTo(disguiseFile);
            } else {
                saveResource("configs/disguises.yml", false);
            }
        }
    }

    private void loadYamlWarnVirus() throws IOException {
        YamlConfiguration pluginYml = ReflectionManager.getPluginYAML(getFile());
        buildNumber = StringUtils.stripToNull(pluginYml.getString("build-number"));
        buildDate = StringUtils.stripToNull(pluginYml.getString("build-date"));

        int fileCount = ReflectionManager.getJarFileCount(getFile());
        int expected = pluginYml.getInt("file-count", fileCount);

        if (fileCount != expected) {
            getLogger().severe(
                "Hi, this is libraryaddict from Lib's Disguises informing you that I have detected that my plugin Lib's Disguises looks " +
                    "like another plugin (don't know which) has injected malware into it as soon as Lib's Disguises started running on " +
                    "your server, there were " + (fileCount - expected) +
                    " unknown files injected into the jar. Please redownload from a trusted source such as SpigotMC. If this warning " +
                    "shows even after updating, try https://www.spigotmc.org/resources/spigot-anti-malware.64982/ but you will likely " +
                    "need to reinstall all your plugins, jars, etc as just one infected plugin will infect everything else when it loads.");
            getLogger().severe(
                "Unfortunately I have seen this happen from time to time, this normally happens when a server owner is tricked into " +
                    "adding third party plugins. Please note that Lib's Disguises is only detecting this and doesn't know what plugins " +
                    "are infected, only that Lib's Disguises itself was infected immediately after you installed it in your server. The " +
                    "jar in your plugins folder will be bigger than what you downloaded from SpigotMC, you can easily check that for " +
                    "yourself. I can't help you deal with the malware, there's no shortcuts but to redownload everything and remember not" +
                    " to download from shady sources.");
        }
    }

    private void logInfo() {
        getLogger().info("File Name: " + getFile().getName());

        String nmsPackageName = ReflectionManager.getNmsPackage();

        if (nmsPackageName.isEmpty()) {
            nmsPackageName = "{Not package relocated}";
        }

        getLogger().info("Discovered nms version: (Package: " + nmsPackageName + ") (LD: " + ReflectionManager.getVersion() + ") (MC: " +
            ReflectionManager.getMinecraftVersion() + ")");

        getLogger().info("Jenkins Build: " + (isJenkins() ? "#" : "") + getBuildNo());

        getLogger().info("Build Date: " + buildDate);
    }

    private void someMoreLogging() {
        if (!LibsPremium.isPremium()) {
            getLogger().info(
                "You are running the free version, commands limited to non-players and operators. (Console, Command Blocks, Admins)");
        }

        // Add a message so people are more aware
        if (!DisguiseConfig.isTallSelfDisguises()) {
            LibsDisguises.getInstance().getLogger().info(
                "Config 'TallSelfDisguises' is set to 'false', LD will hide oversized disguises from self disguise. https://www.spigotmc" +
                    ".org/wiki/lib-s-disguises-faq/#tall-disguises-self-disguises");
        }
    }

    private void startOutdatedPacketevents() {
        if (!DisguiseConfig.isNeverUpdatePacketEvents() && PacketEventsUpdater.isPacketEventsOutdated()) {
            String requiredPacketEvents = PacketEventsUpdater.getMinimumPacketEventsVersion();
            Plugin plugin = Bukkit.getPluginManager().getPlugin("packetevents");
            String version = plugin == null ? "[PacketEvents Plugin Missing]" : plugin.getDescription().getVersion();

            BukkitRunnable runnable = createPacketEventsOutdatedRunnable(version, requiredPacketEvents);
            runnable.run();
            runnable.runTaskTimer(this, 20, 30 * 60 * 20);
        }

        PacketEventsUpdater.doShadedWarning();
    }

    private void registerCommands() {
        registerCommand("libsdisguises", new LibsDisguisesCommand());
        registerCommand("disguise", new DisguiseCommand());
        registerCommand("undisguise", new UndisguiseCommand());
        registerCommand("disguiseplayer", new DisguisePlayerCommand());
        registerCommand("undisguiseplayer", new UndisguisePlayerCommand());
        registerCommand("undisguiseentity", new UndisguiseEntityCommand());
        registerCommand("disguiseentity", new DisguiseEntityCommand());
        registerCommand("disguiseradius", new DisguiseRadiusCommand());
        registerCommand("undisguiseradius", new UndisguiseRadiusCommand());
        registerCommand("disguisehelp", new DisguiseHelpCommand());
        registerCommand("disguiseclone", new DisguiseCloneCommand());
        registerCommand("disguiseviewself", new DisguiseViewSelfCommand());
        registerCommand("disguiseviewbar", new DisguiseViewBarCommand());
        registerCommand("disguisemodify", new DisguiseModifyCommand());
        registerCommand("disguisemodifyentity", new DisguiseModifyEntityCommand());
        registerCommand("disguisemodifyplayer", new DisguiseModifyPlayerCommand());
        registerCommand("disguisemodifyradius", new DisguiseModifyRadiusCommand());
        registerCommand("copydisguise", new CopyDisguiseCommand());
        registerCommand("grabskin", new GrabSkinCommand());
        registerCommand("savedisguise", new SaveDisguiseCommand());
        registerCommand("grabhead", new GrabHeadCommand());

        unregisterCommands(false);
    }

    private void integratePlaceholderApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        new DisguisePlaceholders().register();
        getLogger().info("PlaceholderAPI support enabled");
    }

    @NotNull
    private BukkitRunnable createPacketEventsOutdatedRunnable(String version, String requiredPacketEvents) {
        return new BukkitRunnable() {
            private int timesRun;

            @Override
            public void run() {
                if (isPacketEventsUpdateDownloaded()) {
                    getLogger().severe(
                        "An update for PacketEvents has been downloaded and will be installed when the server restarts. When possible, " +
                            "please restart the server. Lib's Disguises may not work correctly until you do so.");
                } else {
                    getLogger().severe(
                        "Update your PacketEvents! You are running " + version + " but the minimum version you should be on is " +
                            requiredPacketEvents + "!");
                    getLogger().severe("https://www.spigotmc.org/resources/packetevents-api.80279/");
                    getLogger().severe("Or! Use /ld packetevents - To have Lib's Disguises download the latest release from Modrinth");
                }
            }
        };
    }

    public void unregisterCommands(boolean force) {
        CommandMap map = ReflectionManager.getCommandMap();
        Map<String, Command> commands = ReflectionManager.getCommands(map);

        for (String command : getDescription().getCommands().keySet()) {
            PluginCommand cmd = getCommand("libsdisguises:" + command);

            if (cmd == null || (cmd.getExecutor() != this && !force)) {
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
    public @NotNull File getFile() {
        return super.getFile();
    }

    @Override
    public void onDisable() {
        DisguiseUtilities.saveDisguises();
        ClassMappings.saveMappingsCache(getDataFolder());

        reloaded = true;
    }

    public boolean isReleaseBuild() {
        return !getDescription().getVersion().contains("-SNAPSHOT");
    }

    public String getBuildNo() {
        return buildNumber;
    }

    public int getBuildNumber() {
        return isJenkins() ? Integer.parseInt(getBuildNo()) : 0;
    }

    public boolean isJenkins() {
        return getBuildNo() != null && getBuildNo().matches("\\d+");
    }

    public boolean isDebuggingBuild() {
        return !isJenkins();
    }

    private void registerCommand(String commandName, CommandExecutor executioner) {
        String name = commandConfig.getCommand(commandName);

        if (name == null) {
            return;
        }

        PluginCommand command = getCommand("libsdisguises:" + name);

        if (command == null) {
            return;
        }

        command.setExecutor(executioner);

        if (executioner instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executioner);
        }
    }
}
