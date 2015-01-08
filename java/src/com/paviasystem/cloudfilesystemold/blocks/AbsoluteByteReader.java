package com.paviasystem.cloudfilesystemold.blocks;

public interface AbsoluteByteReader extends AutoCloseable {
	int read(byte[] buffer, int bufferOffset, int maxBytesToRead, long fileOffset);
}
