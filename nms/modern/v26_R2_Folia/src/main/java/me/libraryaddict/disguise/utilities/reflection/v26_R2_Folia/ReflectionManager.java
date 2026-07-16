package me.libraryaddict.disguise.utilities.reflection.v26_R2_Folia;

import net.minecraft.server.level.ChunkMap;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class ReflectionManager extends me.libraryaddict.disguise.utilities.reflection.v26_R2.ReflectionManager {
    public ReflectionManager() {
        super();
    }

    @Override
    public ChunkMap.TrackedEntity getEntityTracker(Entity target) {
        // Folia exposes tracked entities through Moonrise rather than Paper's tracker map.
        return ((CraftEntity) target).getHandle().moonrise$getTrackedEntity();
    }
}
