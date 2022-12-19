package me.libraryaddict.disguise.utilities.reflection;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WatcherValue {
    private final MetaIndex metaIndex;
    private final int index;
    private Object value;

    public WatcherValue(int index, Object value) {
        this.metaIndex = null;
        this.index = index;
        this.value = value;
    }

    public WatcherValue(MetaIndex index, Object value) {
        this.index = index.getIndex();
        this.metaIndex = index;
        this.value = value;
    }

    public WatcherValue(FlagWatcher flagWatcher, WrappedDataValue dataValue) {
        this.index = dataValue.getIndex();
        metaIndex = MetaIndex.getMetaIndex(flagWatcher, dataValue.getIndex());
        value = dataValue.getRawValue();
    }

    public WatcherValue(FlagWatcher flagWatcher, WrappedWatchableObject dataValue) {
        this.index = dataValue.getIndex();
        metaIndex = MetaIndex.getMetaIndex(flagWatcher, dataValue.getIndex());
        value = dataValue.getRawValue();
    }

    public WrappedWatchableObject getWatchableObject() {
        return ReflectionManager.createWatchable(getMetaIndex(), getValue());
    }

    public WrappedDataValue getDataValue() {
        return new WrappedDataValue(getMetaIndex().getIndex(), getMetaIndex().getSerializer(), ReflectionManager.convertInvalidMeta(getValue()));
    }

    public static List<WatcherValue> getValues(WrappedDataWatcher dataWatcher) {
        List<WatcherValue> list = new ArrayList<>();

        for (WrappedWatchableObject object : dataWatcher.getWatchableObjects()) {
            list.add(new WatcherValue(object.getIndex(), object.getRawValue()));
        }

        return list;
    }

    public static List<WatcherValue> getValues(FlagWatcher watcher, WrappedDataWatcher dataWatcher) {
        List<WatcherValue> newList = new ArrayList<>();

        for (WrappedWatchableObject object : dataWatcher.getWatchableObjects()) {
            newList.add(new WatcherValue(watcher, object));
        }

        return newList;
    }

    public static List<WatcherValue> getValues(FlagWatcher watcher, PacketContainer packetContainer) {
        List<WatcherValue> newList = new ArrayList<>();

        if (NmsVersion.v1_19_R2.isSupported()) {
            for (WrappedDataValue dataValue : packetContainer.getDataValueCollectionModifier().read(0)) {
                newList.add(new WatcherValue(watcher, dataValue));
            }
        } else {
            for (WrappedWatchableObject object : packetContainer.getWatchableCollectionModifier().read(0)) {
                newList.add(new WatcherValue(watcher, object));
            }
        }

        return newList;
    }
}
