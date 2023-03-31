package me.libraryaddict.disguise.utilities.params;

import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.TranslateType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public abstract class ParamInfo<T> {
    private final Class paramClass;
    private final String descriptiveName;
    private final String name;
    private Map<String, T> possibleValues;
    /**
     * Used for translations, namely ItemStack and it's 'Glowing' and 'null' counterparts
     */
    private String[] otherValues;
    private final String description;

    public ParamInfo(Class<T> paramClass, String name, String description) {
        this(paramClass, name, name, description);
    }

    public ParamInfo(Class<T> paramClass, String name, String descriptiveName, String description) {
        this.name = name;
        this.paramClass = paramClass;
        this.descriptiveName = descriptiveName;
        this.description = description;
    }

    public ParamInfo(Class<T> paramClass, String name, String description, T[] possibleValues) {
        this(paramClass, name, name, description, possibleValues);
    }

    public ParamInfo(Class<T> paramClass, String name, String descriptiveName, String description, T[] possibleValues) {
        this(paramClass, name, descriptiveName, description);

        this.possibleValues = new LinkedHashMap<>();

        for (T anEnum : possibleValues) {
            this.getValues().put(((Enum) anEnum).name(), anEnum);
        }
    }

    public ParamInfo(Class<T> paramClass, String name, String description, Map<String, T> possibleValues) {
        this(paramClass, name, name, description, possibleValues);
    }

    public ParamInfo(Class<T> paramClass, String name, String descriptiveName, String description, Map<String, T> possibleValues) {
        this(paramClass, name, descriptiveName, description);

        this.possibleValues = new LinkedHashMap<>();
        this.possibleValues.putAll(possibleValues);
    }

    public boolean canTranslateValues() {
        return getValues() != null;
    }

    public String[] getOtherValues() {
        return this.otherValues;
    }

    public void setOtherValues(String... otherValues) {
        if (this.otherValues != null) {
            this.otherValues = Arrays.copyOf(this.otherValues, this.otherValues.length + otherValues.length);

            for (int i = 0; i < otherValues.length; i++) {
                this.otherValues[this.otherValues.length - (otherValues.length - i)] = otherValues[i];
            }
        } else {
            this.otherValues = otherValues;
        }
    }

    public boolean canReturnNull() {
        return false;
    }

    protected abstract T fromString(String string) throws DisguiseParseException;

    public abstract String toString(T object);

    public T fromString(List<String> arguments) throws DisguiseParseException {
        // Don't consume a string immediately, if it errors we need to check other param types
        String string = arguments.get(0);

        T value = fromString(string);

        // Throw error if null wasn't expected
        if (value == null && !canReturnNull()) {
            throw new IllegalArgumentException();
        }

        arguments.remove(0);

        return value;
    }

    public int getMinArguments() {
        return 1;
    }

    public boolean hasValues() {
        return getValues() != null;
    }

    protected Class getParamClass() {
        return paramClass;
    }

    public boolean isParam(Class paramClass) {
        return getParamClass() == paramClass;
    }

    public String getName() {
        return TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(getRawName());
    }

    public String getDescriptiveName() {
        return TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(getRawDescriptiveName());
    }

    public String getRawName() {
        return this.name;
    }

    public String getRawDescriptiveName() {
        return descriptiveName;
    }

    public String getDescription() {
        return TranslateType.DISGUISE_OPTIONS_PARAMETERS.get(getRawDescription());
    }

    public String getRawDescription() {
        return description;
    }

    public Map<String, T> getValues() {
        return this.possibleValues;
    }

    public Set<String> getEnums(String tabComplete) {
        if (getOtherValues() != null) {
            HashSet<String> set = new HashSet<>(getValues().keySet());
            set.addAll(Arrays.asList(getOtherValues()));

            return set;
        }

        return getValues().keySet();
    }

    /**
     * Is the values it returns all it can do?
     */
    public boolean isCustomValues() {
        return true;
    }
}
