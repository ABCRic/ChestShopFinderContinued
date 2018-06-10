package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
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
import com.google.common.primitives.Doubles;

public class PriceCommand implements Command {

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.PRICE))
			return false;

		if (args.length < 1) {
			getCommands(sender, cmd);
			return true;
		}

		int amount = 1;// InventoryUtil.getAmount(item, player.getInventory());
		if (args.length > 1 && Plugin.isInt(args[args.length - 1])) {
			amount = Integer.parseInt(args[args.length - 1]);
			args = (String[]) ArrayUtils.remove(args, args.length - 1);
		}

		final ItemStack stock = MaterialUtil.getItem(StringUtil.joinArray(args));
		if (MaterialUtil.isEmpty(stock)) {
			ChatUtils.error(sender, "Unknown item: " + StringUtil.joinArray(args));
			return true;
		}

		final int famount = amount;
		
		Plugin.runTaskAsynchronously(new Runnable() {
			public void run() {
				List<Shop> shops;
				try {
					shops = Plugin.database.getShopsPricesByItem(stock);
				} catch (SQLException e) {
					ChatUtils.error(sender, "Exception caught while executing this command.");
					e.printStackTrace();
					return;
				}
				int dplaces = Config.getInt("properties.price-rounding");

				ChatUtils.send(sender, String.format("§f%s§7 shops for §f%s§7x§f%s§7", shops.size(), MaterialUtil.getName(stock), famount));

				// //////// BUY //////////

				compareBuyPrices bcomparator = new compareBuyPrices();
				Collections.sort(shops, bcomparator);

				// double[] dBuyPrices = new double[shops.size()];
				List<Double> dBuyPrices = new ArrayList<Double>();
				// int[] iAmount = new int[shops.size()];

				double totalBuyPrice = 0;

				int totalBuyAmount = 0;
				// int buyCount = 0;

				for (int i = 0; i < shops.size(); i++) {

					if (shops.get(i).buyPrice > 0 && (shops.get(i).inStock >= shops.get(i).amount)) {
						// buyCount += 1;

						dBuyPrices.add(shops.get(i).buyPrice / shops.get(i).amount);

						// iAmount[i] = shops.get(i).amount;

						totalBuyPrice += shops.get(i).buyPrice;

						totalBuyAmount += shops.get(i).amount;

						// Logger.debug("dBuyPrices: " + shops.get(i).buyPrice /
						// shops.get(i).amount);

					}
				}

				String averageBuy = (dBuyPrices.size() > 0) ? Plugin.Round((totalBuyPrice / totalBuyAmount) * famount, dplaces) : "0";

				String mean = (dBuyPrices.size() > 0) ? Plugin.Round(mean(Doubles.toArray(dBuyPrices)) * famount, dplaces) : "0";
				String median = (dBuyPrices.size() > 0) ? Plugin.Round(median(Doubles.toArray(dBuyPrices)) * famount, dplaces) : "0";
				String mode = (dBuyPrices.size() > 0) ? Plugin.Round(mode(Doubles.toArray(dBuyPrices)) * famount, dplaces) : "0";

				ChatUtils.send(sender, String.format("§7Buy average: $§f%s§7, mean:$§f%s§7, med:$§f%s§7, mod:$§f%s§7.",

				averageBuy, mean, median, mode));

				// ///////// SELL ////////
				compareSellPrices scomparator = new compareSellPrices();
				Collections.sort(shops, scomparator);

				double totalSellPrice = 0;
				// int sellCount = 0;

				// double[] dSellPrices = new double[shops.size()];
				int totalSellAmount = 0;

				List<Double> dSellPrices = new ArrayList<Double>();

				for (int i = 0; i < shops.size(); i++) {
					if (shops.get(i).sellPrice > 0) {
						// sellCount += 1;
						// dSellPrices[i] = (shops.get(i).sellPrice /
						// shops.get(i).amount);
						dSellPrices.add(shops.get(i).sellPrice / shops.get(i).amount);

						totalSellPrice += shops.get(i).sellPrice;
						totalSellAmount += shops.get(i).amount;

						// Logger.debug("dSellPrices: " + shops.get(i).sellPrice
						// / shops.get(i).amount);
					}

				}

				String averageSell = (dSellPrices.size() > 0) ? Plugin.Round((totalSellPrice / totalSellAmount) * famount, dplaces) : "0";

				
				
				mean = (dSellPrices.size() > 0) ? Plugin.Round(mean(Doubles.toArray(dSellPrices)) * famount, dplaces) : "0";
				median = (dSellPrices.size() > 0) ? Plugin.Round(median(Doubles.toArray(dSellPrices)) * famount, dplaces) : "0";
				mode = (dSellPrices.size() > 0) ? Plugin.Round(mode(Doubles.toArray(dSellPrices)) * famount, dplaces) : "0";

				//

				ChatUtils.send(sender, String.format("§7Sell average: $§f%s§7, mean:$§f%s§7, med:$§f%s§7, mod:$§f%s§7.", averageSell, mean, median, mode));
			}
		});

		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.PRICE, "/%s price <item> [amount]", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.PRICE))
			list.add("/%s price - Get the average price of an item.");

	}

	public static double mean(double[] p) {
		double sum = 0; // sum of all the elements
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
		}
		return sum / p.length;
	}// end method mean

	public static double median(double[] m) {
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2.0;
		}
	}

	public static double mode(double[] prices) {
		double maxValue = 0, maxCount = 0;

		for (int i = 0; i < prices.length; ++i) {
			int count = 0;
			for (int j = 0; j < prices.length; ++j) {
				if (prices[j] == prices[i])
					++count;
			}
			if (count > maxCount) {
				maxCount = count;
				maxValue = prices[i];
			}
		}

		return maxValue;
	}

	public class compareBuyPrices implements Comparator<Shop> {
		public int compare(Shop o1, Shop o2) {

			if ((o1.buyPrice / o1.amount) > (o2.buyPrice / o2.amount))
				return +1;

			if ((o1.buyPrice / o1.amount) < (o2.buyPrice / o2.amount))
				return -1;

			return 0;
		}
	}

	public class compareSellPrices implements Comparator<Shop> {
		public int compare(Shop o1, Shop o2) {

			if ((o1.sellPrice / o1.amount) > (o2.sellPrice / o2.amount))
				return +1;

			if ((o1.sellPrice / o1.amount) < (o2.sellPrice / o2.amount))
				return -1;

			return 0;
		}
	}

}
