package com.rammelkast.anticheatreloaded.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NameGenerator {

	private static final List<String> BEGIN = Arrays.asList(new String[]{
		"Pixel",
		"Pro",
		"NoHax",
		"Just",
		"That",
		"New",
		"Ultra",
		"Killer",
		"Swift"
	});

	private static final List<String> END = Arrays.asList(new String[]{
		"Gamer",
		"PvP",
		"JustYolo",
		"Gaming",
		"Minecraft",
		"Games",
		"Bawz",
		"1",
		"51",
		"99",
		"" + new Random().nextInt(100)
	});
	
	public static String generateName() {
		Random r = new Random();
		String begin = BEGIN.get(r.nextInt(BEGIN.size() - 1));
		String end = END.get(r.nextInt(END.size() - 1));
		if (r.nextBoolean())
			return begin + end + r.nextInt(9);
		else
			return begin + end;
	}
	
}
