package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.ChatUtils;
import com.cyprias.ChestShopFinder.utils.MathUtil;
import com.cyprias.ChestShopFinder.utils.MinecraftFontWidthCalculator;

public class BuyCommand implements Command {

	
	
	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.SEARCH, "/%s buy <itemName>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.BUY))
			list.add("/%s buy - Find where to buy an item.");
	}

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, final String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.BUY))
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
					shops = Plugin.database.findBuySellItemNearby(stock, p.getLocation(), true);
				} catch (SQLException e) {
					ChatUtils.error(sender, "Exception caught while executing this command.");
					e.printStackTrace();
					return;
				}
				if (shops == null || shops.size() == 0){
					ChatUtils.send(sender, "§7No shop sells §f" + MaterialUtil.getName(stock) + "§7.");
					return;
				}
				
				
				double closest = p.getLocation().distance(shops.get(0).getLocation());
				double farthest = p.getLocation().distance(shops.get(shops.size()-1).getLocation());
				
				double between = farthest - closest;

				compareShops comparator = new compareShops();
				Collections.sort(shops, comparator);
				
				
				
				ChatUtils.send(sender,String.format("§f%s §7results for §f%s§7.", shops.size(), MaterialUtil.getName(stock)));
				SearchCommand.previousResults.put(sender.getName(), shops);
				// [1] Cyprias has 16 for $2 22 blocks north of you.
				//String shopFormat = "§f%s§7: §f%s §7sells §f%s §7(§f%s§7) for $§f%s §7($§f%s§7e), §f%s §f%s";
				String shopFormat = "§f%s§7: §f%s §7sells §f%s §7for $§f%s §7($§f%s§7e), §f%s §f%s";
				
				String sDir;
				
				Shop shop;
				int pl = Config.getInt("properties.price-rounding");
				String each;
				Location sLoc;
				double dist;
				double distP;
				
				String[] s1 = new String[shops.size()];
				String[] s2 = new String[shops.size()];
				String[] s3 = new String[shops.size()];
				String[] s4 = new String[shops.size()];
				String[] s5 = new String[shops.size()];
				String[] s6 = new String[shops.size()];
				String[] s7 = new String[shops.size()];
				//String[] s8 = new String[shops.size()];
				
				for (int i=0; i<shops.size(); i++){
					shop = shops.get(i);
					
					each = Plugin.Round(shop.buyPrice/shop.amount,pl);
					sLoc = shop.getLocation();
					sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));

					dist = p.getLocation().distance(sLoc);
					
	
					distP = ((dist - closest) / between) * 100;
					distP = Math.max(distP, 0);
					
					//ChatUtils.send(sender, String.format(shopFormat, i+1, Plugin.getPlayerName(shop.owner), shop.amount, Plugin.Round(shop.buyPrice, pl), each, Plugin.getDistanceColour(distP) + Plugin.Round(dist), sDir));

					s1[i] = ""+(i + 1);
					
					s2[i] = Plugin.getPlayerName(shop.owner);
					s3[i] = ""+shop.amount;
					//s4[i] = ""+shop.inStock;
					s4[i] = Plugin.Round(shop.buyPrice, pl);
					s5[i] = each;
					s6[i] = Plugin.getDistanceColour(distP) + Plugin.Round(dist);
					s7[i] = sDir;
					
					//s8[i] = ;
					//s9[i] = ;
					
					
					
					
					
				}
				if (Config.getBoolean("properties.white-space-results")){
					s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
					s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
					s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
					s4 = MinecraftFontWidthCalculator.getWhitespacedStrings(s4);
					s5 = MinecraftFontWidthCalculator.getWhitespacedStrings(s5);
					s6 = MinecraftFontWidthCalculator.getWhitespacedStrings(s6);
					s7 = MinecraftFontWidthCalculator.getWhitespacedStrings(s7);
				//	s8 = MinecraftFontWidthCalculator.getWhitespacedStrings(s8);
					
				}
				
				for (int i=0;i<s1.length;i++)
					ChatUtils.send(sender,String.format(shopFormat, s1[i], s2[i], s3[i], s4[i], s5[i], s6[i], s7[i]));
				
				
				
				
			}
		});
		
		return true;
	}
	
	
	public class compareShops implements Comparator<Shop> {
		public int compare(Shop o1, Shop o2) {
			if ((o1.buyPrice/o1.amount) > (o2.buyPrice/o2.amount)) {
				return +1;
			} else if ((o1.buyPrice/o1.amount) < (o2.buyPrice/o2.amount)) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
