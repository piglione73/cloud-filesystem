package com.paviasystem.cloudfilesystemold.test.blobstore;

import com.paviasystem.cloudfilesystemold.referenceimpl.MemoryBlobStore;

public class MemoryBlobStoreTest extends GenericBlobStoreTest {
	public MemoryBlobStoreTest() {
		blobStore = new MemoryBlobStore();
	}
}
