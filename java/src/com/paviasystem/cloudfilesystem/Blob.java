package com.paviasystem.cloudfilesystem;

import java.nio.channels.FileChannel;

public class Blob implements AutoCloseable {
	public long latestLogSequenceNumber;
	public FileChannel bytes;

	public Blob(long latestLogSequenceNumber, FileChannel bytes) {
		this.latestLogSequenceNumber = latestLogSequenceNumber;
		this.bytes = bytes;
	}

	@Override
	public void close() throws Exception {
		bytes.close();
	}
}
