package com.cyprias.ChestShopFinder.database;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;


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
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.location = location;
		this.inStock = inStock;
	}
	
	public int id;
	public void setId(int id){
		this.id = id;
	}
	
	
	
}
