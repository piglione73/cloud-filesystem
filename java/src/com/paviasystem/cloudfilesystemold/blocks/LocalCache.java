package com.paviasystem.cloudfilesystemold.blocks;

import com.paviasystem.cloudfilesystemold.blocks.data.LocalCacheEntry;

public interface LocalCache {
	ByteWriter openSequentialWriter(String category, String name);

	ByteReader openSequentialReader(String category, String name);

	AbsoluteByteReader openAbsoluteReader(String category, String name);

	AbsoluteByteWriter openAbsoluteWriter(String category, String name);

	void delete(String category, String name);

	Iterable<LocalCacheEntry> list();
}
