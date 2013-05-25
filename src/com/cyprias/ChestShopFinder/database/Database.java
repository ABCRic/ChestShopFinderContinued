package com.cyprias.ChestShopFinder.database;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;



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
	
	//void queueForStockUpdate(int id, int inStock);
	
	boolean insertTransaction(Transaction transaction) throws SQLException;
	
	
	List<Transaction> getOwnerTransactions(CommandSender sender, String playerName, int page) throws SQLException;
	
	List<popularTrader> getTopPopularShopOwner() throws SQLException;
	List<popularTrader> getTopPopularShopClient() throws SQLException;
	class popularTrader{
		public String traderName;
		public int popCount;
		public popularTrader(String traderName, int int1) {
			this.traderName = traderName;
			this.popCount = int1;
		}
		public double dnum;
		public popularTrader(String string, double dnum) {
			this.traderName = string;
			this.dnum = dnum;
		}
	}
	
	
	List<traderCount> getTopOwnersByItemsSold() throws SQLException;
	class traderCount{
		public String playerName;
		public int icount;
		public double dcount;
		public traderCount(String ownerName, int int1) {
			this.playerName = ownerName;
			this.icount = int1;
		}
		public traderCount(String ownerName, double int1) {
			this.playerName = ownerName;
			this.dcount = int1;
		}
	}
	
	
	List<traderCount> getTopOwnerByProfit() throws SQLException;
	List<traderCount> getTopClientBySpent() throws SQLException;
	
	
	
	List<itemTraded> topItemBought(String orderBy) throws SQLException;
	class itemTraded{
		//items.add(new itemTraded(r.getInt("itemId"), r.getInt("durability"), r.getString("enchantments"), r.getInt("totalTransactions"), r.getInt("totalAmount"),r.getDouble("totalPrice")));
		
		public itemTraded(int itemId, int durability, String enchantments, int totalTransactions, int totalAmount, double totalPrice, int clients) {
			this.stock = new ItemStack(itemId, durability);
			stock.addEnchantments(MaterialUtil.Enchantment.getEnchantments(enchantments));
			
			this.transactions = totalTransactions;
			this.amount = totalAmount;
			this.price = totalPrice;
			this.clients = clients;
		}
		public ItemStack stock;
		public int transactions, amount, clients;
		public double price;
	}
	
	List<popularTrader> getOwnersTopClients(String ownerName) throws SQLException;
	
	
	
	
	
}
