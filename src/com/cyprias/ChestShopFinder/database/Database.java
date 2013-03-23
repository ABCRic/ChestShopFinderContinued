package com.cyprias.ChestShopFinder.database;

import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;


public interface Database {
	
	Boolean init();

	boolean insert(String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location, int inStock) throws SQLException;

	Shop getShopAtLocation(Location loc) throws SQLException;
	
	boolean deleteShopAtLocation(Location loc) throws SQLException;

	boolean setInStock(int id, int inStock) throws SQLException;
	
	
}
