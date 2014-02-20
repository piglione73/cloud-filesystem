package com.paviasystem.cloudfilesystem.blocks;

public interface LocalCacheReader extends AutoCloseable {

	int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset);
}
