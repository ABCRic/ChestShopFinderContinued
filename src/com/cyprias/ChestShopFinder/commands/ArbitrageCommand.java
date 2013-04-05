package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class ArbitrageCommand implements Command {

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.ARBITRAGE))
			return false;
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
		
		
		List<Shop> shops = Plugin.database.findArbitrage(stock, p.getLocation());
		
		if (shops == null || shops.size() == 0){
			ChatUtils.send(sender, ChatColor.GRAY+"There's no profits to be made.");
			return true;
			
		}
		SearchCommand.previousResults.put(sender.getName(), shops);
		
		int pl = Config.getInt("properties.price-rounding");
		
		Shop lowestBuy = shops.get(0);
		Shop highestSell = shops.get(1);
		
		String sellFormat = "§7[§f%s§7] §f%s §7sells §f%s §7for $§f%s §7($§f%s§7e), §f%s §7blocks §f%s";
		
		
		String each = Plugin.Round(lowestBuy.buyPrice/lowestBuy.amount,pl);
		Location sLoc = lowestBuy.getLocation();
		String sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));

		
		
		ChatUtils.send(sender, String.format(sellFormat, 2, lowestBuy.owner, lowestBuy.amount, Plugin.Round(lowestBuy.buyPrice, pl), each, Plugin.Round(p.getLocation().distance(sLoc)), sDir));

		
		
		//lowestBuy
		String buyFormat = "§7[§f%s§7] §f%s §7buys §f%s §7for $§f%s §7($§f%s§7e), §f%s §7blocks §f%s";
		
		
		 each = Plugin.Round(highestSell.sellPrice/highestSell.amount,pl);
		 sLoc = highestSell.getLocation();
		 sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, sLoc.getBlockX(), sLoc.getBlockZ()));

		
		
		ChatUtils.send(sender, String.format(buyFormat, 1, highestSell.owner, highestSell.amount, Plugin.Round(highestSell.sellPrice, pl), each, Plugin.Round(p.getLocation().distance(sLoc)), sDir));

		
		

		
		
		
		//ChatUtils.send(sender, lowestBuy.owner + " sells " + args[0] + " for " + lowestBuy.buyPrice);
		//ChatUtils.send(sender, highestSell.owner + " buys " + args[0] + " for " + highestSell.sellPrice);
		
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
