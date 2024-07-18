package me.libraryaddict.disguise.utilities.parser;

import lombok.Getter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Objects;

public class DisguisePerm {
    private final DisguiseType disguiseType;
    private String permName;
    @Getter
    private final String regexedName;
    private boolean customDisguise;

    public DisguisePerm(DisguiseType disguiseType) {
        this.disguiseType = disguiseType;
        regexedName = toReadable().replaceAll("[ |_]", "").toLowerCase(Locale.ENGLISH);
    }

    public DisguisePerm(DisguiseType disguiseType, String disguisePerm) {
        this.disguiseType = disguiseType;
        permName = disguisePerm;
        customDisguise = true;
        regexedName = toReadable().replaceAll("[ |_]", "").toLowerCase(Locale.ENGLISH);
    }

    public boolean isCustomDisguise() {
        return customDisguise;
    }

    public Class getEntityClass() {
        return getType().getEntityClass();
    }

    public EntityType getEntityType() {
        return getType().getEntityType();
    }

    public DisguiseType getType() {
        return disguiseType;
    }

    public Class<? extends FlagWatcher> getWatcherClass() {
        return getType().getWatcherClass();
    }

    public boolean isMisc() {
        return getType().isMisc();
    }

    public boolean isMob() {
        return getType().isMob();
    }

    public boolean isPlayer() {
        return getType().isPlayer();
    }

    public boolean isUnknown() {
        return getType().isUnknown();
    }

    public String toReadable() {
        return permName == null ? getType().toReadable() : permName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((disguiseType == null) ? 0 : disguiseType.hashCode());
        result = prime * result + ((permName == null) ? 0 : permName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof DisguisePerm)) {
            return false;
        }

        DisguisePerm other = (DisguisePerm) obj;

        if (disguiseType != other.disguiseType) {
            return false;
        }

        return Objects.equals(permName, other.permName);
    }

    @Override
    public String toString() {
        return "DisguisePerm{" + "disguiseType=" + disguiseType + ", permName='" + permName + '\'' + '}';
    }
}
