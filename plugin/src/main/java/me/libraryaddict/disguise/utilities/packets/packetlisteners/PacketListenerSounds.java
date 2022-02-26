package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup.SoundType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;

public class PacketListenerSounds extends PacketAdapter {

    public PacketListenerSounds(LibsDisguises plugin) {
        super(plugin, ListenerPriority.NORMAL, Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled() || event.isAsync() || event.isPlayerTemporary()) {
            return;
        }

        handleNamedSoundEffect(event);
    }

    private void handleNamedSoundEffect(PacketEvent event) {
        StructureModifier<Object> mods = event.getPacket().getModifier();
        Player observer = event.getPlayer();

        SoundType soundType = null;

        Entity disguisedEntity = null;
        SoundGroup soundGroup = null;
        Object soundEffectObj = mods.read(0);

        Disguise disguise = null;

        int[] soundCords = new int[]{(Integer) mods.read(2), (Integer) mods.read(3), (Integer) mods.read(4)};

        for (Set<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
            for (TargetedDisguise entityDisguise : disguises) {
                Entity entity = entityDisguise.getEntity();

                if (entity == null || entity.getWorld() != observer.getWorld()) {
                    continue;
                }

                if (!entityDisguise.canSee(observer)) {
                    continue;
                }

                Location loc = entity.getLocation();

                int[] entCords = new int[]{(int) (loc.getX() * 8), (int) (loc.getY() * 8), (int) (loc.getZ() * 8)};

                if (soundCords[0] != entCords[0] || soundCords[1] != entCords[1] || soundCords[2] != entCords[2]) {
                    continue;
                }

                disguise = entityDisguise;
                disguisedEntity = entity;
                soundGroup = SoundGroup.getGroup(entity.getType().name());

                if (soundGroup.getSound(soundEffectObj) == null) {
                    return;
                }

                if ((!(entity instanceof LivingEntity)) || ((LivingEntity) entity).getHealth() > 0) {
                    boolean hasInvun = ReflectionManager.hasInvul(entity);
                    soundType = soundGroup.getType(soundEffectObj, !hasInvun);
                } else {
                    soundType = SoundType.DEATH;
                }
            }

            if (disguise != null) {
                break;
            }
        }

        if (disguise == null || !disguise.isSoundsReplaced()) {
            return;
        }

        // Blocks null and CANCEL, HURT and DEATH are 100% handled by entity status!
        if (soundType != SoundType.STEP && soundType != SoundType.IDLE) {
            //event.setCancelled(true);
            // return;
        }

        if (disguisedEntity == observer && !disguise.isSelfDisguiseSoundsReplaced()) {
            return;
        }

        SoundGroup disguiseSound = SoundGroup.getGroup(disguise);

        if (disguiseSound == null) {
            event.setCancelled(true);
            return;
        }

        Object sound = disguiseSound.getSound(soundType);

        if (sound == null) {
            event.setCancelled(true);
            return;
        }

        Enum soundCat = ReflectionManager.getSoundCategory(disguise.getType());
        float volume = (float) mods.read(5);
        float pitch = (float) mods.read(6);

        // If the volume is the default, set it to what the real disguise sound group expects
        if (volume == soundGroup.getDamageAndIdleSoundVolume()) {
            volume = disguiseSound.getDamageAndIdleSoundVolume();
        }

        if (disguise instanceof MobDisguise && disguisedEntity instanceof LivingEntity && ((MobDisguise) disguise).doesDisguiseAge()) {
            if (((MobDisguise) disguise).isAdult()) {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.0F;
            } else {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.4F;
            }
        }

        PacketContainer newPacket;

        if (sound.getClass().getSimpleName().equals("MinecraftKey")) {
            newPacket = new PacketContainer(Server.CUSTOM_SOUND_EFFECT);
            StructureModifier<Object> newModifs = newPacket.getModifier();

            newModifs.write(2, mods.read(2));
            newModifs.write(3, mods.read(3));
            newModifs.write(4, mods.read(4));

            mods = newModifs;
        } else {
            newPacket = event.getPacket().shallowClone();
            mods = newPacket.getModifier();
        }

        mods.write(0, sound);
        mods.write(1, soundCat);
        mods.write(5, volume);
        mods.write(6, pitch);

        event.setPacket(newPacket);
    }
}
