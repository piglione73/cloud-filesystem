package com.paviasystem.cloudfilesystem.test.blobstore;

import com.paviasystem.cloudfilesystem.referenceimpl.MemoryBlobStore;

public class MemoryBlobStoreTest extends GenericBlobStoreTest {
	public MemoryBlobStoreTest() {
		blobStore = new MemoryBlobStore();
	}
}
