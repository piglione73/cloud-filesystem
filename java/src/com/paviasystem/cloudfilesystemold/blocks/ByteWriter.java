package com.paviasystem.cloudfilesystemold.blocks;

public interface ByteWriter extends AutoCloseable {
	void write(byte[] buffer, int bufferOffset, int numBytesToWrite);
}
