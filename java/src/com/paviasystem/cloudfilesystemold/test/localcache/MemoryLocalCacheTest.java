package com.paviasystem.cloudfilesystemold.test.localcache;

import com.paviasystem.cloudfilesystemold.referenceimpl.MemoryLocalCache;

public class MemoryLocalCacheTest extends GenericLocalCacheTest {
	public MemoryLocalCacheTest() {
		localCache = new MemoryLocalCache();
	}
}
