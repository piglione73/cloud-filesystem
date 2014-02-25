package com.paviasystem.cloudfilesystem.blocks;

public interface ByteWriter extends AutoCloseable {
	void write(byte[] buffer, int bufferOffset, int numBytesToWrite);
}
