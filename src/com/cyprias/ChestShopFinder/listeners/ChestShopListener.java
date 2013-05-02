package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
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
		ShopDestroyedEvent.getHandlerList().unregister(instance);
		TransactionEvent.getHandlerList().unregister(instance);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onShopDestroyed(ShopDestroyedEvent event) throws SQLException {

		// event.getSign();

		// event.getSign().getLocation()

		// Shop shop =
		// Plugin.database.getShopAtLocation(event.getSign().getLocation());

		Logger.debug("onShopDestroyed " + Plugin.database.deleteShopAtLocation(event.getSign().getLocation()));

	}

	/*
	 * @EventHandler(priority = EventPriority.NORMAL) public void
	 * onPreTransaction(PreTransactionEvent event) throws SQLException {
	 * Logger.debug(event.getEventName()); }
	 */

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTransaction(TransactionEvent event) throws SQLException {

		Logger.debug(event.getEventName());

		final Sign sign = event.getSign();

		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				try {

					Shop shop = Plugin.database.getShopAtLocation(sign.getLocation());

					if (shop == null) {
						// Shop's not in our db yet.
						
						if (Config.getBoolean("properties.auto-register"))
							ChestShopListener.registerShop(sign);

					} else {

						// Logger.debug("Sign is shop!");

						int inStock = 0;

						String owner = sign.getLines()[0];
						ItemStack stock = MaterialUtil.getItem(sign.getLines()[3]);

						if (ChestShopSign.isAdminShop(owner)) {
							inStock = 64 * 9 * 6; // Full chest. :P
						} else {
							Chest chest = uBlock.findConnectedChest(sign.getBlock());
							if (chest != null) {
								inStock = InventoryUtil.getAmount(stock, chest.getInventory());
							}
						}

						// Logger.debug("onTransaction inStock: " + inStock);
						Logger.debug("Setting shop #" + shop.id + "'s stock to " + inStock);
						shop.setInStock(inStock);

					}

				} catch (SQLException e) {
					Logger.warning("Exception caught updating stock on transaction.");
					e.printStackTrace();
				}

			}
		});

	}

	public static void registerShopLines(String[] lines, final Sign sign) throws SQLException {

		final String owner = lines[0];

		if (!(Plugin.isInt(lines[1]))){
			Logger.debug("registerShopLines amount is not int");
			return;
		}
		final int amount = Integer.valueOf(lines[1]);
		// Logger.debug("amount: " + amount);

		String sPrice = lines[2];
		final double buyPrice = PriceUtil.getBuyPrice(sPrice);
		final double sellPrice = PriceUtil.getSellPrice(sPrice);

		final ItemStack stock = MaterialUtil.getItem(lines[3]);

		// Logger.debug("stock: " + stock.getTypeId() + " " +
		// stock.getDurability());
		final String enchantments = MaterialUtil.Enchantment.encodeEnchantment(stock);


		int inStock = 0;
		if (ChestShopSign.isAdminShop(owner)) {
			inStock = 64 * 9 * 6; // Full chest. :P
		} else {
			Chest chest = uBlock.findConnectedChest(sign.getBlock());

			if (chest != null) {

				inStock = InventoryUtil.getAmount(stock, chest.getInventory());

			}
		}
		final int finStock = inStock;

		//Logger.debug("registerShop inStock: " + inStock);

		Plugin.database.deleteShopAtLocation(sign.getLocation());
		Plugin.database.insert(owner, stock, enchantments, amount, buyPrice, sellPrice, sign.getLocation(), finStock);

	}

	public static void registerShop(final Sign sign) throws SQLException {
		registerShopLines(sign.getLines(), sign);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onShopCreated(final ShopCreatedEvent event) throws SQLException {

		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				try {
					if (Config.getBoolean("properties.auto-register"))
						registerShopLines(event.getSignLines(), event.getSign());
				} catch (SQLException e) {
					Logger.warning("Exception caught while registering new shop");
					e.printStackTrace();
				}
			}
		});

		// registerShop(event.getSign());
	}
}
