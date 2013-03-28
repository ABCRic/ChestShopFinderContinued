package com.cyprias.ChestShopFinder.configuration;

import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;

import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;


public class Config {
	private static final Plugin plugin = Plugin.getInstance();
	
	public static long getLong(String property){
		return plugin.getConfig().getLong(property);
	}

	public static int getInt(String property) {
			return plugin.getConfig().getInt(property);
	}

	public static double getDouble(String property) {
		return plugin.getConfig().getDouble(property);
	}

	public static boolean getBoolean(String property) {
			return plugin.getConfig().getBoolean(property);
	}

	public static String getString(String property) {
			return plugin.getConfig().getString(property);
	}

	public static String getColouredString(String property) {
		return getString(property).replaceAll("(?i)&([a-k0-9])", "\u00A7$1");
	}
	
	public static void checkForMissingProperties() throws IOException, InvalidConfigurationException{
		YML diskConfig = new YML(plugin.getDataFolder(), "config.yml");
		YML defaultConfig = new YML(plugin.getResource("config.yml"));

		for (String property : defaultConfig.getKeys(true)){
			if (!diskConfig.contains(property))
				Logger.warning(property + " is missing from your config.yml, using default.");
		}
		
	}

}
