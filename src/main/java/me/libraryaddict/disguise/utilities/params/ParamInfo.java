package me.libraryaddict.disguise.utilities.params;

import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.TranslateType;

import java.util.*;

/**
 * Created by libraryaddict on 7/09/2018.
 */
public abstract class ParamInfo {
    private Class paramClass;
    private String descriptiveName;
    private String name;
    private Map<String, Object> possibleValues;
    /**
     * Used for translations, namely ItemStack and it's 'Glowing' and 'null' counterparts
     */
    private String[] otherValues;
    private String description;

    public ParamInfo(Class paramClass, String name, String description) {
        this(paramClass, name, name, description);
    }

    public ParamInfo(Class paramClass, String name, String descriptiveName, String description) {
        this.name = name;
        this.paramClass = paramClass;
        this.descriptiveName = descriptiveName;
        this.description = description;
    }

    public ParamInfo(Class paramClass, String name, String description, Enum[] possibleValues) {
        this(paramClass, name, name, description, possibleValues);
    }

    public ParamInfo(Class paramClass, String name, String descriptiveName, String description, Enum[] possibleValues) {
        this(paramClass, name, descriptiveName, description);

        this.possibleValues = new LinkedHashMap<>();

        for (Enum anEnum : possibleValues) {
            this.getValues().put(anEnum.name(), anEnum);
        }
    }

    public ParamInfo(Class paramClass, String name, String description, Map<String, Object> possibleValues) {
        this(paramClass, name, name, description, possibleValues);
    }

    public ParamInfo(Class paramClass, String name, String descriptiveName, String description,
            Map<String, Object> possibleValues) {
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

    protected abstract Object fromString(String string) throws DisguiseParseException;

    public abstract String toString(Object object);

    public Object fromString(List<String> arguments) throws DisguiseParseException {
        // Don't consume a string immediately, if it errors we need to check other param types
        String string = arguments.get(0);

        Object value = fromString(string);

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

    public Map<String, Object> getValues() {
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
