package com.cyprias.ChestShopFinder.database;

import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.cyprias.ChestShopFinder.Plugin;


public class Shop {
	//String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location
	
	public String owner, enchantments;
	public ItemStack stock;
	public int amount, inStock;
	public double buyPrice, sellPrice;
	public Location location;
	
	public Shop(String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location, int inStock){
		this.owner = owner;
		this.stock = stock;
		this.enchantments = enchantments;
		this.amount = amount;
		
		//Note Buy price is how much it costs the player to 'buy' from the shop. Same with sell.
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.location = location;
		this.inStock = inStock;
	}
	
	public int id;
	public void setId(int id){
		this.id = id;
	}
	public void setInStock(int inStock2) throws SQLException {
		this.inStock = inStock2;
		Plugin.database.setInStock(id, inStock2);
		
	}
	
	
	
}
