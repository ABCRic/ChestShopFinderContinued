package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;

public class WorldListener implements Listener {

	// Our task to run asyncly to get shops from DB.
	Runnable getShopsFromDB = new Runnable() {
		public void run() {
			
			if (chunks.size() <= 0){
				taskCheckingShops.cancel();
				taskCheckingShops = null;
				return;
			}
			chunkCoords c = chunks.remove(0);
			
			//Logger.debug("getShopsFromDB " + c.worldName + ", " + c.xStart + ", " + c.xEnd + ", " + c.zStart + ", " + c.zEnd + ", total: " + chunks.size());

			List<Shop> shops;
			try {
				shops = Plugin.database.getShopsInCoords(c.worldName, c.xStart, c.xEnd, c.zStart, c.zEnd);
			} catch (SQLException e) {
				Logger.warning("Database error while looking up shops within a chunk.");
				e.printStackTrace();
				return;
			}
			
			if (shops.size() == 0)
				return;
			
			// Pass our shops table back into the main thread so we can interact with blocks. 
			checkChunkForShops runable = new checkChunkForShops();
			runable.shops = shops;
			runable.chunk = c.chunk;
			Plugin.runTask(runable);//Runs in main thread.
			
			
		}
	};

	class checkChunkForShops implements Runnable {
		protected Chunk chunk;
		protected List<Shop> shops;

		public void run() {
			Sign sign;
			Logger.debug("checkChunkForShops: " + shops.size());
			
			if (chunk.isLoaded() == false){
				// Chunk's no longer loaded, remove chunk from our checked chunks list so if it gets loaded again we'll check again.
				checkedChunks.remove(chunk);
				return;
			}
			
			
			for (int i=0; i<shops.size(); i++){
				final Shop shop = shops.get(i);
				final int id = shops.get(i).id;
				
				sign = shopExists(shops.get(i));
				
				if (sign == null){
					// Sign no longer exists.
					Logger.warning("Shop #" + shops.get(i).id + " no longer exists, removing from DB.");

					//Spawn a async thread to delete it from DB.
					Plugin.runTaskAsynchronously(new Runnable() {
						public void run() {
							try {
								Plugin.database.deleteShop(id);
							} catch (SQLException e) {
								Logger.warning("Database error while attempting to delete shop.");
								e.printStackTrace();
							}
						}});

				}else{
					// Sign still exists in world, lets check its stock and refresh our DB.

					int inStock = 0;

					String owner = sign.getLines()[0];
					ItemStack stock = MaterialUtil.getItem(sign.getLines()[3]);

					if (ChestShopSign.isAdminShop(owner)) {
						inStock = 64 * 9 * 6; // Full chest. :P
					} else {
						Chest chest = uBlock.findConnectedChest(sign.getBlock());
						if (chest != null) {
							try{
								inStock = InventoryUtil.getAmount(stock, chest.getInventory());
							} catch (NullPointerException e) {
								inStock = 0;
							}
						}
					}

					//Spawn a async thread to input the info intot he DB.
					final int finStock = inStock;
					Plugin.runTaskAsynchronously(new Runnable() {
						public void run() {
							try {
								shop.setInStock(finStock);
							} catch (SQLException e) {
								Logger.warning("Database error while setting stock amount.");
								e.printStackTrace();
							}
						}});

				}
			}
		}
		
		
	}
	
	BukkitTask taskCheckingShops;
	
	static public void unregisterEvents(JavaPlugin instance) {
		ChunkLoadEvent.getHandlerList().unregister(instance);
	}

	ArrayList<chunkCoords> chunks = new ArrayList<chunkCoords>();
	
	class chunkCoords {
		private String worldName;
		public int xStart;
		public int xEnd;
		public int zStart;
		public int zEnd;
		public Chunk chunk;
		public chunkCoords(String worldName, int xStart, int xEnd, int zStart, int zEnd) {
			this.worldName = worldName;
			this.xStart = xStart;
			this.xEnd = xEnd;
			this.zStart = zStart;
			this.zEnd = zEnd;
		}
		public chunkCoords(Chunk chunk) {
			
			this.chunk = chunk;
			Block aBlock = chunk.getBlock(0, 0, 0);
			Block bBlock = chunk.getBlock(15, 256, 15);
				
			
			this.worldName = chunk.getWorld().getName();
			this.xStart = aBlock.getLocation().getBlockX();
			this.xEnd = bBlock.getLocation().getBlockX();
			this.zStart = aBlock.getLocation().getBlockZ();
			this.zEnd = bBlock.getLocation().getBlockZ();
		}
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


		chunks.add(new chunkCoords(chunk));
		if (taskCheckingShops == null)
			taskCheckingShops = Plugin.runTaskTimerAsynchronously(getShopsFromDB, 0, 1L);
		
			}

	public Sign shopExists(Shop shop){

		Block block = shop.getWorld().getBlockAt(shop.getLocation());
		
		if (block.getType() == Material.WALL_SIGN)
			return (Sign) block.getState();
		
		
		return null;
	}
	
}
