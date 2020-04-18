package me.libraryaddict.disguise.utilities.modded;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by libraryaddict on 14/04/2020.
 */
@AllArgsConstructor
@Getter
public class ModdedEntity {
    @Setter
    private Object entityType;
    private final String name;
    private final boolean living;
    private final String mod;
    private final String[] versions;
    private final String required;
    @Setter
    private int typeId;
}
