package com.cyprias.ChestShopFinder.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.ChatUtils;
import com.cyprias.ChestShopFinder.utils.MathUtil;
import com.cyprias.ChestShopFinder.utils.MinecraftFontWidthCalculator;

public class SearchCommand implements Command {
	public static HashMap<String, List<Shop>> previousResults = new HashMap<String, List<Shop>>();
	
	private String getPriceText(double buyPrice, double sellPrice){
		String t = "";

		if (buyPrice> 0){
			t = "B " + Plugin.Round(buyPrice, Config.getInt("properties.price-rounding"));
		}
		if (sellPrice > 0){
			if (t != "")
				t += ":";
			t += Plugin.Round(sellPrice, Config.getInt("properties.price-rounding")) + " S";
		}

		return t;
	}
	
	
	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, final String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.SEARCH))
			return false;
		
		if (args.length < 1){
			getCommands(sender, cmd);
			return true;
		}
		
		final ItemStack stock = MaterialUtil.getItem(StringUtil.joinArray(args));
		if (MaterialUtil.isEmpty(stock)) {
				ChatUtils.error(sender, "Unknown item: " + StringUtil.joinArray(args));
				return true;
		}
		
		final Player p = (Player) sender;
		final double pX = p.getLocation().getX();
		final double pZ = p.getLocation().getZ();
		

		
		Plugin.runTaskAsynchronously(new Runnable() {
			public void run() {
				List<Shop> shops = null;
				try {
					shops = Plugin.database.findItemNearby(stock, p.getLocation());
				} catch (SQLException e) {
					ChatUtils.error(sender, "Exception caught while executing this command.");
					e.printStackTrace();
					return;
				}
				if (shops == null || shops.size() == 0){
					ChatUtils.send(sender, "§7No shop for §f" + MaterialUtil.getName(stock) + "§7 found.");
					return;
				}
				
				double closest = p.getLocation().distance(shops.get(0).getLocation());
				double farthest = p.getLocation().distance(shops.get(shops.size()-1).getLocation());
				double between = farthest - closest;
				
				ChatUtils.send(sender,String.format("§f%s §7results for §f%s§7.", shops.size(), MaterialUtil.getName(stock)));
				
				previousResults.put(sender.getName(), shops);
				// [1] Cyprias has 16 for $2 22 blocks north of you.
				String shopFormat = "§f%s§7: §f%s §7has §f%s §7for §f%s§7, §f%s §f%s";
				String sDir;
				
				Shop shop;
				int pl = Config.getInt("properties.price-rounding");
				String each;
				Location sLoc;
				double dist, distP;
				
				String[] s1 = new String[shops.size()];
				String[] s2 = new String[shops.size()];
				String[] s3 = new String[shops.size()];
				String[] s4 = new String[shops.size()];
				String[] s5 = new String[shops.size()];
				String[] s6 = new String[shops.size()];
				//String[] s7 = new String[shops.size()];
				for (int i=0; i<shops.size(); i++){
					shop = shops.get(i);
					
					//each = Plugin.Round(shop.buyPrice/shop.amount,pl);
					sLoc = shop.getLocation();
					sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));

					dist = p.getLocation().distance(sLoc);
					distP = ((dist - closest) / between) * 100;
					distP = Math.max(distP, 0);
					
					//ChatUtils.send(sender, String.format(shopFormat, i+1, Plugin.getPlayerName(shop.owner), shop.amount, getPriceText(shop.buyPrice, shop.sellPrice),  Plugin.getDistanceColour(distP) + Plugin.Round(dist), sDir));

					s1[i] = ""+(i + 1);
					
					s2[i] = Plugin.getPlayerName(shop.owner);
					s3[i] = ""+shop.amount;
					//s4[i] = Plugin.Round(shop.buyPrice, pl);
					s4[i] = getPriceText(shop.buyPrice, shop.sellPrice);
					s5[i] = Plugin.getDistanceColour(distP) + Plugin.Round(dist);
					s6[i] = sDir;
					
					
				}
				if (Config.getBoolean("properties.white-space-results")){
					s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
					s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
					s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
					s4 = MinecraftFontWidthCalculator.getWhitespacedStrings(s4);
					s5 = MinecraftFontWidthCalculator.getWhitespacedStrings(s5);
					s6 = MinecraftFontWidthCalculator.getWhitespacedStrings(s6);
				}
				//s7 = MinecraftFontWidthCalculator.getWhitespacedStrings(s7);
				
				for (int i=0;i<s1.length;i++)
					ChatUtils.send(sender,String.format(shopFormat, s1[i], s2[i], s3[i], s4[i], s5[i], s6[i]));
				
				
			}
		});
		
		return true;
	}
	
	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.SEARCH, "/%s search <itemName>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.SEARCH))
			list.add("/%s search - Search for an item nearby.");
	}
}
