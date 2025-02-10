package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import com.google.gson.annotations.SerializedName;

public enum SkinVariant {
    // Unknown is the default
    @SerializedName("unknown") UNKNOWN,
    @SerializedName("classic") CLASSIC,
    @SerializedName("slim") SLIM
}