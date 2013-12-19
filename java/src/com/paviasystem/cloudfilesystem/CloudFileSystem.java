package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;

public class CloudFileSystem implements FileSystem {
	final BlobStore blobStore;
	final Index index;
	final LocalCache localCache;
	final Log log;
	final LockManager lockManager;
	final LazyMirroring lazyMirroring;

	public CloudFileSystem(Index index) {
		this.blobStore = null;
		this.index = index;
		this.localCache = null;
		this.log = null;
		this.lockManager = null;
		this.lazyMirroring = null;
	}

	@Override
	public ArrayList<FileSystemEntry> list(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		ArrayList<IndexEntry> entries = index.listEntries(absolutePath);
		ArrayList<FileSystemEntry> ret = new ArrayList<FileSystemEntry>();
		for (IndexEntry entry : entries)
			ret.add(new FileSystemEntry(this, entry.isFile, entry.absolutePath,
					entry.timestamp));

		return ret;
	}

	@Override
	public FileSystemEntry getEntry(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		IndexEntry entry = index.getEntry(absolutePath);
		if (entry != null)
			return new FileSystemEntry(this, entry.isFile, entry.absolutePath,
					entry.timestamp);

		return null;
	}

	@Override
	public void createDirectory(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.createDirectoryEntry(absolutePath);
	}

	@Override
	public void deleteDirectory(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.deleteEntry(absolutePath);
	}
}
