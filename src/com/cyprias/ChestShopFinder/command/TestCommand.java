package com.cyprias.ChestShopFinder.command;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.database.MySQL;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class TestCommand implements Command {

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		/*
		Player p = (Player) sender;
		
		
		List<Shop> shops = MySQL.findShopsNear(sender, p.getLocation());
		

		if (shops.size()>0){
			Location sLoc = shops.get(0).getLocation();
			
			double x = sLoc.getBlockX();
			double y = sLoc.getBlockY() - 1;
			double z = sLoc.getBlockZ();
					
			Location pLoc = p.getLocation();
			float yaw = MathUtil.getLookAtYaw(pLoc, sLoc) + 90;
			pLoc.setYaw(yaw);
			
			double motX = (x) - p.getLocation().getX();
			double motY = (y) - p.getLocation().getY();
			double motZ = (z) - p.getLocation().getZ();
			
			float pitch = MathUtil.getLookAtPitch(motX, motY, motZ);
		
			pLoc.setPitch(pitch);
			p.teleport(pLoc);
			
			
			for (int i=0; i<shops.size(); i++){
				
				sender.sendMessage(i + ": " + shops.get(i).id + " has " + shops.get(i).inStock + " " + pLoc.distance(sLoc) + " blocks away");
				
			}
			
		}
		*/
		
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.TEST, "/%s test", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.TEST))
			list.add("/%s test");
	}
}
