package com.paviasystem.cloudfilesystem.blocks;

public interface LocalCache {
	ByteWriter openSequentialWriter(String category, String name);

	ByteReader openSequentialReader(String category, String name);

	AbsoluteByteReader openAbsoluteReader(String category, String name);

	AbsoluteByteWriter openAbsoluteWriter(String category, String name);

	void delete(String category, String name);

	String getLatestLogBlobName(String category, String name);

	void setLatestLogBlobName(String category, String name, String latestLogBlobName);
}
