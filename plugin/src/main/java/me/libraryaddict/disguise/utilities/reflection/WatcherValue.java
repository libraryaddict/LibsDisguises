package me.libraryaddict.disguise.utilities.reflection;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WatcherValue {
    private final MetaIndex metaIndex;
    private final EntityDataType dataType;
    private final int index;
    private boolean bukkitReadable;
    private Object value;

    public WatcherValue(EntityData entityData) {
        this.metaIndex = null;
        this.dataType = entityData.getType();
        this.index = entityData.getIndex();
        this.value = entityData.getValue();
        this.bukkitReadable = false;
    }

    public WatcherValue(MetaIndex index, Object value, boolean bukkitReadable) {
        this.dataType = index.getDataType();
        this.index = index.getIndex();
        this.metaIndex = index;
        this.value = value;
        this.bukkitReadable = bukkitReadable;
    }

    public EntityData getDataValue() {
        return ReflectionManager.getEntityData(getMetaIndex(), getValue(), isBukkitReadable());
    }

    public static List<WatcherValue> getValues(Entity entity) {
        List<WatcherValue> list = new ArrayList<>();

        for (EntityData data : ReflectionManager.getEntityWatcher(entity)) {
            list.add(new WatcherValue(data));
        }

        return list;
    }
}
