package com.paviasystem.cloudfilesystem.blocks;

public interface AbsoluteByteWriter extends AutoCloseable {
	void write(byte[] buffer, int bufferOffset, int numBytesToWrite, long fileOffset);
}
