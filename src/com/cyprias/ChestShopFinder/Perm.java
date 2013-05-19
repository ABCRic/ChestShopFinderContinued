package com.cyprias.ChestShopFinder;

import java.util.HashMap;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.cyprias.ChestShopFinder.configuration.Config;


public enum Perm {

	SEARCH("csf.search"), 
	LOOK("csf.look"), 
	BUY("csf.buy"), 
	SELL("csf.sell"),
	PRICE("csf.price"),
	ARBITRAGE("csf.arbitrage"),
	PARENT_USER("csf.user", SEARCH, LOOK, BUY, SELL, PRICE, ARBITRAGE),
	
	VERSION("csf.version"), 
	RELOAD("csf.reload"),
	PARENT_ADMIN("csf.admin", VERSION, RELOAD),

	TRANSACTIONS_MINE("csf.transactions.mine"),
	
	TRANSACTIONS("csf.transactions", TRANSACTIONS_MINE),
	PARENT_TEST("csf.test", TRANSACTIONS);
	
	private Perm(String value, Perm... childrenArray) {
		this(value, String.format(DEFAULT_ERROR_MESSAGE, value), childrenArray);
	}

	private Perm(String perm, String errorMess) {
		this.permission = perm;
		this.errorMessage = errorMess;
		this.bukkitPerm = new Permission(permission, PermissionDefault.getByName(Config.getString("properties.permission-default")));
	}

	private Perm(String value, String errorMess, Perm... childrenArray) {
		this(value, String.format(DEFAULT_ERROR_MESSAGE, value));
		for (Perm child : childrenArray) {
			child.setParent(this);
		}
	}

	public static HashMap<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();

	private final String permission;
	public static final String DEFAULT_ERROR_MESSAGE = "You do not have access to %s";

	public Perm getParent() {
		return parent;
	}

	private final Permission bukkitPerm;
	private Perm parent;
	private final String errorMessage;

	private void setParent(Perm parentValue) {
		if (this.parent != null)
			return;
		this.parent = parentValue;
	}

	public String getPermission() {
		return permission;
	}

	public void loadPermission(PluginManager pm) {
		pm.addPermission(bukkitPerm);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void unloadPermission(PluginManager pm) {
		pm.removePermission(bukkitPerm);
	}
}
