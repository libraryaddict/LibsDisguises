package me.libraryaddict.disguise.DisguiseTypes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.Packet40EntityMetadata;
import net.minecraft.server.v1_5_R3.WatchableObject;

public class FlagWatcher {

    private static HashMap<Class, Integer> classTypes = new HashMap<Class, Integer>();
    static {
        classTypes.put(Byte.class, 0);
        classTypes.put(Short.class, 1);
        classTypes.put(Integer.class, 2);
        classTypes.put(String.class, 4);
        classTypes.put(ItemStack.class, 5);
    }
    private int entityId;
    private HashMap<Integer, Object> entityValues = new HashMap<Integer, Object>();

    protected FlagWatcher(int entityId) {
        this.entityId = entityId;
    }

    public List<WatchableObject> convert(List<WatchableObject> list) {
        Iterator<WatchableObject> itel = list.iterator();
        List<WatchableObject> newList = new ArrayList<WatchableObject>();
        List<Integer> sentValues = new ArrayList<Integer>();
        boolean sendAllCustom = false;
        while (itel.hasNext()) {
            WatchableObject watch = itel.next();
            sentValues.add(watch.a());
            // Its sending the air metadata. This is the least commonly sent metadata which all entitys still share.
            // I send my custom values if I see this!
            if (watch.a() == 1)
                sendAllCustom = true;
            if (entityValues.containsKey(watch.a())) {
                if (entityValues.get(watch.a()) == null)
                    continue;
                boolean doD = watch.d();
                Object value = entityValues.get(watch.a());
                watch = new WatchableObject(classTypes.get(value.getClass()), watch.a(), value);
                if (!doD)
                    watch.a(false);
            }
            newList.add(watch);
        }
        if (sendAllCustom) {
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

    private boolean getFlag(int i) {
        return ((Byte) getValue(0) & 1 << i) != 0;
    }

    protected Object getValue(int no) {
        return entityValues.get(no);
    }

    public boolean isBurning() {
        return getFlag(0);
    }

    public boolean isInvisible() {
        return getFlag(5);
    }

    public boolean isRiding() {
        return getFlag(2);
    }

    public boolean isRightClicking() {
        return getFlag(4);
    }

    public boolean isSneaking() {
        return getFlag(1);
    }

    public boolean isSprinting() {
        return getFlag(3);
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

    public void setBurning(boolean setBurning) {
        if (isSneaking() != setBurning) {
            setFlag(0, true);
            sendData(0);
        }
    }

    private void setFlag(int i, boolean flag) {
        byte currentValue = (Byte) getValue(0);
        if (flag) {
            setValue(0, Byte.valueOf((byte) (currentValue | 1 << i)));
        } else {
            setValue(0, Byte.valueOf((byte) (currentValue & ~(1 << i))));
        }
    }

    public void setInvisible(boolean setInvis) {
        if (isInvisible() != setInvis) {
            setFlag(5, true);
            sendData(5);
        }
    }

    public void setRiding(boolean setRiding) {
        if (isSprinting() != setRiding) {
            setFlag(2, true);
            sendData(2);
        }
    }

    public void setRightClicking(boolean setRightClicking) {
        if (isRightClicking() != setRightClicking) {
            setFlag(4, true);
            sendData(4);
        }
    }

    public void setSneaking(boolean setSneaking) {
        if (isSneaking() != setSneaking) {
            setFlag(1, true);
            sendData(1);
        }
    }

    public void setSprinting(boolean setSprinting) {
        if (isSprinting() != setSprinting) {
            setFlag(3, true);
            sendData(3);
        }
    }

    protected void setValue(int no, Object value) {
        entityValues.put(no, value);
    }

}
