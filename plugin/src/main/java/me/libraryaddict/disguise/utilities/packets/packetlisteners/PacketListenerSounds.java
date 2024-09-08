package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup;
import me.libraryaddict.disguise.utilities.sounds.SoundGroup.SoundType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Set;

public class PacketListenerSounds extends SimplePacketListenerAbstract {
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

        if (observer == null) {
            return;
        }

        Sound sound;
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

            if (sound == null || sound.getSoundId() == null) {
                // Set to null so PE doesn't try to re-encode it
                event.setLastUsedWrapper(null);
                return;
            }

            String soundKey = NmsVersion.v1_16.isSupported() ? sound.getSoundId().toString() : sound.getSoundId().getKey();

            Vector3i loc = soundEffect.getEffectPosition();

            loop:
            for (Set<TargetedDisguise> disguises : new ArrayList<>(DisguiseUtilities.getDisguises().values())) {
                for (TargetedDisguise entityDisguise : new ArrayList<>(disguises)) {
                    Entity entity = entityDisguise.getEntity();

                    if (entity == null || entity.getWorld() != observer.getWorld()) {
                        continue;
                    }

                    if (!entityDisguise.canSee(observer)) {
                        continue;
                    }

                    Location eLoc = entity.getLocation();

                    int[] entCords = new int[]{(int) (eLoc.getX() * 8), (int) (eLoc.getY() * 8), (int) (eLoc.getZ() * 8)};

                    // If entity is within 0.25 blocks of the sound, because the packet isn't immediate..
                    if (Math.abs(loc.getX() - entCords[0]) > 2 || Math.abs(loc.getY() - entCords[1]) > 2 ||
                        Math.abs(loc.getZ() - entCords[2]) > 2) {
                        continue;
                    }

                    group = SoundGroup.getGroup(entity.getType().name());

                    if (group == null) {
                        continue;
                    }

                    if (group.getSound(soundKey) == null) {
                        continue;
                    }

                    disguise = entityDisguise;

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

            disguise = DisguiseUtilities.getDisguise(observer, entitySoundEffect.getEntityId());
        }

        if (disguise == null || !disguise.isSoundsReplaced()) {
            return;
        }

        Entity entity = disguise.getEntity();

        if (entity == observer && !disguise.isSelfDisguiseSoundsReplaced()) {
            return;
        }

        if (group == null) {
            group = SoundGroup.getGroup(entity.getType().name());
        }

        if (group == null) {
            return;
        }

        // Prior to 1.16 didn't use resource key afaik
        String asString = NmsVersion.v1_16.isSupported() ? sound.getSoundId().toString() : sound.getSoundId().getKey();
        SoundType soundType = group.getType(asString);

        if (soundType == null) {
            return;
        }

        SoundGroup disguiseSound = SoundGroup.getGroup(disguise);

        if (disguiseSound == null) {
            event.setCancelled(true);
            return;
        }

        String newSound = disguiseSound.getSound(soundType);

        if (newSound == null) {
            event.setCancelled(true);
            return;
        }

        // If the volume is the default, set it to what the real disguise sound group expects
        if (volume == group.getDamageAndIdleSoundVolume()) {
            volume = disguiseSound.getDamageAndIdleSoundVolume();
        }

        if (disguise instanceof MobDisguise && entity instanceof LivingEntity && ((MobDisguise) disguise).doesDisguiseAge()) {
            if (((MobDisguise) disguise).isAdult()) {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.0F;
            } else {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.4F;
            }
        }

        Sound nSound = Sounds.getByName(newSound);

        if (nSound == null) {
            event.setCancelled(true);
            // Well then, api is lacking. May as well send via bukkit methods
            Location loc = entity.getLocation();
            observer.playSound(loc, newSound, volume, pitch);
            return;
        }

        SoundCategory soundCat = ReflectionManager.getSoundCategory(disguise.getType());

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
