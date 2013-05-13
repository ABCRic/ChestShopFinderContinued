package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
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

public class WorldListener implements Listener {

	static public void unregisterEvents(JavaPlugin instance) {
		ChunkLoadEvent.getHandlerList().unregister(instance);
	}

	public static HashMap<String, Boolean> checkedChunks = new HashMap<String, Boolean>();
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoad(ChunkLoadEvent event) throws SQLException {

		if (!Config.getBoolean("properties.verify-chunk-shops"))
			return;

		final Chunk chunk = event.getChunk();
		
		String t = event.getChunk().getX() + " " + event.getChunk().getZ();
		if (checkedChunks.containsKey(t))
			return;
		checkedChunks.put(t, true);
		
		/*
		for (BlockState state : chunk.getTileEntities()) {
			
			if ((state == null) || (!(state instanceof Sign)))
				continue;
			try {
				load(com.sk89q.worldedit.bukkit.BukkitUtil.toWorldVector(state.getBlock()));
			} catch (InvalidMechanismException ignored) {
			} catch (Exception t) {
				Bukkit.getLogger().severe(GeneralUtil.getStackTrace(t));
			}
			
			
			
		}*/
		
		Block aBlock = chunk.getBlock(0, 0, 0);
		Block bBlock = chunk.getBlock(15, 256, 15);
			
		final int xStart = aBlock.getLocation().getBlockX();
		final int xEnd = bBlock.getLocation().getBlockX();
		final int zStart = aBlock.getLocation().getBlockZ();
		final int zEnd = bBlock.getLocation().getBlockZ();
			
	//	Logger.debug("onChunkLoad "+chunk.getX() + " " + chunk.getZ() +" | " + aBlock.getLocation().getBlockX() + " to " + bBlock.getLocation().getBlockX());
		
		// Run our DB call in a async thread.
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {

				try {
				//	Logger.debug("Checking for missing shops between X: " + xStart + " - " + xEnd + ", Z: " + zStart + " - " + zEnd);
					
					final List<Shop> shops = Plugin.database.getShopsInCoords(chunk.getWorld().getName(), xStart, xEnd, zStart, zEnd);
					
					
					// Run our check in the main thread. 
					Plugin.getInstance().getServer().getScheduler().runTask(Plugin.getInstance(), new Runnable() {
						public void run() {
							Sign sign;
							for (int i=0; i<shops.size(); i++){
								
								sign = shopExists(shops.get(i));
								
								if (sign == null){
									Logger.warning("Shop #" + shops.get(i).id + " no longer exists, removing from DB.");
									try {
										Plugin.database.deleteShop(shops.get(i).id);
									} catch (SQLException e) {
										Logger.warning("Exception caught while lookign up shops in chunk.");
										e.printStackTrace();
									}
									
								}else{
									
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
									Logger.debug("Setting shop #" + shops.get(i).id + "'s stock to " + inStock);
									try {
										shops.get(i).setInStock(inStock);
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									
								}
							}
							
						}});

				} catch (SQLException e) {
					Logger.warning("Exception caught while lookign up shops in chunk.");
					e.printStackTrace();
				}
				
				
				
			}});
		
		

	}

	public Sign shopExists(Shop shop){

		Block block = shop.getWorld().getBlockAt(shop.getLocation());
		
		if (block.getType() == block.getType().WALL_SIGN){
			
			return (Sign) block.getState();
		}
		
		return null;
	}
	
}
