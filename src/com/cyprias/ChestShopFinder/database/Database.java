package com.cyprias.ChestShopFinder.database;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;



public interface Database {
	
	Boolean init();

	boolean insert(String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location, int inStock) throws SQLException;

	Shop getShopAtLocation(Location loc) throws SQLException;
	
	boolean deleteShopAtLocation(Location loc) throws SQLException;

	boolean setInStock(int id, int inStock) throws SQLException;
	
	List<Shop> findItemNearby(ItemStack stock, Location loc) throws SQLException;
	List<Shop> findBuySellItemNearby(ItemStack stock, Location loc, boolean isBuy) throws SQLException;
	
	List<Shop> getShopsInCoords(String worldName, int xStart, int xEnd, int zStart, int zEnd) throws SQLException;
	
	boolean deleteShop(int id)  throws SQLException;

	List<Shop> findArbitrage(ItemStack stock, Location loc)  throws SQLException;
	
}
