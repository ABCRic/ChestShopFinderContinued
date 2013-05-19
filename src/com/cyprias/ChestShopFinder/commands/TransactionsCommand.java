package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.database.Transaction;
import com.cyprias.ChestShopFinder.utils.ChatUtils;

public class TransactionsCommand implements Command {

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.SEARCH, "/%s transactions <blaw>", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.TRANSACTIONS))
			list.add("/%s transactions - See your transaction histroy.");
	}

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, final String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.TRANSACTIONS))
			return false;
		
		
		
		List<Transaction> transactions = Plugin.database.getOwnerTransactions(sender.getName());
		
		for (int i=0; i<transactions.size();i++){
			ChatUtils.send(sender, i + ": " + transactions.get(i).getTime());
		}
		
		
		
		return true;
	}

}
