package com.paviasystem.cloudfilesystem.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

import com.paviasystem.cloudfilesystem.referenceimpl.MemorySeekableByteChannel;

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

	public static Blob from(long latestLogSequenceNumber, byte[] bytes) throws IOException {
		MemorySeekableByteChannel ch = new MemorySeekableByteChannel();
		ch.write(ByteBuffer.wrap(bytes));

		return new Blob(latestLogSequenceNumber, ch);
	}

	public static Blob from(long latestLogSequenceNumber, String text) throws IOException {
		return Blob.from(latestLogSequenceNumber, text.getBytes(StandardCharsets.UTF_8));
	}

	public byte[] toByteArray() throws IOException {
		bytes.position(0);
		ByteBuffer buf = Utils.readFrom(bytes, (int) bytes.size());
		return buf.array();
	}

	@Override
	public String toString() {
		try {
			return new String(toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		}
	}
}
