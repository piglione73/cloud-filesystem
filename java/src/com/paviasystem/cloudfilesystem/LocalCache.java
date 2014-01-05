package com.paviasystem.cloudfilesystem;

import java.util.Iterator;

public interface LocalCache {
	byte[] read(String category1, String category2, String name);

	void write(String category1, String category2, String name, byte[] bytes);

	void remove(String category1, String category2, String name);

	Iterator<CacheLogEntry> list(String category1, String category2);
}
