package com.rammelkast.anticheatreloaded.util;

import java.util.Random;

public class NameUtil {

	private static String[] firstParts = new String[] {
		"Gamer",
		"Pixel",
		"Derp",
		"Random",
		"That",
		"NoHaxJust",
		"Derp",
		"Fun",
		"Luigi"
	};

	private static String[] secondParts = new String[] {
		"01",
		"Boy",
		"Guy",
		"Mario",
		"Plain",
		"Simple",
		"NotHacking",
		"Random",
		"11",
		"06"
	};
	
	private static Random theRandom = new Random();
	
	public static String getRandomName() {
		return firstParts[theRandom.nextInt(firstParts.length - 1)] + secondParts[theRandom.nextInt(secondParts.length - 1)];
	}
}
