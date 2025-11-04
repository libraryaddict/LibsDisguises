package me.libraryaddict.disguise.disguisetypes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GolemCrack {
    HEALTH_100(100),
    HEALTH_75(74),
    HEALTH_50(49),
    HEALTH_25(24);
    @Getter
    private final float healthShown;
}
