package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;

public class CloudFileSystem implements FileSystem {
	BlobStore blobStore;
	Index index;
	LocalCache localCache;
	Log log;
	LockManager lockManager;
	LazyMirroring lazyMirroring;

	@Override
	public ArrayList<FileSystemEntry> list(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		ArrayList<IndexEntry> entries = index.list(absolutePath);
		ArrayList<FileSystemEntry> ret = new ArrayList<FileSystemEntry>();
		for (IndexEntry entry : entries)
			ret.add(new FileSystemEntry(this, entry.isFile, entry.absolutePath));

		return ret;
	}

	@Override
	public FileSystemEntry getEntry(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		IndexEntry entry = index.getEntry(absolutePath);
		if (entry != null)
			return new FileSystemEntry(this, entry.isFile, entry.absolutePath);

		return null;
	}
}
