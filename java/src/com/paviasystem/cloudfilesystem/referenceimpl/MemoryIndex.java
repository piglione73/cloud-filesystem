package com.paviasystem.cloudfilesystem.referenceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.paviasystem.cloudfilesystem.blocks.Index;
import com.paviasystem.cloudfilesystem.blocks.data.IndexEntry;
import com.paviasystem.cloudfsobsolete.Path;

public class MemoryIndex implements Index {

	HashMap<String, IndexEntry> entries = new HashMap<String, IndexEntry>();

	@Override
	public ArrayList<IndexEntry> listEntries(String absolutePath) {
		// List children of absolutePath
		ArrayList<IndexEntry> ret = new ArrayList<IndexEntry>();
		for (IndexEntry entry : entries.values()) {
			if (Path.getParent(entry.absolutePath).equals(absolutePath))
				ret.add(entry);
		}

		return ret;
	}

	@Override
	public IndexEntry getEntry(String absolutePath) {
		return entries.get(absolutePath);
	}

	@Override
	public void createDirectoryEntry(String absolutePath) {
		entries.put(absolutePath, new IndexEntry(absolutePath, false, new Date(), null));
	}

	@Override
	public IndexEntry createFileEntry(String absolutePath, String blobName) {
		IndexEntry entry = new IndexEntry(absolutePath, true, new Date(), blobName);
		entries.put(absolutePath, entry);
		return entry;
	}

	@Override
	public void deleteEntry(String absolutePath) {
		entries.remove(absolutePath);
	}

}
