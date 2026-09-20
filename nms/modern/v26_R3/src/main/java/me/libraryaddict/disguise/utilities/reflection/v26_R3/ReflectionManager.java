package me.libraryaddict.disguise.utilities.reflection.v26_R3;

import com.mojang.authlib.minecraft.SessionService;

public class ReflectionManager extends ReflectionManagerLayered {
    @Override
    public SessionService getMinecraftSessionService() {
        return getMinecraftServer().services().sessionService();
    }

    @Override
    public final ItemStack getCraftItem(ItemStack bukkitItem) {
        return CraftItemStack.getCraftStack(bukkitItem);
    }

    @Override
    public boolean hasInvul(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (nmsEntity instanceof net.minecraft.world.entity.LivingEntity) {
            return nmsEntity.isTemporarilyInvulnerable();
        } else {
            return nmsEntity.isInvulnerableToBase(nmsEntity.damageSources().generic());
        }
    }

    @Override
    public Object registerEntityType(NamespacedKey key) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity> newEntity =
            new net.minecraft.world.entity.EntityType<>(null, MobCategory.MISC, false, false, false, false, null, null, 0, 0, 0,
                "descId." + key.toString(), Optional.empty(), FeatureFlagSet.of(), true, true);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(key), newEntity);
        newEntity.getDescriptionId();
        return newEntity;
    }

    @Override
    public List<ByteBuf> getRegistryPacketdata() {
        DynamicOps<Tag> dynamicOps = getMinecraftServer().registries().compositeAccess().createSerializationContext(NbtOps.INSTANCE);
        List<ByteBuf> registerBuf = new ArrayList<>();

        RegistrySynchronization.packRegistries(dynamicOps, getMinecraftServer().registries().getAccessFrom(RegistryLayer.WORLD),
            new HashSet<>(), (resourceKey, list) -> {
                ClientboundRegistryDataPacket packet = new ClientboundRegistryDataPacket(resourceKey, list);
                ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
                FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buf);

                ClientboundRegistryDataPacket.STREAM_CODEC.encode(friendlyByteBuf, packet);

                registerBuf.add(buf);
            });

        return registerBuf;
    }
}
