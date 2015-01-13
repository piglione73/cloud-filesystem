package com.paviasystem.cloudfilesystem.referenceimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class MemorySeekableByteChannel implements SeekableByteChannel {

	boolean open;
	ByteBuffer buf;
	long pos;

	public MemorySeekableByteChannel() {
		open = true;
		buf = ByteBuffer.allocate(1);
		pos = 0;
		buf.limit(0);
	}

	void ensureCapacity(int minCapacity) {
		if (minCapacity > buf.capacity()) {
			// Extend "buf"
			int l = buf.limit();
			ByteBuffer newBuf = ByteBuffer.allocate(minCapacity);
			newBuf.position(0).limit(l);

			buf.position(0);
			newBuf.put(buf);

			buf = newBuf;
		}
	}

	public byte[] toByteArray() {
		byte[] arr = new byte[buf.limit()];
		ByteBuffer buf2 = buf.duplicate();
		buf2.position(0);
		buf2.get(arr);
		return arr;
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
		return pos;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		pos = newPosition;
		return this;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if (pos >= buf.limit())
			return -1;

		int r = dst.remaining();

		// Let's try to read "r" bytes from the channel into "dst"
		buf.position((int) pos);
		ByteBuffer bytesToTransfer = buf.slice();
		bytesToTransfer.limit(Math.min(r, bytesToTransfer.limit()));
		if (bytesToTransfer.limit() == 0) {
			// EOF
			return -1;
		} else {
			dst.put(bytesToTransfer);
			pos += bytesToTransfer.limit();
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

		pos = Math.min(pos, size);

		return this;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int r = src.remaining();
		int newLimit = Math.max(buf.limit(), (int) pos + r);
		ensureCapacity(newLimit);
		buf.limit(newLimit);
		buf.position((int) pos);
		buf.put(src);
		pos += r;
		return r;
	}

}
