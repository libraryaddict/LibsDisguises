package me.libraryaddict.disguise;

import java.util.concurrent.ConcurrentHashMap;

import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import net.minecraft.server.v1_5_R3.*;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class DisguiseAPI {

    private static ConcurrentHashMap<String, Disguise> disguises = new ConcurrentHashMap<String, Disguise>();

    /**
     * @param Player
     *            - The player to disguise
     * @param Disguise
     *            - The disguise to wear
     */
    public static void disguiseToAll(Player p, Disguise disguise) {
        disguises.put(p.getName(), disguise);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() != player.getWorld())
                continue;
            player.hidePlayer(p);
            player.showPlayer(p);
        }
    }

    /**
     * @param Player
     *            - The player who is being disguised
     * @param Player
     *            - The player who is watching the disguised
     * @param Disguise
     *            - The disguise he is wearing
     */
    public static void disguiseToPlayer(Player disguiser, Player observer, Disguise disguise) {
        disguises.put(disguiser.getName(), disguise);
        Packet29DestroyEntity destroyPacket = new Packet29DestroyEntity(new int[] { disguiser.getEntityId() });
        Packet spawnPacket = disguise.constructPacket(disguiser);
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(destroyPacket);
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(spawnPacket);

    }

    /**
     * @param Disguiser
     * @return Disguise
     */
    public static Disguise getDisguise(Player p) {
        return getDisguise(p.getName());
    }

    /**
     * @param Disguiser
     * @return Disguise
     */
    public static Disguise getDisguise(String name) {
        return disguises.get(name);
    }

    /**
     * @param Disguiser
     * @return Boolean - If the disguiser is disguised
     */
    public static boolean isDisguised(Player p) {
        return isDisguised(p.getName());
    }

    /**
     * @param Disguiser
     * @return boolean - If the disguiser is disguised
     */
    public static boolean isDisguised(String name) {
        return disguises.containsKey(name);
    }

    /**
     * @param Disguiser
     *            - Undisguises him
     */
    public static void undisguiseToAll(Player p) {
        disguises.remove(p.getName());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() != player.getWorld())
                continue;
            player.hidePlayer(p);
            player.showPlayer(p);
        }
    }

}