package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.HashMap;
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
import com.cyprias.ChestShopFinder.database.Database.traderCount;
import com.cyprias.ChestShopFinder.database.Database.popularTrader;
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
				String stype;
				int pl = Config.getInt("properties.price-rounding");
				//String each;

				String g = ChatColor.GRAY.toString();
				String w = ChatColor.WHITE.toString();

				// String mFormat =
				// g+"%s "+w+"%s"+g+"x"+w+"%s "+g+"for "+g+"$"+w+"%s "+g+"("+w+"%s"+g+"e) "+w+"%s "+g+"ago";
				String mFormat = g + "%s " + w + "%s" + g + "x" + w + "%s " + g + "for " + g + "$" + w + "%s %s " + g + "ago";

				String[] s1 = new String[transactions.size()];
				String[] s2 = new String[transactions.size()];
				String[] s3 = new String[transactions.size()];
				String[] s4 = new String[transactions.size()];
				String[] s5 = new String[transactions.size()];
				
				
				for (int i = 0; i < transactions.size(); i++) {
					t = transactions.get(i);
					// each = Plugin.Round(t.getPrice()/t.getAmount(),pl);
					
					stype = "";
					if (t.isBuy()) {
						stype = "Sold";
					} else if (t.isSell()) {
						stype = "Bought";
					}
					//msg = String.format(mFormat, stype, t.getItemName(), t.getAmount(), Plugin.Round(t.getPrice()), Plugin.secondsToString(t.getTimeOffset()));
					//ChatUtils.send(sender, msg);
					
					
					s1[i] = stype;
					s2[i] = t.getItemName();
					s3[i] = ""+t.getAmount();
					s4[i] = Plugin.Round(t.getPrice(), pl);
					s5[i] = Plugin.secondsToString(t.getTimeOffset());
					
					/*
					s6[i] = ;
					s7[i] = ;
					s8[i] = ;
					s9[i] = ;
					s10[i] = ;*/
					
					
				}
				if (Config.getBoolean("properties.white-space-results")){
					s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
					s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
					s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
					s4 = MinecraftFontWidthCalculator.getWhitespacedStrings(s4);
					s5 = MinecraftFontWidthCalculator.getWhitespacedStrings(s5);
					
				}
				
				//String fMsg = ChatColor.WHITE + "%s %s§7: §f%s §7items sold";

				for (int i=0;i<s1.length;i++)
					ChatUtils.send(sender,String.format(mFormat, s1[i], s2[i], s3[i], s4[i], s5[i]));
				
				
				
				
				
				return true;

			} else if (args[0].equalsIgnoreCase("owner")) {
				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_OWNER))
					return false;

				if (args.length > 1) {

					if (args[1].equalsIgnoreCase("popular")) {
						List<popularTrader> owners = Plugin.database.getTopPopularShopOwner();
						/*
						popularTrader o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
						}*/
						String[] s1 = new String[owners.size()];
						String[] s2 = new String[owners.size()];
						String[] s3 = new String[owners.size()];
						popularTrader o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
						//	ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
							
							s1[i] = ""+(i + 1);
							s2[i] = o.traderName;
							s3[i] = ""+o.popCount;
							
						}
						if (Config.getBoolean("properties.white-space-results")){
							s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
							s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
							s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
						}
						String fMsg = ChatColor.WHITE + "§f%s §f%s §7had §f%s §7clients";

						for (int i=0;i<s1.length;i++)
							ChatUtils.send(sender,String.format(fMsg, s1[i], s2[i], s3[i]));
						

						return true;
					}else if (args[1].equalsIgnoreCase("items")) {
						List<traderCount> owners = Plugin.database.getTopOwnersByItemsSold();
						
						String[] s1 = new String[owners.size()];
						String[] s2 = new String[owners.size()];
						String[] s3 = new String[owners.size()];
						
						traderCount o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							
							s1[i] = ""+(i + 1);
							s2[i] = o.playerName;
							s3[i] = ""+o.icount;
						}
						if (Config.getBoolean("properties.white-space-results")){
							s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
							s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
							s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
						}
						
						String fMsg = ChatColor.WHITE + "§f%s §f%s §7sold §f%s  §7items";

						for (int i=0;i<s1.length;i++)
							ChatUtils.send(sender,String.format(fMsg, s1[i], s2[i], s3[i]));

						return true;

					}else if (args[1].equalsIgnoreCase("profit")) {
						List<traderCount> owners = Plugin.database.getTopOwnerByProfit();
						traderCount o;

						String[] s1 = new String[owners.size()];
						String[] s2 = new String[owners.size()];
						String[] s3 = new String[owners.size()];
						String[] s4 = new String[owners.size()];
						//String[] s5 = new String[owners.size()];
						
						List<popularTrader> c;//clients
						//HashMap<String, List<popularTrader>> ownerClients = new HashMap<String, List<popularTrader>>();
						
						
						double mpercent;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							//ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": $" + o.dcount + " made.");
							
							s1[i] = ""+(i + 1);
							s2[i] = o.playerName;
							s3[i] = Plugin.Round(o.dcount);
							
							//clients = ;
							//ownerClients.put(o.playerName, );
							
							
							s4[i] = ""+0;
							//s5[i] = "Unknown";
							
							c = Plugin.database.getOwnersTopClients(o.playerName);
							
							mpercent = 0;
							if (c.size() > 0){
								for (int ci=0; ci<(c.size()); ci++){
									mpercent += (c.get(ci).dnum / o.dcount);
									s4[i] = ""+(ci+1);
									
									if (mpercent > .5)
										break;
									
								}

							}
							
						}
						if (Config.getBoolean("properties.white-space-results")){
							s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
							s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
							s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
							s4 = MinecraftFontWidthCalculator.getWhitespacedStrings(s4);
						//	s5 = MinecraftFontWidthCalculator.getWhitespacedStrings(s5);
						}
						
						//String fMsg = ChatColor.WHITE + "§f%s §f%s§7 §7made $§f%s §7from §f%s§7.";
						//String fMsg = ChatColor.WHITE + "§f%s §f%s§7 §7made $§f%s§7, §f%s§7%% from §f%s§7.";
						String fMsg = ChatColor.WHITE + "§f%s §f%s§7 §7made $§f%s§7 mostly from §f%s §7client(s).";
						
						
						for (int i=0;i<s1.length;i++)
							ChatUtils.send(sender,String.format(fMsg, s1[i], s2[i], s3[i], s4[i]));

						
						return true;
						
						
						
						
					}

				}

				ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions owner popular: Most popular shop owners");
				ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions owner items: Most items sold");
				ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions owner profit: Most money made.");
				
				return true;
			} else if (args[0].equalsIgnoreCase("item")) {
				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_ITEM))
					return false;
				
				if (args.length > 1) {

					if (args[1].equalsIgnoreCase("topbuy")) {
						
						String orderBy = "totalPrice";
						
						if (args.length > 2) {
							
							if (args[2].equalsIgnoreCase("amount")){
								orderBy = "totalAmount";
							}else if (args[2].equalsIgnoreCase("price")){
								orderBy = "totalPrice";
							}else if (args[2].equalsIgnoreCase("trans") || args[2].equalsIgnoreCase("transactions") ){
								orderBy = "totalTransactions";
							}else if (args[2].equalsIgnoreCase("clients")){
								orderBy = "uniqueClients";
								
							}
							
						}

						List<itemTraded> items = Plugin.database.topItemBought(orderBy);
						
						String[] s1 = new String[items.size()+1];
						String[] s2 = new String[items.size()+1];
						String[] s3 = new String[items.size()+1];
						String[] s4 = new String[items.size()+1];
						String[] s5 = new String[items.size()+1];
						String[] s6 = new String[items.size()+1];
						
						s1[0] = "#";
						s2[0] = "Item";
						s3[0] = "Clients";
						s4[0] = "Trans";
						s5[0] = "Amount";
						s6[0] = "Price";
						
						
						
						itemTraded o;
						for (int i = 0; i < items.size(); i++) {
							o = items.get(i);
							//ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": $" + o.dcount + " made.");
							
							s1[i+1] = ""+(i + 1);
							
							s2[i+1] = MaterialUtil.getName(o.stock);
							s3[i+1] = ""+o.clients;
							s4[i+1] = ""+o.transactions;
							s5[i+1] = ""+o.amount;
							s6[i+1] = ""+Plugin.Round(o.price);
							

						}
						if (Config.getBoolean("properties.white-space-results")){
							s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
							s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
							s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
							s4 = MinecraftFontWidthCalculator.getWhitespacedStrings(s4);
							s5 = MinecraftFontWidthCalculator.getWhitespacedStrings(s5);
							s6 = MinecraftFontWidthCalculator.getWhitespacedStrings(s6);
							
						}
						
						String fMsg = ChatColor.WHITE + "§f%s %s %s %s %s §7$§f%s";

						ChatUtils.send(sender,String.format(fMsg, s1[0], s2[0], s3[0], s4[0], s5[0], s6[0]));
						
						for (int i=1;i<s1.length;i++)
							ChatUtils.send(sender,String.format(fMsg, s1[i], s2[i], s3[i], s4[i], s5[i], s6[i]));
						
						
						return true;
					}
				}
				
				
				ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions item topbuy [clients|trans|amount|price]");
				
				return true;
				
			} else if (args[0].equalsIgnoreCase("client")) {
				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_ITEM))
					return false;
				
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("spent")) {
						List<traderCount> clients = Plugin.database.getTopClientBySpent();
						/*
						popularTrader o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
						}*/
						String[] s1 = new String[clients.size()];
						String[] s2 = new String[clients.size()];
						String[] s3 = new String[clients.size()];
						traderCount o;
						for (int i = 0; i < clients.size(); i++) {
							o = clients.get(i);
						//	ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
							
							s1[i] = ""+(i + 1);
							s2[i] = o.playerName;
							s3[i] = ""+Plugin.Round(o.dcount);
						}
						if (Config.getBoolean("properties.white-space-results")){
							s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
							s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
							s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
						}
						String fMsg = "§f%s §f%s§7 §7spent $§f%s";
	
						for (int i=0;i<s1.length;i++)
							ChatUtils.send(sender,String.format(fMsg, s1[i], s2[i], s3[i]));
						
	
						return true;
					}else if (args[1].equalsIgnoreCase("popular")) {
						List<popularTrader> owners = Plugin.database.getTopPopularShopClient();
						/*
						popularTrader o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
							ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
						}*/
						String[] s1 = new String[owners.size()];
						String[] s2 = new String[owners.size()];
						String[] s3 = new String[owners.size()];
						popularTrader o;
						for (int i = 0; i < owners.size(); i++) {
							o = owners.get(i);
						//	ChatUtils.send(sender, (i + 1) + " " + o.ownerName + ": " + o.clientCount + " clients");
							
							s1[i] = ""+(i + 1);
							s2[i] = o.traderName;
							s3[i] = ""+o.popCount;
							
						}
						if (Config.getBoolean("properties.white-space-results")){
							s1 = MinecraftFontWidthCalculator.getWhitespacedStrings(s1);
							s2 = MinecraftFontWidthCalculator.getWhitespacedStrings(s2);
							s3 = MinecraftFontWidthCalculator.getWhitespacedStrings(s3);
						}
						String fMsg = ChatColor.WHITE + "§f%s §f%s §7has visited §f%s §7shop owners.";

						for (int i=0;i<s1.length;i++)
							ChatUtils.send(sender,String.format(fMsg, s1[i], s2[i], s3[i]));
						

						return true;
					}
				}
				
				
				
				
				
				ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions client spent");
				ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions client popular");
				
				
				
				return true;
				
			}
			
		}

		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_MINE))
			ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions mine [page]");

		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_ITEM))
			ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions item");
		
		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_OWNER))
			ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions owner");
		
		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_CLIENT))
			ChatUtils.send(sender, ChatColor.GRAY+"/" + cmd.getName() + " transactions client");
		
		return true;
	}


	
}
