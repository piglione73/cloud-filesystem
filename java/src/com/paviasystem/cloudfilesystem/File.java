package com.paviasystem.cloudfilesystem;

public interface File {

	void flush();

	int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset);

	void write(byte[] buffer, int bufferOffset, int bytesToWrite, long fileOffset);

	void setLength(long newLength);

}
