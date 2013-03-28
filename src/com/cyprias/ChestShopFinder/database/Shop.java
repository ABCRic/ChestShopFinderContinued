package com.cyprias.ChestShopFinder.database;

import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.cyprias.ChestShopFinder.Plugin;


public class Shop {
	//String owner, ItemStack stock, String enchantments, int amount, double buyPrice, double sellPrice, Location location
	
	public String owner, enchantments;
	//public ItemStack stock;
	
	public int typeId;
	public short durability;
	
	public int amount, inStock;
	public double buyPrice, sellPrice;
//	public Location location;
	
	public String worldName;
	public int x,y,z;
	
	public Shop(String owner, int typeId, short durability, String enchantments, int amount, double buyPrice, double sellPrice, int inStock){
		this.owner = owner;
		this.enchantments = enchantments;
		this.amount = amount;
		
		this.typeId = typeId;
		this.durability = durability;
		
		//Note Buy price is how much it costs the player to 'buy' from the shop. Same with sell.
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.inStock = inStock;
		
		
		
		//this.worldName = worldName;
		//this.x = x;
		//this.y = y;
		//this.z = z;
		
	}
	
	public ItemStack getStock(){
		ItemStack stock = new ItemStack(typeId, durability);
		stock.addEnchantments(MaterialUtil.Enchantment.getEnchantments(enchantments));
		return stock;
	}
	
	
	public void setLocation(Location loc){
		
		 //String worldName, int x, int y, int z
		
		this.worldName = loc.getWorld().getName();
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
	}
	
	public void setWorldName(String worldName){
		this.worldName = worldName;
	}
	
	public void setX(int x){
		this.x = x;
	}
	public void setY(int y){
		this.y = y;
	}
	public void setZ(int z){
		this.z = z;
	}
	
	public int getX(){
		return this.x;
	}
	public int getY(){
		return this.y;
	}
	public int getZ(){
		return this.z;
	}
	
	public int id;
	public void setId(int id){
		this.id = id;
	}
	public void setInStock(int inStock2) throws SQLException {
		this.inStock = inStock2;
		Plugin.database.setInStock(id, inStock2);
		
	}
	
	public World getWorld(){
		return Plugin.getInstance().getServer().getWorld(worldName);
	}
	
	public Location getLocation(){
		return new Location(getWorld(), x, y, z);
	}
	
	public boolean delete() throws SQLException{
		return Plugin.database.deleteShop(this.id);
	}
	
	
}
