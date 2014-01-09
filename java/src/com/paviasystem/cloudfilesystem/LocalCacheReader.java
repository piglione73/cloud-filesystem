package com.paviasystem.cloudfilesystem;

public interface LocalCacheReader extends AutoCloseable {

	int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset);
}
