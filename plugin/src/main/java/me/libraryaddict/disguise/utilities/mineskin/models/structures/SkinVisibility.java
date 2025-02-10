package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import com.google.gson.annotations.SerializedName;

public enum SkinVisibility {
    // public is the default
    @SerializedName("public") PUBLIC,
    @SerializedName("unlisted") UNLISTED,
    @SerializedName("private") PRIVATE
}