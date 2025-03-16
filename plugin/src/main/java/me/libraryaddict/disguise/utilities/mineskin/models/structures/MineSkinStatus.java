package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import com.google.gson.annotations.SerializedName;

public enum MineSkinStatus {
    // unknown: The job status is unknown
    @SerializedName("unknown") UNKNOWN,
    // waiting: The job is waiting to be processed
    @SerializedName("waiting") WAITING,
    // active: The job is currently being processed
    @SerializedName("active") ACTIVE,
    // processing: Not sure what this means, wasn't documented at time of writing
    @SerializedName("processing") PROCESSING,
    // failed: The job has failed. The root 'errors' array may contain more details
    @SerializedName("failed") FAILED,
    // completed: The job has completed. The 'result' field will contain the job result
    @SerializedName("completed") COMPLETED
}
