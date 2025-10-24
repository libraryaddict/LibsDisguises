package me.libraryaddict.disguise.utilities.config.migrations.generics;

import lombok.SneakyThrows;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.DisguiseFiles;
import me.libraryaddict.disguise.utilities.config.ConfigMigrator;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class AbstractBundledConfigInjectorMigration implements ConfigMigrator.ConfigMigration {

    /**
     * @return The file to be modified.
     */
    public abstract File getTargetFile();

    public abstract String getBundledConfigName();

    /**
     * @return The string to search for in the target file to determine the injection point.
     * The content will be injected immediately following the line containing this marker.
     * If null, empty, or not found, content will be injected at the start of the file.
     */
    public abstract String getInjectionMarker();

    /**
     * @return The string in the bundled config that marks the beginning of the text to inject.
     * The content to be copied will include this marker string.
     */
    public abstract String getInjectionStartText();

    /**
     * @return The string in the bundled config that marks the end of the text to inject.
     * The content to be copied will end immediately after this string.
     */
    public abstract String getInjectionEndText();

    @Override
    public boolean shouldBeDelayed() {
        return !getTargetFile().exists();
    }

    @SneakyThrows
    public String getContentToInject(YamlConfiguration loadedConfig) {
        String resourceContent = DisguiseFiles.getResourceAsString(LibsDisguises.getInstance().getFile(), getBundledConfigName());
        if (resourceContent == null) {
            return "";
        }

        String startMarker = getInjectionStartText();
        String endMarker = getInjectionEndText();

        int startIndex = resourceContent.indexOf(startMarker);

        if (startIndex == -1) {
            throw new IllegalArgumentException("Cannot find marker '" + startMarker + "' in resource '" + resourceContent + "'");
        }

        int endIndex = resourceContent.indexOf(endMarker, startIndex);

        if (endIndex == -1) {
            throw new IllegalArgumentException("Cannot find marker '" + endMarker + "' in resource '" + resourceContent + "'");
        }

        return resourceContent.substring(startIndex, endIndex + endMarker.length());
    }

    @SneakyThrows
    @Override
    public void migrate(YamlConfiguration loadedConfig) {
        File targetFile = getTargetFile();
        if (!targetFile.exists()) {
            return;
        }

        String targetContent = new String(Files.readAllBytes(targetFile.toPath()), StandardCharsets.UTF_8);
        String injectionMarker = getInjectionMarker();
        int injectIndex = 0;

        if (injectionMarker != null && !injectionMarker.isEmpty()) {
            int markerPos = targetContent.indexOf(injectionMarker);

            if (markerPos != -1) {
                // Find the end of the line that contains the marker
                int endOfLine = targetContent.indexOf('\n', markerPos);
                if (endOfLine != -1) {
                    injectIndex = endOfLine + 1; // Set injection point to the start of the next line
                } else {
                    // If marker is on the last line with no trailing newline, append it.
                    targetContent += "\n";
                    injectIndex = targetContent.length();
                }
            }
        }

        String contentToInject = getContentToInject(loadedConfig);
        if (contentToInject == null || contentToInject.isEmpty()) {
            return;
        }

        // Use a StringBuilder to insert the content efficiently
        String newContent = new StringBuilder(targetContent).insert(injectIndex, contentToInject).toString();

        Files.write(targetFile.toPath(), newContent.getBytes(StandardCharsets.UTF_8));
    }
}