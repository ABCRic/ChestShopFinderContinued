package com.cyprias.ChestShopFinder.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.configuration.Config;

public class MySQL implements Database {




	static String prefix;
	static String shops_table;
	public Boolean init() {
		if (!canConnect()){
			Logger.info("Failed to connect to MySQL!");
			return false;
		}
		prefix = Config.getString("mysql.prefix");
		shops_table = prefix+ "Shops";
		
		
		try {
			createTables();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	public void createTables() throws SQLException{
		Connection con = getConnection();
		
		if (tableExists(shops_table) == false) {
			Logger.info("Creating "+shops_table+" table.");
			con.prepareStatement("CREATE TABLE `"+shops_table+"` (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `owner` VARCHAR(16) NOT NULL, `typeId` INT NOT NULL, `durability` INT NOT NULL, `enchantments` VARCHAR(16) NOT NULL, `amount` INT NOT NULL, `buyPrice` DOUBLE NOT NULL, `sellPrice` DOUBLE NOT NULL, `world` VARCHAR(16) NOT NULL, `x` INT NOT NULL, `y` INT NOT NULL, `z` INT NOT NULL, `inStock` INT NOT NULL) ENGINE = InnoDB").executeUpdate();
		}
		
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
	
	public static int getResultCount(String query, Object... args) throws SQLException {
		queryReturn qReturn = executeQuery(query, args);
		qReturn.result.first();
		int rows = qReturn.result.getInt(1);
		qReturn.close();
		return rows;
	}
	
	public static boolean tableExists(String tableName) throws SQLException {
		boolean exists = false;
		Connection con = getConnection();
		ResultSet result = con.prepareStatement("show tables like '" + tableName + "'").executeQuery();
		result.last();
		if (result.getRow() != 0) 
			exists = true;
		con.close();
		return exists;
	}

	
	private static String getURL(){
		return "jdbc:mysql://" + Config.getString("mysql.hostname") + ":" + Config.getInt("mysql.port") + "/" + Config.getString("mysql.database");
	}
	
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(getURL(), Config.getString("mysql.username"), Config.getString("mysql.password"));
	}
	
	private Boolean canConnect(){
		try {
			@SuppressWarnings("unused")
			Connection con = getConnection();
		} catch (SQLException e) {
			return false;
		}
		return true;
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


	public boolean insert(String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location, int inStock) throws SQLException {
		// TODO Auto-generated method stub
		if (enchantments == null)
			enchantments = "";
		
		
		String query = "INSERT INTO `"+shops_table+"` (`id` ,`owner` ,`typeId` ,`durability` ,`enchantments` ,`amount` ,`buyPrice` ,`sellPrice` ,`world` ,`x` ,`y` ,`z`, `inStock`)VALUES (NULL , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		int success = executeUpdate(query, owner, stock.getTypeId(), stock.getDurability(), enchantments, amount, buyPrice, sellPrice, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), inStock);
		return (success > 0) ? true : false;
		
	}


	@Override
	public Shop getShopAtLocation(Location loc) throws SQLException {
		
		Shop foundShop = null;
		
		queryReturn results = executeQuery("SELECT * FROM `"+shops_table+"` WHERE `world` LIKE ? AND `x` = ? AND `y` = ? AND `z` = ?", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		
		ResultSet r = results.result;

		
		if (r.next()) {
			//String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location
			
			ItemStack stock = new ItemStack(r.getInt("typeId"),r.getShort("durability"));
			
			stock.addEnchantments(MaterialUtil.Enchantment.getEnchantments(r.getString("enchantments")));
			
			
			
			foundShop = new Shop(r.getString("owner"), stock, r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), loc, r.getInt("inStock"));

			foundShop.setId(r.getInt("id"));
			
			
		}
		
		return foundShop;
	}


	@Override
	public boolean deleteShopAtLocation(Location loc) throws SQLException {
		// TODO Auto-generated method stub
		
		//DELETE FROM `minecraft`.`CSF_Shops` WHERE `CSF_Shops`.`id` = 3
		
		String query = "DELETE FROM `"+shops_table+"` WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?";
		int success = executeUpdate(query, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		return (success > 0) ? true : false;
		
	}


	@Override
	public boolean setInStock(int id, int inStock) throws SQLException {
		// 
		
		String query = "UPDATE `"+shops_table+"` SET `inStock` = ? WHERE `id` = ?;";
		int success = executeUpdate(query, inStock, id);
		return (success > 0) ? true : false;
	}



	
}
