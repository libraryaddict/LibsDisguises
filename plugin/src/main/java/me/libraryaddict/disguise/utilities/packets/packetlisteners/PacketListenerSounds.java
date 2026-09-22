package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseRunnable;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.sounds.DisguiseChunkTracker;
import me.libraryaddict.disguise.utilities.sounds.DisguiseSound;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup.SoundType;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedEntity;
import me.libraryaddict.disguise.utilities.wrapped.IWrappedPlayer;
import me.libraryaddict.disguise.utilities.wrapped.WrappedManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;

public class PacketListenerSounds extends SimplePacketListenerAbstract {
    /**
     * If the entity is within range of the threshold
     */
    private boolean isNearby(Vector3i packetLocation, Location entityLocation, int threshold) {
        if (Math.abs(packetLocation.getX() - (int) (entityLocation.getX() * 8)) > threshold) {
            return false;
        }

        if (Math.abs(packetLocation.getY() - (int) (entityLocation.getY() * 8)) > threshold) {
            return false;
        }

        return Math.abs(packetLocation.getZ() - (int) (entityLocation.getZ() * 8)) <= threshold;
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // TODO May need to add named_sound_effect, depends if MC would send the sound itself or not for a normal entity
        if (event.getPacketType() != Server.ENTITY_SOUND_EFFECT && event.getPacketType() != Server.SOUND_EFFECT) {
            return;
        }

        Player observer = event.getPlayer();

        if (observer == null || DisguiseUtilities.getDisguises().isEmpty()) {
            return;
        }

        IWrappedPlayer wrappedPlayer = WrappedManager.getWrappedPlayer(observer);

        Sound sound;
        ResourceLocation soundId;
        float volume;
        float pitch;
        WrapperPlayServerSoundEffect soundEffect = null;
        WrapperPlayServerEntitySoundEffect entitySoundEffect = null;
        Disguise disguise = null;
        SoundGroup group = null;

        if (event.getPacketType() == Server.SOUND_EFFECT) {
            soundEffect = new WrapperPlayServerSoundEffect(event);

            volume = soundEffect.getVolume();
            pitch = soundEffect.getPitch();
            sound = soundEffect.getSound();

            if (sound == null || (soundId = sound.getSoundId()) == null) {
                // Set to null so PE doesn't try to re-encode it
                event.setLastUsedWrapper(null);
                return;
            }

            if (!SoundGroup.isReplaceableSound(soundId)) {
                return;
            }

            Vector3i loc = soundEffect.getEffectPosition();
            World world = wrappedPlayer.getWorld();

            loop:
            for (IWrappedEntity<?> entity : DisguiseChunkTracker.getDisguisedNearby(loc)) {
                Location entityLocation = entity.getLocation();

                if (entityLocation.getWorld() != world || !isNearby(loc, entityLocation, 2)) {
                    continue;
                }

                Set<TargetedDisguise> disguises = DisguiseUtilities.getDisguises().get(entity.getEntityId());

                if (disguises == null) {
                    continue;
                }

                SoundGroup entityGroup = SoundGroup.getGroup(entity);

                if (entityGroup == null || entityGroup.getSound(soundId) == null) {
                    continue;
                }

                for (TargetedDisguise entityDisguise : disguises) {
                    if (!entityDisguise.isSoundsReplaced() || !entityDisguise.canSee(wrappedPlayer)) {
                        continue;
                    }

                    disguise = entityDisguise;
                    group = entityGroup;

                    break loop;
                }
            }
        } else {
            entitySoundEffect = new WrapperPlayServerEntitySoundEffect(event);

            volume = entitySoundEffect.getVolume();
            pitch = entitySoundEffect.getPitch();
            sound = entitySoundEffect.getSound();

            if (sound == null) {
                // Set to null so PE doesn't try to re-encode it
                event.setLastUsedWrapper(null);
                return;
            }

            soundId = sound.getSoundId();
            disguise = DisguiseUtilities.getDisguise(wrappedPlayer, entitySoundEffect.getEntityId());
        }

        if (disguise == null || !disguise.isSoundsReplaced()) {
            return;
        }

        IWrappedEntity<?> entity = disguise.getWrappedEntity();

        if (entity == wrappedPlayer && !disguise.isSelfDisguiseSoundsReplaced()) {
            return;
        }

        if (group == null) {
            group = SoundGroup.getGroup(entity);
        }

        if (group == null) {
            return;
        }

        SoundType soundType = group.getType(soundId);

        if (soundType == null) {
            return;
        }

        // Idle sounds are reset on hurt
        if (soundType == SoundType.HURT && disguise.isPlayIdleSounds()) {
            DisguiseRunnable runnable = disguise.getInternals().getRunnable();

            if (runnable != null) {
                runnable.resetAmbientSoundTime();
            }
        }

        SoundGroup disguiseSound = SoundGroup.getGroup(disguise);

        if (disguiseSound == null) {
            event.setCancelled(true);
            return;
        }

        DisguiseSound newSound = disguiseSound.getSound(soundType, soundId, group);

        if (newSound == null || newSound.getSound() == null) {
            event.setCancelled(true);
            return;
        }

        if (newSound.hasVolume()) {
            volume = newSound.getVolume();
        } else if (volume == group.getDamageAndIdleSoundVolume()) {
            // If the volume is the default, set it to what the real disguise sound group expects
            volume = disguiseSound.getDamageAndIdleSoundVolume();
        }

        if (newSound.hasPitch()) {
            pitch = newSound.getPitch();
        } else if (disguise instanceof MobDisguise && entity.getEntity() instanceof LivingEntity &&
            ((MobDisguise) disguise).doesDisguiseAge()) {
            if (((MobDisguise) disguise).isAdult()) {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.0F;
            } else {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.4F;
            }
        }

        Sound nSound = Sounds.getByName(newSound.toString());

        if (nSound == null) {
            event.setCancelled(true);
            // Well then, api is lacking. May as well send via bukkit methods
            Location loc = entity.getLocation();
            // Namespace was a 1.16 thing, so 1.16+ we will include the 'minecraft:'
            wrappedPlayer.playSound(loc, NmsVersion.v1_16.isSupported() ? newSound.toString() : newSound.getKey(),
                disguise.getEffectiveSoundCategory().getBukkitSoundCategory(disguise), volume, pitch);
            return;
        }

        SoundCategory soundCat = disguise.getEffectiveSoundCategory().getSoundCategory(disguise);

        if (soundEffect != null) {
            soundEffect.setSound(nSound);
            soundEffect.setVolume(volume);
            soundEffect.setPitch(pitch);
            soundEffect.setSoundCategory(soundCat);
        } else {
            entitySoundEffect.setSound(nSound);
            entitySoundEffect.setVolume(volume);
            entitySoundEffect.setPitch(pitch);
            entitySoundEffect.setSoundCategory(soundCat);
        }

        event.markForReEncode(true);
    }
}
