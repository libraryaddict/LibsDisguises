package me.libraryaddict.disguise.utilities.metrics;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by libraryaddict on 3/01/2019.
 */
public class MetricsInitalizer {
    public MetricsInitalizer() {
        infectWithMetrics();
    }

    private void infectWithMetrics() {
        LibsDisguises plugin = LibsDisguises.getInstance();

        String version = plugin.getDescription().getVersion();

        /*
        // If a release build, attach build number
        if (!plugin.isReleaseBuild() || !LibsPremium.isPremium()) {
            version += "-";

            // 9.7.0-SNAPSHOT-b30
            if (plugin.isNumberedBuild()) {
                version += "b";
            }
            // else 9.7.0-SNAPSHOT-unknown

            version += plugin.getBuildNo();
        }*/

        Metrics metrics = new Metrics(plugin, version);
        final String premiumType;

        if (LibsPremium.isPremium()) {
            PluginInformation info = LibsPremium.getPaidInformation();

            if (info == null) {
                info = LibsPremium.getPluginInformation();
            }

            boolean customPremium = !info.getUserID().matches("[0-9]+") || info.getUserID().equals("1") ||
                    !info.getResourceID().equals("32453") || !info.getDownloadID().matches("-?[0-9]+");

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

        metrics.addCustomChart(new Metrics.SimplePie("bisecthosting") {
            @Override
            public String getValue() {
                return "" + LibsPremium.isBisectHosted();
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("grabskin_command") {
            @Override
            public String getValue() {
                return "" + DisguiseUtilities.isGrabSkinCommandUsed();
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("grabhead_command") {
            @Override
            public String getValue() {
                return "" + DisguiseUtilities.isGrabHeadCommandUsed();
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("default_libraryaddict") {
            @Override
            public String getValue() {
                return (DisguiseAPI.getRawCustomDisguise("libraryaddict") != null) + "";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("save_disguise_command") {
            @Override
            public String getValue() {
                return "" + DisguiseUtilities.isSaveDisguiseCommandUsed();
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("copydisguise_command") {
            @Override
            public String getValue() {
                return "" + DisguiseUtilities.isCopyDisguiseCommandUsed();
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("premium") {
            @Override
            public String getValue() {
                return premiumType;
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("translations") {
            @Override
            public String getValue() {
                return LibsPremium.isPremium() && DisguiseConfig.isUseTranslations() ? "Yes" : "No";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("custom_disguises") {
            @Override
            public String getValue() {
                HashMap map = DisguiseConfig.getCustomDisguises();

                return map.size() + (map.containsKey("libraryaddict") ? -1 : 0) > 0 ? "Yes" : "No";
            }
        });

        metrics.addCustomChart(new Metrics.MultiLineChart("disguised_entities") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hashMap) {
                for (Set<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                    for (Disguise disg : list) {
                        if (disg.getEntity() == null || !disg.isDisguiseInUse())
                            continue;

                        String name = disg.getEntity().getType().name();

                        hashMap.put(name, hashMap.containsKey(name) ? hashMap.get(name) + 1 : 1);
                    }
                }

                return hashMap;
            }
        });

        metrics.addCustomChart(new Metrics.MultiLineChart("disguises_used") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hashMap) {
                for (Set<TargetedDisguise> list : DisguiseUtilities.getDisguises().values()) {
                    for (Disguise disg : list) {
                        if (disg.getEntity() == null || !disg.isDisguiseInUse())
                            continue;

                        String name = disg.getType().name();

                        hashMap.put(name, hashMap.containsKey(name) ? hashMap.get(name) + 1 : 1);
                    }
                }

                return hashMap;
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("disguised_using") {
            @Override
            public String getValue() {
                if (DisguiseUtilities.isPluginsUsed()) {
                    if (DisguiseUtilities.isCommandsUsed()) {
                        return "Plugins and Commands";
                    }

                    return "Plugins";
                } else if (DisguiseUtilities.isCommandsUsed()) {
                    return "Commands";
                }

                return "Unknown";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("active_disguises") {
            @Override
            public String getValue() {
                int disgs = 0;

                for (Set set : DisguiseUtilities.getDisguises().values()) {
                    disgs += set.size();
                }

                if (disgs == 0)
                    return "0";
                if (disgs <= 5)
                    return "1 to 5";
                else if (disgs <= 15)
                    return "6 to 15";
                else if (disgs <= 30)
                    return "16 to 30";
                else if (disgs <= 60)
                    return "30 to 60";
                else if (disgs <= 100)
                    return "60 to 100";
                else if (disgs <= 200)
                    return "100 to 200";

                return "More than 200";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("self_disguises") {
            @Override
            public String getValue() {
                return DisguiseConfig.isViewDisguises() ? "Yes" : "No";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("commands") {
            @Override
            public String getValue() {
                return DisguiseConfig.isDisableCommands() ? "Enabled" : "Disabled";
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("spigot") {
            @Override
            public String getValue() {
                try {
                    Class.forName("org.spigotmc.SpigotConfig");
                    return "Yes";
                }
                catch (Exception ex) {
                    return "No";
                }
            }
        });

        final boolean updates = plugin.getConfig().getBoolean("NotifyUpdate");

        metrics.addCustomChart(new Metrics.SimplePie("updates") {
            @Override
            public String getValue() {
                return updates ? "Enabled" : "Disabled";
            }
        });

        if (plugin.getBuildNo() != null) {
            // Initalize final variable so we don't need to hold onto the plugin variable
            final String buildNo = plugin.getBuildNo();

            metrics.addCustomChart(new Metrics.SimplePie("build_number") {
                @Override
                public String getValue() {
                    return buildNo;
                }
            });
        }

        metrics.addCustomChart(new Metrics.SimplePie("targeted_disguises") {
            /**
             * Store value just to minimize amount of times it's called, and to persist even when not using anymore
             */
            private boolean targetedDisguises;

            @Override
            public String getValue() {
                if (targetedDisguises) {
                    return "Yes";
                }

                Collection<Set<TargetedDisguise>> list = DisguiseUtilities.getDisguises().values();

                if (list.isEmpty())
                    return "Unknown";

                for (Set<TargetedDisguise> dList : list) {
                    for (TargetedDisguise disg : dList) {
                        if (disg.getObservers().isEmpty())
                            continue;

                        targetedDisguises = true;
                        return "Yes";
                    }
                }

                return "No";
            }
        });
    }
}
