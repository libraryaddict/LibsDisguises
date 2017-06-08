package me.libraryaddict.disguise.utilities.packetlisteners;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.utilities.DisguiseSound;
import me.libraryaddict.disguise.utilities.DisguiseSound.SoundType;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class PacketListenerSounds extends PacketAdapter {
    /**
     * This is a fix for the stupidity that is
     * "I can't separate the sounds from the sounds the player heard, and the sounds of the entity tracker heard"
     */
    private static boolean cancelSound;

    public PacketListenerSounds(LibsDisguises plugin) {
        super(plugin, ListenerPriority.NORMAL, Server.NAMED_SOUND_EFFECT, Server.ENTITY_STATUS);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.isAsync()) {
            return;
        }

        if (event.getPlayer().getName().contains("UNKNOWN[")) // If the player is temporary
            return;

        event.setPacket(event.getPacket().deepClone());

        StructureModifier<Object> mods = event.getPacket().getModifier();

        Player observer = event.getPlayer();

        if (event.getPacketType() == Server.NAMED_SOUND_EFFECT) {
            SoundType soundType = null;

            int[] soundCords = new int[]{(Integer) mods.read(2), (Integer) mods.read(3), (Integer) mods.read(4)};

            int chunkX = (int) Math.floor((soundCords[0] / 8D) / 16D);
            int chunkZ = (int) Math.floor((soundCords[2] / 8D) / 16D);

            if (!observer.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                return;
            }

            Entity disguisedEntity = null;
            DisguiseSound entitySound = null;

            Disguise disguise = null;

            String soundEffect = ReflectionManager.convertSoundEffectToString(mods.read(0));
            Entity[] entities = observer.getWorld().getChunkAt(chunkX, chunkZ).getEntities();

            for (Entity entity : entities) {
                Disguise entityDisguise = DisguiseAPI.getDisguise(observer, entity);

                if (entityDisguise != null) {
                    Location loc = entity.getLocation();

                    int[] entCords = new int[]{(int) (loc.getX() * 8), (int) (loc.getY() * 8), (int) (loc.getZ() * 8)};

                    if (soundCords[0] != entCords[0] || soundCords[1] != entCords[1] || soundCords[2] != entCords[2]) {
                        continue;
                    }

                    entitySound = DisguiseSound.getType(entity.getType().name());

                    if (entitySound == null) {
                        continue;
                    }

                    Object obj = null;

                    if (entity instanceof LivingEntity) {
                        try {
                            // Use reflection so that this works for either int or double methods
                            obj = LivingEntity.class.getMethod("getHealth").invoke(entity);

                            if (obj instanceof Double ? (Double) obj == 0 : (Integer) obj == 0) {
                                soundType = SoundType.DEATH;
                            } else {
                                obj = null;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (obj == null) {
                        boolean hasInvun = false;

                        Object nmsEntity = ReflectionManager.getNmsEntity(entity);

                        try {
                            if (entity instanceof LivingEntity) {
                                hasInvun = ReflectionManager.getNmsField("Entity", "noDamageTicks").getInt(
                                        nmsEntity) == ReflectionManager.getNmsField("EntityLiving",
                                        "maxNoDamageTicks").getInt(nmsEntity);
                            } else {
                                Class clazz = ReflectionManager.getNmsClass("DamageSource");

                                hasInvun = (Boolean) ReflectionManager.getNmsMethod("Entity", "isInvulnerable",
                                        clazz).invoke(nmsEntity,
                                        ReflectionManager.getNmsField(clazz, "GENERIC").get(null));
                            }
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        soundType = entitySound.getType(soundEffect, !hasInvun);
                    }

                    if (soundType != null) {
                        disguise = entityDisguise;
                        disguisedEntity = entity;
                        break;
                    }
                }
            }

            if (disguise != null && disguise.isSoundsReplaced() && (disguise.isSelfDisguiseSoundsReplaced() || disguisedEntity != observer)) {
                String sound = null;

                DisguiseSound dSound = DisguiseSound.getType(disguise.getType().name());

                if (dSound != null)
                    sound = dSound.getSound(soundType);

                if (sound == null) {
                    event.setCancelled(true);
                } else {
                    if (sound.equals("step.grass")) {
                        try {
                            int typeId = observer.getWorld().getBlockTypeIdAt((int) Math.floor(soundCords[0] / 8D),
                                    (int) Math.floor(soundCords[1] / 8D), (int) Math.floor(soundCords[2] / 8D));

                            Object block = ReflectionManager.getNmsMethod("RegistryMaterials", "getId",
                                    int.class).invoke(ReflectionManager.getNmsField("Block", "REGISTRY").get(null),
                                    typeId);

                            if (block != null) {
                                Object step = ReflectionManager.getNmsField("Block", "stepSound").get(block);

                                mods.write(0, ReflectionManager.getNmsMethod(step.getClass(), "d").invoke(step));
                                mods.write(1, ReflectionManager.getSoundCategory(disguise.getType()));
                            }
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        // There is no else statement. Because seriously. This should never be null. Unless
                        // someone is
                        // sending fake sounds. In which case. Why cancel it.
                    } else {
                        mods.write(0, ReflectionManager.getCraftSoundEffect(sound));
                        mods.write(1, ReflectionManager.getSoundCategory(disguise.getType()));

                        // Time to change the pitch and volume
                        if (soundType == SoundType.HURT || soundType == SoundType.DEATH || soundType == SoundType.IDLE) {
                            // If the volume is the default
                            if (mods.read(5).equals(entitySound.getDamageAndIdleSoundVolume())) {
                                mods.write(5, dSound.getDamageAndIdleSoundVolume());
                            }

                            // Here I assume its the default pitch as I can't calculate if its real.
                            if (disguise instanceof MobDisguise && disguisedEntity instanceof LivingEntity && ((MobDisguise) disguise).doesDisguiseAge()) {
                                boolean baby = false;

                                if (disguisedEntity instanceof Zombie) {
                                    baby = ((Zombie) disguisedEntity).isBaby();
                                } else if (disguisedEntity instanceof Ageable) {
                                    baby = !((Ageable) disguisedEntity).isAdult();
                                }

                                if (((MobDisguise) disguise).isAdult() == baby) {
                                    float pitch = (Float) mods.read(6);

                                    if (baby) {
                                        // If the pitch is not the expected
                                        if (pitch > 97 || pitch < 111)
                                            return;

                                        pitch = (DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F + 1.5F;
                                        // Min = 1.5
                                        // Cap = 97.5
                                        // Max = 1.7
                                        // Cap = 110.5
                                    } else {
                                        // If the pitch is not the expected
                                        if (pitch >= 63 || pitch <= 76)
                                            return;

                                        pitch = (DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F + 1.0F;
                                        // Min = 1
                                        // Cap = 63
                                        // Max = 1.2
                                        // Cap = 75.6
                                    }

                                    pitch *= 63;

                                    if (pitch < 0)
                                        pitch = 0;

                                    if (pitch > 255)
                                        pitch = 255;

                                    mods.write(6, pitch);
                                }
                            }
                        }
                    }
                }
            }
        } else if (event.getPacketType() == Server.ENTITY_STATUS) {
            if ((byte) mods.read(1) != 2) {
                return;
            }

            // It made a damage animation
            Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);

            Disguise disguise = DisguiseAPI.getDisguise(observer, entity);

            if (disguise != null && !disguise.getType().isPlayer() && (disguise.isSelfDisguiseSoundsReplaced() || entity != event.getPlayer())) {
                DisguiseSound disSound = DisguiseSound.getType(entity.getType().name());

                if (disSound == null)
                    return;

                SoundType soundType = null;
                Object obj = null;

                if (entity instanceof LivingEntity) {
                    try {
                        obj = LivingEntity.class.getMethod("getHealth").invoke(entity);

                        if (obj instanceof Double ? (Double) obj == 0 : (Integer) obj == 0) {
                            soundType = SoundType.DEATH;
                        } else {
                            obj = null;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (obj == null) {
                    soundType = SoundType.HURT;
                }

                if (disSound.getSound(
                        soundType) == null || (disguise.isSelfDisguiseSoundsReplaced() && entity == event.getPlayer())) {
                    if (disguise.isSelfDisguiseSoundsReplaced() && entity == event.getPlayer()) {
                        cancelSound = !cancelSound;

                        if (cancelSound)
                            return;
                    }

                    disSound = DisguiseSound.getType(disguise.getType().name());

                    if (disSound != null) {
                        String sound = disSound.getSound(soundType);

                        if (sound != null) {
                            Location loc = entity.getLocation();

                            PacketContainer packet = new PacketContainer(Server.NAMED_SOUND_EFFECT);

                            mods = packet.getModifier();

                            Object craftSoundEffect = ReflectionManager.getCraftSoundEffect(sound);

                            mods.write(0, craftSoundEffect);
                            mods.write(1, ReflectionManager.getSoundCategory(disguise.getType())); // Meh
                            mods.write(2, (int) (loc.getX() * 8D));
                            mods.write(3, (int) (loc.getY() * 8D));
                            mods.write(4, (int) (loc.getZ() * 8D));
                            mods.write(5, disSound.getDamageAndIdleSoundVolume());

                            float pitch;

                            if (disguise instanceof MobDisguise && !((MobDisguise) disguise).isAdult()) {
                                pitch = (DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F + 1.5F;
                            } else
                                pitch = (DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F + 1.0F;

                            if (disguise.getType() == DisguiseType.BAT)
                                pitch *= 95F;

                            pitch *= 63;

                            if (pitch < 0)
                                pitch = 0;

                            if (pitch > 255)
                                pitch = 255;

                            mods.write(6, (int) pitch);

                            try {
                                ProtocolLibrary.getProtocolManager().sendServerPacket(observer, packet, false);
                            }
                            catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
