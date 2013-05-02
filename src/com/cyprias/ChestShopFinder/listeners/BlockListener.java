package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;

public class BlockListener implements Listener {

	static public void unregisterEvents(JavaPlugin instance) {
		SignChangeEvent.getHandlerList().unregister(instance);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event) throws SQLException {
		if (event.isCancelled())
			return;

		final Block block = event.getBlock();

		if (!(block.getType() == block.getType().WALL_SIGN))
			return;

		// Wait 1 tick so the lines on the sign update.
		Plugin.getInstance().getServer().getScheduler().runTaskLater(Plugin.getInstance(), new Runnable() {
			public void run() {

				Sign sign = (Sign) block.getState();

				// Make sure the sign's still there.
				if (sign == null)
					return;

				try {
					ChestShopListener.registerShop(sign);
				} catch (SQLException e) {
					Logger.warning("Exception caught while registering shop.");
					e.printStackTrace();
				}

			}
		}, 20L);

	}

}
