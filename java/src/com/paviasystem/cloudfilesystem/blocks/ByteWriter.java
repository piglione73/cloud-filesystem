package com.paviasystem.cloudfilesystem.blocks;

public interface ByteWriter {
	void write(byte[] buffer, int offset, int numBytesToWrite);
}
