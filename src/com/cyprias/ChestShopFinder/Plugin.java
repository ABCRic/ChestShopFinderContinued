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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.cyprias.ChestShopFinder.command.CommandManager;
import com.cyprias.ChestShopFinder.commands.BuyCommand;
import com.cyprias.ChestShopFinder.commands.ArbitrageCommand;
import com.cyprias.ChestShopFinder.commands.LookCommand;
import com.cyprias.ChestShopFinder.commands.ReloadCommand;
import com.cyprias.ChestShopFinder.commands.SearchCommand;
import com.cyprias.ChestShopFinder.commands.SellCommand;
import com.cyprias.ChestShopFinder.commands.TestCommand;
import com.cyprias.ChestShopFinder.commands.VersionCommand;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.configuration.YML;
import com.cyprias.ChestShopFinder.database.Database;
import com.cyprias.ChestShopFinder.database.MySQL;
import com.cyprias.ChestShopFinder.database.SQLite;
import com.cyprias.ChestShopFinder.listeners.ChestShopListener;
import com.cyprias.ChestShopFinder.listeners.EntityListener;
import com.cyprias.ChestShopFinder.listeners.InventoryListener;
import com.cyprias.ChestShopFinder.listeners.PlayerListener;
import com.cyprias.ChestShopFinder.listeners.WorldListener;

public class Plugin extends JavaPlugin {
	// static PluginDescriptionFile description;
	private static Plugin instance = null;

	//public void onLoad() {}

	public static Database database;
	public static HashMap<String, String> aliases = new HashMap<String, String>();
	public static Server server ;
	
	public void onEnable() {
		instance = this;
		server = this.getServer();
		
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
		
		//Check if the config on disk is missing any settings, tell console if so.
		try {
			Config.checkForMissingProperties();
		} catch (IOException e4) {
			e4.printStackTrace();
		} catch (InvalidConfigurationException e4) {
			e4.printStackTrace();
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
		cm.registerCommand("reload", new ReloadCommand());
		cm.registerCommand("version", new VersionCommand());
		cm.registerCommand("sell", new SellCommand());
		cm.registerCommand("buy", new BuyCommand());
		cm.registerCommand("arbitrage", new ArbitrageCommand());
		this.getCommand("csf").setExecutor(cm);

		// Load our command aliases.
		try {
			YML yml = new YML(getResource("aliases.yml"), getDataFolder(), "aliases.yml");
			for (String key : yml.getKeys(false)) {
				aliases.put(key, yml.getString(key));
			}
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (InvalidConfigurationException e2) {
			e2.printStackTrace();
		}
		
		// Load our permission nodes.
		loadPermissions();

		//Register our event listeners. 
		registerListeners(new EntityListener(), new ChestShopListener(), new InventoryListener(), new WorldListener(), new PlayerListener());

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
					VersionChecker version = new VersionChecker("http://dev.bukkit.org/server-mods/chestshopfinder/files.rss");
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
	
	public static String getPlayerName(String playerName){
		if (Config.getBoolean("properties.show-nicknames")){
			Player player = server.getPlayerExact(playerName);
			if (player != null)
				return player.getDisplayName();
		}
	
		
		return playerName;
	}

	static List<ChatColor> distanceColours = new ArrayList<ChatColor>();// = new List<ChatColor>;

	static {
		distanceColours.add(ChatColor.GREEN);
		distanceColours.add(ChatColor.YELLOW);
		distanceColours.add(ChatColor.GOLD);
		distanceColours.add(ChatColor.RED);
	}
	
	public static ChatColor getDistanceColour(double num){
		if (num > 1)
			num = num / 100;

		return distanceColours.get((int) Math.round((distanceColours.size()-1) * num));
	}
	
}
