package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Database.itemTraded;
import com.cyprias.ChestShopFinder.database.Database.ownerCount;
import com.cyprias.ChestShopFinder.database.Database.popularOwner;
import com.cyprias.ChestShopFinder.database.Transaction;
import com.cyprias.ChestShopFinder.utils.ChatUtils;
import com.cyprias.ChestShopFinder.utils.MinecraftFontWidthCalculator;

public class TransactionsCommand implements Command {

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.TRANSACTIONS, "/%s transactions <blaw>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS))
			list.add("/%s transactions - See your transaction histroy.");
	}

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		// if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS))
		// return false;

		if (args.length > 0) {

			if (args[0].equalsIgnoreCase("mine")) {

				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_MINE))
					return false;

				int page = -1; // Default to last page.
				if (args.length > 1) {// && args[1].equalsIgnoreCase("compact"))
					if (Plugin.isInt(args[1])) {
						page = Integer.parseInt(args[1]);
						if (page > 0)
							page -= 1;
					} else {
						ChatUtils.error(sender, "Invalid page: " + args[1]);
						return true;
					}
				}

				List<Transaction> transactions = Plugin.database.getOwnerTransactions(sender, sender.getName(), page);
				Transaction t;
				String stype, msg;
				int pl = Config.getInt("properties.price-rounding");
				String each;

				String g = ChatColor.GRAY.toString();
				String w = ChatColor.WHITE.toString();

				// String mFormat =
				// g+"%s "+w+"%s"+g+"x"+w+"%s "+g+"for "+g+"$"+w+"%s "+g+"("+w+"%s"+g+"e) "+w+"%s "+g+"ago";
				String mFormat = g + "%s " + w + "%s" + g + "x" + w + "%s " + g + "for " + g + "$" + w + "%s %s " + g + "ago";

				for (int i = 0; i < transactions.size(); i++) {
					t = transactions.get(i);
					// each = Plugin.Round(t.getPrice()/t.getAmount(),pl);
					msg = "";
					stype = "";
					if (t.isBuy()) {
						stype = "Sold";
					} else if (t.isSell()) {
						stype = "Bought";
					}
					msg = String.format(mFormat, stype, t.getItemName(), t.getAmount(), Plugin.Round(t.getPrice()), Plugin.secondsToString(t.getTimeOffset()));
					ChatUtils.send(sender, msg);
				}
				return true;

			} else if (args[0].equalsIgnoreCase("owner")) {
				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_OWNER))
					return false;

				if (args.length > 1) {

					if (args[1].equalsIgnoreCase("popular")) {
						List<popularOwner> owners = Plugin.database.getTopPopularShopOwner();

						popularOwner o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
						}

						return true;
					}else if (args[1].equalsIgnoreCase("items")) {
						List<ownerCount> owners = Plugin.database.getTopOwnersByItemsSold();
						ownerCount o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.icount + " items sold");
						}
						return true;

					}else if (args[1].equalsIgnoreCase("profit")) {
						List<ownerCount> owners = Plugin.database.getTopOwnerByProfit();
						ownerCount o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": $" + o.dcount + " made.");
						}
						return true;
						
						
						
						
					}

				}

				ChatUtils.send(sender, "/" + cmd.getName() + " transactions owner popular: Most popular shop owners");
				ChatUtils.send(sender, "/" + cmd.getName() + " transactions owner items: Most items sold");
				ChatUtils.send(sender, "/" + cmd.getName() + " transactions owner profit: Most money made.");
				
				return true;
			} else if (args[0].equalsIgnoreCase("item")) {
				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_ITEM))
					return false;
				
				if (args.length > 1) {

					if (args[1].equalsIgnoreCase("topbuy")) {
						
						String orderBy = "totalTransactions";
						if (args.length > 2) {
							
							if (args[2].equalsIgnoreCase("amount")){
								orderBy = "totalAmount";
							}else if (args[2].equalsIgnoreCase("price")){
								orderBy = "totalPrice";
							}
							
						}
						
						
						List<itemTraded> items = Plugin.database.topItemBought(orderBy);
						
						
						int wItem = MinecraftFontWidthCalculator.getStringWidth("Item");
						int wTransactions = MinecraftFontWidthCalculator.getStringWidth("Trans");
						int wAmount = MinecraftFontWidthCalculator.getStringWidth("Amount");
						int wPrice = MinecraftFontWidthCalculator.getStringWidth("Price");
						
						
						for (itemTraded o : items){
							
							wItem = Math.max(wItem, MinecraftFontWidthCalculator.getStringWidth(MaterialUtil.getName(o.stock)));
							wTransactions = Math.max(wTransactions, MinecraftFontWidthCalculator.getStringWidth(String.valueOf(o.transactions)));
							wAmount = Math.max(wAmount, MinecraftFontWidthCalculator.getStringWidth(String.valueOf(o.amount)));
							wPrice = Math.max(wPrice, MinecraftFontWidthCalculator.getStringWidth("$" + Plugin.Round(o.price)));

						}
						
						wItem += 10;
						wTransactions +=10;
						wAmount += 10;
						wPrice += 10;
						
						
						//MinecraftFontWidthCalculator.
						
						ChatUtils.send(sender,MinecraftFontWidthCalculator.whitespace((items.size()+1) + " ") +  
							MinecraftFontWidthCalculator.textWithWhitespace("Item", wItem) + 
							MinecraftFontWidthCalculator.textWithWhitespace("Trans", wTransactions) + 
							MinecraftFontWidthCalculator.textWithWhitespace("Amount", wAmount) + 
							MinecraftFontWidthCalculator.textWithWhitespace("Price", wPrice)
							
							
							);
						
						
						
						
						itemTraded o;
						String line;
						for (int i = 0; i < items.size(); i++) {
							o = items.get(i);
							
							/*
							if (i < 10){
								line = (i + 1) + "  ";
							}else{
								line = (i + 1) + " ";
							}
							*/
							
							line = MinecraftFontWidthCalculator.textWithWhitespace(""+(i + 1) + " ",(items.size()+1) + " ");
													
							
							
							
							//line = MaterialUtil.getName(o.stock) + MinecraftFontWidthCalculator.whitespace(wItem - MinecraftFontWidthCalculator.getStringWidth(MaterialUtil.getName(o.stock)));
							
							
							
							line += MinecraftFontWidthCalculator.textWithWhitespace(MaterialUtil.getName(o.stock), wItem);
							line += MinecraftFontWidthCalculator.textWithWhitespace(o.transactions, wTransactions);
							line += MinecraftFontWidthCalculator.textWithWhitespace(o.amount, wAmount);
							line += MinecraftFontWidthCalculator.textWithWhitespace("$" + o.price, wPrice);
							
						
							
							ChatUtils.send(sender,line);
							
							
							//ChatUtils.send(sender, (i + 1) + " " + MaterialUtil.getName(o.stock) + " transactions " + o.transactions+ ", amount: " + o.amount + " price: $" + o.price);
							
						}
						return true;
						
						
						
					}
				}
				
				
				ChatUtils.send(sender, "/" + cmd.getName() + " transactions item topbuy [amount|price]: Most bought item.");
				
				return true;
			}
			
		}

		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_MINE))
			ChatUtils.send(sender, "/" + cmd.getName() + " transactions mine [page]");

		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_OWNER))
			ChatUtils.send(sender, "/" + cmd.getName() + " transactions owner");

		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_ITEM))
			ChatUtils.send(sender, "/" + cmd.getName() + " transactions item");
		
		return true;
	}


	
}
