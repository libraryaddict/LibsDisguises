package me.libraryaddict.disguise.utilities.reflection;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class WatcherInfo {
    // Serialized names only lowers size by 10% compressed (1kb), but there's a bigger change when uncompressed! (17kb to 10kb)
    @SerializedName("a")
    private int added = -1;
    @SerializedName("b")
    private int removed = -1;
    @SerializedName("c")
    private boolean deprecated;
    @SerializedName("d")
    private String returnType;
    @SerializedName("e")
    private boolean randomDefault;
    @SerializedName("f")
    private String watcher;
    @SerializedName("g")
    private String method;
    @SerializedName("h")
    private String mappedAs;
    @SerializedName("i")
    private String param;
    @SerializedName("j")
    private String descriptor;
    @SerializedName("k")
    private String description;
    @SerializedName("l")
    private boolean noVisibleDifference;
    @SerializedName("m")
    private List<Integer> unusableBy = new ArrayList<>();
    @SerializedName("n")
    private List<Integer> hiddenFor = new ArrayList<>();

    public void setHiddenFor(DisguiseType[] types) {
        for (DisguiseType type : types) {
            hiddenFor.add(type.ordinal());
        }
    }

    public void setUnusableBy(DisguiseType[] types) {
        for (DisguiseType type : types) {
            unusableBy.add(type.ordinal());
        }
    }

    public boolean isSupported() {
        if (getAdded() >= 0 && added > ReflectionManager.getVersion().ordinal()) {
            return false;
        }

        return getRemoved() < 0 || removed > ReflectionManager.getVersion().ordinal();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        WatcherInfo that = (WatcherInfo) object;
        return added == that.added && removed == that.removed && deprecated == that.deprecated && randomDefault == that.randomDefault &&
            Objects.equals(returnType, that.returnType) && Objects.equals(watcher, that.watcher) && Objects.equals(method, that.method) &&
            Objects.equals(mappedAs, that.mappedAs) && Objects.equals(param, that.param) && Objects.equals(descriptor, that.descriptor) &&
            Objects.equals(unusableBy, that.unusableBy) && Objects.equals(hiddenFor, that.hiddenFor);
    }
}
