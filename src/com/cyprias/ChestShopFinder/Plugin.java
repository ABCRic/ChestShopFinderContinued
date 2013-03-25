package com.cyprias.ChestShopFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.cyprias.ChestShopFinder.command.CommandManager;
import com.cyprias.ChestShopFinder.command.LookCommand;
import com.cyprias.ChestShopFinder.command.SearchCommand;
import com.cyprias.ChestShopFinder.command.TestCommand;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.configuration.YML;
import com.cyprias.ChestShopFinder.database.Database;
import com.cyprias.ChestShopFinder.database.MySQL;
import com.cyprias.ChestShopFinder.database.SQLite;
import com.cyprias.ChestShopFinder.listeners.ChestShopListener;
import com.cyprias.ChestShopFinder.listeners.EntityListener;
import com.cyprias.ChestShopFinder.listeners.InventoryListener;

public class Plugin extends JavaPlugin {
	// static PluginDescriptionFile description;
	private static Plugin instance = null;

	//public void onLoad() {}

	public static Database database;

	public void onEnable() {
		instance = this;

		// Check if config.yml exists on disk, copy it over if not. This keeps our comments intact.
		if (!(new File(getDataFolder(), "config.yml").exists())) {
			Logger.info("Copying config.yml to disk.");
			try {
				YML.toFile(getResource("config.yml"), getDataFolder(), "config.yml");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		// Check which DB we should be using in config.
		if (Config.getString("properties.db-type").equalsIgnoreCase("mysql")) {
			database = new MySQL();
		} else if (Config.getString("properties.db-type").equalsIgnoreCase("sqlite")) {
			database = new SQLite();
		} else {
			Logger.severe("No database selected (" + Config.getString("properties.db-type") + "), unloading plugin...");
			instance.getPluginLoader().disablePlugin(instance);
			return;
		}

		// Check if we can connect to DB, shutdown if we can't.
		if (!database.init()) {
			Logger.severe("Failed to initilize database, unloading plugin...");
			instance.getPluginLoader().disablePlugin(instance);
			return;
		}
		
		// Regster our commands.
		CommandManager cm = new CommandManager();
		cm.registerCommand("test", new TestCommand());
		cm.registerCommand("search", new SearchCommand());
		cm.registerCommand("look", new LookCommand());
		this.getCommand("csf").setExecutor(cm);

		// Load our permission nodes.
		loadPermissions();

		//Register our event listeners. 
		registerListeners(new EntityListener(), new ChestShopListener(), new InventoryListener());
		
		
		// Load our itemnames file into memory, run in async task to not slow down startup. 
		getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			public void run() {
				try {
					Logger.debug("Loading item names from items.csv file...");
					loadItemIds();
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
			
		// Start plugin metrics, see how popular our plugin is.
		if (Config.getBoolean("properties.use-metrics")){
			try {
				new Metrics(this).start();
			} catch (IOException e) {}
		}
		
		// Check if there's a new version available, notify console if so. 
		if (Config.getBoolean("properties.check-new-version"))
			checkVersion();
		
		Logger.info("enabled.");
	}


	private void loadPermissions() {
		PluginManager pm = Bukkit.getPluginManager();
		for (Perm permission : Perm.values()) {
			permission.loadPermission(pm);
		}
	}

	private void checkVersion() {
		// Run our version checker in async thread, so not to lockup if timeout.
		getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			public void run() {
				try {
					VersionChecker version = new VersionChecker("http://dev.bukkit.org/server-mods/dynamicdroprate/files.rss");
					VersionChecker.versionInfo info = (version.versions.size() > 0) ? version.versions.get(0) : null;
					if (info != null) {
						String curVersion = getDescription().getVersion();
						if (VersionChecker.compareVersions(curVersion, info.getTitle()) < 0) {
							Logger.warning("We're running v" + curVersion + ", v" + info.getTitle() + " is available");
							Logger.warning(info.getLink());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

			}
		});
	}

	
	Listener[] listenerList;
	private void registerListeners(Listener... listeners) {
		PluginManager manager = getServer().getPluginManager();

		listenerList = listeners;

		for (Listener listener : listeners) {
			manager.registerEvents(listener, this);
		}
	}

	public void onDisable() {

		PluginManager pm = Bukkit.getPluginManager();
		for (Perm permission : Perm.values()) {
			// permission.loadPermission(pm);
			permission.unloadPermission(pm);
		}

		CommandManager.unregisterCommands();
		this.getCommand("csf").setExecutor(null);
		
		
		instance.getServer().getScheduler().cancelAllTasks();
		
		EntityListener.unregisterEvents(instance);

		instance = null;
		Logger.info("disabled.");
	}

	public static void reload() {
		instance.reloadConfig();
	}

	static public boolean hasPermission(CommandSender sender, Perm permission) {
		if (sender != null) {
			if (sender instanceof ConsoleCommandSender)
				return true;

			if (sender.hasPermission(permission.getPermission())) {
				return true;
			} else {
				Perm parent = permission.getParent();
				return (parent != null) ? hasPermission(sender, parent) : false;
			}
		}
		return false;
	}

	public static boolean checkPermission(CommandSender sender, Perm permission) {
		if (!hasPermission(sender, permission)) {
			String mess = permission.getErrorMessage();
			if (mess == null)
				mess = Perm.DEFAULT_ERROR_MESSAGE;
			ChatUtils.error(sender, mess);
			return false;
		}
		return true;
	}

	public static final Plugin getInstance() {
		return instance;
	}

	public static double getUnixTime() {
		return (System.currentTimeMillis() / 1000D);
	}

	public static String getFinalArg(final String[] args, final int start) {
		final StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	public static boolean isInt(final String sInt) {
		try {
			Integer.parseInt(sInt);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isDouble(final String sDouble) {
		try {
			Double.parseDouble(sDouble);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static String Round(double val, int pl) {
		String format = "#";
		
		if (1 <= pl)
			format += ".";
		
		for (int i = 1; i <= pl; i++)
			format += "#";

		DecimalFormat df = new DecimalFormat(format);
		return df.format(val);
	}

	public static String Round(double val) {
		return Round(val, 0);
	}

	public static ItemStack getItemStack(int itemID, short itemDur, int amount, String enchants) {
		ItemStack itemStack = new ItemStack(itemID, amount);
		itemStack.setDurability(itemDur);

		if (enchants != null && !enchants.equalsIgnoreCase(""))
			itemStack.addEnchantments(MaterialUtil.Enchantment.getEnchantments(enchants));

		return itemStack;
	}

	public static ItemStack getItemStack(int itemID, short itemDur, String enchants) {
		return getItemStack(itemID, itemDur, 1, enchants);
	}

	public static ItemStack getItemStack(int itemID, short itemDur) {
		return getItemStack(itemID, itemDur, 1, null);
	}

	public static ItemStack getItemStack(ItemStack stock, String enchants) {
		if (enchants != null && !enchants.equalsIgnoreCase(""))
			stock.addEnchantments(MaterialUtil.Enchantment.getEnchantments(enchants));
		return stock;
	}
	
	public static ItemStack getItemStack(String id) {
		int itemid = 0;
		String itemname = null;
		short metaData = 0;

		String[] split = id.trim().split("-");
		String enchant = null;
		if (split.length > 1) {
			id = split[0];
			enchant = split[1];
		}
		if (id.matches("^\\d+[:+',;.]\\d+$")) {
			itemid = Integer.parseInt(id.split("[:+',;.]")[0]);
			metaData = Short.parseShort(id.split("[:+',;.]")[1]);
		} else if (id.matches("^\\d+$")) {
			itemid = Integer.parseInt(id);
		} else if (id.matches("^[^:+',;.]+[:+',;.]\\d+$")) {
			itemname = id.split("[:+',;.]")[0].toLowerCase(Locale.ENGLISH);
			metaData = Short.parseShort(id.split("[:+',;.]")[1]);
		} else {
			itemname = id.toLowerCase(Locale.ENGLISH);
		}

		if (itemid > 0) {
			return getItemStack(itemid, metaData, enchant);
		}
		if (itemname != null) {
		//	Logger.info("getItemStack", itemname, enchant);
			if (nameToStack.containsKey(itemname)) 
				return getItemStack(nameToStack.get(itemname), enchant);
			
			ItemStack mat = MaterialUtil.getItem(id);
			if (mat != null){
				if (enchant != null)
					mat.addEnchantments(MaterialUtil.Enchantment.getEnchantments(enchant));
				
				return mat;
			}
		}

		return null;
	}
	
	static HashMap<String, ItemStack> nameToStack = new HashMap<String, ItemStack>();
	static HashMap<String, String> stockToName = new HashMap<String, String>();
	
	private void loadItemIds() throws NumberFormatException, IOException {

		File file = new File(instance.getDataFolder(), "items.csv");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			copy(getResource("items.csv"), file);
		}
		@SuppressWarnings("resource")
		BufferedReader r = new BufferedReader(new FileReader(file));

		String line;

		int l = 0;
		ItemStack stock;
		String id_dur;
		while ((line = r.readLine()) != null) {
			l = l + 1;
			if (l > 3) {
				String[] values = line.split(",");
				stock = getItemStack(Integer.parseInt(values[1]), Short.parseShort(values[2]));
				nameToStack.put(values[0], stock);

				id_dur = String.valueOf(stock.getTypeId());
				if (stock.getDurability() > 0)
					id_dur += ":" + stock.getDurability();

				if (!stockToName.containsKey(id_dur))
					stockToName.put(id_dur, values[0]);
				/*
				 * sID = values[1];// + ":" + values[2]; if
				 * (!values[2].equalsIgnoreCase("0")) sID+=values[2];
				 * 
				 * if (!idToName.containsKey(sID)) idToName.put(sID, values[0]);
				 */
			}
		}

	}
	public void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		in.close();
	}
	
	
}
