package com.paviasystem.cloudfilesystem;

public class Utils {
	public static String padLeft(long number) {
		return number >= 0 ? String.format("%019d", number) : String.format("%020d", number);
	}
	
	public static String formatTimestamp(Date timestamp) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String nowAsString = df.format(new Date());
	}
}
