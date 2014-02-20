package com.paviasystem.cloudfilesystem;

public interface ByteReader {
	int read(byte[] buffer, int offset, int maxBytesToRead);
}
