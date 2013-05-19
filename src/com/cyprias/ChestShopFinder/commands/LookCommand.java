package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.ChatUtils;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class LookCommand  implements Command {
	
	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.LOOK))
			return false;
		
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
			
			ChatUtils.send(sender, "§7You have not searched anything.");
			return true;
		}
		
		List<Shop> shops = SearchCommand.previousResults.get(sender.getName());
		
		if (index >= shops.size()){
			ChatUtils.send(sender, "§7That index does not exist in your previous query.");
			return true;
		}

		Shop shop = shops.get(index);
		
		
		Location sLoc = shop.getLocation();
		double x = shop.getX();
		double y = shop.getY() - 1;
		double z = shop.getZ();
		
		Player p = (Player) sender;
		
		Location pLoc = p.getLocation();
		float yaw = MathUtil.getLookAtYaw(pLoc, sLoc) + 90;
		pLoc.setYaw(yaw);
		
		final double pX = p.getLocation().getX();
		final double pZ = p.getLocation().getZ();
		
		double motX = x - pX;
		double motY = y - p.getLocation().getY();
		double motZ = z - pZ;
		
		float pitch = MathUtil.getLookAtPitch(motX, motY, motZ);
	
		pLoc.setPitch(pitch);
		p.teleport(pLoc);
		
		
		
		String msg = "§7Looking at §f%s§7's shop, §f%s §7blocks §f%s";
		String sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));
		
		ChatUtils.send(sender, String.format(msg, Plugin.getPlayerName(shop.owner), Plugin.Round(p.getLocation().distance(sLoc)), sDir));
		
		return true;
	}
	
	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.LOOK, "/%s look <index>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.LOOK) && SearchCommand.previousResults.containsKey(sender.getName()))
			list.add("/%s look - Look in the direction of a shop index.");
	}
}
