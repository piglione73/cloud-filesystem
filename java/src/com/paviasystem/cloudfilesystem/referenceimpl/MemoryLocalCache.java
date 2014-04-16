package com.paviasystem.cloudfilesystem.referenceimpl;

import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteReader;
import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteWriter;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;
import com.paviasystem.cloudfilesystem.blocks.LocalCache;
import com.paviasystem.cloudfilesystem.blocks.data.LocalCacheEntry;

public class MemoryLocalCache implements LocalCache {

	@Override
	public ByteWriter openSequentialWriter(String category, String name) {
	}

	@Override
	public ByteReader openSequentialReader(String category, String name) {
	}

	@Override
	public AbsoluteByteReader openAbsoluteReader(String category, String name) {
	}

	@Override
	public AbsoluteByteWriter openAbsoluteWriter(String category, String name) {
	}

	@Override
	public void delete(String category, String name) {
	}

	@Override
	public Iterable<LocalCacheEntry> list() {
	}

}
