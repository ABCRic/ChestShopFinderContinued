package com.cyprias.ChestShopFinder.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.MySQL.queryReturn;

public class SQLite implements Database {
	private static String sqlDB;
	
	static String shops_table = "Shops";
	
	@Override
	public Boolean init() {
		File file = Plugin.getInstance().getDataFolder();
		String pluginPath = file.getPath() + File.separator;

		sqlDB = "jdbc:sqlite:" + pluginPath + "database.sqlite";
		
		try {
			createTables();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(sqlDB);
	}
	
	public static boolean tableExists(String tableName) throws SQLException {
		boolean exists = false;
		Connection con = getConnection();
		ResultSet result = con.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';");
		while (result.next()) {
			exists = true;
			break;
		}
		con.close();
		return exists;
	}
	
	public static void createTables() throws SQLException, ClassNotFoundException {
		// database.plugin.debug("Creating SQLite tables...");
		Class.forName("org.sqlite.JDBC");
		Connection con = getConnection();
		Statement stat = con.createStatement();

		if (tableExists(shops_table) == false) {
			Logger.info("Creating SQLite " + shops_table + " table.");
			stat.executeUpdate("CREATE TABLE `"+shops_table+"` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `owner` VARCHAR(16) NOT NULL, `typeId` INT NOT NULL, `durability` INT NOT NULL, `enchantments` VARCHAR(16) NOT NULL, `amount` INT NOT NULL, `buyPrice` DOUBLE NOT NULL, `sellPrice` DOUBLE NOT NULL, `world` VARCHAR(16) NOT NULL, `x` INT NOT NULL, `y` INT NOT NULL, `z` INT NOT NULL, `inStock` INT NOT NULL)");
		}
		
		stat.close();
		con.close();

	}
	
	public static int getResultCount(String query, Object... args) throws SQLException {
		queryReturn qReturn = executeQuery(query, args);
		//qReturn.result.first();
		int rows = qReturn.result.getInt(1);
		qReturn.close();
		return rows;
	}
	
	public static int executeUpdate(String query, Object... args) throws SQLException {
		Connection con = getConnection();
		int sucessful = 0;

		PreparedStatement statement = con.prepareStatement(query);
		int i = 0;
		for (Object a : args) {
			i += 1;
			statement.setObject(i, a);
		}
		sucessful = statement.executeUpdate();
		con.close();
		return sucessful;
	}


	public static class queryReturn {
		Connection con;
		PreparedStatement statement;
		public ResultSet result;

		public queryReturn(Connection con, PreparedStatement statement, ResultSet result) {
			this.con = con;
			this.statement = statement;
			this.result = result;
		}

		public void close() throws SQLException {
			this.result.close();
			this.statement.close();
			this.con.close();
		}

	}
	
	public static queryReturn executeQuery(String query, Object... args) throws SQLException {
		Connection con = getConnection();
		queryReturn myreturn = null;// = new queryReturn();
		PreparedStatement statement = con.prepareStatement(query);
		int i = 0;
		for (Object a : args) {
			i += 1;
			// plugin.info("executeQuery "+i+": " + a);
			statement.setObject(i, a);
		}
		ResultSet result = statement.executeQuery();
		myreturn = new queryReturn(con, statement, result);
		return myreturn;
	}


	public Shop getShopAtLocation(Location loc) throws SQLException {
		Shop foundShop = null;
		
		queryReturn results = executeQuery("SELECT * FROM `"+shops_table+"` WHERE `world` LIKE ? AND `x` = ? AND `y` = ? AND `z` = ?", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		
		ResultSet r = results.result;

		
		if (r.next()) {
			foundShop = new Shop(r.getString("owner"), r.getInt("typeId"),r.getShort("durability"), r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), r.getInt("inStock"));
			foundShop.setLocation(loc);
			foundShop.setId(r.getInt("id"));
		}
		results.close();
		return foundShop;
	}

	public boolean deleteShopAtLocation(Location loc) throws SQLException {
		String query = "DELETE FROM `"+shops_table+"` WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?";
		int success = executeUpdate(query, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		return (success > 0) ? true : false;
	}

	@Override
	public boolean insert(String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location, int inStock)
		throws SQLException {
		if (enchantments == null)
			enchantments = "";
		
		
		String query = "INSERT INTO `"+shops_table+"` (`id` ,`owner` ,`typeId` ,`durability` ,`enchantments` ,`amount` ,`buyPrice` ,`sellPrice` ,`world` ,`x` ,`y` ,`z`, `inStock`)VALUES (NULL , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		int success = executeUpdate(query, owner, stock.getTypeId(), stock.getDurability(), enchantments, amount, buyPrice, sellPrice, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), inStock);
		return (success > 0) ? true : false;
	}

	@Override
	public boolean setInStock(int id, int inStock) throws SQLException {
		String query = "UPDATE `"+shops_table+"` SET `inStock` = ? WHERE `id` = ?;";
		int success = executeUpdate(query, inStock, id);
		return (success > 0) ? true : false;
	}

	@Override
	public List<Shop> findItemNearby(ItemStack stock, Location loc) throws SQLException {
		List<Shop> shops =  new ArrayList<Shop>();
		
		int pX = loc.getBlockX();
		int pZ = loc.getBlockZ();
		
		String itemSearch = " AND `typeId` = ? AND `durability` = ? AND `enchantments` = ?";
		
		String enchantments = MaterialUtil.Enchantment.encodeEnchantment(stock);
		if (enchantments == null)
			enchantments = "";

		String qry = "SELECT *, SQRT(("+pX+"-x)*("+pX+"-x) + ("+pZ+"-z)*("+pZ+"-z)) as distance FROM "+shops_table+" WHERE `inStock` >= `amount` AND `world` LIKE ? "+itemSearch+ " ORDER BY distance LIMIT 0 , " + Config.getInt("properties.search-results");

		queryReturn results = executeQuery(qry, loc.getWorld().getName(), stock.getTypeId(), stock.getDurability(), enchantments);
		ResultSet r = results.result;

		Shop shop;
		while (r.next()) {
			
			//tLoc = new Location(Plugin.getInstance().getServer().getWorld(r.getString("world")), r.getDouble("x"), r.getDouble("y"), r.getDouble("z"));

			shop = new Shop(r.getString("owner"), stock.getTypeId(), stock.getDurability(), r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), r.getInt("inStock"));
			shop.setId(r.getInt("id"));
			shop.setWorldName(r.getString("world"));
			shop.setX(r.getInt("x"));
			shop.setY(r.getInt("y"));
			shop.setZ(r.getInt("z"));
			
			
			shops.add(shop);
			
		}
		results.close();
		return shops;
	}


	@Override
	public List<Shop> getShopsInCoords(String worldName, int xStart, int xEnd, int zStart, int zEnd) throws SQLException {
		List<Shop> shops =  new ArrayList<Shop>();
		
		String qry = "SELECT * FROM `"+shops_table+"` WHERE `world` LIKE ? AND `x` >= ? AND `x` <= ? AND `z` >= ? AND `z` <= ?";
		

		queryReturn results = executeQuery(qry, worldName, xStart, xEnd, zStart, zEnd);
		ResultSet r = results.result;

		Shop shop;
		while (r.next()) {
			
			shop = new Shop(r.getString("owner"), r.getInt("typeId"), r.getShort("durability"), r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), r.getInt("inStock"));
			shop.setId(r.getInt("id"));
			shop.setWorldName(r.getString("world"));
			shop.setX(r.getInt("x"));
			shop.setY(r.getInt("y"));
			shop.setZ(r.getInt("z"));
			
			
			shops.add(shop);
			
		}
		
		results.close();
		return shops;
	}

	@Override
	public boolean deleteShop(int id) throws SQLException {
		String query = "DELETE FROM `"+shops_table+"` WHERE `id` = ?";
		int success = executeUpdate(query, id);
		return (success > 0) ? true : false;
	}

	@Override
	public List<Shop> findBuySellItemNearby(ItemStack stock, Location loc, boolean isBuy) throws SQLException {
		List<Shop> shops =  new ArrayList<Shop>();
		
		int pX = loc.getBlockX();
		int pZ = loc.getBlockZ();
		
		String enchantments = MaterialUtil.Enchantment.encodeEnchantment(stock);
		if (enchantments == null)
			enchantments = "";

		
		
		String qry = "SELECT *";	//Select all columns
		qry += ", SQRT(("+pX+"-x)*("+pX+"-x) + ("+pZ+"-z)*("+pZ+"-z)) as distance";	//Create column `distance` with our relative distance
		qry += "  FROM `"+shops_table+"`";	// From the shops table, save it as q.
		
		qry += " WHERE `world` LIKE ?";	// Only include the world we're in.
		qry += " AND `typeId` = ? AND `durability` = ? AND `enchantments` = ?";	// Only show the item we're searching for.
		if (isBuy == true){
			qry += " AND `buyPrice` > 0 AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR  `inStock` >= `amount`)"; // Only show shops with a buy price, and only if their stock is more than their amount on sign.
		}else{
			qry += " AND `sellPrice` > 0";
			
			//Exclude full shops.
			if (Config.getBoolean("properties.exclude-full-chests-from-sell"))
				qry += " AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR (`inStock` != '1728' AND `inStock` != '3456'))";
			

		}
		
		if (Config.getBoolean("properties.one-owner-per-results"))
			qry += " GROUP BY `owner`";
		
		qry += " ORDER BY distance"; // Sort listing by the distance column we made.
		qry += " LIMIT 0 , " + Config.getInt("properties.search-results"); //Only pull the first 10.
		

		Logger.debug("qry: " + qry);
		
		queryReturn results = executeQuery(qry, loc.getWorld().getName(), stock.getTypeId(), stock.getDurability(), enchantments);
		ResultSet r = results.result;

		Shop shop;
		while (r.next()) {

			shop = new Shop(r.getString("owner"), stock.getTypeId(), stock.getDurability(), r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), r.getInt("inStock"));
			shop.setId(r.getInt("id"));
			shop.setWorldName(r.getString("world"));
			shop.setX(r.getInt("x"));
			shop.setY(r.getInt("y"));
			shop.setZ(r.getInt("z"));
			
			shops.add(shop);
			
		}
		results.close();
		return shops;
	}

	@Override
	public List<Shop> findArbitrage(ItemStack stock, Location loc) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
