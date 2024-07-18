package me.libraryaddict.disguise.utilities.modded;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class ModdedEntity {
    @Setter
    private Object entityType;
    private EntityType packetEntityType;
    private final String name;
    private final boolean living;
    private final String mod;
    private final String[] versions;
    private final String required;
    @Setter
    private int typeId;
}
