package com.paviasystem.cloudfilesystem.referenceimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class MemorySeekableByteChannel implements SeekableByteChannel {

	boolean open;
	ByteBuffer buf;

	public MemorySeekableByteChannel() {
		open = true;
		buf = ByteBuffer.allocate(1);
		buf.position(0);
		buf.limit(0);
	}

	void ensureCapacity(int minCapacity) {
		if (minCapacity > buf.capacity()) {
			// Extend "buf"
			int p = buf.position();
			int l = buf.limit();
			ByteBuffer newBuf = ByteBuffer.allocate(minCapacity);
			newBuf.position(0).limit(l);

			buf.position(0);
			newBuf.put(buf).clear().position(p).limit(l);

			buf = newBuf;
		}
	}

	@Override
	public void close() throws IOException {
		open = false;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public long position() throws IOException {
		return buf.position();
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		buf.position((int) newPosition);
		return this;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		int r = dst.remaining();

		// Let's try to read "r" bytes from the channel into "dst"
		ByteBuffer bytesToTransfer = buf.slice();
		bytesToTransfer.limit(Math.min(r, bytesToTransfer.limit()));
		if (bytesToTransfer.limit() == 0) {
			// EOF
			return -1;
		} else {
			dst.put(buf);
			return bytesToTransfer.limit();
		}
	}

	@Override
	public long size() throws IOException {
		return buf.limit();
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		if (size < buf.limit())
			buf.limit((int) size);

		buf.position(Math.min(buf.position(), (int) size));

		return this;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int r = src.remaining();
		int newLimit = buf.position() + r;
		ensureCapacity(newLimit);
		buf.limit(newLimit);
		buf.put(src);
		return r;
	}

}
