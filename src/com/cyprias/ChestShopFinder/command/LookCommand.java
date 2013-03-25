package com.cyprias.ChestShopFinder.command;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class LookCommand  implements Command {
	
	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		
		if (args.length < 1 || args.length > 2){
			getCommands(sender, cmd);
			return true;
		}
		int index;
		if (Plugin.isInt(args[0])) {
			index = Integer.parseInt(args[0]);
		} else {
			ChatUtils.error(sender, "Invalid index: " +  args[0]);
			return true;
		}
		index -= 1; //our table indexs start at 0.
		
		if (!SearchCommand.previousResults.containsKey(sender.getName())){
			
			ChatUtils.send(sender, "You have not made any searches recently.");
			return true;
		}
		
		List<Shop> shops = SearchCommand.previousResults.get(sender.getName());
		
		if (index > shops.size()){
			ChatUtils.send(sender, "That index does nt exist in your previous search.");
			return true;
		}
		
		double x = shops.get(index).location.getBlockX();
		double y = shops.get(index).location.getBlockY() - 1;
		double z = shops.get(index).location.getBlockZ();
		
		Player p = (Player) sender;
		
		Location pLoc = p.getLocation();
		float yaw = MathUtil.getLookAtYaw(pLoc, shops.get(index).location) + 90;
		pLoc.setYaw(yaw);
		
		
		double motX = (x) - p.getLocation().getX();
		double motY = (y) - p.getLocation().getY();
		double motZ = (z) - p.getLocation().getZ();
		
		float pitch = MathUtil.getLookAtPitch(motX, motY, motZ);
	
		pLoc.setPitch(pitch);
		p.teleport(pLoc);
		
		return true;
	}
	
	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.LOOK, "/%s look", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.LOOK))
			list.add("/%s look");
	}
}
