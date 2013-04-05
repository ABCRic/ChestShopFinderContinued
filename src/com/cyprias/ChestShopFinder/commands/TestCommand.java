package com.cyprias.ChestShopFinder.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.cyprias.ChestShopFinder.ChatUtils;
import com.cyprias.ChestShopFinder.Perm;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.command.Command;
import com.cyprias.ChestShopFinder.command.CommandAccess;
import com.cyprias.ChestShopFinder.database.MySQL;
import com.cyprias.ChestShopFinder.database.Shop;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class TestCommand implements Command {

	public boolean execute(final CommandSender sender, org.bukkit.command.Command cmd, String[] args) throws SQLException {
		if (!Plugin.checkPermission(sender, Perm.TEST))
			return false;
		
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.TEST, "/%s test", cmd);
	}

	public boolean hasValues() {
		return false;
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if (Plugin.hasPermission(sender, Perm.TEST))
			list.add("/%s test");
	}
}
