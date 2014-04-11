package com.paviasystem.cloudfilesystem.blocks;

import com.paviasystem.cloudfilesystem.blocks.data.IndexEntry;

public interface Index {
	IndexEntry read(String key1, String key2);

	void write(IndexEntry entry);

	void delete(String key1, String key2);

	void updateKey(String oldKey1, String oldKey2, String newKey1, String newKey2);

	Iterable<IndexEntry> list(String key1, String key2Prefix);
}
