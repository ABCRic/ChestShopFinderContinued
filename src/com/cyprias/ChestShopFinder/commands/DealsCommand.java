package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;

public class DealsCommand implements Command {

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.DEALS))
			return false;
		
		
		
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.DEALS, "/%s deals", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.DEALS))
			list.add("/%s deals");
	}
}
