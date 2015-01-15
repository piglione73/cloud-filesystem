package com.paviasystem.cloudfilesystem.components;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

	public static FileChannel createTempFileChannel() throws IOException {
		Path path = Files.createTempFile("CloudFileSystem", null);
		return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DELETE_ON_CLOSE);
	}

	public static void copyAll(SeekableByteChannel from, SeekableByteChannel to) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(64 * 1024);
		for (int n = from.read(buf); n != -1; n = from.read(buf)) {
			buf.flip();
			to.write(buf);
			buf.clear();
		}
	}

	public static ByteBuffer readFrom(SeekableByteChannel from, int bytesToRead) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(bytesToRead);
		while (bytesToRead > 0) {
			int n = from.read(buf);
			if (n == -1)
				throw new EOFException();

			bytesToRead -= n;
		}

		buf.flip();
		return buf;
	}

}
