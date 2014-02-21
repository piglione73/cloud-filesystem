package com.paviasystem.cloudfilesystem.blocks;

public interface ByteWriter extends AutoCloseable {
	void write(byte[] buffer, int offset, int numBytesToWrite);
}
