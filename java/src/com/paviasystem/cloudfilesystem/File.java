package com.paviasystem.cloudfilesystem;

public interface File {

	void flush() throws Exception;

	int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset) throws Exception;

	void write(byte[] buffer, int bufferOffset, int bytesToWrite, long fileOffset) throws Exception;

	void setLength(long newLength) throws Exception;

}
