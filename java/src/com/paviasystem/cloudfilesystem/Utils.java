package com.paviasystem.cloudfilesystem;

public class Utils {
	public static String padLeft(long number) {
		return number >= 0 ? String.format("%019d", number) : String.format("%020d", number);
	}
}
