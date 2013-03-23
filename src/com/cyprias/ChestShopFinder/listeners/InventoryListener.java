package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;

import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.database.Shop;

public class InventoryListener implements Listener {
	
	static public void unregisterEvents(JavaPlugin instance){
		EntityDeathEvent.getHandlerList().unregister(instance);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) throws SQLException  {
		Logger.info(event.getEventName());

		
        if (event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest){
        	InventoryHolder holder = event.getInventory().getHolder();
        	
        	Chest c = (Chest) holder;
        //	Block b = (Block) holder;
        	
        	//Logger.info("loc: " + c.getLocation().getBlockX() + " " + c.getLocation().getBlockY() + " " + c.getLocation().getBlockZ());
        	
        	//Logger.info("isShopChest: " + ChestShopSign.isShopChest(b));
        	
        	//Logger.info("findAnyNearbyShopSign: " + uBlock.findAnyNearbyShopSign(b));
        	
        	Sign sign = uBlock.getConnectedSign(c);
        	
        	if (sign != null){
        		Logger.info("getConnectedSign: " + sign);
        	
        		
        		Shop shop = Plugin.database.getShopAtLocation(sign.getLocation());
        		
        		if (shop != null){
        			
        			Logger.info("Sign is shop!");
        			
        			int inStock = 0;

        			String owner = sign.getLines()[0];
        			ItemStack stock = MaterialUtil.getItem(sign.getLines()[3]);
        			
        			if (ChestShopSign.isAdminShop(owner)){
        				inStock = 64*9*6; //Full chest. :P
        			}else{
        				Chest chest = uBlock.findConnectedChest(sign.getBlock());
        				if (chest != null) {
        					inStock = InventoryUtil.getAmount(stock, chest.getInventory());
        				}
        			}
        			
        			
        			Logger.info("inStock: " + inStock);
        			
        			shop.setInStock(inStock);
        			
        			
        			
        		}
        		
        		
        	}
        	
        	
        }
        
        
		
	}

}
