package com.paviasystem.cloudfilesystem;

public class CloudFileSystem {
	BlobStore blobStore;
	Index index;
	LocalCache localCache;
	Log log;
	LockManager lockManager;
	LazyMirroring lazyMirroring;
}
