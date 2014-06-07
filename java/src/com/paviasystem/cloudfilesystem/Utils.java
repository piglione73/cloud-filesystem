package com.paviasystem.cloudfilesystem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

	static DateFormat timestampFormat;

	static {
		timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		TimeZone UTC = TimeZone.getTimeZone("UTC");
		timestampFormat.setTimeZone(UTC);
	}

	public static String padLeft(long number) {
		return number >= 0 ? String.format("%019d", number) : String.format("%020d", number);
	}

	public static String formatTimestamp(Date timestamp) {
		return timestampFormat.format(timestamp);
	}

	public static Date parseTimestamp(String timestamp) throws ParseException {
		return timestampFormat.parse(timestamp);
	}

	public static <T> ArrayList<T> toList(Iterable<T> list) {
		ArrayList<T> ret = new ArrayList<>();
		for (T x : list)
			ret.add(x);

		return ret;
	}

}
