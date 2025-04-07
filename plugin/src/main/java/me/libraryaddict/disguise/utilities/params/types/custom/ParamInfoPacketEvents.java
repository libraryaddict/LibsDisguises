package me.libraryaddict.disguise.utilities.params.types.custom;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import me.libraryaddict.disguise.utilities.params.types.ParamInfoEnum;

import java.util.stream.Collectors;

public class ParamInfoPacketEvents extends ParamInfoEnum<MappedEntity> {
    public ParamInfoPacketEvents(Class<? extends MappedEntity> classType, VersionedRegistry<? extends MappedEntity> registry, String name,
                                 String description) {
        super(classType, name, description, registry.getEntries().stream().collect(Collectors.toMap(e -> e.getName().getKey(), e -> e)));
    }
}
