package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Containers.AdminInventory;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;

// http://dev.bukkit.org/server-mods/chestshop/pages/chest-shops-api/
public class ChestShopListener implements Listener {

	static public void unregisterEvents(JavaPlugin instance) {
		ShopCreatedEvent.getHandlerList().unregister(instance);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onShopDestroyed(ShopDestroyedEvent event) throws SQLException {

		// event.getSign();

		// event.getSign().getLocation()

		// Shop shop =
		// Plugin.database.getShopAtLocation(event.getSign().getLocation());

		Logger.info("onShopDestroyed " + Plugin.database.deleteShopAtLocation(event.getSign().getLocation()));

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onShopCreated(ShopCreatedEvent event) throws SQLException {

		Logger.info("onShopCreated: ");

		// for (int i=0; i<event.getSignLines().length; i++){
		// Logger.info(event.getSignLines()[i]);
		// }

		String owner = event.getSignLines()[0];
		Logger.info("owner: " + owner);

		int amount = Integer.valueOf(event.getSignLines()[1]);
		Logger.info("amount: " + amount);

		String sPrice = event.getSignLines()[2];
		double buyPrice = PriceUtil.getBuyPrice(sPrice);
		double sellPrice = PriceUtil.getSellPrice(sPrice);
		Logger.info("buyPrice: " + buyPrice + ", sellPrice: " + sellPrice);

		// if (!PriceUtil.hasBuyPrice(sPrice) &&
		// !PriceUtil.hasSellPrice(sPrice)) {
		// event.setOutcome(INVALID_PRICE);
		// }

		ItemStack stock = MaterialUtil.getItem(event.getSignLines()[3]);

		Logger.info("stock: " + stock.getTypeId() + " " + stock.getDurability());
		String enchantments = MaterialUtil.Enchantment.encodeEnchantment(stock);
		Logger.info("enchantments: " + enchantments);



		// Block rBlock = event.getSign().getBlock().getRelative(0, -1, 0);

		// Logger.info("rBlock: " + rBlock.getType());

		int inStock = 0;

		
		if (ChestShopSign.isAdminShop(owner)){
			inStock = 64*9*6; //Full chest. :P
		}else{
			Chest chest = uBlock.findConnectedChest(event.getSign().getBlock());

			if (chest != null) {
		
				inStock = InventoryUtil.getAmount(stock, chest.getInventory());

			}
		}

		Logger.info("inStock: " + inStock);
		
		
		if (Config.getBoolean("properties.auto-register"))
			Plugin.database.insert(owner, stock, enchantments, amount, buyPrice, sellPrice, event.getSign().getLocation(), inStock);
		
	}
}
