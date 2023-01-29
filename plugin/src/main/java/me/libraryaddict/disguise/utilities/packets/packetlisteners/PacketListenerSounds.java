package me.libraryaddict.disguise.utilities.packets.packetlisteners;

import com.comphenix.protocol.PacketType;
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
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
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
        super(plugin, ListenerPriority.NORMAL,
            NmsVersion.v1_19_R2.isSupported() ? new PacketType[]{Server.NAMED_SOUND_EFFECT, Server.ENTITY_SOUND} : new PacketType[]{Server.NAMED_SOUND_EFFECT});
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

        SoundType soundType;
        SoundGroup soundGroup = null;
        Object soundEffectObj = mods.read(0);
        int offset = 0;

        Disguise disguise = null;
        Entity entity = null;

        if (event.getPacketType() == Server.NAMED_SOUND_EFFECT) {
            offset = 3;

            int[] soundCords = new int[]{(Integer) mods.read(2), (Integer) mods.read(3), (Integer) mods.read(4)};

            loop:
            for (Set<TargetedDisguise> disguises : DisguiseUtilities.getDisguises().values()) {
                for (TargetedDisguise entityDisguise : disguises) {
                    entity = entityDisguise.getEntity();

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
                    soundGroup = SoundGroup.getGroup(entity.getType().name());

                    break loop;
                }
            }
        } else {
            disguise = DisguiseUtilities.getDisguise(observer, (int) mods.read(2));

            if (disguise == null) {
                return;
            }

            entity = disguise.getEntity();
            soundGroup = SoundGroup.getGroup(entity.getType().name());
        }

        if (disguise == null || !disguise.isSoundsReplaced()) {
            return;
        }

        if (soundGroup == null || soundGroup.getSound(soundEffectObj) == null) {
            return;
        }

        if ((!(entity instanceof LivingEntity)) || ((LivingEntity) entity).getHealth() > 0) {
            soundType = soundGroup.getType(soundEffectObj);
        } else {
            soundType = SoundType.DEATH;
        }

        if (entity == observer && !disguise.isSelfDisguiseSoundsReplaced()) {
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
        float volume = (float) mods.read(offset + 2);
        float pitch = (float) mods.read(offset + 3);

        // If the volume is the default, set it to what the real disguise sound group expects
        if (volume == soundGroup.getDamageAndIdleSoundVolume()) {
            volume = disguiseSound.getDamageAndIdleSoundVolume();
        }

        if (disguise instanceof MobDisguise && entity instanceof LivingEntity && ((MobDisguise) disguise).doesDisguiseAge()) {
            if (((MobDisguise) disguise).isAdult()) {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.0F;
            } else {
                pitch = ((DisguiseUtilities.random.nextFloat() - DisguiseUtilities.random.nextFloat()) * 0.2F) + 1.4F;
            }
        }

        PacketContainer newPacket;

        if (!NmsVersion.v1_19_R2.isSupported() && sound.getClass().getSimpleName().equals("MinecraftKey")) {
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
        mods.write(offset + 2, volume);
        mods.write(offset + 3, pitch);

        event.setPacket(newPacket);
    }
}
