package com.cyprias.ChestShopFinder.utils;

import org.bukkit.ChatColor;

/**
 * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE Version 2, December 2004
 * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 * 
 * Everyone is permitted to copy and distribute verbatim or modified copies of
 * this license document, and changing it is allowed as long as the name is
 * changed.
 * 
 * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE TERMS AND CONDITIONS FOR COPYING,
 * DISTRIBUTION AND MODIFICATION
 * 
 * 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
public class MinecraftFontWidthCalculator {
	
	private static String charWidthIndexIndex = " !\"#$%&'()*+,-./0123456789:;<=>?@" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "[\\]^_'abcdefghijklmnopqrstuvwxyz"
		+ "{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";

	private static int[] charWidths = { 4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6,
		6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6,
		6, 5, 2, 5, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6,
		6, 2, 6, 6, 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5,
		9, 9, 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7, 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1 };

	public static int getStringWidth(String s) {
		int i = 0;
		if (s != null)
			for (int j = 0; j < s.length(); j++)
				i += getCharWidth(s.charAt(j));
		return i;
	}

	public static int getCharWidth(char c) {
		int k = charWidthIndexIndex.indexOf(c);
		if (c != '\247' && k >= 0)
			return charWidths[k];
		return 0;
	}

	public static StringBuilder whitespace(int length) {
		int spaceWidth = getStringWidth(" ");

		StringBuilder ret = new StringBuilder();

		for (int i = 0; (i + spaceWidth) < length; i += spaceWidth) {
			ret.append(" ");
		}

		return ret;
	}

	public static StringBuilder whitespace(String s) {
		return whitespace(getStringWidth(s));

	}

	public static int getMaxStringWidth() {
		return getStringWidth("---------------------------------------------------");
	}

	public static String textWithWhitespace(String s, int maxWidth) {
		return s + whitespace(maxWidth - getStringWidth(s));
	}

	public static String textWithWhitespace(int s, int maxWidth) {
		return textWithWhitespace(String.valueOf(s), maxWidth);
	}

	public static String textWithWhitespace(double s, int maxWidth) {
		return textWithWhitespace(String.valueOf(s), maxWidth);
	}

	public static String textWithWhitespace(String s, String oString) {
		return textWithWhitespace(s, getStringWidth(oString));
	}

	public static String[] getWhitespacedStrings(String[] strings) {
		int maxWidth = 0;

		for (String s : strings) {
			maxWidth = Math.max(maxWidth, getStringWidth(ChatColor.stripColor(s)));
		}

		
		
		for (int i = 0; i < strings.length; i++) {
			strings[i] = textWithWhitespace(strings[i], maxWidth);
		}

		return strings;

	}

}