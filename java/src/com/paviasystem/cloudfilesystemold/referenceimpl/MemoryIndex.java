package com.paviasystem.cloudfilesystemold.referenceimpl;

import java.util.ArrayList;

import com.paviasystem.cloudfilesystemold.blocks.Index;
import com.paviasystem.cloudfilesystemold.blocks.data.IndexEntry;

public class MemoryIndex implements Index {
	ArrayList<IndexEntry> entries = new ArrayList<>();

	@Override
	public synchronized Iterable<IndexEntry> list(String key1, String fromKey2, String toKey2) {
		ArrayList<IndexEntry> ret = new ArrayList<>();
		for (IndexEntry entry : entries) {
			if (key1 != null && !key1.equals(entry.key1))
				continue;
			if (fromKey2 != null && fromKey2.compareTo(entry.key2) > 0)
				continue;
			if (toKey2 != null && toKey2.compareTo(entry.key2) < 0)
				continue;

			ret.add(entry.clone());
		}

		return ret;
	}

	@Override
	public synchronized IndexEntry read(String key1, String key2) {
		for (IndexEntry entry : entries) {
			if (key1.equals(entry.key1) && key2.equals(entry.key2))
				return entry;
		}

		return null;
	}

	@Override
	public synchronized void write(IndexEntry entry) {
		IndexEntry clone = entry.clone();

		for (int i = 0; i < entries.size(); i++) {
			IndexEntry x = entries.get(i);
			if (x.key1.equals(clone.key1) && x.key2.equals(clone.key2)) {
				entries.set(i, clone);
				return;
			}
		}

		entries.add(clone);
	}

	@Override
	public synchronized void delete(String key1, String key2) {
		for (int i = 0; i < entries.size(); i++) {
			IndexEntry x = entries.get(i);
			if (x.key1.equals(key1) && x.key2.equals(key2)) {
				entries.remove(i);
				return;
			}
		}
	}

	@Override
	public synchronized boolean update(String key1, String key2, String newKey1, String newKey2) {
		for (int i = 0; i < entries.size(); i++) {
			IndexEntry x = entries.get(i);
			if (x.key1.equals(key1) && x.key2.equals(key2)) {
				x.key1 = newKey1;
				x.key2 = newKey2;
				return true;
			}
		}

		return false;
	}

	@Override
	public synchronized boolean update(String key1, String key2, String data1, String data2, String newData1, String newData2, String newData3, String newData4) {
		for (int i = 0; i < entries.size(); i++) {
			IndexEntry x = entries.get(i);
			if (x.key1.equals(key1) && x.key2.equals(key2) && x.data1.equals(data1) && x.data2.equals(data2)) {
				x.data1 = newData1;
				x.data2 = newData2;
				x.data3 = newData3;
				x.data4 = newData4;
				return true;
			}
		}

		return false;
	}
}
