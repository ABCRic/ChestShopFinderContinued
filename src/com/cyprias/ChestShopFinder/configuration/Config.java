package com.cyprias.ChestShopFinder.configuration;

import java.util.List;

import com.cyprias.ChestShopFinder.Plugin;


public class Config {
	private static final Plugin plugin = Plugin.getInstance();


	public static long getLong(String property) {
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

	public static List<String> getStringList(String property) {
		return plugin.getConfig().getStringList(property);
	}
	
	public static Boolean inStringList(String property, String find){
		List<String> list = getStringList(property);
		for (int i=0; i<list.size();i++){
			if (list.get(i).equalsIgnoreCase(find))
				return true;
		}
		return false;
	}
	
}
