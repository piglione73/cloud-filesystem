package com.paviasystem.cloudfilesystem;

import java.nio.channels.SeekableByteChannel;

public class Blob implements AutoCloseable {
	public long latestLogSequenceNumber;
	public SeekableByteChannel bytes;

	public Blob(long latestLogSequenceNumber, SeekableByteChannel bytes) {
		this.latestLogSequenceNumber = latestLogSequenceNumber;
		this.bytes = bytes;
	}

	@Override
	public void close() throws Exception {
		bytes.close();
	}
}
