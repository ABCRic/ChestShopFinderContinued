package com.cyprias.ChestShopFinder.database;

import java.sql.SQLException;

import com.Acrobot.ChestShop.Events.TransactionEvent.TransactionType;
import com.cyprias.ChestShopFinder.Logger;
import com.cyprias.ChestShopFinder.Plugin;
import com.cyprias.ChestShopFinder.configuration.Config;
import com.cyprias.ChestShopFinder.utils.MathUtil;

public class Transaction {
	private String owner, client, enchantments;

	private int flags, typeId, amount;
	private short durability;

	private double price, time;

	public static int mask_BUY = (int) Math.pow(2, 0);
	public static int mask_SELL = (int) Math.pow(2, 1);

	// public static int mask_ADMINSHOP=(int) Math.pow(2, 2);


	
	public Transaction(String owner, String client, int flags, double price, int typeId, short durability, String enchantments, int amount, double time) {
		this.owner = owner;
		this.client = client;

		if (enchantments == null)
			enchantments = "";

		this.enchantments = enchantments;

		this.flags = flags;

		this.price = price;
		this.typeId = typeId;
		this.durability = durability;

		this.amount = amount;
		this.time = time;
		
	}
	
	public TransactionType getType() {
		if (MathUtil.hasMask(this.flags, mask_BUY))
			return TransactionType.BUY;

		if (MathUtil.hasMask(this.flags, mask_SELL))
			return TransactionType.SELL;

		return null;
	}

	public String getOwner() {
		return owner;
	}

	public String getClient() {
		return client;
	}

	public String getEnchantments() {
		return enchantments;
	}

	public double getPrice() {
		return price;
	}
	public double getTime() {
		return time;
	}
	
	public int getTypeId() {
		return typeId;
	}

	public short getDurability() {
		return durability;
	}

	public int getAmount() {
		return amount;
	}

	public int getFlags() {
		return flags;
	}

	
	
	public void sendToDB() {
		final Transaction t = this;
	//	Logger.debug("Sending transaction to db...");
		Plugin.getInstance().getServer().getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
			public void run() {
				try {
			//		Logger.debug("Sending transaction...");
					Plugin.database.insertTransaction(t);
				} catch (SQLException e) {
					Logger.warning("Exception caught inserting transaction into DB.");
					e.printStackTrace();
				}
				
			}
		});
	}
}
