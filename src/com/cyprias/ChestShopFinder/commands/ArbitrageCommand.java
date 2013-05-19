package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
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

public class ArbitrageCommand implements Command {

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.ARBITRAGE))
			return false;
		if (args.length < 1 ){
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
		
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
		List<Shop> shops;
		try {
			shops = Plugin.database.findArbitrage(stock, p.getLocation());
		} catch (SQLException e) {
			ChatUtils.error(sender, "Exception caught while executing this command.");
			e.printStackTrace();
			return;
		}

		if (shops == null || shops.size() == 0){
			ChatUtils.send(sender, ChatColor.GRAY+"There's no profits to be made.");
			return;
			
		}
		SearchCommand.previousResults.put(sender.getName(), shops);
		
		int pl = Config.getInt("properties.price-rounding");
		
		Shop lowestBuy = shops.get(0);
		Shop highestSell = shops.get(1);
		
		String sellFormat = "§7[§f%s§7] §f%s §7sells §f%s §7for $§f%s §7($§f%s§7e), §f%s §7blocks §f%s";
		String each = Plugin.Round(lowestBuy.buyPrice/lowestBuy.amount,pl);
		Location sLoc = lowestBuy.getLocation();
		String sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));
		ChatUtils.send(sender, String.format(sellFormat, 1, Plugin.getPlayerName(lowestBuy.owner), lowestBuy.amount, Plugin.Round(lowestBuy.buyPrice, pl), each, Plugin.Round(p.getLocation().distance(sLoc)), sDir));

		
		
		String buyFormat = "§7[§f%s§7] §f%s §7buys §f%s §7for $§f%s §7($§f%s§7e), §f%s §7blocks §f%s";
		 each = Plugin.Round(highestSell.sellPrice/highestSell.amount,pl);
		 sLoc = highestSell.getLocation();
		 sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));
		ChatUtils.send(sender, String.format(buyFormat, 2, Plugin.getPlayerName(highestSell.owner), highestSell.amount, Plugin.Round(highestSell.sellPrice, pl), each, Plugin.Round(p.getLocation().distance(sLoc)), sDir));

		
		double profitEach = (highestSell.sellPrice/highestSell.amount) - (lowestBuy.buyPrice/lowestBuy.amount);
		ChatUtils.send(sender, ChatColor.GRAY+"Profit: $" + ChatColor.WHITE + Plugin.Round(profitEach,pl) + ChatColor.GRAY + "e");

			}
		});
		
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.ARBITRAGE, "/%s arbitrage <itemName>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.ARBITRAGE))
			list.add("/%s arbitrage - Find cheap items to sell to another shop.");
	}
}
