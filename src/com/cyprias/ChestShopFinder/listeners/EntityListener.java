package com.cyprias.ChestShopFinder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.ChestShop.Events.ShopCreatedEvent;


public class EntityListener implements Listener {

	
	static public void unregisterEvents(JavaPlugin instance){
		EntityDeathEvent.getHandlerList().unregister(instance);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event)  {
	
	}
	

	
}
