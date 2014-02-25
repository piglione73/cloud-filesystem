package com.paviasystem.cloudfilesystem.blocks;

public interface ByteReader extends AutoCloseable {
	int read(byte[] buffer, int bufferOffset, int maxBytesToRead);
}
