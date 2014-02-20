package com.paviasystem.cloudfilesystem.blocks;

public interface ByteReader {
	int read(byte[] buffer, int offset, int maxBytesToRead);
}
