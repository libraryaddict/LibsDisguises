package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_5_R3.Packet40EntityMetadata;
import net.minecraft.server.v1_5_R3.WatchableObject;

public abstract class FlagWatcher {

    private static HashMap<Class, Integer> classTypes = new HashMap<Class, Integer>();
    static {
        classTypes.put(Byte.class, 0);
        classTypes.put(Short.class, 1);
        classTypes.put(Integer.class, 2);
        classTypes.put(String.class, 4);
    }
    private int entityId;
    private HashMap<Integer, Object> entityValues = new HashMap<Integer, Object>();

    protected FlagWatcher(int entityId) {
        this.entityId = entityId;
        setValue(6, (byte) 0);
        setValue(5, "");
    }

    public List<WatchableObject> convert(List<WatchableObject> list) {
        Iterator<WatchableObject> itel = list.iterator();
        List<WatchableObject> newList = new ArrayList<WatchableObject>();
        List<Integer> sentValues = new ArrayList<Integer>();
        while (itel.hasNext()) {
            WatchableObject watch = itel.next();
            sentValues.add(watch.a());
            if (entityValues.containsKey(watch.a())) {
                boolean doD = watch.d();
                watch = new WatchableObject(watch.c(), watch.a(), watch.b());
                if (!doD)
                    watch.a(false);
                if (entityValues.get(watch.a()) == null) {
                    continue;
                } else {
                    Object value = entityValues.get(watch.a());
                    if (watch.b().getClass() != value.getClass()) {
                        watch.a(value);
                        try {
                            Field field = WatchableObject.class.getDeclaredField("a");
                            field.setAccessible(true);
                            field.set(watch, classTypes.get(value.getClass()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        watch.a(entityValues.get(watch.a()));
                    }
                }
            }
            newList.add(watch);
        }
        if (list.size() == 10) {
            // Its sending the entire meta data. Better add the custom meta
            for (int value : entityValues.keySet()) {
                if (sentValues.contains(value))
                    continue;
                Object obj = entityValues.get(value);
                if (obj == null)
                    continue;
                WatchableObject watch = new WatchableObject(classTypes.get(obj.getClass()), value, obj);
                newList.add(watch);
            }
        }
        return newList;
    }

    protected Object getValue(int no) {
        return entityValues.get(no);
    }

    protected void sendData(int data) {
        Packet40EntityMetadata packet = new Packet40EntityMetadata();
        try {
            packet.a = entityId;
            Field field = Packet40EntityMetadata.class.getDeclaredField("b");
            field.setAccessible(true);
            Object value = entityValues.get(data);
            List<WatchableObject> list = new ArrayList<WatchableObject>();
            list.add(new WatchableObject(classTypes.get(value.getClass()), data, value));
            field.set(packet, list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void setName(String name) {
        setValue(5, name);
    }

    public void displayName(boolean display) {
        if ((Byte) getValue(6) != (display ? 1 : 0)) {
            setValue(6, (byte) (display ? 1 : 0));
            sendData(6);
        }
    }

    protected void setValue(int no, Object value) {
        entityValues.put(no, value);
    }

}
