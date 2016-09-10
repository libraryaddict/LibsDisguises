package me.libraryaddict.disguise.commands;

import java.util.UUID;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguiseListCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender.hasPermission("libsdisguises.seecmd.disguiselist"))
		{
			String players = "";
			int numPlayers = 0;
			int numOther = 0;
			
			// Go through all disguises
			for (UUID uuid: DisguiseUtilities.getDisguises().keySet())
			{
				Player player = Bukkit.getPlayer(uuid);
				TargetedDisguise disguise = DisguiseUtilities.getMainDisguise(uuid);

				if (player == null || !player.isOnline())
				{
					// Assume this is a non-player entity
					numOther++;
				}
				else
				{
					// This is a player
					numPlayers++;
					players += " " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + "("
							+ disguise.getType().toReadable();
					
					// Special treatment if the disguise is a player
					if (disguise.getType() == DisguiseType.PLAYER && disguise instanceof PlayerDisguise)
						players += ":" + ((PlayerDisguise)disguise).getName();
					
					players += ")";
				}
			}
			
			// Formatting
			players = "" + ChatColor.AQUA + numPlayers + ChatColor.DARK_AQUA 
					+ " disguised players:" + players;
			String entities = ChatColor.DARK_AQUA + "Also " + ChatColor.AQUA 
					+ numOther + ChatColor.DARK_AQUA + " other diguised entities.";
			
			sender.sendMessage(new String[] {players, entities});
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "You are forbidden to use this command.");
		}
		return true;
	}
}
