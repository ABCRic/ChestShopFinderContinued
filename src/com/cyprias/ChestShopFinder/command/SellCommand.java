package com.cyprias.ChestShopFinder.command;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class SellCommand implements Command {

	
	
	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.SEARCH, "/%s sell <itemName>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.SELL))
			list.add("/%s sell");
	}

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, final String[] args) throws SQLException {
		
		if (args.length < 1 || args.length > 2){
			getCommands(sender, cmd);
			return true;
		}
		
		final ItemStack stock = Plugin.getItemStack(args[0]);
		if (stock == null || stock.getTypeId() == 0) {
			ChatUtils.error(sender, "Unknown item: " + args[0]);
			return true;
		}
		
		final Player p = (Player) sender;
		final double pX = p.getLocation().getX();
		final double pZ = p.getLocation().getZ();
		
		
		
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				List<Shop> shops = null;
				try {
					shops = Plugin.database.findBuySellItemNearby(stock, p.getLocation(), false);
				} catch (SQLException e) {
					ChatUtils.error(sender, "Exception caught while executing this command.");
					e.printStackTrace();
					return;
				}
				if (shops == null || shops.size() == 0){
					ChatUtils.send(sender, "No shops found with that item.");
					return;
				}
				
				compareShops comparator = new compareShops();
				Collections.sort(shops, comparator);
				
				ChatUtils.send(sender,String.format("§f%s §7results for §f%s§7.", shops.size(), args[0]));
				SearchCommand.previousResults.put(sender.getName(), shops);
				// [1] Cyprias has 16 for $2 22 blocks north of you.
				String shopFormat = "§7[§f%s§7] §f%s §7buys §f%s §7for $§f%s §7($§f%s§7e), §f%s §7blocks §f%s";
				String sDir;
				
				Shop shop;
				int pl = Config.getInt("properties.price-rounding");
				String each;
				Location sLoc;
				for (int i=0; i<shops.size(); i++){
					shop = shops.get(i);
					
					each = Plugin.Round(shop.sellPrice/shop.amount,pl);
					sLoc = shop.getLocation();
					sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));

					
					
					ChatUtils.send(sender, String.format(shopFormat, i+1, shop.owner, shop.amount, Plugin.Round(shop.sellPrice, pl), each, Plugin.Round(p.getLocation().distance(sLoc)), sDir));

					
				}
				
			}
		});
		
		return true;
	}
	
	public class compareShops implements Comparator<Shop> {
		public int compare(Shop o1, Shop o2) {
			if (o1.sellPrice/o1.amount < o2.sellPrice/o2.amount) {
				return +1;
			} else if (o1.sellPrice/o1.amount > o2.sellPrice/o2.amount) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
