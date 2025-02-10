package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import lombok.Getter;

@Getter
public class MineSkinCredits {
    @Getter
    public static class CurrentCredits {
        private String type;
        private long balance;
        private long total;
    }

    @Getter
    public static class AllCredits {
        private long balance;
        private long total;
    }

    private CurrentCredits current;
    private AllCredits all;
}
