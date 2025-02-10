package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import lombok.Getter;

@Getter
public class MineSkinRateLimit {
    @Getter
    public static class Next {
        // Absolute timestamp (in milliseconds) after which the next request can be made
        private long absolute;
        // Relative delay (in milliseconds) after which the next request can be made
        private long relative;
    }

    @Getter
    public static class Delay {
        // Delay between requests (in milliseconds) - depends on the API key used for this request
        private long millis;
        // Delay between requests (in seconds) - depends on the API key used for this request
        private long seconds;
    }

    @Getter
    public static class Limit {
        // Limit of requests in the current window (usually 1 minute). Same as the 'X-RateLimit-Limit' header
        private long limit;
        // Remaining requests in the current window. Same as the 'X-RateLimit-Remaining' header
        private long remaining;
    }

    private Next next;
    private Delay delay;
    private Limit limit;
}
