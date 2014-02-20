package com.paviasystem.cloudfilesystem.blocks;

import java.util.Date;

import com.paviasystem.cloudfilesystem.blocks.data.CacheLogEntry;

public interface LocalCache {
	LocalCacheReader read(String category1, String category2, String name);

	LocalCacheWriter write(String category1, String category2, String name);

	Date getTimestamp(String category1, String category2, String name);

	void remove(String category1, String category2, String name);

	Iterable<CacheLogEntry> list(String category1, String category2);
}
