package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import lombok.Data;

@Data
public class MineSkinUsage {
    @Data
    public static class Credits {
        // Credits used for this request
        private final long used;
        // Remaining credits
        private final long remaining;
    }

    @Data
    public static class Metered {
        // Number of metered units used for this request
        private final long used;
    }
}
