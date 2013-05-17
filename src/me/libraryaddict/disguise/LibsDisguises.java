package me.libraryaddict.disguise;

import java.lang.reflect.Method;
import java.util.List;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.MiscDisguise;
import me.libraryaddict.disguise.DisguiseTypes.MobDisguise;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import net.minecraft.server.v1_5_R3.DataWatcher;
import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin {

    public void onEnable() {
        getCommand("disguise").setExecutor(new DisguiseCommand());
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.NAMED_ENTITY_SPAWN,
                        Packets.Server.ENTITY_METADATA) {
                    public void onPacketSending(PacketEvent event) {
                        StructureModifier<Object> mods = event.getPacket().getModifier();
                        try {
                            Player observer = event.getPlayer();
                            org.bukkit.entity.Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);
                            if (entity instanceof Player) {
                                Player watched = (Player) entity;
                                if (DisguiseAPI.isDisguised(watched.getName())) {
                                    Disguise disguise = DisguiseAPI.getDisguise(watched);
                                    if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                                        if (disguise.getType().isMob()) {
                                            event.setCancelled(true);
                                            DisguiseAPI.disguiseToPlayer(watched, observer, (MobDisguise) disguise);
                                        } else if (disguise.getType().isMisc()) {
                                            event.setCancelled(true);
                                            DisguiseAPI.disguiseToPlayer(watched, observer, (MiscDisguise) disguise);
                                        } else if (disguise.getType().isPlayer()) {
                                            String name = (String) mods.read(1);
                                            if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                                event.setCancelled(true);
                                                DisguiseAPI.disguiseToPlayer(watched, observer, (PlayerDisguise) disguise);
                                            }
                                        }
                                    } else if (!disguise.getType().isPlayer()) {
                                        mods.write(1, modifyDataWatcher(disguise, watched));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    private List modifyDataWatcher(Disguise disguise, Player p) {
        Entity e = disguise.getEntity();
        EntityPlayer hE = ((CraftPlayer) p).getHandle();
        e.setAirTicks(hE.getAirTicks());
        e.fireTicks = p.getFireTicks();
        a(e.getDataWatcher(), 0, e.fireTicks > 0);
        e.setSprinting(p.isSprinting());
        e.setSneaking(p.isSneaking());
        if (e instanceof EntityLiving) {
            EntityLiving lE = (EntityLiving) e;
            lE.setInvisible(hE.isInvisible());
            lE.effects = hE.effects;
            lE.updateEffects = true;
            try {
                Method method = EntityLiving.class.getDeclaredMethod("bA");
                method.setAccessible(true);
                method.invoke(lE);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return e.getDataWatcher().b();
    }

    protected void a(DataWatcher datawatcher, int i, boolean flag) {
        byte b0 = datawatcher.getByte(0);
        if (flag) {
            datawatcher.watch(0, Byte.valueOf((byte) (b0 | 1 << i)));
        } else {
            datawatcher.watch(0, Byte.valueOf((byte) (b0 & ~(1 << i))));
        }
    }
}