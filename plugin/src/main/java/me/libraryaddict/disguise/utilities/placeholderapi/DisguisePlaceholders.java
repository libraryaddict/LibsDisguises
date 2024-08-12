package me.libraryaddict.disguise.utilities.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.libraryaddict.disguise.utilities.LibsPremium;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DisguisePlaceholders extends PlaceholderExpansion {
    private final HashMap<String, DPlaceholder> placeholders = new HashMap<>();

    public DisguisePlaceholders() {
        add(new PlaceholderDisguiseType());
        add(new PlaceholderDisguiseName());
        add(new PlaceholderDisguiseHeight());
        add(new PlaceholderIsDisguised());
        add(new PlaceholderDisguiseSkin());
        add(new PlaceholderDisguiseMaterial());
        add(new PlaceholderDisguiseFlag());
    }

    private void add(DPlaceholder dPlaceholder) {
        if (placeholders.containsKey(dPlaceholder.getName())) {
            throw new IllegalStateException(String.format(
                "Error while trying to register the placeholder '%s' for class %s. The placeholder was already registered by %s",
                dPlaceholder.getName(), dPlaceholder.getClass().getName(),
                placeholders.get(dPlaceholder.getName()).getClass().getSimpleName()));
        }

        placeholders.put(dPlaceholder.getName(), dPlaceholder);
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return placeholders.values().stream().map(DPlaceholder::getStructure).collect(Collectors.toList());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "libsdisguises";
    }

    @Override
    public @NotNull String getAuthor() {
        return "libraryaddict";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] split = params.split(":");

        DPlaceholder placeholder = placeholders.get(split[0]);

        if (placeholder == null) {
            return null;
        }

        return placeholder.parse(player, Arrays.copyOfRange(split, 1, split.length));

        // Global
        // Disguise active count, flag to filter by types, disguised entity type, world, etc
        //
    }
}
