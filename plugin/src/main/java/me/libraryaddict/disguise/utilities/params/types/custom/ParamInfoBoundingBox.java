package me.libraryaddict.disguise.utilities.params.types.custom;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.movements.InteractiveBoundingBox;
import me.libraryaddict.disguise.utilities.params.ParamInfo;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.TranslateType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ParamInfoBoundingBox extends ParamInfo<InteractiveBoundingBox> {
    private enum BoxTypes {
        // INTERACTION:3.3
        // INTERACTION:4,2.0,3.1
        INTERACTION(new DisguiseType[]{DisguiseType.INTERACTION}, "-?\\d+(\\.\\d+)?(,-?\\d+(\\.\\d+)?)?(:-?\\d+(\\.\\d+)?)?",
            (type, string) -> {
                String[] split = string.split(":");
                String[] sizeSplit = split[1].split(",");
                float width = Float.parseFloat(sizeSplit[0]);
                float height = Float.parseFloat(sizeSplit[sizeSplit.length - 1]);

                InteractiveBoundingBox box = new InteractiveBoundingBox(DisguiseType.INTERACTION);
                box.setSize(width, height);

                if (split.length > 2) {
                    box.setYOffset(Double.parseDouble(split[2]));
                }

                return box;
            }) {
            @Override
            public boolean isValid(String string) {
                return DisguiseType.INTERACTION.isValid() && super.isValid(string);
            }
        },
        SLIME(new DisguiseType[]{DisguiseType.SLIME, DisguiseType.MAGMA_CUBE}, "\\d+(:-?\\d+(\\.\\d+)?)?", (type, string) -> {
            String[] spl = string.split("[:,]");

            InteractiveBoundingBox box = new InteractiveBoundingBox(type.getType(spl[0]));
            box.setSize(Integer.parseInt(spl[1]));

            if (spl.length == 3) {
                box.setYOffset(Double.parseDouble(spl[2]));
            }

            return box;
        }),
        LIVING(Stream.of(DisguiseType.values()).filter(d -> d.isValid() && LivingWatcher.class.isAssignableFrom(d.getWatcherClass()))
            .toArray(DisguiseType[]::new), "-?\\d+(\\.\\d+)?(:-?\\d+(\\.\\d+)?)?", (type, string) -> {
            String[] spl = string.split(":");

            InteractiveBoundingBox box = new InteractiveBoundingBox(type.getType(spl[0]));
            box.setScale(Double.parseDouble(spl[1]));

            if (spl.length == 3) {
                box.setYOffset(Double.parseDouble(spl[2]));
            }

            return box;
        });

        private final DisguiseType[] validTypes;
        private final String validRegex;
        @Getter
        private final BiFunction<BoxTypes, String, InteractiveBoundingBox> parseToBox;

        public boolean isValid(String string) {
            String first = string.split(":")[0];

            if (getType(first) == null) {
                return false;
            }

            return string.substring(string.indexOf(":") + 1).matches(validRegex);
        }

        BoxTypes(DisguiseType[] validTypes, String validRegex, BiFunction<BoxTypes, String, InteractiveBoundingBox> parseToBox) {
            this.validTypes = Arrays.stream(validTypes).filter(DisguiseType::isValid).toArray(DisguiseType[]::new);
            this.validRegex = validRegex;
            this.parseToBox = parseToBox;
        }

        private DisguiseType getType(String name) {
            for (DisguiseType type : validTypes) {
                if (!type.name().equalsIgnoreCase(name) && !TranslateType.DISGUISES.get(type.toReadable()).equalsIgnoreCase(name)) {
                    continue;
                }

                return type;
            }

            return null;
        }
    }

    public ParamInfoBoundingBox() {
        super(InteractiveBoundingBox.class, "Interactive Box",
            "Creates a fake entity to simulate a hitbox that would otherwise not be there, Interaction is default using '<width.0>:<height.0>'. " +
                "'Slime:<size>', <Living>:<scale.0>. Add an extra ':<y-offset.0>' for offsetting Y location");
    }

    @Override
    protected InteractiveBoundingBox fromString(String string) throws DisguiseParseException {
        // If there was no 'type' supplied, prefix with "INTERACTION"
        if (string.split(":")[0].matches("[-\\d]+")) {
            string = DisguiseType.INTERACTION.toReadable() + ":" + string;
        }

        for (BoxTypes boxType : BoxTypes.values()) {
            if (!boxType.isValid(string)) {
                continue;
            }

            return boxType.parseToBox.apply(boxType, string);
        }

        return null;
    }

    @Override
    public String toString(InteractiveBoundingBox object) {
        if (object == null) {
            return "null";
        }

        return object.asString();
    }

    @Override
    public boolean hasTabCompletion() {
        return true;
    }

    @Override
    public Set<String> getEnums(String tabComplete) {
        if (tabComplete.contains(":")) {
            return null;
        }

        Set<String> toReturn = new HashSet<>();

        for (BoxTypes boxType : BoxTypes.values()) {
            for (DisguiseType type : boxType.validTypes) {
                String translated = TranslateType.DISGUISES.get(type.toReadable());

                if (!translated.toLowerCase(Locale.ENGLISH).startsWith(tabComplete.toLowerCase(Locale.ENGLISH))) {
                    continue;
                }

                toReturn.add(translated + ":");
            }
        }

        if (toReturn.isEmpty()) {
            return null;
        }

        return toReturn;
    }
}
