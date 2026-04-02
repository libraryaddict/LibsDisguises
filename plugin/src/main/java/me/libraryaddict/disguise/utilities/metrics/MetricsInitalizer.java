package me.libraryaddict.disguise.utilities.metrics;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.plugin.LibsDisgInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MetricsInitalizer {
    public MetricsInitalizer() {
        infectWithMetrics();
    }

    private void infectWithMetrics() {
        LibsDisguises plugin = LibsDisguises.getInstance();

        Metrics metrics = new Metrics(plugin, 996);
        final String premiumType;

        if (LibsPremium.isPremium()) {
            LibsDisgInfo info = LibsPremium.getPaidInformation();

            if (info == null) {
                info = LibsPremium.getPluginInformation();
            }

            boolean customPremium =
                !info.getUserID().matches("[0-9]+") || info.getUserID().equals("1") || !info.getResourceID().equals("32453") ||
                    !info.getDownloadID().matches("-?[0-9]+");

            if (customPremium) {
                if (plugin.isReleaseBuild() && LibsPremium.getPaidInformation() == null) {
                    premiumType = "Custom Plugin";
                } else {
                    premiumType = "Custom Builds";
                }
            } else if (plugin.isReleaseBuild()) {
                premiumType = "Paid Plugin";
            } else {
                premiumType = "Paid Builds";
            }
        } else {
            premiumType = "Free Builds";
        }

        metrics.addCustomChart(new Metrics.SimplePie("bisecthosting", () -> String.valueOf(LibsPremium.isBisectHosted())));

        metrics.addCustomChart(new Metrics.SimplePie("grabskin_command", () -> String.valueOf(DisguiseUtilities.isGrabSkinCommandUsed())));

        metrics.addCustomChart(new Metrics.SimplePie("grabhead_command", () -> String.valueOf(DisguiseUtilities.isGrabHeadCommandUsed())));

        metrics.addCustomChart(
            new Metrics.SimplePie("default_libraryaddict", () -> (DisguiseAPI.getRawCustomDisguise("libraryaddict") != null) + ""));

        metrics.addCustomChart(
            new Metrics.SimplePie("save_disguise_command", () -> String.valueOf(DisguiseUtilities.isSaveDisguiseCommandUsed())));

        metrics.addCustomChart(
            new Metrics.SimplePie("copydisguise_command", () -> String.valueOf(DisguiseUtilities.isCopyDisguiseCommandUsed())));

        metrics.addCustomChart(new Metrics.SimplePie("premium", () -> premiumType));

        metrics.addCustomChart(
            new Metrics.SimplePie("translations", () -> LibsPremium.isPremium() && DisguiseConfig.isUseTranslations() ? "Yes" : "No"));

        metrics.addCustomChart(new Metrics.SimplePie("custom_disguises", () -> {
            HashMap map = DisguiseConfig.getCustomDisguises();

            return map.size() + (map.containsKey("libraryaddict") ? -1 : 0) > 0 ? "Yes" : "No";
        }));

        metrics.addCustomChart(new Metrics.MultiLineChart("disguised_entities", () -> {
            Map<String, Integer> hashMap = new LinkedHashMap<>();

            for (Set<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                for (Disguise disg : list) {
                    if (disg.getEntity() == null || !disg.isDisguiseInUse()) {
                        continue;
                    }

                    String name = disg.getEntity().getType().name();

                    hashMap.put(name, hashMap.containsKey(name) ? hashMap.get(name) + 1 : 1);
                }
            }

            return hashMap;
        }));

        metrics.addCustomChart(new Metrics.MultiLineChart("disguises_used", () -> {
            Map<String, Integer> hashMap = new LinkedHashMap<>();

            for (Set<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                for (Disguise disg : list) {
                    if (disg.getEntity() == null || !disg.isDisguiseInUse()) {
                        continue;
                    }

                    String name = disg.getType().name();

                    hashMap.put(name, hashMap.getOrDefault(name, 0) + 1);
                }
            }

            return hashMap;
        }));

        metrics.addCustomChart(new Metrics.SimplePie("disguised_using", () -> {
            if (DisguiseUtilities.isPluginsUsed()) {
                if (DisguiseUtilities.isCommandsUsed()) {
                    return "Plugins and Commands";
                }

                return "Plugins";
            } else if (DisguiseUtilities.isCommandsUsed()) {
                return "Commands";
            }

            return "Unknown";
        }));

        metrics.addCustomChart(new Metrics.SimplePie("active_disguises", () -> {
            int disgs = 0;

            for (Set set : DisguiseUtilities.getDisguises().values()) {
                disgs += set.size();
            }

            if (disgs == 0) {
                return "0";
            }
            if (disgs <= 5) {
                return "1 to 5";
            } else if (disgs <= 15) {
                return "6 to 15";
            } else if (disgs <= 30) {
                return "16 to 30";
            } else if (disgs <= 60) {
                return "30 to 60";
            } else if (disgs <= 100) {
                return "60 to 100";
            } else if (disgs <= 200) {
                return "100 to 200";
            }

            return "More than 200";
        }));

        metrics.addCustomChart(new Metrics.SimplePie("self_disguises", () -> DisguiseConfig.isViewDisguises() ? "Yes" : "No"));

        metrics.addCustomChart(new Metrics.SimplePie("spigot", () -> {
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                return "Yes";
            } catch (Exception ex) {
                return "No";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("bisect_hosting", () -> LibsPremium.isBisectHosted() ? "Yes" : "No"));

        final boolean updates = DisguiseConfig.isNotifyUpdate();

        metrics.addCustomChart(new Metrics.SimplePie("updates", () -> updates ? "Enabled" : "Disabled"));

        if (plugin.getBuildNo() != null) {
            // Initalize final variable so we don't need to hold onto the plugin variable
            final String buildNo = plugin.getBuildNo();

            metrics.addCustomChart(new Metrics.SimplePie("build_number", () -> buildNo));
        }

        // Store value just to minimize amount of times it's called, and to persist even when not using anymore
        AtomicBoolean targetedDisguises = new AtomicBoolean(false);

        metrics.addCustomChart(new Metrics.SimplePie("targeted_disguises", () -> {
            if (targetedDisguises.get()) {
                return "Yes";
            }

            Collection<Set<TargetedDisguise>> list = DisguiseUtilities.getDisguises().values();

            if (list.isEmpty()) {
                return "Unknown";
            }

            for (Set<TargetedDisguise> dList : list) {
                for (TargetedDisguise disg : dList) {
                    if (disg.getObservers().isEmpty()) {
                        continue;
                    }

                    targetedDisguises.set(true);

                    return "Yes";
                }
            }

            return "No";
        }));
    }
}
