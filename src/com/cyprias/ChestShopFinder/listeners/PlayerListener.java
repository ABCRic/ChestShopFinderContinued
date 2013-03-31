package com.cyprias.ChestShopFinder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.cyprias.ChestShopFinder.Plugin;

public class PlayerListener implements Listener {

	static public void unregisterEvents(JavaPlugin instance) {
		PlayerCommandPreprocessEvent.getHandlerList().unregister(instance);

	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();
		String command = msg.split(" ")[0].replace("/", "");

		if (Plugin.aliases.containsKey(command.toLowerCase())) {
			event.setMessage(msg.replaceFirst("/" + command, "/" + Plugin.aliases.get(command.toLowerCase())));
			return;
		}
	}

}
