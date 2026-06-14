package me.libraryaddict.disguise.utilities.reflection.v26_R1_Folia;

import net.minecraft.server.level.ChunkMap;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class ReflectionManager extends me.libraryaddict.disguise.utilities.reflection.v26_R1.ReflectionManager {
    public ReflectionManager() {
        super();
    }

    @Override
    public ChunkMap.TrackedEntity getEntityTracker(Entity target) {
        // This method is not in spigot unfortunately
        return ((CraftEntity) target).getHandle().moonrise$getTrackedEntity();
    }
}
