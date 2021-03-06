package com.cyprias.ChestShopFinder.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.utils.ChatUtils;

public class MySQL implements Database {




	static String prefix;
	static String shops_table;
	static String transactions_table;
	
	public Boolean init() {
		if (!canConnect()){
			Logger.info("Failed to connect to MySQL!");
			return false;
		}
		prefix = Config.getString("mysql.prefix");
		shops_table = prefix+ "Shops";
		transactions_table = prefix+ "Transactions";
		
		try {
			createTables();
		} catch (SQLException e) {
			Logger.warning("Caught error while creating DB tables. ");
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
		
		if (tableExists(transactions_table) == false) {
			Logger.info("Creating "+transactions_table+" table.");
			con.prepareStatement("CREATE TABLE "+transactions_table+" (`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `owner` VARCHAR(32) NOT NULL, `client` VARCHAR(32) NOT NULL, `flags` INT NOT NULL, `price` DOUBLE NOT NULL, `typeId` INT NOT NULL, `durability` INT NOT NULL, `enchantments` VARCHAR(16) NOT NULL, `amount` INT NOT NULL, `time` DOUBLE NOT NULL) ENGINE = InnoDB").executeUpdate();
		}
		
		con.close();
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
		if (enchantments == null)
			enchantments = "";
		
		
		String query = "INSERT INTO `"+shops_table+"` (`id` ,`owner` ,`typeId` ,`durability` ,`enchantments` ,`amount` ,`buyPrice` ,`sellPrice` ,`world` ,`x` ,`y` ,`z`, `inStock`)VALUES (NULL , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		int success = executeUpdate(query, owner, stock.getTypeId(), stock.getDurability(), enchantments, amount, buyPrice, sellPrice, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), inStock);
		return (success > 0) ? true : false;
		
	}


	// Return a shop if it exists at a location. 
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


	// Delete a shop at a given location. 
	public boolean deleteShopAtLocation(Location loc) throws SQLException {
		String query = "DELETE FROM `"+shops_table+"` WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?";
		return (executeUpdate(query, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) > 0) ? true : false;
	}


	//Update how much a shop has in stock. 
	public boolean setInStock(int id, int inStock) throws SQLException {
		String query = "UPDATE `"+shops_table+"` SET `inStock` = ? WHERE `id` = ?;";
		return (executeUpdate(query, inStock, id) > 0) ? true : false;
	}

	//http://forums.phpfreaks.com/topic/84811-solved-sorting-distance-from-a-point-in-a-coordinate-system-from-a-mysql-table/
	//String qry = "SELECT *,(((acos(sin((" + latitude + "*pi()/180)) * sin((`x`*pi()/180))+cos((" + latitude + "*pi()/180)) * cos((`x`*pi()/180)) * cos(((" + longitude + "- `z`)*pi()/180))))*180/pi())*60*1.1515) as distance FROM `"+shops_table+"` HAVING distance >= " + distance;

	/*
	 Return the 10 nearest shops to a location. 
	 */
	public List<Shop> findItemNearby(ItemStack stock, Location loc) throws SQLException {
		List<Shop> shops =  new ArrayList<Shop>();
		
		int pX = loc.getBlockX();
		int pZ = loc.getBlockZ();
		
		String enchantments = null; //MaterialUtil.Enchantment.encodeEnchantment(stock);
		if (enchantments == null)
			enchantments = "";

		String qry = "SELECT *, SQRT(("+pX+"-x)*("+pX+"-x) + ("+pZ+"-z)*("+pZ+"-z)) as distance FROM "+shops_table+" WHERE `inStock` >= `amount` AND `world` LIKE ?"
				+ " AND `typeId` = ? AND `durability` = ? AND `enchantments` = ?";

		if (Config.getBoolean("properties.one-owner-per-results"))
			qry += " GROUP BY `owner`";
		
		qry += " ORDER BY distance LIMIT 0 , " + Config.getInt("properties.search-results");
		
		
		
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

	/*
SELECT * FROM `CSF_Shops` AS q
LEFT JOIN `iConomy` AS i ON (
    q.owner LIKE i.username
)
WHERE `sellPrice` > 0 AND `balance` >= `sellPrice`;

	Shows only sell shops where the account's balance is above the shop's sell price, so you're guarenteed to to sell an item there.

	 */

	public List<Shop> getShopsPricesByItem(ItemStack stock) throws SQLException {
		List<Shop> shops =  new ArrayList<Shop>();
		
		//String qry = "SELECT (`buyPrice` / `amount`) as buyEach, (`sellPrice` / `amount`) as sellEach FROM `"+shops_table+"` WHERE `typeId` = 264 AND `durability` = 0 AND `enchantments` = "" AND `inStock` > 0;";
		
		//String qry = "SELECT (`buyPrice` / `amount`) as buyEach, (`sellPrice` / `amount`) as sellEach";
		String qry = "SELECT *";
		
		qry += " FROM `"+shops_table+"`";
		//qry += " WHERE `world` LIKE ?";	// Only include the world we're in.
		qry += " WHERE `typeId` = ? AND `durability` = ? AND `enchantments` = ?";	// Only show the item we're searching for.
		//qry += " AND `inStock` > 0;";
		//qry += " ORDER BY `"+getColumn+"` DESC";
		
		
			
		
		
		String enchantments = null; //MaterialUtil.Enchantment.encodeEnchantment(stock);
		if (enchantments == null)
			enchantments = "";
		
		

		queryReturn results = executeQuery(qry, stock.getTypeId(), stock.getDurability(), enchantments);
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
		
		
	//	Dobule[] a = shops.toArray();
		
		
		
		
		return shops; //Doubles.toArray(shops);
		
	}
	
	public List<Shop> findBuySellItemNearby(ItemStack stock, Location loc, boolean isBuy) throws SQLException {
		List<Shop> shops =  new ArrayList<Shop>();
		
		int pX = loc.getBlockX();
		int pZ = loc.getBlockZ();
		
		String enchantments = null; //MaterialUtil.Enchantment.encodeEnchantment(stock);
		if (enchantments == null)
			enchantments = "";

		
		
		String qry = "SELECT *";	//Select all columns
		qry += ", SQRT(("+pX+"-x)*("+pX+"-x) + ("+pZ+"-z)*("+pZ+"-z)) as distance";	//Create column `distance` with our relative distance
		qry += "  FROM `"+shops_table+"` AS q";	// From the shops table, save it as q.
		if (Config.getString("mysql.iConomy_table") != "false")
			qry += " LEFT JOIN `"+Config.getString("mysql.iConomy_table")+"` AS i ON (q.owner LIKE i.username)";	// Include the iConomy table, binded by the owner and username columns.
		
		
		qry += " WHERE `world` LIKE ?";	// Only include the world we're in.
		qry += " AND `typeId` = ? AND `durability` = ? AND `enchantments` = ?";	// Only show the item we're searching for.
		if (isBuy == true){
			qry += " AND `buyPrice` > 0 AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR  `inStock` >= `amount`)"; // Only show shops with a buy price, and only if their stock is more than their amount on sign.
		}else{
			
			//Only include those with a sell price./whois 
			qry += " AND `sellPrice` > 0";
			
			//Exclude full shops.
			if (Config.getBoolean("properties.exclude-full-chests-from-sell"))
				qry += " AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR (`inStock` != '1728' AND `inStock` != '3456'))";
			
			// Make sure shop owner can afford the item.
			if (Config.getString("mysql.iConomy_table") != "false")
				qry += " AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR `balance` >= `sellPrice`)";	// Only show shops with a sell price, and only if the owner's money balance is more than their sell price.
			
			
			
			
		}
		
		if (Config.getBoolean("properties.one-owner-per-results"))
			qry += " GROUP BY `owner`";
		
		qry += " ORDER BY distance"; // Sort listing by the distance column we made.
		qry += " LIMIT 0 , " + Config.getInt("properties.search-results"); //Only pull the first 10.
		

	//	Logger.debug("qry: " + qry);
		
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
	
	/*
	 Return all shops between a set of coords, used when a chunk is loaded to check if any registers shops are missing from the world.
	 */
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



	

	public boolean deleteShop(int id) throws SQLException {
		String query = "DELETE FROM `"+shops_table+"` WHERE `id` = ?";
		return (executeUpdate(query, id) > 0) ? true : false;
	}


	@Override
	public List<Shop> findArbitrage(ItemStack stock, Location loc) throws SQLException {
		
		
		String enchantments = null; //MaterialUtil.Enchantment.encodeEnchantment(stock);
		if (enchantments == null)
			enchantments = "";
		
		String worldName = loc.getWorld().getName();
		
		String qry = "SELECT * FROM `"+shops_table+"`";
		qry += " WHERE `typeId` = ? AND `durability` = ? AND `enchantments` = ?";
		qry += " AND `world` LIKE ?";
		//qry += " AND `buyPrice` > 0";

		qry += " AND `buyPrice` > 0 AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR  `inStock` >= `amount`)"; // Only show shops with a buy price, and only if their stock is more than their amount on sign.
		
		
		qry += " ORDER BY (`buyPrice` / `amount`) ASC";
		qry += " LIMIT 0 , 1";
		queryReturn results = executeQuery(qry, stock.getTypeId(), stock.getDurability(), enchantments, worldName);
		ResultSet r = results.result;
		Shop lowestBuy = null;
		Shop highestSell = null;
		if (r.next()) {
			lowestBuy = new Shop(r.getString("owner"), r.getInt("typeId"),r.getShort("durability"), r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), r.getInt("inStock"));
			lowestBuy.setWorldName(r.getString("world"));
			lowestBuy.setX(r.getInt("x"));
			lowestBuy.setY(r.getInt("y"));
			lowestBuy.setZ(r.getInt("z"));
			
			lowestBuy.setId(r.getInt("id"));
		
		}
		if (lowestBuy == null)
			return null;
		
		//Logger.debug("lowestBuy: " + lowestBuy.buyPrice);
		
		qry = "SELECT * FROM `"+shops_table+"` AS q";
		
		if (Config.getString("mysql.iConomy_table") != "false")
			qry += " LEFT JOIN `"+Config.getString("mysql.iConomy_table")+"` AS i ON (q.owner LIKE i.username)";	// Include the iConomy table, binded by the owner and username columns.
		
		
		qry += " WHERE `typeId` = ? AND `durability` = ? AND `enchantments` = ?";
		qry += " AND `world` LIKE ?";
		//qry += " AND `sellPrice` > 0";
		
		//Exclude full shops.
		if (Config.getBoolean("properties.exclude-full-chests-from-sell"))
			qry += " AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR (`inStock` != '1728' AND `inStock` != '3456'))";
		
		// Make sure shop owner can afford the item.
		if (Config.getString("mysql.iConomy_table") != "false")
			qry += " AND (`owner` LIKE '" + Config.getString("properties.admin-shop") + "' OR `balance` >= `sellPrice`)";	// Only show shops with a sell price, and only if the owner's money balance is more than their sell price.
		
		
		qry += " ORDER BY (`sellPrice` / `amount`) DESC";
		qry += " LIMIT 0 , 1";
		
		results = executeQuery(qry, stock.getTypeId(), stock.getDurability(), enchantments, worldName);
		r = results.result;
		
		if (r.next()) {
			highestSell = new Shop(r.getString("owner"), r.getInt("typeId"),r.getShort("durability"), r.getString("enchantments"), r.getInt("amount"), r.getDouble("buyPrice"), r.getDouble("sellPrice"), r.getInt("inStock"));
			highestSell.setWorldName(r.getString("world"));
			highestSell.setX(r.getInt("x"));
			highestSell.setY(r.getInt("y"));
			highestSell.setZ(r.getInt("z"));
			
			highestSell.setId(r.getInt("id"));
		}
		if (highestSell == null)
			return null;
		
		//Logger.debug("highestSell: " + highestSell.sellPrice);
		
		
		if ((lowestBuy.buyPrice / lowestBuy.amount) >= (highestSell.sellPrice / highestSell.amount))
			return null;
		
		//Logger.debug("Deal found?");
		List<Shop> shops =  new ArrayList<Shop>();
		shops.add(lowestBuy);
		shops.add(highestSell);
		
		
		return shops;
	}


	public boolean insertTransaction(Transaction transaction) throws SQLException {
		String query = "INSERT INTO "+transactions_table + " (`id` ,`owner` ,`client` ,`flags` ,`price` ,`typeId` ,`durability` ,`enchantments` ,`amount` ,`time`)VALUES (NULL , ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		int success = executeUpdate(query, 
			transaction.getOwner(), 
			transaction.getClient(),
			transaction.getFlags(),
			transaction.getPrice(),
			transaction.getTypeId(),
			transaction.getDurability(),
			transaction.getEnchantments(),
			transaction.getAmount(),
			transaction.getTime()
		);
		return (success > 0) ? true : false;
	}

	
	
	public List<Transaction> getOwnerTransactions(CommandSender sender, String ownerName, int page) throws SQLException {
		int rows = getResultCount("SELECT COUNT(*) FROM " + transactions_table + " WHERE `owner` LIKE ?", ownerName);
		int perPage = Config.getInt("properties.transaction-results");
		int max = (rows / perPage);// + 1;
		if (rows % perPage == 0)
			max--;
		if (page < 0){
			page = max - (Math.abs(page) - 1);
		}else{
			if (page > max)
				page = max;

		}
		
		
		
		if (sender != null)
			ChatUtils.send(sender, "�7Page: �f" + (page+1) + "�7/�f" + (max+1));
		
		if (rows == 0)
			return null;
		
		
		
		
		
	
		
		
		List<Transaction> transactions =  new ArrayList<Transaction>();
		String qry = "SELECT * FROM `"+transactions_table+"` WHERE `owner` LIKE ? AND `time` >= ? ORDER BY `id` LIMIT "+(perPage * page)+" , " + perPage;

		queryReturn results = executeQuery(qry, ownerName, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;

		Transaction transaction;
		while (r.next()) {

			transaction = new Transaction(
				r.getString("owner"), 
				r.getString("client"), 
				r.getInt("flags"), 
				r.getDouble("price"), 
				r.getInt("typeId"),
				r.getShort("durability"),
				r.getString("enchantments"),
				r.getInt("amount"),
				r.getLong("time")
			);
		
			
			transaction.setId(r.getInt("id"));
		
			
			
			transactions.add(transaction);
			
		}
		results.close();
		return transactions;
	}


	@Override
	public List<popularTrader> getTopPopularShopOwner() throws SQLException {
		List<popularTrader> traders =  new ArrayList<popularTrader>();
		String qry = "SELECT owner, count(distinct client) as uniqueClients FROM `"+transactions_table+"` WHERE `owner` != '"+Config.getString("properties.admin-shop")+"'  AND `time` >= ? GROUP BY `owner` ORDER BY `uniqueClients` DESC LIMIT 0 , "+Config.getInt("properties.transaction-results");

		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {
			traders.add(new popularTrader(r.getString("owner"), r.getInt("uniqueClients")));
		}
		results.close();
		return traders;
	}
	public List<popularTrader> getTopPopularShopClient() throws SQLException {
		List<popularTrader> traders =  new ArrayList<popularTrader>();
		String qry = "SELECT client, count(distinct owner) as uniqueOwners FROM `"+transactions_table+"` WHERE `owner` != '"+Config.getString("properties.admin-shop")+"'  AND `time` >= ? GROUP BY `client` ORDER BY `uniqueOwners` DESC LIMIT 0 , "+Config.getInt("properties.transaction-results");

		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {
			traders.add(new popularTrader(r.getString("client"), r.getInt("uniqueOwners")));
		}
		results.close();
		return traders;
	}
	

	@Override
	public List<traderCount> getTopOwnersByItemsSold() throws SQLException {

		String qry = "SELECT owner, SUM(`amount`) as amountTotal FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `owner` ORDER BY `amountTotal` DESC LIMIT 0 , "+Config.getInt("properties.transaction-results");
		List<traderCount> owners =  new ArrayList<traderCount>();
		
		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {
			owners.add(new traderCount(r.getString("owner"), r.getInt("amountTotal")));
		}
		results.close();
		return owners;
	}


	@Override
	public List<traderCount> getTopOwnerByProfit() throws SQLException {
		String qry = "SELECT owner, SUM(`price`) as topProfit FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `owner` ORDER BY `topProfit` DESC LIMIT 0 , " + Config.getInt("properties.transaction-results");
		List<traderCount> owners =  new ArrayList<traderCount>();
		
		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {
			owners.add(new traderCount(r.getString("owner"), r.getDouble("topProfit")));
		}
		results.close();
		return owners;
	}

	public List<traderCount> getTopClientBySpent() throws SQLException {
		String qry = "SELECT client, SUM(`price`) as topProfit FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `client` ORDER BY `topProfit` DESC LIMIT 0 , " + Config.getInt("properties.transaction-results");
		List<traderCount> owners =  new ArrayList<traderCount>();
		
		
		
		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {
			owners.add(new traderCount(r.getString("client"), r.getDouble("topProfit")));
		}
		
	
		
		results.close();
		return owners;
	}
	

	@Override
	public List<itemTraded> topItemBought(String orderBy) throws SQLException {
		//String qry = "SELECT count(*) as totalTransactions, typeId, durability, enchantments, SUM(`amount`) as totalAmount, SUM(`price`) as totalPrice FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `typeId`, `durability`, `enchantments` ORDER BY `"+orderBy+"` DESC LIMIT 0 , " + Config.getInt("properties.transaction-results");
		String qry = "SELECT count(distinct `client`) as uniqueClients, count(*) as totalTransactions, typeId, durability, enchantments, SUM(`amount`) as totalAmount, SUM(`price`) as totalPrice FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `typeId`, `durability`, `enchantments` ORDER BY `"+orderBy+"` DESC LIMIT 0 , " + Config.getInt("properties.transaction-results");
		
		List<itemTraded> items =  new ArrayList<itemTraded>();
		
		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {

			items.add(new itemTraded(r.getInt("typeId"), r.getInt("durability"), r.getString("enchantments"), r.getInt("totalTransactions"), r.getInt("totalAmount"),r.getDouble("totalPrice"), r.getInt("uniqueClients")));
			
			
		}
		results.close();
		return items;
	}
	public List<itemTraded> topItemSold(String orderBy) throws SQLException {
		//String qry = "SELECT count(*) as totalTransactions, typeId, durability, enchantments, SUM(`amount`) as totalAmount, SUM(`price`) as totalPrice FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `typeId`, `durability`, `enchantments` ORDER BY `"+orderBy+"` DESC LIMIT 0 , " + Config.getInt("properties.transaction-results");
		String qry = "SELECT count(distinct `owner`) as uniqueOwners, count(*) as totalTransactions, typeId, durability, enchantments, SUM(`amount`) as totalAmount, SUM(`price`) as totalPrice FROM `"+transactions_table+"` WHERE `flags` = 2 AND `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` >= ? GROUP BY `typeId`, `durability`, `enchantments` ORDER BY `"+orderBy+"` DESC LIMIT 0 , " + Config.getInt("properties.transaction-results");
		
		List<itemTraded> items =  new ArrayList<itemTraded>();
		
		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {

			items.add(new itemTraded(r.getInt("typeId"), r.getInt("durability"), r.getString("enchantments"), r.getInt("totalTransactions"), r.getInt("totalAmount"),r.getDouble("totalPrice"), r.getInt("uniqueOwners")));
			
			
		}
		results.close();
		return items;
	}
	

	@Override
	public List<popularTrader> getOwnersTopClients(String ownerName) throws SQLException {
		String qry = "SELECT owner, client, SUM(`price`) as totalPrice FROM `"+transactions_table+"` WHERE `flags` = 1 AND `owner` = ? AND `time` >= ? GROUP BY `client` ORDER BY `totalPrice` DESC";// LIMIT 0 , 10
		
		
		List<popularTrader> traders =  new ArrayList<popularTrader>();
		
		queryReturn results = executeQuery(qry, ownerName, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {

			//traders.add(new itemTraded(r.getInt("typeId"), r.getInt("durability"), r.getString("enchantments"), r.getInt("totalTransactions"), r.getInt("totalAmount"),r.getDouble("totalPrice"), r.getInt("uniqueClients")));
			
			traders.add(new popularTrader(r.getString("client"), r.getDouble("totalPrice")));

		}
		results.close();
		return traders;
	}

	public List<popularTrader> getClientsTopOwners(String clientName) throws SQLException {
		String qry = "SELECT client, owner, SUM(`price`) as totalPrice FROM `"+transactions_table+"` WHERE `flags` = 1 AND `client` = ? AND `time` >= ? GROUP BY `owner` ORDER BY `totalPrice` DESC;"; // LIMIT 0 , 10
		
		
		List<popularTrader> traders =  new ArrayList<popularTrader>();
		
		queryReturn results = executeQuery(qry, clientName, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		while (r.next()) {

			//traders.add(new itemTraded(r.getInt("typeId"), r.getInt("durability"), r.getString("enchantments"), r.getInt("totalTransactions"), r.getInt("totalAmount"),r.getDouble("totalPrice"), r.getInt("uniqueClients")));
			
			traders.add(new popularTrader(r.getString("owner"), r.getDouble("totalPrice")));

		}
		results.close();
		return traders;
	}


	@Override
	public Stats getOverallStats() throws SQLException {
		String qry = "SELECT COUNT('*') as totalCount, SUM(`price`) as totalPrice, SUM(`amount`) as totalAmount FROM `"+transactions_table+"` WHERE `owner` != '"+Config.getString("properties.admin-shop")+"' AND `time` > ? ;"; // LIMIT 0 , 10
		
		Stats stats = null;
		
		queryReturn results = executeQuery(qry, (Plugin.getUnixTime() - Config.getInt("properties.transaction-age-include")));
		ResultSet r = results.result;
		if (r.next()) 
			stats = new Stats(r.getInt("totalCount"), r.getDouble("totalPrice"), r.getInt("totalAmount"));
		
		results.close();
		return stats;
	}



	/*
	public static HashMap<Integer, Integer> updateStock = new HashMap<Integer, Integer>();
	
	BukkitTask stockUpdateTask = null;
	
	public void queueForStockUpdate(int id, int inStock) {
		updateStock.put(id, inStock);
		
		if (stockUpdateTask != null)
			return;
		
		stockUpdateTask = Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				Connection con;
				PreparedStatement statement;
				try {
					con = getConnection();
					
					Iterator it = updateStock.entrySet().iterator();
					
				    while (it.hasNext()) {
				        Map.Entry pairs = (Map.Entry)it.next();
				       // System.out.println(pairs.getKey() + " = " + pairs.getValue());
				        statement =  con.prepareStatement("UPDATE `"+shops_table+"` SET `inStock` = ? WHERE `id` = ?;");
				        
				        statement.setInt(1, (Integer) pairs.getKey());
				        statement.setInt(2, (Integer) pairs.getValue());
				        
				        Logger.debug("queueForStockUpdate " + pairs.getKey() + " = " + pairs.getValue());
				        
				        statement.executeUpdate();
				        
				        it.remove(); // avoids a ConcurrentModificationException
				    }
				    con.close();
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				
				stockUpdateTask = null;

			    
			    
			}});
		
	}
	 */

	
	
}
