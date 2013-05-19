package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Transaction;
import com.cyprias.ChestShopFinder.utils.ChatUtils;

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
		//if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS))
		//	return false;
		
		
		if (args.length > 0){
			
			if (args[0].equalsIgnoreCase("mine")){
				
				if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS_MINE))
					return false;
				
				
				int page = -1; //Default to last page.
				if (args.length > 1) {// && args[1].equalsIgnoreCase("compact"))
					if (Plugin.isInt(args[1])) {
						page = Integer.parseInt(args[1]);
						if (page>0)
							page-=1;
					} else {
						ChatUtils.error(sender, "Invalid page: " +  args[1]);
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
				
				//String mFormat = g+"%s "+w+"%s"+g+"x"+w+"%s "+g+"for "+g+"$"+w+"%s "+g+"("+w+"%s"+g+"e) "+w+"%s "+g+"ago";
				String mFormat = g+"%s "+w+"%s"+g+"x"+w+"%s "+g+"for "+g+"$"+w+"%s %s "+g+"ago";
				
				
				for (int i=0; i<transactions.size();i++){
					
					t = transactions.get(i);
					
					//each = Plugin.Round(t.getPrice()/t.getAmount(),pl);
					
					msg = "";
					stype = "";
					if (t.isBuy()){
						stype = "Sold";
					}else if (t.isSell()){
						stype = "Bought";
					}
					
					
					
					msg = String.format(mFormat, stype, t.getItemName(), t.getAmount(), Plugin.Round(t.getPrice()), Plugin.secondsToString(t.getTimeOffset()));
					
				//	msg = stype+" " + t.getItemName() + "x" + t.getAmount() + " for $" + t.getPrice() + " (" + each + "e) " + Plugin.secondsToString(t.getTimeOffset()) + " ago";
					
					
					ChatUtils.send(sender, msg);
					
					
				}
				
				
				
				
				
				return true;
			}
			
			
		}
		
		

		
		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS_MINE))
			ChatUtils.send(sender, "/" + cmd.getName() + " transactions mine [page]");

		
		
		return true;
	}

}
