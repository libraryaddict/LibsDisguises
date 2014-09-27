package me.libraryaddict.disguise.disguisetypes;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

public enum FutureDisguiseType {

    ARMOR_STAND(Entity.class, 30, 2, new float[] { 0F, 0F, 0F }, new Object[] {

    1, (short) 300,

    2, "",

    3, (byte) 0,

    4, (byte) 0,

    6, 1F,

    7, 0,

    8, (byte) 0,

    9, (byte) 0,

    10, (byte) 0,

    // 11,
    // 12,
    // 13,
    // 14,
    // 15,
    // 16
            }),

    ELDER_GUARDIAN(Monster.class, 68, 80, new float[] { 0F, 0F, 0F }, new Object[] {

    1, (short) 300,

    2, "",

    3, (byte) 0,

    4, (byte) 0,

    6, 1F,

    7, 0,

    8, (byte) 0,

    9, (byte) 0,

    15, (byte) 0,

    16, 0 | 4,

    17, 0

    }),

    ENDERMITE(Monster.class, 67, 8, new float[] { 0F, 0F, 0F }, new Object[] {

    0, (byte) 0,

    1, (short) 300,

    2, "",

    3, (byte) 0,

    4, (byte) 0,

    6, 1F,

    7, 0,

    8, (byte) 0,

    9, (byte) 0,

    15, (byte) 0

    }),

    GUARDIAN(Monster.class, 68, 30, new float[] { 0F, 0F, 0F }, new Object[] {

    1, (short) 300,

    2, "",

    3, (byte) 0,

    4, (byte) 0,

    6, 1F,

    7, 0,

    8, (byte) 0,

    9, (byte) 0,

    15, (byte) 0,

    16, 0,

    17, 0

    }),

    RABBIT(Animals.class, 101, 10, new float[] { 0F, 0F, 0F }, new Object[] { 1, (short) 300,

    2, "",

    3, (byte) 0,

    4, (byte) 0,

    6, 1F,

    7, 0,

    8, (byte) 0,

    9, (byte) 0,

    12, 0,

    15, (byte) 0,

    18, (byte) 0

    });

    private float[] boundingBox;
    private Object[] dataWatcher;
    private Class<? extends Entity> entityClass;
    private int entityId;
    private float maxHealth;

    private FutureDisguiseType(Class<? extends Entity> entityClass, int entityId, float maxHealth, float[] boundingBox,
            Object[] watcherValues) {
        this.entityClass = entityClass;
        this.dataWatcher = watcherValues;
        this.boundingBox = boundingBox;
        if (watcherValues.length % 2 != 0) {
            System.out.print("Error! " + name() + " has odd number of params!");
        }
        this.entityId = entityId;
    }

    public float[] getBoundingBox() {
        return boundingBox;
    }

    public Object[] getDataWatcher() {
        return dataWatcher;
    }

    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    public int getEntityId() {
        return entityId;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return this != ARMOR_STAND;
    }

}
