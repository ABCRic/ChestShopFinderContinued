package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;


public class InventoryListener implements Listener {

	static public void unregisterEvents(JavaPlugin instance) {
		InventoryCloseEvent.getHandlerList().unregister(instance);
		InventoryMoveItemEvent.getHandlerList().unregister(instance);
	}

	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) throws SQLException {
		if (event.isCancelled())
			return;
		
		if (!Config.getBoolean("properties.hopper-detection"))
			return;

		if (event.getDestination().getHolder() instanceof Chest || event.getDestination().getHolder() instanceof DoubleChest) {
			InventoryHolder holder = event.getDestination().getHolder();

			
			
			if (holder instanceof Chest) {
				Sign sign = uBlock.getConnectedSign((Chest) holder);

				if (sign != null)
					checkSign(sign);
			} else if (holder instanceof DoubleChest) {

				DoubleChest dc = (DoubleChest) holder;

				Sign lsign = null, rsign;
				if (dc.getLeftSide() instanceof Chest) {
					lsign = uBlock.getConnectedSign((Chest) dc.getLeftSide());
					if (lsign != null)
						checkSign(lsign);
				}

				if (dc.getRightSide() instanceof Chest) {
					rsign = uBlock.getConnectedSign((Chest) dc.getRightSide());
					if (rsign != null && (!rsign.equals(lsign)))
						checkSign(rsign);
				}
			}
			
			
			
		}else	if (event.getSource().getHolder() instanceof Chest || event.getSource().getHolder() instanceof DoubleChest) {
			InventoryHolder holder = event.getSource().getHolder();
			//Logger.debug("onInventoryMoveItem 5");
			
			if (holder instanceof Chest) {
				Sign sign = uBlock.getConnectedSign((Chest) holder);

				if (sign != null)
					checkSign(sign);
			} else if (holder instanceof DoubleChest) {

				DoubleChest dc = (DoubleChest) holder;

				Sign lsign = null, rsign;
				if (dc.getLeftSide() instanceof Chest) {
					lsign = uBlock.getConnectedSign((Chest) dc.getLeftSide());
					if (lsign != null)
						checkSign(lsign);
				}

				if (dc.getRightSide() instanceof Chest) {
					rsign = uBlock.getConnectedSign((Chest) dc.getRightSide());
					if (rsign != null && (!rsign.equals(lsign)))
						checkSign(rsign);
				}
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) throws SQLException {
		// Logger.debug(event.getEventName());
		
		if (event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest) {
			InventoryHolder holder = event.getInventory().getHolder();

			if (holder instanceof Chest) {
				Sign sign = uBlock.getConnectedSign((Chest) holder);

				if (sign != null)
					checkSign(sign);
			} else if (holder instanceof DoubleChest) {

				DoubleChest dc = (DoubleChest) holder;

				Sign lsign = null, rsign;
				if (dc.getLeftSide() instanceof Chest) {
					lsign = uBlock.getConnectedSign((Chest) dc.getLeftSide());
					if (lsign != null)
						checkSign(lsign);
				}

				if (dc.getRightSide() instanceof Chest) {
					rsign = uBlock.getConnectedSign((Chest) dc.getRightSide());
					if (rsign != null && (!rsign.equals(lsign)))
						checkSign(rsign);
				}
			}

		}

	}

	public static HashMap<Sign, Long> checkThrottle = new HashMap<Sign, Long>();
	
	static void checkSign(final Sign fsign) {
		//Exit if we've checked this shop in the past 5 seconds, the event seems to fire a few times.
		if (checkThrottle.containsKey(fsign))
			if ((Plugin.getUnixTime() - checkThrottle.get(fsign))  < 5)
				return;
		checkThrottle.put(fsign, Plugin.getUnixTime());

		// Run in async thread so our DB call doesn't lockup the thread. It isn't perfect since we shouldn't
		// be doing block lookups asyncly, hopefully no harm comes about.
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				try {

					Shop shop = Plugin.database.getShopAtLocation(fsign.getLocation());

					if (shop == null) {
						// Shop's not in our db yet.
						
						if (Config.getBoolean("properties.auto-register"))
							ChestShopListener.registerShop(fsign);

					} else {

						// Logger.info("Sign is shop!");
						int inStock = 0;

						String owner = fsign.getLines()[0];
						ItemStack stock = MaterialUtil.getItem(fsign.getLines()[3]);

						if (ChestShopSign.isAdminShop(owner)) {
							inStock = 64 * 9 * 6; // Full chest. :P
						} else {
							Chest chest = uBlock.findConnectedChest(fsign.getBlock());
							if (chest != null) {
								try{
									inStock = InventoryUtil.getAmount(stock, chest.getInventory());
								} catch (NullPointerException e) {
									inStock = 0;
								}
							}
						}

							
						shop.setInStock(inStock);

					}

				} catch (SQLException e) {
					Logger.warning("Exception caught while updating shop stock.");
					e.printStackTrace();
				}

			}
		});

	}
	
	
}
