package com.cyprias.ChestShopFinder.listeners;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.database.Shop;

public class WorldListener implements Listener {

	static public void unregisterEvents(JavaPlugin instance) {
		ChunkLoadEvent.getHandlerList().unregister(instance);
	}

	public static HashMap<Chunk, Boolean> checkedChunks = new HashMap<Chunk, Boolean>();
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkLoad(ChunkLoadEvent event) throws SQLException {

		if (!Config.getBoolean("properties.verify-chunk-shops"))
			return;

		final Chunk chunk = event.getChunk();
		
		if (checkedChunks.containsKey(chunk))
			return;
		
		checkedChunks.put(chunk, true);
		
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
		
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				
				
				
				try {
					List<Shop> shops = Plugin.database.getShopsInCoords(chunk.getWorld().getName(), xStart, xEnd, zStart, zEnd);
					
					for (int i=0; i<shops.size(); i++){
						
						if (!shopExists(shops.get(i))){
							Logger.debug("Shop #" + shops.get(i).id + " no longer exists, removing from DB.");
							shops.get(i).delete();
						}
					}
					
					
					
				} catch (SQLException e) {
					Logger.warning("Exception caught while lookign up shops in chunk.");
					e.printStackTrace();
				}
				
				
				
			}});
		
		

	}

	public boolean shopExists(Shop shop){

		Block block = shop.getWorld().getBlockAt(shop.getLocation());
		
		if (block.getType() == block.getType().WALL_SIGN){
			
			return true;
		}
		
		return false;
	}
	
}
