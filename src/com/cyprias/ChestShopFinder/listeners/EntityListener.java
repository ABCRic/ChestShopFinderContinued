package com.cyprias.ChestShopFinder.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.cyprias.ChestShopFinder.Logger;


public class EntityListener implements Listener {

	
	static public void unregisterEvents(JavaPlugin instance){
		EntityDeathEvent.getHandlerList().unregister(instance);
	}
	
	/*
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event)  {
	
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventory(InventoryEvent event)  {
		Logger.info(event.getEventName());
	}*/
	

	
	/*
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryOpen(InventoryOpenEvent event)  {
		Logger.info(event.getEventName());
		
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event)  {
		Logger.info(event.getEventName());
		
		
	}*/
	
}
