package com.paviasystem.cloudfilesystem.blocks;

public interface LocalCacheWriter extends AutoCloseable {

	int write(byte[] buffer, int bufferOffset, int bytesToWrite, long fileOffset);
}