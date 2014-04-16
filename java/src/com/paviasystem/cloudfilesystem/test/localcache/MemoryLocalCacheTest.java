package com.paviasystem.cloudfilesystem.test.localcache;

import com.paviasystem.cloudfilesystem.referenceimpl.MemoryLocalCache;

public class MemoryLocalCacheTest extends GenericLocalCacheTest {
	public MemoryLocalCacheTest() {
		localCache = new MemoryLocalCache();
	}
}
