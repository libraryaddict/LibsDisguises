package me.libraryaddict.disguise.utilities.packetlisteners;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.AnimalColor;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;

public class PacketListenerClientInteract extends PacketAdapter
{
    public PacketListenerClientInteract(LibsDisguises plugin)
    {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);
    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {

        if (event.isCancelled())
            return;

        try
        {
            Player observer = event.getPlayer();

            if (observer.getName().contains("UNKNOWN[")) // If the player is temporary
                return;

            StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());

            Entity entity = entityModifer.read(0);

            if (entity instanceof ExperienceOrb || entity instanceof Item || entity instanceof Arrow || entity == observer)
            {
                event.setCancelled(true);
            }

            for (ItemStack item : new ItemStack[]
                {
                        observer.getInventory().getItemInMainHand(), observer.getInventory().getItemInOffHand()
                })
            {
                if (item == null || item.getType() != Material.INK_SACK)
                    continue;

                Disguise disguise = DisguiseAPI.getDisguise(observer, entity);

                if (disguise == null || (disguise.getType() != DisguiseType.SHEEP && disguise.getType() != DisguiseType.WOLF))
                    continue;

                AnimalColor color = AnimalColor.getColor(item.getDurability());

                if (disguise.getType() == DisguiseType.SHEEP)
                {
                    SheepWatcher watcher = (SheepWatcher) disguise.getWatcher();

                    watcher.setColor(DisguiseConfig.isSheepDyeable() ? color : watcher.getColor());
                }
                else
                {
                    WolfWatcher watcher = (WolfWatcher) disguise.getWatcher();

                    watcher.setCollarColor(DisguiseConfig.isWolfDyeable() ? color : watcher.getCollarColor());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
