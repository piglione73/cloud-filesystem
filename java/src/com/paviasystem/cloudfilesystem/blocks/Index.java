package com.paviasystem.cloudfilesystem.blocks;

import com.paviasystem.cloudfilesystem.blocks.data.IndexEntry;

public interface Index {
	Iterable<IndexEntry> list(String key1, String fromKey2, String toKey2);

	IndexEntry read(String key1, String key2);

	void write(IndexEntry entry);

	void delete(String key1, String key2);

	boolean update(String key1, String key2, String newKey1, String newKey2);

	boolean update(String key1, String key2, String data1, String data2, String newData1, String newData2, String newData3, String newData4);
}
